package com.example.chitchat.models.appModels

import android.graphics.Bitmap
import com.example.chitchat.models.databaseModels.MessageDB

data class Group(val groupId: String, val name: String, val members: MutableList<User>, val groupImage: Bitmap, var lastMessage: MessageDB)
