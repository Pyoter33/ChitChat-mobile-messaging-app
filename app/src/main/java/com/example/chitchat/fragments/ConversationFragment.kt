package com.example.chitchat.fragments

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.models.databaseModels.ImageMessageDB
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import java.util.*


class ConversationFragment : MessageHandling() {

    override val IDENTIFIER = "conversations"
    private lateinit var args: ConversationFragmentArgs
    private lateinit var recipientId: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args =
            ConversationFragmentArgs.fromBundle(requireArguments())
        binding.imageButtonInfo.visibility = View.GONE
        recipientId = args.conversationId.replace(userId, "")
        observeConversationUser()
        observeMessages()
        observeUserState()
        getConversationData()
        setOnSendClickListener()
        setOnCameraClickListener()
        observeImageSaveResult()
    }

    private fun observeUserState() {
        viewModel.getUserState(args.conversationId, IDENTIFIER, userId)
        viewModel.getUserStateFromConversation.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    if (resource.data)
                        binding.imageUserActiveIndicator.visibility = View.VISIBLE
                    else
                        binding.imageUserActiveIndicator.visibility = View.INVISIBLE
                }
            }
        })


    }

    private fun observeConversationUser() {
        viewModel.getUserFromConversation.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.imageConversation.setImageResource(R.drawable.image_loading)
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    chatUser = User(
                        resource.data.id,
                        resource.data.name,
                        resource.data.photoUrl,
                        resource.data.registrationTokens
                    )
                    Glide.with(this).load(chatUser.photoUrl).into(binding.imageConversation)
                    binding.textConversationName.text = chatUser.name
                }
            }

        })
    }


    private fun observeMessages() {
        viewModel.getConversationMessagesList(args.conversationId, IDENTIFIER)
        viewModel.conversationMessagesList.observe(viewLifecycleOwner, { resource ->

            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    val list = resource.data
                    viewModel.sortMessagesList(list)
                    adapter.submitList(list)
                    adapter.notifyDataSetChanged()
                    binding.messagesList.scheduleLayoutAnimation()
                    viewModel.getLastMessage(args.conversationId, IDENTIFIER, userId)
                }
            }


        })
    }

    private fun observeImageSaveResult() {
        viewModel.imageSaveResult.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(
                        context,
                        "Sending...",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't send the message!",
                        Toast.LENGTH_SHORT
                    ).show()
                    closePhoto()
                }

                is Resource.Success -> {
                    if (resource.data == null)
                        return@observe

                    val newMessage = ImageMessageDB(
                        resource.data.toString(),
                        userId,
                        args.conversationId,
                        Date(),
                        hashMapOf(userId to true, recipientId to false),
                        recipientId
                    )
                    viewModel.updateLastMessage(args.conversationId, newMessage, IDENTIFIER)
                    viewModel.addNewMessage(args.conversationId, newMessage, IDENTIFIER)
                    viewModel.resetSendingImageResult()
                }
            }
        })
    }


    private fun getConversationData() {
        viewModel.findUserInConversation(args.conversationId, IDENTIFIER, userId)
    }


    private fun setOnSendClickListener() {
        binding.imageButtonSendMessage.setOnClickListener {
            val messageContent = binding.editTextMessage.text.toString()

            if (messageContent.isNotEmpty() && binding.editTextMessage.isVisible) {
                val newMessage = TextMessageDB(
                    messageContent,
                    userId,
                    args.conversationId,
                    Date(),
                    hashMapOf(userId to true, recipientId to false),
                    recipientId
                )
                viewModel.updateLastMessage(args.conversationId, newMessage, IDENTIFIER)
                viewModel.addNewMessage(args.conversationId, newMessage, IDENTIFIER)
                binding.editTextMessage.text.clear()
                return@setOnClickListener
            }

            val currentImage = binding.imagePhoto
            if (currentImage.isVisible) {
                viewModel.saveImageInStorage(args.conversationId, currentImage.drawable.toBitmap())
                closePhoto()
            }
        }


    }

    override fun onLongClickMessage(view: View, messageDate: Date) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item != null) {
                when (item.itemId) {
                    R.id.delete -> viewModel.deleteMessage(
                        args.conversationId,
                        messageDate,
                        IDENTIFIER
                    )
                }
            }
            true
        }
        popupMenu.inflate(R.menu.menu_popup_message)
        popupMenu.show()
    }

    private fun setOnCameraClickListener() {
        binding.imageButtonCamera.setOnClickListener {
            findNavController().navigate(ConversationFragmentDirections.actionConversationFragmentToCameraFragment())
        }

    }

}
