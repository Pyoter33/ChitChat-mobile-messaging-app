package com.example.chitchat.models.databaseModels

import java.util.*
import kotlin.collections.HashMap

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val DELETED = "DELETED"
}

interface MessageDB {
    val senderId: String?
    val currentConversationId: String
    val time: Date
    val read: HashMap<String, Boolean>
    val receiverId: String
    val type: String
}


