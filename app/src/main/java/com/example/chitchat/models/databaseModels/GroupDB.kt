package com.example.chitchat.models.databaseModels

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference

data class GroupDB(val name: String, val membersIds: MutableList<DocumentReference>, val groupImage: Blob, var lastMessage: MessageDB)
