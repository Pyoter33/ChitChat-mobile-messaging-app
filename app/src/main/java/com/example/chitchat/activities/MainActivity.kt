package com.example.chitchat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val scope = MainScope()
    private val database = ChatDatabaseImpl(ChatDatabaseRepositoryImpl())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null)
        scope.launch {
            database.setStateToActive(currentUser.uid)
        }

    }

    override fun onStop() {
        super.onStop()
        val currentUser = Firebase.auth.currentUser
        if(currentUser != null)
            scope.launch {
                database.setStateToNotActive(currentUser.uid)
            }
    }
}