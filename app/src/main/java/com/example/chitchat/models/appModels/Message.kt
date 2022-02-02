package com.example.chitchat.models.appModels

import java.util.*

interface Message {
    val sender: User
    val currentConversationId: String
    val time: Date
    val read: HashMap<String, Boolean>
    val receiverId: String
    val type: String
}
