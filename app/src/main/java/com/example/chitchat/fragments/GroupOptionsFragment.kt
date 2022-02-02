package com.example.chitchat.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.R
import com.example.chitchat.adapters.GroupUsersListAdapter
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.databinding.FragmentGroupOptionsBinding
import com.example.chitchat.dialogs.LeaveGroupDialog
import com.example.chitchat.dialogs.RenameGroupDialog
import com.example.chitchat.viewModels.GroupOptionsViewModel
import com.example.chitchat.viewModels.GroupOptionsViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.IOException

class GroupOptionsFragment : Fragment(), GroupDialogInterface {

    companion object {
        const val REQUEST_IMAGE_FROM_GALLERY = 1
    }

    private lateinit var viewModel: GroupOptionsViewModel
    private lateinit var binding: FragmentGroupOptionsBinding
    private lateinit var args: GroupOptionsFragmentArgs
    private lateinit var adapter: GroupUsersListAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_group_options, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory =
            GroupOptionsViewModelFactory(ChatDatabaseImpl(ChatDatabaseRepositoryImpl()))
        viewModel = ViewModelProvider(this, viewModelFactory).get(GroupOptionsViewModel::class.java)
        args =
            GroupOptionsFragmentArgs.fromBundle(requireArguments())
        userId = Firebase.auth.uid!!
        adapter = GroupUsersListAdapter()
        val layoutManager = LinearLayoutManager(context)
        binding.listGroupMembers.adapter = adapter
        binding.listGroupMembers.layoutManager = layoutManager
        observeCurrentGroup()
        observeChangeNameResult()
        observeChangeImageResult()
        observeLeaveGroup()
        observeRemoveUserFromGroup()
        setOnChangeImageClickListener()
        setOnGoBackClickListener()
        setOnLeaveGroupClickListener()
        setOnChangeNameClickListener()
        setOnAddUserClickListener()
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
                    val group = resource.data
                    binding.imageConversation.setImageBitmap(group.groupImage)
                    binding.textConversationName.text = group.name
                    viewModel.sortListUserFirst(group.members, userId)
                    adapter.submitList(group.members)
                }
            }
        })
    }

    private fun observeChangeNameResult() {
        viewModel.renameResult.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(context, "Couldn't rename the group!", Toast.LENGTH_SHORT)
                        .show()
                }

                is Resource.Success -> {
                    viewModel.getCurrentGroup(args.groupId)
                    Toast.makeText(context, "Group successfully renamed", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        })
    }

    private fun observeChangeImageResult() {
        viewModel.changeImageResult.observe(viewLifecycleOwner, { resource ->
            Log.i("options", "observe")
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't update the group image!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                is Resource.Success -> {
                    viewModel.getCurrentGroup(args.groupId)
                    Toast.makeText(context, "Image successfully updated", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        })
    }

    private fun observeLeaveGroup() {
        viewModel.leaveGroupResult.observe(viewLifecycleOwner, { resource ->
            Log.i("options", "observe")
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(context, "Couldn't leave the group!", Toast.LENGTH_SHORT)
                        .show()
                }

                is Resource.Success -> {
                    findNavController().popBackStack(R.id.conversationsPagerFragment, false)
                    Toast.makeText(context, "You left the group!", Toast.LENGTH_SHORT).show()

                }
            }
        })
    }

    private fun observeRemoveUserFromGroup() {
        viewModel.removeUserResult.observe(viewLifecycleOwner, { resource ->
            Log.i("options", "observe")
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't remove user from the group!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    viewModel.getCurrentGroup(args.groupId)
                    Toast.makeText(context, "User removed from the group!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun setOnChangeNameClickListener() {
        binding.imageButtonEditName.setOnClickListener {
            val dialog = RenameGroupDialog(this)
            dialog.show(childFragmentManager, "GroupOptionsFragment")
        }
    }

    private fun setOnGoBackClickListener() {
        binding.imageButtonGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setOnLeaveGroupClickListener() {
        binding.buttonLeaveGroup.setOnClickListener {
            val dialog = LeaveGroupDialog(this)
            dialog.show(childFragmentManager, "GroupOptionsFragment")
        }
    }

    private fun setOnAddUserClickListener() {
        binding.imageButtonAddMember.setOnClickListener {
            findNavController().navigate(
                GroupOptionsFragmentDirections.actionGroupOptionsFragmentToUserGroupSearchFragment(
                    args.groupId
                )
            )
        }
    }

    private fun setOnChangeImageClickListener() {
        binding.imageButtonEditImage.setOnClickListener {
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
        if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val bitmap = getBitmapFromUri(uri)
            viewModel.updateGroupImage(args.groupId, bitmap!!)
        }
    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = if (Build.VERSION.SDK_INT > 27) {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    override fun onChangeName(newName: String) {
        viewModel.updateGroupName(args.groupId, newName)
    }

    override fun onRemoveUserFromGroup(userId: String) {
        viewModel.removeUser(args.groupId, userId)
    }

    override fun onLeaveGroup() {
        viewModel.leaveGroup(args.groupId, Firebase.auth.uid!!)
    }
}


interface GroupDialogInterface {
    fun onChangeName(newName: String)
    fun onRemoveUserFromGroup(userId: String)
    fun onLeaveGroup()

}