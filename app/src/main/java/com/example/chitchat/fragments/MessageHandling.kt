package com.example.chitchat.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.viewModels.ConversationViewModel
import com.example.chitchat.viewModels.ConversationViewModelFactory
import com.example.chitchat.adapters.MessagesListAdapter
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.FragmentConversationBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

abstract class MessageHandling : Fragment(), MessageClickListener {

    protected lateinit var viewModel: ConversationViewModel
    protected lateinit var binding: FragmentConversationBinding
    protected lateinit var adapter: MessagesListAdapter
    protected lateinit var chatUser: User
    protected lateinit var userId: String
    protected open val IDENTIFIER: String = ""

    companion object {
        const val REQUEST_IMAGE_FROM_GALLERY = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutEnlargedImage.isVisible) {
                    binding.layoutEnlargedImage.visibility = View.GONE
                    binding.layoutConversation.visibility = View.VISIBLE
                } else
                    findNavController().popBackStack()
            }
        })

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_conversation, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory =
            ConversationViewModelFactory(
                ChatDatabaseImpl(ChatDatabaseRepositoryImpl())
            )
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ConversationViewModel::class.java)

        userId = Firebase.auth.uid!!
        adapter = MessagesListAdapter(this, viewModel, viewLifecycleOwner, userId)
        binding.messagesList.adapter = adapter
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        binding.messagesList.layoutManager = layoutManager
        binding.messagesList.itemAnimator = null
        observeSendingMessages()
        setOnGoBackClickListener()
        setOnEditTextChangedListener()
        setOnRecoverClickListener()
        setOnClosePhotoClickListener()
        setOnGalleryClickListener()
        setOnCLoseEnlargedPhotoClickListener()
        observePhotoUri()
        observeDeletingMessage()
    }

    private fun observeDeletingMessage() {
        viewModel.isMessageDeleted.observe(viewLifecycleOwner, { resource ->
            if (resource is Resource.Failure)
                Toast.makeText(
                    context,
                    "Couldn't delete the message!",
                    Toast.LENGTH_SHORT
                ).show()
        })
    }


    private fun observeSendingMessages() {
        viewModel.isMessageSent.observe(viewLifecycleOwner, { resource ->
            if (resource is Resource.Failure)
                Toast.makeText(
                    context,
                    "Couldn't send the message!",
                    Toast.LENGTH_SHORT
                ).show()
        })

    }


    private fun setOnGoBackClickListener() {
        binding.imageButtonGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnCLoseEnlargedPhotoClickListener() {
        binding.imageButtonCloseEnlargedPhoto.setOnClickListener {
            binding.layoutEnlargedImage.visibility = View.GONE
            binding.layoutConversation.visibility = View.VISIBLE
        }
    }

    private fun setOnRecoverClickListener() {
        binding.imageButtonRecover.setOnClickListener {
            binding.imageButtonGallery.visibility = View.VISIBLE
            binding.imageButtonCamera.visibility = View.VISIBLE
            binding.imageButtonRecover.visibility = View.GONE
        }

    }

    private fun setOnClosePhotoClickListener() {
        binding.imageButtonClosePhoto.setOnClickListener {
            closePhoto()
        }
    }

    protected fun closePhoto() {
        viewModel.updateCurrentPhotoUri(null)
    }

    private fun observePhotoUri() {
        viewModel.currentPhotoUri.observe(viewLifecycleOwner, { uri ->
            if (uri == null) {
                binding.imageButtonClosePhoto.visibility = View.GONE
                binding.imagePhoto.visibility = View.GONE
                binding.imagePhoto.setImageResource(0)
                binding.editTextMessage.visibility = View.VISIBLE
            } else {
                binding.imagePhoto.setImageURI(uri)
                binding.imagePhoto.visibility = View.VISIBLE
                binding.imageButtonClosePhoto.visibility = View.VISIBLE
                binding.editTextMessage.visibility = View.GONE
            }
        })

    }

    private fun setOnGalleryClickListener() {
        binding.imageButtonGallery.setOnClickListener {
            val getGalleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            try {
                startActivityForResult(getGalleryIntent, REQUEST_IMAGE_FROM_GALLERY)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Couldn't open the gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("Intent", requestCode.toString())
        if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            viewModel.updateCurrentPhotoUri(uri)
        }
    }

    private fun setOnEditTextChangedListener() {
        binding.editTextMessage.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isEmpty()) {
                    binding.imageButtonGallery.visibility = View.VISIBLE
                    binding.imageButtonCamera.visibility = View.VISIBLE
                    binding.imageButtonRecover.visibility = View.GONE
                } else {
                    binding.imageButtonGallery.visibility = View.GONE
                    binding.imageButtonCamera.visibility = View.GONE
                    binding.imageButtonRecover.visibility = View.VISIBLE
                }
            }
        })

    }


    override fun onLongClickMessage(view: View, messageDate: Date) {}


    override fun onClickImage(image: String) {
        Glide.with(this).load(image.toUri()).into(binding.imageEnlargedImage)
        binding.layoutEnlargedImage.visibility = View.VISIBLE
        binding.layoutConversation.visibility = View.GONE
    }
}

interface MessageClickListener {
    fun onLongClickMessage(view: View, messageDate: Date)
    fun onClickImage(image: String)
}