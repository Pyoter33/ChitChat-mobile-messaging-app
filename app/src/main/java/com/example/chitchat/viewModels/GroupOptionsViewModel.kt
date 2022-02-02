package com.example.chitchat.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.appModels.Group
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import kotlinx.coroutines.launch

class GroupOptionsViewModel(private val database: ChatDatabase) : ViewModel() {
    private val _currentGroup = MutableLiveData<Resource<Group>>()
    val currentGroup: LiveData<Resource<Group>> = _currentGroup

    private val _renameResult = MutableLiveData<Resource<Unit>>()
    val renameResult: LiveData<Resource<Unit>> = _renameResult

    private val _changeImageResult = MutableLiveData<Resource<Unit>>()
    val changeImageResult: LiveData<Resource<Unit>> = _changeImageResult

    private val _removeUserResult = MutableLiveData<Resource<Unit>>()
    val removeUserResult: LiveData<Resource<Unit>> = _removeUserResult

    private val _leaveGroupResult = MutableLiveData<Resource<Unit>>()
    val leaveGroupResult: LiveData<Resource<Unit>> = _leaveGroupResult

    fun getCurrentGroup(docId: String) {
        _currentGroup.value = Resource.Loading()
        viewModelScope.launch {
            _currentGroup.value = database.getGroup(docId)
        }
    }


    fun sortListUserFirst(list: MutableList<User>, userId: String) {
        if (list.first().id == userId)
            return
        val firstUser = list.first()
        for (i in list.indices) {
            if (list[i].id == userId) {
                list[0] = list[i]
                list[i] = firstUser
                return
            }
        }
    }

    fun updateGroupName(groupId: String, newName: String) {
        viewModelScope.launch {
            _renameResult.value = database.renameGroup(groupId, newName)
        }
    }

    fun updateGroupImage(groupId: String, newImage: Bitmap) {
        viewModelScope.launch {
            _changeImageResult.value = database.changeGroupImage(groupId, newImage)
        }
    }

    fun removeUser(groupId: String, userId: String) {
        viewModelScope.launch {
            _removeUserResult.value = database.removeUserFromGroup(groupId, userId)
        }
    }

    fun leaveGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            _leaveGroupResult.value = database.removeUserFromGroup(groupId, userId)
        }
    }

}