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

class ChatDatabaseImpl(private val chatDatabaseRepo: ChatDatabaseRepositoryInterface): ChatDatabase {
    override suspend fun addNewUser(currentUser: FirebaseUser): Resource<Boolean> = chatDatabaseRepo.addNewUserDB(currentUser)
    override suspend fun getUserProfile(userId: String): Flow<Resource<User>> = chatDatabaseRepo.getUserProfileDB(userId)
    override suspend fun getConversations(userId: String): Flow<Resource<MutableList<Conversation>>> = chatDatabaseRepo.getConversationsDB(userId)
    override suspend fun getUsersByName(name: String): Resource<MutableList<User>> = chatDatabaseRepo.getUsersByNameDB(name)
    override suspend fun addNewConversation(conversation: ConversationDB, userId: String): Resource<Boolean> = chatDatabaseRepo.addNewConversationDB(conversation, userId)
    override suspend fun getUserStateFromConversation(docId: String, collectionId: String, userId: String): Flow<Resource<Boolean>> = chatDatabaseRepo.getUserStateFromConversationDB(docId, collectionId, userId)
    override suspend fun getUserFromConversation(docId: String, collectionId: String, userId: String): Resource<User> = chatDatabaseRepo.getUserFromConversationDB(docId, collectionId, userId)
    override suspend fun addNewMessage(docId: String, collectionId: String, message: MessageDB): Resource<Unit> = chatDatabaseRepo.addNewMessageDB(docId, collectionId, message)
    override suspend fun getMessages(docId: String, collectionId: String): Flow<Resource<MutableList<Message>>> = chatDatabaseRepo.getMessagesDB(docId, collectionId)
    override suspend fun setMessagesToRead(docId: String, collectionId: String, userId: String): Resource<Boolean> = chatDatabaseRepo.setMessagesToReadDB(docId, collectionId, userId)
    override suspend fun updateLastMessage(lastMessage: MessageDB, docId: String, collectionId: String): Resource<Unit> = chatDatabaseRepo.updateLastMessageDB(lastMessage, docId, collectionId)
    override suspend fun renameUser(userId: String, newName: String): Resource<Unit> = chatDatabaseRepo.renameUserDB(userId, newName)
    override suspend fun deleteUser(currentUser: FirebaseUser): Resource<Unit> = chatDatabaseRepo.deleteUserDB(currentUser)
    override suspend fun setNewToken(newToken: String, userId: String): Resource<Unit> = chatDatabaseRepo.setNewTokenDB(newToken, userId)
    override suspend fun getConversationMessage(docId: String, collectionId: String): Flow<Triple<String, HashMap<User, Boolean>, Date>> = chatDatabaseRepo.getConversationMessageDB(docId, collectionId)
    override suspend fun setStateToActive(userId: String): Resource<Boolean> = chatDatabaseRepo.setStateToActiveDB(userId)
    override suspend fun setStateToNotActive(userId: String): Resource<Boolean> = chatDatabaseRepo.setStateToNotActiveDB(userId)
    override suspend fun deleteMessage(docId: String, collectionId: String, messageDate: Date): Resource<Unit> = chatDatabaseRepo.deleteMessageDB(docId, collectionId, messageDate)
    override suspend fun addNewGroup(group: GroupDB): Resource<Unit> = chatDatabaseRepo.addNewGroupDB(group)
    override suspend fun getGroups(userId: String): Flow<Resource<MutableList<Group>>> = chatDatabaseRepo.getGroupsDB(userId)
    override suspend fun getGroup(docId: String): Resource<Group> = chatDatabaseRepo.getGroup(docId)
    override suspend fun renameGroup(docId: String, newName: String): Resource<Unit> = chatDatabaseRepo.renameGroupDB(docId, newName)
    override suspend fun changeGroupImage(docId: String, newImage: Bitmap): Resource<Unit> = chatDatabaseRepo.changeGroupImageDB(docId, newImage)
    override suspend fun removeUserFromGroup(docId: String, userId: String): Resource<Unit> = chatDatabaseRepo.removeUserFromGroupDB(docId, userId)
    override suspend fun addUserToGroup(docId: String, userId: String): Resource<Boolean> = chatDatabaseRepo.addUserToGroupDB(docId, userId)
    override suspend fun updateSubscriptions(userId: String, remainingSubscriptions: MutableList<String>) = chatDatabaseRepo.updateSubscriptionsDB(userId, remainingSubscriptions)
    override suspend fun saveImageInStorage(docId: String, imageBitmap: Bitmap): Resource<Uri> = chatDatabaseRepo.saveImageInStorageDB(docId, imageBitmap)

}