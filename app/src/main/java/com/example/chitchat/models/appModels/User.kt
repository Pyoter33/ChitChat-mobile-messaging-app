package com.example.chitchat.models.appModels

data class User(
    val id: String? = "",
    var name: String = "",
    var photoUrl: String = "",
    var registrationTokens: MutableList<String> = mutableListOf(),
    val subscribedTopics: MutableList<String> = mutableListOf(),
    var active: Boolean = true
)
