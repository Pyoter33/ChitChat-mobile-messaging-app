package com.example.chitchat.repository

import android.graphics.Bitmap
import android.net.Uri
import com.example.chitchat.models.databaseModels.ConversationDB
import com.example.chitchat.models.databaseModels.GroupDB
import com.example.chitchat.models.databaseModels.MessageDB
import com.example.chitchat.models.appModels.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import java.util.*

interface ChatDatabase {
    suspend fun addNewUser(currentUser: FirebaseUser): Resource<Boolean>
    suspend fun getUserProfile(userId: String): Flow<Resource<User>>
    suspend fun getConversations(userId: String): Flow<Resource<MutableList<Conversation>>>
    suspend fun getUsersByName(name: String): Resource<MutableList<User>>
    suspend fun addNewConversation(conversation: ConversationDB, userId: String): Resource<Boolean>
    suspend fun getUserStateFromConversation(docId: String, collectionId: String, userId: String): Flow<Resource<Boolean>>
    suspend fun getUserFromConversation(docId: String, collectionId: String, userId: String): Resource<User>
    suspend fun addNewMessage(docId: String, collectionId: String, message: MessageDB): Resource<Unit>
    suspend fun getMessages(docId: String, collectionId: String): Flow<Resource<MutableList<Message>>>
    suspend fun setMessagesToRead(docId: String, collectionId: String, userId: String): Resource<Boolean>
    suspend fun updateLastMessage(lastMessage: MessageDB, docId: String, collectionId: String,): Resource<Unit>
    suspend fun renameUser(userId: String, newName: String): Resource<Unit>
    suspend fun deleteUser(currentUser: FirebaseUser): Resource<Unit>
    suspend fun setNewToken(newToken: String, userId: String): Resource<Unit>
    suspend fun getConversationMessage(docId: String, collectionId: String): Flow<Triple<String, HashMap<User, Boolean>, Date>>
    suspend fun setStateToActive(userId: String): Resource<Boolean>
    suspend fun setStateToNotActive(userId: String): Resource<Boolean>
    suspend fun deleteMessage(docId: String, collectionId: String, messageDate: Date): Resource<Unit>
    suspend fun addNewGroup(group: GroupDB): Resource<Unit>
    suspend fun getGroups(userId: String): Flow<Resource<MutableList<Group>>>
    suspend fun getGroup(docId: String): Resource<Group>
    suspend fun renameGroup(docId: String, newName: String): Resource<Unit>
    suspend fun changeGroupImage(docId: String, newImage: Bitmap): Resource<Unit>
    suspend fun removeUserFromGroup(docId: String, userId: String): Resource<Unit>
    suspend fun addUserToGroup(docId: String, userId: String): Resource<Boolean>
    suspend fun updateSubscriptions(userId: String, remainingSubscriptions: MutableList<String>)
    suspend fun saveImageInStorage(docId: String, imageBitmap: Bitmap): Resource<Uri>
}