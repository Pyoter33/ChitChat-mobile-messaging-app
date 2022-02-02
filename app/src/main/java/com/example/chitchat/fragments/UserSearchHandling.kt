package com.example.chitchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.databaseModels.ConversationDB
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.FragmentUserSearchBinding
import com.example.chitchat.viewModels.UserSearchViewModel
import com.example.chitchat.viewModels.UserSearchViewModelFactory
import com.example.chitchat.adapters.UsersListAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

open class UserSearchHandling : Fragment(), UsersClickListener {

    protected lateinit var binding: FragmentUserSearchBinding
    protected lateinit var viewModel: UserSearchViewModel
    protected lateinit var adapter: UsersListAdapter
    protected lateinit var currentUser: User
    protected lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_search, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = Firebase.auth.uid!!
        val viewModelFactory =
            UserSearchViewModelFactory(ChatDatabaseImpl(ChatDatabaseRepositoryImpl()))
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSearchViewModel::class.java)
        adapter = UsersListAdapter(this, userId)
        binding.foundUsersList.adapter = adapter
        binding.foundUsersList.layoutManager = LinearLayoutManager(context)
        setOnSearchListener()
        setOnGoBackClickListener()
        observeUsersList()
        observeCurrentUser()
    }

    private fun setOnSearchListener() {
        binding.buttonSearch.setOnClickListener {
            val name = binding.editTextUserName.text
            viewModel.updateUsersList(name.toString())
        }


    }

    private fun observeUsersList() {
        viewModel.usersList.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                }

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
                        adapter.submitList(list)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun observeCurrentUser() {
        viewModel.getCurrentUser(userId)
        viewModel.currentUser.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    currentUser = resource.data
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
        val readMap = mapOf(
            (viewModel.currentUser.value as Resource.Success<User>).data.id!! to false,
            user.id!! to false
        )

        val newConversation = ConversationDB(
            mutableListOf(
                Firebase.firestore.document("users/${(viewModel.currentUser.value as Resource.Success<User>).data.id}"),
                Firebase.firestore.document("users/${user.id}")
            ),
            TextMessageDB("", "New conversation!", "", Date(), readMap as HashMap<String, Boolean>)
        )
        viewModel.addNewConversation(newConversation, userId)

    }


}

interface UsersClickListener {
    fun onAddUser(user: User)

}