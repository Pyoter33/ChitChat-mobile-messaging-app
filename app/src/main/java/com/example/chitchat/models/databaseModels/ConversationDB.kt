package com.example.chitchat.models.databaseModels
import com.google.firebase.firestore.DocumentReference

data class ConversationDB (val membersIds: MutableList<DocumentReference>, var lastMessage: MessageDB)
