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

interface ChatDatabaseRepositoryInterface {
    suspend fun addNewUserDB(currentUser: FirebaseUser): Resource<Boolean>
    suspend fun getUserProfileDB(userId: String): Flow<Resource<User>>
    suspend fun getConversationsDB(userId: String): Flow<Resource<MutableList<Conversation>>>
    suspend fun getUsersByNameDB(name: String): Resource<MutableList<User>>
    suspend fun addNewConversationDB(conversation: ConversationDB, userId: String): Resource<Boolean>
    suspend fun getUserFromConversationDB(docId: String, collectionId: String, userId: String): Resource<User>
    suspend fun getUserStateFromConversationDB(docId: String, collectionId: String, userId: String): Flow<Resource<Boolean>>
    suspend fun addNewMessageDB(docId: String, collectionId: String, message: MessageDB): Resource<Unit>
    suspend fun getMessagesDB(docId: String, collectionId: String): Flow<Resource<MutableList<Message>>>
    suspend fun setMessagesToReadDB(docId: String, collectionId: String, userId: String): Resource<Boolean>
    suspend fun updateLastMessageDB(lastMessage: MessageDB, docId: String, collectionId: String): Resource<Unit>
    suspend fun renameUserDB(userId: String, newName: String): Resource<Unit>
    suspend fun deleteUserDB(currentUser: FirebaseUser): Resource<Unit>
    suspend fun setNewTokenDB(newToken: String, userId: String): Resource<Unit>
    suspend fun getConversationMessageDB(docId: String, collectionId: String): Flow<Triple<String, HashMap<User, Boolean>, Date>>
    suspend fun setStateToActiveDB(userId: String): Resource<Boolean>
    suspend fun setStateToNotActiveDB(userId: String): Resource<Boolean>
    suspend fun deleteMessageDB(docId: String, collectionId: String, messageDate: Date): Resource<Unit>
    suspend fun addNewGroupDB(group: GroupDB): Resource<Unit>
    suspend fun getGroupsDB(userId: String): Flow<Resource<MutableList<Group>>>
    suspend fun getGroup(docId: String): Resource<Group>
    suspend fun renameGroupDB(docId: String, newName: String): Resource<Unit>
    suspend fun changeGroupImageDB(docId: String, newImage: Bitmap): Resource<Unit>
    suspend fun removeUserFromGroupDB(docId: String, userId: String): Resource<Unit>
    suspend fun addUserToGroupDB(docId: String, userId: String): Resource<Boolean>
    suspend fun updateSubscriptionsDB(userId: String, remainingSubscriptions: MutableList<String>)
    suspend fun saveImageInStorageDB(docId: String, imageBitmap: Bitmap): Resource<Uri>
}