package com.example.chitchat.models.databaseModels

import java.util.*
import kotlin.collections.HashMap

data class ImageMessageDB(
    val image: String = "",
    override val senderId: String? = "",
    override val currentConversationId: String = "",
    override val time: Date = Date(),
    override val read: HashMap<String, Boolean>,
    override val receiverId: String = "",
    override val type: String = MessageType.IMAGE
) : MessageDB

