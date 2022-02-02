package com.example.chitchat.viewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.appModels.Conversation
import com.example.chitchat.models.appModels.Group
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

class ConversationsPagerViewModel(private val database: ChatDatabase) : ViewModel() {

    var currentPage = 0

    private val _conversations = MutableLiveData<Resource<MutableList<Conversation>>>()
    val conversations: LiveData<Resource<MutableList<Conversation>>> = _conversations

    private val _groups = MutableLiveData<Resource<MutableList<Group>>>()
    val groups: LiveData<Resource<MutableList<Group>>> = _groups

    private val _userProfile = MutableLiveData<Resource<User>>()
    val userProfile: LiveData<Resource<User>> = _userProfile

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

    fun getConversations(userId: String) {
        viewModelScope.launch {
            try {
                database.getConversations(userId).collect {
                    _conversations.value = it
                }
            } catch (e: Exception) {
                _conversations.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }

    }

    fun getGroups(userId: String) {
        viewModelScope.launch {
            try {
                database.getGroups(userId).collect {
                    _groups.value = it
                }
            } catch (e: Exception) {
                _groups.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }

    }


    fun sortConversationsList(list: MutableList<Conversation>) {
        list.sortBy { conversation ->
            conversation.lastMessage.time
        }
        list.reverse()
    }

    fun sortGroupsList(list: MutableList<Group>) {
        list.sortBy { conversation ->
            conversation.lastMessage.time
        }
        list.reverse()
    }

    fun compareSubscriptionsWithGroups(
        currentUser: User,
        groupsList: MutableList<Group>,
        messaging: FirebaseMessaging
    ) {
        val remainingSubscriptions = currentUser.subscribedTopics
        val groupsIds = groupsList.map { it.groupId }

        for (id in groupsIds) {
            if (!currentUser.subscribedTopics.contains(id))
                remainingSubscriptions.add(id)

        }

        for (id in currentUser.subscribedTopics)
            if (!groupsIds.contains(id)) {
                messaging.unsubscribeFromTopic(id)
                remainingSubscriptions.remove(id)
            }

        for (id in remainingSubscriptions)
            messaging.subscribeToTopic(id)

        viewModelScope.launch {
            database.updateSubscriptions(currentUser.id!!, remainingSubscriptions)
        }
    }

}