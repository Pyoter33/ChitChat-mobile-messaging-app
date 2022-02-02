package com.example.chitchat.fragments

import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.databaseModels.GroupDB
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.FragmentGroupCreatorBinding
import com.example.chitchat.viewModels.GroupCreatorViewModel
import com.example.chitchat.viewModels.GroupCreatorViewModelFactory
import com.example.chitchat.adapters.GroupMembersListAdapter
import com.example.chitchat.adapters.UsersGroupListAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.util.*

class GroupCreatorFragment : Fragment(), UsersGroupClickListener {

    private lateinit var binding: FragmentGroupCreatorBinding
    private lateinit var viewModel: GroupCreatorViewModel
    private lateinit var usersAdapter: UsersGroupListAdapter
    private lateinit var currentMembersAdapter: GroupMembersListAdapter
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_group_creator, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelFactory =
            GroupCreatorViewModelFactory(ChatDatabaseImpl(ChatDatabaseRepositoryImpl()))
        viewModel = ViewModelProvider(this, viewModelFactory).get(GroupCreatorViewModel::class.java)
        usersAdapter = UsersGroupListAdapter(this)
        currentMembersAdapter = GroupMembersListAdapter(this)
        binding.foundUsersList.adapter = usersAdapter
        binding.groupMembersList.adapter = currentMembersAdapter
        binding.foundUsersList.layoutManager = LinearLayoutManager(context)
        binding.groupMembersList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setOnSearchClickListener()
        setOnGoBackClickListener()
        observeUsersList()
        observeCurrentUser()
        observeAddingConversation()
        observeCurrentMembersList()
        setOnCreateClickListener()

    }

    private fun setOnSearchClickListener() {
        binding.buttonSearch.setOnClickListener {
            val name = binding.editTextUserName.text
            viewModel.updateUsersList(name.toString())
        }

    }

    private fun setOnCreateClickListener() {
        binding.buttonCreate.setOnClickListener {
            createGroup()
        }
    }

    private fun createGroup() {
        val groupName = binding.editTextGroupName.text.toString()
        if (groupName.isEmpty())
            return

        val membersIds = mutableListOf<DocumentReference>()
        val readMap = mutableMapOf<String, Boolean>()
        for (elem in viewModel.currentMembersList.value!!) {
            membersIds.add(Firebase.firestore.document("users/${elem.id}"))
            readMap[elem.id!!] = false
        }
        val bitmap = viewModel.createBitmap(groupName, requireContext())
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageByteArray = outputStream.toByteArray()


        val group = GroupDB(
            groupName, membersIds, Blob.fromBytes(imageByteArray),
            TextMessageDB("", "New group!", "", Date(), readMap as HashMap<String, Boolean>)
        )

        viewModel.addNewGroup(group)
        findNavController().popBackStack()
    }

    private fun observeUsersList() {
        viewModel.usersList.observe(viewLifecycleOwner, { resource ->
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

                    if (list.isEmpty())
                        binding.textNoUsersFound.visibility = View.VISIBLE
                    else {
                        binding.textNoUsersFound.visibility = View.GONE
                        usersAdapter.submitList(resource.data)
                        usersAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun observeCurrentMembersList() {
        viewModel.currentMembersList.observe(viewLifecycleOwner, { list ->
            currentMembersAdapter.submitList(list)
            currentMembersAdapter.notifyDataSetChanged()
        })


    }

    private fun observeCurrentUser() {
        viewModel.getCurrentUser(Firebase.auth.uid!!)
        viewModel.currentUser.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    currentUser = resource.data
                    viewModel.addNewCurrentMember(currentUser)
                }
            }
        })

    }

    private fun observeAddingConversation() {
        viewModel.isGroupAdded.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't add the group!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    Toast.makeText(context, "New group added!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }


    private fun setOnGoBackClickListener() {
        binding.imageButtonGoBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    override fun onAddUser(user: User) {
        for (elem in viewModel.currentMembersList.value!!)
            if (elem.id == user.id) {
                Toast.makeText(
                    context,
                    "This user is already added to the group!",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        viewModel.addNewCurrentMember(user)
    }

    override fun onDeleteUser(user: User) {
        viewModel.deleteCurrentMember(user)
    }
}

interface UsersGroupClickListener {
    fun onAddUser(user: User)
    fun onDeleteUser(user: User)

}