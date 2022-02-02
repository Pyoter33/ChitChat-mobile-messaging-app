package com.example.chitchat.services

import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null)
            runBlocking {
                launch {
                    ChatDatabaseImpl(ChatDatabaseRepositoryImpl()).setNewToken(
                        newToken,
                        currentUser.uid
                    )
                }
            }

    }

}