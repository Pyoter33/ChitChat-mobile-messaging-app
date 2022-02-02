package com.example.chitchat.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.databaseModels.ConversationDB
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

class UserSearchViewModel(private val database: ChatDatabase) : ViewModel() {

    private val _usersList = MutableLiveData<Resource<MutableList<User>>>()
    val usersList: LiveData<Resource<MutableList<User>>> = _usersList

    private val _conversationAddedResult = MutableLiveData<Resource<Boolean>>()
    val conversationAddedResult: LiveData<Resource<Boolean>> = _conversationAddedResult

    private val _groupUserAddedResult = MutableLiveData<Resource<Boolean>>()
    val groupUserAddedResult: LiveData<Resource<Boolean>> = _groupUserAddedResult

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    fun getCurrentUser(userId: String) {
        viewModelScope.launch {
            try {
                database.getUserProfile(userId).collect {
                    _currentUser.value = it
                }
            } catch (e: Exception) {
                _currentUser.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }
    }

    fun updateUsersList(name: String) {
        viewModelScope.launch {
            _usersList.value = database.getUsersByName(name)
        }
    }

    fun addNewConversation(conversation: ConversationDB, userId: String) {
        viewModelScope.launch {
            _conversationAddedResult.value = database.addNewConversation(conversation, userId)
        }
    }

    fun addNewUserToGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            _groupUserAddedResult.value = database.addUserToGroup(groupId, userId)
        }

    }

}