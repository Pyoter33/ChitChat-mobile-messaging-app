package com.example.chitchat.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

class UserProfileViewModel(private val database: ChatDatabase) : ViewModel() {

    private val _userProfile = MutableLiveData<Resource<User>>()
    val userProfile: LiveData<Resource<User>> = _userProfile

    private val _deleteResult = MutableLiveData<Resource<Unit>>()
    val deleteResult: LiveData<Resource<Unit>> = _deleteResult

    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            try {
                database.getUserProfile(userId).collect {
                    _userProfile.value = it
                }
            } catch (e: Exception) {
                _userProfile.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }
    }

    fun renameUser(userId: String, newName: String) {
        viewModelScope.launch {
            database.renameUser(userId, newName)
        }
    }

    fun deleteUser(currentUser: FirebaseUser) {
        viewModelScope.launch {
            _deleteResult.value = database.deleteUser(currentUser)
        }
    }
}