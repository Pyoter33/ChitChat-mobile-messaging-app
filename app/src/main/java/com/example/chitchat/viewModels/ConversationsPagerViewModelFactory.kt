package com.example.chitchat.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chitchat.repository.ChatDatabase

class ConversationsPagerViewModelFactory(private val database: ChatDatabase) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationsPagerViewModel::class.java)) {
            return ConversationsPagerViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}