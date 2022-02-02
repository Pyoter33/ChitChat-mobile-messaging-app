package com.example.chitchat.models.appModels

import com.example.chitchat.models.databaseModels.MessageType
import java.util.*

data class TextMessage(
    val content: String = "",
    override val sender: User,
    override val currentConversationId: String = "",
    override val time: Date = Date(),
    override val read: HashMap<String, Boolean>,
    override val receiverId: String = "",
    override val type: String = MessageType.TEXT
) : Message