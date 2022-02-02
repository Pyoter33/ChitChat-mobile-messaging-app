package com.example.chitchat.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chitchat.repository.ChatDatabase

class ConversationViewModelFactory(private val database: ChatDatabase) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            return ConversationViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}