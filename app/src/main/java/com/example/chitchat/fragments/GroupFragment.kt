package com.example.chitchat.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.chitchat.R
import com.example.chitchat.models.databaseModels.ImageMessageDB
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.models.appModels.Group
import com.example.chitchat.models.appModels.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class GroupFragment : MessageHandling() {

    override val IDENTIFIER = "groups"

    private lateinit var currentGroup: Group
    private lateinit var args: GroupFragmentArgs
    private val readMap: MutableMap<String, Boolean> = mutableMapOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args = GroupFragmentArgs.fromBundle(requireArguments())
        observeMessages()
        setOnSendClickListener()
        observeCurrentGroup()
        setOnOptionsClickListener()
        setOnCameraClickListener()
        observeImageSaveResult()
    }

    private fun observeCurrentGroup() {
        viewModel.getCurrentGroup(args.groupId)
        viewModel.currentGroup.observe(viewLifecycleOwner, { resource ->
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
                    currentGroup = resource.data
                    binding.imageConversation.setImageBitmap(currentGroup.groupImage)
                    binding.textConversationName.text = currentGroup.name

                    for (elem in currentGroup.members)
                        readMap[elem.id!!] = elem.id == Firebase.auth.uid
                }
            }
        })


    }

    private fun observeMessages() {
        viewModel.getGroupMessagesList(args.groupId, IDENTIFIER)
        viewModel.groupMessagesList.observe(viewLifecycleOwner, { resource ->
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
                    Log.i("group", args.groupId)
                    Log.i("group", list.toString())
                    viewModel.sortMessagesList(list)
                    adapter.submitList(list)
                    adapter.notifyDataSetChanged()
                    binding.messagesList.scheduleLayoutAnimation()
                    viewModel.getLastMessage(args.groupId, IDENTIFIER, userId)
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
                        args.groupId,
                        Date(),
                        readMap as HashMap<String, Boolean>,
                    )
                    viewModel.updateLastMessage(args.groupId, newMessage, IDENTIFIER)
                    viewModel.addNewMessage(args.groupId, newMessage, IDENTIFIER)
                    viewModel.resetSendingImageResult()
                }
            }
        })
    }

    private fun setOnOptionsClickListener() {
        binding.imageButtonInfo.setOnClickListener {
            findNavController().navigate(
                GroupFragmentDirections.actionGroupFragmentToGroupOptionsFragment(
                    args.groupId
                )
            )
        }
    }

    private fun setOnSendClickListener() {
        binding.imageButtonSendMessage.setOnClickListener {
            val messageContent = binding.editTextMessage.text.toString()

            if (messageContent.isNotEmpty() && binding.editTextMessage.isVisible) {
                val newMessage = TextMessageDB(
                    messageContent,
                    userId,
                    args.groupId,
                    Date(),
                    readMap as HashMap<String, Boolean>
                )

                viewModel.updateLastMessage(args.groupId, newMessage, IDENTIFIER)
                viewModel.addNewMessage(args.groupId, newMessage, IDENTIFIER)
                binding.editTextMessage.text.clear()
                return@setOnClickListener
            }

            val currentImage = binding.imagePhoto
            if (currentImage.isVisible) {
                viewModel.saveImageInStorage(args.groupId, currentImage.drawable.toBitmap())
                closePhoto()
            }
        }


    }

    override fun onLongClickMessage(view: View, messageDate: Date) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.setOnMenuItemClickListener { item ->
            if (item != null) {
                when (item.itemId) {
                    R.id.delete -> viewModel.deleteMessage(args.groupId, messageDate, IDENTIFIER)
                }
            }
            true
        }
        popupMenu.inflate(R.menu.menu_popup_message)
        popupMenu.show()
    }

    private fun setOnCameraClickListener() {
        binding.imageButtonCamera.setOnClickListener {
            findNavController().navigate(GroupFragmentDirections.actionGroupFragmentToCameraFragment())

        }

    }
}

