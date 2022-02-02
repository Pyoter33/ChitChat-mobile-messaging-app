package com.example.chitchat.viewModels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.databaseModels.MessageDB
import com.example.chitchat.models.appModels.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class ConversationViewModel(
    private val database: ChatDatabase
) : ViewModel() {

    private val _currentPhotoUri = MutableLiveData<Uri?>(null)
    val currentPhotoUri: LiveData<Uri?> = _currentPhotoUri

    private val _getUserFromConversation = MutableLiveData<Resource<User>>()
    val getUserFromConversation: LiveData<Resource<User>> = _getUserFromConversation

    private val _isMessageSent = MutableLiveData<Resource<Unit>>()
    val isMessageSent: LiveData<Resource<Unit>> = _isMessageSent

    private val _isMessageDeleted = MutableLiveData<Resource<Unit>>()
    val isMessageDeleted: LiveData<Resource<Unit>> = _isMessageDeleted

    private val _currentGroup = MutableLiveData<Resource<Group>>()
    val currentGroup: LiveData<Resource<Group>> = _currentGroup

    private val _imageSaveResult = MutableLiveData<Resource<Uri?>>(null)
    val imageSaveResult: LiveData<Resource<Uri?>> = _imageSaveResult

    private val _getUserStateFromConversation = MutableLiveData<Resource<Boolean>>()
    val getUserStateFromConversation: LiveData<Resource<Boolean>> = _getUserStateFromConversation

    private val _conversationMessagesList = MutableLiveData<Resource<MutableList<Message>>>()
    val conversationMessagesList: LiveData<Resource<MutableList<Message>>> =
        _conversationMessagesList

    private val _lastMessage = MutableLiveData<Triple<String, HashMap<User, Boolean>, Date>>()
    val lastMessage: LiveData<Triple<String, HashMap<User, Boolean>, Date>> = _lastMessage

    private val _groupMessagesList = MutableLiveData<Resource<MutableList<Message>>>()
    val groupMessagesList: LiveData<Resource<MutableList<Message>>> = _groupMessagesList

    fun getUserState(docId: String, identifier: String, userId: String) {
        viewModelScope.launch {
            try {
                database.getUserStateFromConversation(docId, identifier, userId).collect {
                    _getUserStateFromConversation.value = it
                }
            } catch (e: Exception) {
                _getUserStateFromConversation.value = Resource.Failure()
            }
        }

    }


    fun findUserInConversation(docId: String, identifier: String, userId: String) {
        viewModelScope.launch {
            _getUserFromConversation.value = Resource.Loading()
            _getUserFromConversation.value =
                database.getUserFromConversation(docId, identifier, userId)
        }
    }

    fun addNewMessage(docId: String, message: MessageDB, identifier: String) {
        viewModelScope.launch {
            _isMessageSent.value = database.addNewMessage(docId, identifier, message)
        }
    }

    fun saveImageInStorage(docId: String, imageBitmap: Bitmap) {
        viewModelScope.launch {
            _imageSaveResult.value = Resource.Loading()
            _imageSaveResult.value = database.saveImageInStorage(docId, imageBitmap)
        }
    }

    fun updateLastMessage(docId: String, message: MessageDB, identifier: String) {
        viewModelScope.launch {
            database.updateLastMessage(message, docId, identifier)
        }

    }

    private fun setMessagesToRead(docId: String, identifier: String, userId: String) {
        viewModelScope.launch {
            database.setMessagesToRead(docId, identifier, userId)
        }

    }

    fun deleteMessage(docId: String, messageDate: Date, identifier: String) {
        viewModelScope.launch {
            try {
                _isMessageDeleted.value = database.deleteMessage(docId, identifier, messageDate)
            } catch (e: Exception) {
                _isMessageDeleted.value = Resource.Failure()
            }
        }
    }

    fun getCurrentGroup(docId: String) {
        _currentGroup.value = Resource.Loading()
        viewModelScope.launch {
            _currentGroup.value = database.getGroup(docId)
        }
    }

    fun sortMessagesList(list: MutableList<Message>) {
        list.sortBy { message ->
            message.time
        }
    }


    fun getConversationMessagesList(docId: String, identifier: String) {
        viewModelScope.launch {
            try {
                database.getMessages(docId, identifier).collect {
                    _conversationMessagesList.value = it
                }
            } catch (e: Exception) {
                _conversationMessagesList.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }

    }

    fun getLastMessage(docId: String, identifier: String, userId: String) {
        viewModelScope.launch {
            Log.i("read", "changed")
            setMessagesToRead(docId, identifier, userId)
            try {
                database.getConversationMessage(docId, identifier).collect {
                    _lastMessage.value = it
                }
            } catch (e: Exception) {
                Log.i("Database error", e.message.toString())
            }
        }
    }


    fun getGroupMessagesList(docId: String, identifier: String) {
        viewModelScope.launch {
            try {
                database.getMessages(docId, identifier).collect {
                    _groupMessagesList.value = it
                }
            } catch (e: Exception) {
                _groupMessagesList.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }

    }

    fun updateCurrentPhotoUri(newPhotoUri: Uri?) {
        _currentPhotoUri.value = newPhotoUri
    }

    fun resetSendingImageResult() {
        _imageSaveResult.value = Resource.Success(null)

    }

}