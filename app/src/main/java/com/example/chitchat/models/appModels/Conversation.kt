package com.example.chitchat.models.appModels

import com.example.chitchat.models.databaseModels.MessageDB

data class Conversation(val id: String = "", val members: MutableList<User> = mutableListOf(), var lastMessage: MessageDB)