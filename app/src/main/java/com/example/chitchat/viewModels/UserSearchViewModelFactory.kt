package com.example.chitchat.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chitchat.repository.ChatDatabase

class UserSearchViewModelFactory(private val database: ChatDatabase) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserSearchViewModel::class.java)) {
            return UserSearchViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}