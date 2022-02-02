package com.example.chitchat.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.*
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabase
import com.example.chitchat.models.databaseModels.GroupDB
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GroupCreatorViewModel(private val database: ChatDatabase) : ViewModel() {

    private val _currentMembersList = MutableLiveData<MutableList<User>>(mutableListOf())
    val currentMembersList: LiveData<MutableList<User>> = _currentMembersList

    private val _usersList = MutableLiveData<Resource<MutableList<User>>>()
    val usersList: LiveData<Resource<MutableList<User>>> = _usersList

    private val _isGroupAdded = MutableLiveData<Resource<Unit>>()
    val isGroupAdded: LiveData<Resource<Unit>> = _isGroupAdded

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val possibleColours = listOf(
        R.color.bitmap_background1, R.color.bitmap_background2,
        R.color.bitmap_background3, R.color.bitmap_background4,
        R.color.bitmap_background5, R.color.bitmap_background6
    )

    fun getCurrentUser(userId: String) {
        viewModelScope.launch {
            try {
                database.getUserProfile(userId).collect {
                    _currentUser.value = it
                }
            } catch (e: Exception) {
                _currentUser.value = Resource.Failure()
                Log.i("Database error", e.message.toString())
            }
        }
    }

    fun updateUsersList(name: String) {
        viewModelScope.launch {
            _usersList.value = database.getUsersByName(name)
        }
    }

    fun addNewGroup(group: GroupDB) {
        viewModelScope.launch {
            _isGroupAdded.value = database.addNewGroup(group)
        }
    }

    fun addNewCurrentMember(user: User) {
        _currentMembersList.value?.add(user)
        _currentMembersList.notifyObserver()
    }

    fun deleteCurrentMember(user: User) {
        currentMembersList.value?.remove(user)
        _currentMembersList.notifyObserver()
    }

    fun createBitmap(groupName: String, context: Context): Bitmap {
        val width = 100
        val height = 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.color = ResourcesCompat.getColor(context.resources, possibleColours.random(), null)
        paint.style = Paint.Style.FILL
        canvas.drawPaint(paint)

        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.textSize = 70f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(groupName[0].toUpperCase().toString(), width / 2f, height / 1.35f, paint)
        return bitmap
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}