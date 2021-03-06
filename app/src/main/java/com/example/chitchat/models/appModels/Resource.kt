package com.example.chitchat.models.appModels

sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    class Failure<out T> : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
}

