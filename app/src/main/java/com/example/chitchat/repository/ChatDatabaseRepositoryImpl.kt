package com.example.chitchat.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.chitchat.models.databaseModels.*
import com.example.chitchat.models.appModels.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap


class ChatDatabaseRepositoryImpl : ChatDatabaseRepositoryInterface {

    companion object {
        val DELETED_USER = User(null, "Deleted user", "")
        const val MESSAGE_DELETED_TEXT = "Message deleted!"
    }

    override suspend fun addNewUserDB(currentUser: FirebaseUser): Resource<Boolean> {
        val result = Firebase.firestore.collection("users")
            .document(currentUser.uid).get().await()

        val token = FirebaseMessaging.getInstance().token.await()
        if (result.exists()) {
            setStateToActiveDB(currentUser.uid)
            val registrationTokens = result["registrationTokens"] as MutableList<String>
            if (!registrationTokens.contains(token)) {
                registrationTokens.add(token)
                Firebase.firestore.collection("users")
                    .document(currentUser.uid).update("registrationTokens", registrationTokens)
                    .await()
            }
            return Resource.Success(false)
        }

        val newUser = User(
            currentUser.uid,
            currentUser.displayName!!,
            currentUser.photoUrl.toString() + "?type=large",
            mutableListOf(token)
        )

        Firebase.firestore.collection("users").document(currentUser.uid).set(newUser).await()

        return Resource.Success(true)
    }

    override suspend fun addNewConversationDB(
        conversation: ConversationDB,
        userId: String
    ): Resource<Boolean> {
        val conversationIdFirst =
            conversation.membersIds.first().path.replace(
                "users/",
                ""
            ) + conversation.membersIds.last().path
                .replace("users/", "")
        val conversationIdSecond =
            conversation.membersIds.last().path.replace(
                "users/",
                ""
            ) + conversation.membersIds.first().path
                .replace("users/", "")

        val result = Firebase.firestore.collection("conversations")
            .whereArrayContains(
                "membersIds",
                Firebase.firestore.document("users/$userId")
            ).get().await()

        for (doc in result) {
            if (doc.id == conversationIdFirst || doc.id == conversationIdSecond) {
                return Resource.Success(false)
            }
        }
        Firebase.firestore.collection("conversations")
            .document(conversationIdFirst).set(conversation).await()
        return Resource.Success(true)
    }

    override suspend fun getUserFromConversationDB(
        docId: String,
        collectionId: String,
        userId: String
    ): Resource<User> {
        val result = Firebase.firestore.collection(collectionId)
            .document(docId).get().await()

        for (doc in result["membersIds"] as MutableList<DocumentReference>) {
            if (doc.id != userId) {
                val userDoc = doc.get().await()
                return try {
                    Resource.Success(userDoc.toObject()!!)

                } catch (e: java.lang.NullPointerException) {
                    Resource.Success(DELETED_USER)

                }
            }

        }
        return Resource.Failure()
    }


    @ExperimentalCoroutinesApi
    override suspend fun getUserStateFromConversationDB(
        docId: String,
        collectionId: String,
        userId: String
    ): Flow<Resource<Boolean>> =
        callbackFlow {
            val result = Firebase.firestore.collection(collectionId)
                .document(docId).get().await()

            for (doc in result["membersIds"] as MutableList<DocumentReference>)
                if (doc.id != userId) {
                    val registration = doc.addSnapshotListener { userSnapshot, _ ->
                        if (userSnapshot != null)
                            try {

                                trySend(Resource.Success(userSnapshot["active"] as Boolean))

                            } catch (e: java.lang.NullPointerException) {
                                trySend(Resource.Success(false))

                            }
                    }
                    awaitClose {
                        registration.remove()
                    }
                }
        }


    override suspend fun addNewMessageDB(
        docId: String, collectionId: String,
        message: MessageDB
    ): Resource<Unit> {
        Firebase.firestore.collection(collectionId)
            .document(docId).collection("messages")
            .add(message).await()
        return Resource.Success(Unit)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getUserProfileDB(userId: String): Flow<Resource<User>> = callbackFlow {
        val query = Firebase.firestore.collection("users")
            .document(userId)

        val registration = query.addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists())
                return@addSnapshotListener
            try {
                val user =
                    User(
                        snapshot.getString("id")!!,
                        snapshot["name"] as String,
                        snapshot["photoUrl"] as String,
                        snapshot["registrationTokens"] as MutableList<String>,
                        snapshot["subscribedTopics"] as MutableList<String>,
                    )
                trySend(Resource.Success(user))

            } catch (e: NullPointerException) {
                trySend(Resource.Success(DELETED_USER))
            }


        }
        awaitClose {
            registration.remove()
        }

    }

    @ExperimentalCoroutinesApi
    override suspend fun getConversationsDB(userId: String): Flow<Resource<MutableList<Conversation>>> =
        callbackFlow {

            val query = Firebase.firestore.collection("conversations")
                .whereArrayContains(
                    "membersIds",
                    Firebase.firestore.document("users/$userId")
                )

            val registration = query.addSnapshotListener { snapshot, _ ->
                if (snapshot == null)
                    return@addSnapshotListener
                val conversationsList = mutableListOf<Conversation>()
                val conversationsTasks = mutableListOf<Task<MutableList<Any>>>()
                for (doc in snapshot) {
                    val membersList = mutableListOf<User>()
                    val membersTasks = mutableListOf<Task<DocumentSnapshot>>()
                    for (id in doc["membersIds"] as MutableList<*>)
                        membersTasks.add((id as DocumentReference).get())

                    val membersListTask = Tasks.whenAllSuccess<Any>(membersTasks)
                        .addOnSuccessListener { members ->
                            for (userSnapshot in members)
                                try {
                                    Log.i("active", userSnapshot.toString())
                                    val user = User(
                                        (userSnapshot as DocumentSnapshot).getString("id")!!,
                                        userSnapshot["name"] as String,
                                        userSnapshot["photoUrl"] as String,
                                        userSnapshot["registrationTokens"] as MutableList<String>,
                                        userSnapshot["subscribedTopics"] as MutableList<String>,
                                        userSnapshot["active"] as Boolean
                                    )
                                    membersList.add(user)
                                } catch (e: NullPointerException) {
                                    membersList.add(DELETED_USER)
                                }
                        }

                    val conversationTask = membersListTask.addOnSuccessListener {
                        val lastMessageMap = doc["lastMessage"] as HashMap<String, Any>
                        val senderId = lastMessageMap["senderId"] as String
                        val recipientId = lastMessageMap["receiverId"] as String
                        val read = lastMessageMap["read"] as HashMap<String, Boolean>
                        val time = (lastMessageMap["time"] as Timestamp).toDate()
                        val type = (lastMessageMap["type"] as String)
                        if (type == MessageType.IMAGE) {
                            val image = lastMessageMap["image"] as String
                            conversationsList.add(
                                Conversation(
                                    doc.id,
                                    membersList,
                                    ImageMessageDB(
                                        image,
                                        senderId,
                                        doc.id,
                                        time,
                                        read,
                                        recipientId
                                    )
                                )
                            )
                        } else {
                            val content = lastMessageMap["content"] as String
                            conversationsList.add(
                                Conversation(
                                    doc.id,
                                    membersList,
                                    TextMessageDB(
                                        content,
                                        senderId,
                                        doc.id,
                                        time,
                                        read,
                                        recipientId
                                    )
                                )
                            )
                        }


                    }
                    conversationsTasks.add(conversationTask)

                }
                Tasks.whenAllComplete(conversationsTasks).addOnSuccessListener {
                    trySend(Resource.Success(conversationsList))
                }
            }
            awaitClose {
                registration.remove()
            }

        }


    @ExperimentalCoroutinesApi
    override suspend fun getMessagesDB(
        docId: String,
        collectionId: String
    ): Flow<Resource<MutableList<Message>>> =
        callbackFlow {
            val query = Firebase.firestore.collection(collectionId).document(docId)
                .collection("messages")

            val registration = query.addSnapshotListener { snapshot, _ ->
                if (snapshot == null)
                    return@addSnapshotListener
                val messagesList = mutableListOf<Message>()
                val messagesTasks = mutableListOf<Task<DocumentSnapshot>>()
                for (doc in snapshot) {
                    val senderId = doc["senderId"] as String
                    val type = doc["type"] as String
                    val userTask = Firebase.firestore.collection("users")
                        .document(senderId).get()

                    val messageTask = userTask.addOnSuccessListener { snapshot ->
                        val user = try {
                            snapshot.toObject<User>()!!
                        } catch (e: java.lang.NullPointerException) {
                            DELETED_USER
                        }
                        if (type == MessageType.IMAGE) {
                            messagesList.add(
                                ImageMessage(
                                    doc["image"] as String,
                                    user,
                                    doc["currentConversationId"] as String,
                                    doc.getTimestamp("time")!!.toDate(),
                                    doc["read"] as HashMap<String, Boolean>,
                                    doc["receiverId"] as String,
                                    type
                                )
                            )
                        } else {
                            Log.i("db", doc["read"].toString())
                            messagesList.add(
                                TextMessage(
                                    doc["content"] as String,
                                    user,
                                    doc["currentConversationId"] as String,
                                    doc.getTimestamp("time")!!.toDate(),
                                    doc["read"] as HashMap<String, Boolean>,
                                    doc["receiverId"] as String,
                                    type
                                )
                            )
                        }
                    }
                    messagesTasks.add(messageTask)
                }
                Tasks.whenAllComplete(messagesTasks).addOnSuccessListener {
                    trySend(Resource.Success(messagesList))
                }
            }
            awaitClose {
                registration.remove()
            }
        }

    override suspend fun setMessagesToReadDB(
        docId: String,
        collectionId: String,
        userId: String
    ): Resource<Boolean> {
        Firebase.firestore.runTransaction { transaction ->
            val docRef = Firebase.firestore.collection(collectionId).document(docId)
            val result = transaction.get(docRef)

            val lastMessageMap = result["lastMessage"] as HashMap<String, Any>
            val senderId = lastMessageMap["senderId"] as String
            val read = lastMessageMap["read"] as MutableMap<String, Boolean>
            val type = lastMessageMap["type"] as String
            val recipientId = lastMessageMap["receiverId"] as String
            val readByUser = read[userId]

            if (readByUser!!) {
                Log.i("read", "not changed")
                return@runTransaction Resource.Success(true)
            }

            val currentConversationId = lastMessageMap["currentConversationId"] as String
            val time = (lastMessageMap["time"] as Timestamp).toDate()
            read[userId] = true
            Log.i("read", "changed to read")

            if (type == MessageType.IMAGE) {
                val image = lastMessageMap["image"] as String
                transaction
                    .update(
                        docRef,
                        "lastMessage",
                        ImageMessageDB(
                            image,
                            senderId,
                            currentConversationId,
                            time,
                            read as HashMap<String, Boolean>,
                            recipientId
                        )
                    )
            } else {
                val content = lastMessageMap["content"] as String
                transaction
                    .update(
                        docRef,
                        "lastMessage",
                        TextMessageDB(
                            content,
                            senderId,
                            currentConversationId,
                            time,
                            read as HashMap<String, Boolean>,
                            recipientId
                        )
                    )
            }
        }
        return Resource.Success(true)
    }

    override suspend fun updateLastMessageDB(
        lastMessage: MessageDB,
        docId: String, collectionId: String
    ): Resource<Unit> {
        Firebase.firestore.collection(collectionId).document(docId)
            .update("lastMessage", lastMessage).await()
        return Resource.Success(Unit)
    }

    override suspend fun renameUserDB(userId: String, newName: String): Resource<Unit> {
        Firebase.firestore.collection("users").document(userId)
            .update("name", newName).await()
        return Resource.Success(Unit)
    }

    override suspend fun deleteUserDB(currentUser: FirebaseUser): Resource<Unit> {
        val deleteTasks = mutableListOf<Task<Void>>()
        val dbTask = Firebase.firestore.collection("users").document(currentUser.uid).delete()
        val authTask = Firebase.auth.currentUser!!.delete()
        deleteTasks.add(dbTask)
        deleteTasks.add(authTask)

        Tasks.whenAllComplete(deleteTasks).await()

        return Resource.Success(Unit)

    }

    override suspend fun setNewTokenDB(newToken: String, userId: String): Resource<Unit> {
        Firebase.firestore.collection("users").document(userId)
            .update("registrationToken", mutableListOf(newToken)).await()

        return Resource.Success(Unit)
    }


    override suspend fun getUsersByNameDB(name: String): Resource<MutableList<User>> {
        val result = Firebase.firestore.collection("users")
            .whereEqualTo("name", name).get().await()

        val usersList = mutableListOf<User>()
        for (doc in result) {
            usersList.add(
                User(
                    doc["id"] as String,
                    doc["name"] as String,
                    doc["photoUrl"] as String
                )
            )
        }
        return Resource.Success(usersList)
    }


    @ExperimentalCoroutinesApi
    override suspend fun getConversationMessageDB(
        docId: String,
        collectionId: String
    ): Flow<Triple<String, HashMap<User, Boolean>, Date>> =
        callbackFlow {
            val query = Firebase.firestore.collection(collectionId).document(docId)
            val registration = query.addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists())
                    return@addSnapshotListener

                val lastMessageMap = snapshot.get("lastMessage") as HashMap<String, Any>
                val read = lastMessageMap["read"] as HashMap<String, Boolean>
                val usersTasks = mutableListOf<Task<DocumentSnapshot>>()
                val usersMap = mutableMapOf<User, Boolean>()
                for (key in read.keys) {
                    val task = Firebase.firestore.collection("users").document(key).get()
                    usersTasks.add(task)
                }

                val sendTask = Tasks.whenAllSuccess<DocumentSnapshot>(usersTasks)
                    .addOnSuccessListener { snapshot ->
                        for (elem in snapshot)
                            try {
                                usersMap[elem.toObject()!!] = read[elem.id]!!
                            } catch (e: java.lang.NullPointerException) {
                                usersMap[DELETED_USER] = false
                            }
                    }
                sendTask.addOnSuccessListener {
                    trySend(
                        Triple(
                            lastMessageMap["senderId"] as String,
                            usersMap as HashMap<User, Boolean>,
                            (lastMessageMap["time"] as Timestamp).toDate()
                        )
                    )
                }
            }
            awaitClose {
                registration.remove()
            }
        }

    override suspend fun setStateToActiveDB(userId: String): Resource<Boolean> {
        Firebase.firestore.collection("users").document(userId)
            .update("active", true).await()

        return Resource.Success(true)
    }

    override suspend fun setStateToNotActiveDB(userId: String): Resource<Boolean> {
        Firebase.firestore.collection("users").document(userId)
            .update("active", false).await()

        return Resource.Success(true)
    }

    override suspend fun deleteMessageDB(
        docId: String, collectionId: String,
        messageDate: Date
    ): Resource<Unit> {
        val messageResult = Firebase.firestore.collection(collectionId).document(docId)
            .collection("messages").whereEqualTo("time", messageDate).get()
            .await()

        val conversationResult = Firebase.firestore.collection(collectionId)
            .document(docId).get().await()

        val snapshot = messageResult.documents.first()
        val read = snapshot["read"] as MutableMap<String, Boolean>

        for (id in read.keys)
            read[id] = true


        val lastMessageMap = conversationResult["lastMessage"] as HashMap<String, Any>
        if ((lastMessageMap["time"] as Timestamp).toDate() == messageDate) {
            val newMessage = TextMessageDB(
                MESSAGE_DELETED_TEXT,
                snapshot["senderId"] as String,
                docId,
                messageDate,
                lastMessageMap["read"] as HashMap<String, Boolean>,
                snapshot["receiverId"] as String,
                MessageType.DELETED
            )
            Firebase.firestore.collection(collectionId).document(docId)
                .update("lastMessage", newMessage)
            Firebase.firestore.collection(collectionId).document(docId)
                .collection("messages").document(snapshot.id).set(newMessage)

            return Resource.Success(Unit)
        }
        val newMessage = TextMessageDB(
            MESSAGE_DELETED_TEXT,
            snapshot["senderId"] as String,
            docId,
            messageDate,
            read as HashMap<String, Boolean>,
            snapshot["receiverId"] as String,
            MessageType.DELETED
        )

        Firebase.firestore.collection(collectionId).document(docId)
            .collection("messages").document(snapshot.id).set(newMessage)

        return Resource.Success(Unit)
    }

    override suspend fun addNewGroupDB(group: GroupDB): Resource<Unit> {
        val task = Firebase.firestore.collection("groups").add(group)
        val userTasks = mutableListOf<Task<DocumentSnapshot>>()
        task.addOnSuccessListener { doc ->
            for (elem in group.membersIds) {
                userTasks.add(elem.get())
            }

            Tasks.whenAllSuccess<DocumentSnapshot>(userTasks).addOnSuccessListener { snapshot ->
                for (elem in snapshot) {
                    val subscribedTopics = elem["subscribedTopics"] as MutableList<String>
                    subscribedTopics.add(doc.id)
                    Firebase.firestore.collection("users").document(elem.id)
                        .update("subscribedTopics", subscribedTopics)
                }
            }
        }

        return Resource.Success(Unit)
    }

    @ExperimentalCoroutinesApi
    override suspend fun getGroupsDB(userId: String): Flow<Resource<MutableList<Group>>> =
        callbackFlow {
            val query = Firebase.firestore.collection("groups")
                .whereArrayContains(
                    "membersIds",
                    Firebase.firestore.document("users/$userId")
                )

            val registration = query.addSnapshotListener { snapshot, _ ->
                if (snapshot == null)
                    return@addSnapshotListener
                val groupsList = mutableListOf<Group>()
                val groupsTasks = mutableListOf<Task<MutableList<Any>>>()
                for (doc in snapshot) {
                    val membersList = mutableListOf<User>()
                    val membersTasks = mutableListOf<Task<DocumentSnapshot>>()
                    for (id in doc["membersIds"] as List<*>)
                        membersTasks.add((id as DocumentReference).get())

                    val membersListTask = Tasks.whenAllSuccess<Any>(membersTasks)
                        .addOnSuccessListener { members ->
                            for (userSnapshot in members)
                                try {
                                    Log.i("active", userSnapshot.toString())
                                    val user = User(
                                        (userSnapshot as DocumentSnapshot).getString("id")!!,
                                        userSnapshot["name"] as String,
                                        userSnapshot["photoUrl"] as String,
                                        userSnapshot["registrationTokens"] as MutableList<String>,
                                        userSnapshot["subscribedTopics"] as MutableList<String>,
                                        userSnapshot["active"] as Boolean
                                    )
                                    membersList.add(user)
                                } catch (e: NullPointerException) {
                                    val updatedMembers =
                                        (doc["membersIds"] as MutableList<DocumentReference>)
                                    updatedMembers.remove(Firebase.firestore.document("users/${(userSnapshot as DocumentSnapshot).id}"))
                                    Firebase.firestore.collection("groups").document(doc.id)
                                        .update("membersIds", updatedMembers)
                                }
                        }

                    val groupTask = membersListTask.addOnSuccessListener {
                        val name = doc["name"] as String
                        val groupImage = doc["groupImage"] as Blob
                        val byteArray = groupImage.toBytes()
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        val lastMessageMap = doc["lastMessage"] as HashMap<String, Any>
                        val senderId = lastMessageMap["senderId"] as String
                        val read = lastMessageMap["read"] as HashMap<String, Boolean>
                        val time = (lastMessageMap["time"] as Timestamp).toDate()
                        val recipientId = lastMessageMap["receiverId"] as String
                        val type = (lastMessageMap["type"] as String)
                        if (type == MessageType.IMAGE) {
                            val image = lastMessageMap["image"] as String
                            groupsList.add(
                                Group(
                                    doc.id,
                                    name,
                                    membersList,
                                    bitmap,
                                    ImageMessageDB(
                                        image,
                                        senderId,
                                        doc.id,
                                        time,
                                        read,
                                        recipientId
                                    )
                                )
                            )
                        } else {
                            val content = lastMessageMap["content"] as String
                            groupsList.add(
                                Group(
                                    doc.id,
                                    name,
                                    membersList,
                                    bitmap,
                                    TextMessageDB(
                                        content,
                                        senderId,
                                        doc.id,
                                        time,
                                        read,
                                        recipientId
                                    )
                                )
                            )
                        }
                    }
                    groupsTasks.add(groupTask)

                }
                Tasks.whenAllComplete(groupsTasks).addOnSuccessListener {
                    trySend(Resource.Success(groupsList))
                }
            }
            awaitClose {
                registration.remove()
            }
        }

    override suspend fun getGroup(docId: String): Resource<Group> {
        val result = Firebase.firestore.collection("groups").document(docId).get().await()

        val membersList = mutableListOf<User>()
        for (id in result["membersIds"] as MutableList<*>) {
            val userSnapshot = (id as DocumentReference).get().await()
            try {
                val user = User(
                    (userSnapshot as DocumentSnapshot).getString("id")!!,
                    userSnapshot["name"] as String,
                    userSnapshot["photoUrl"] as String,
                    userSnapshot["registrationTokens"] as MutableList<String>,
                    userSnapshot["subscribedTopics"] as MutableList<String>,
                    userSnapshot["active"] as Boolean
                )
                membersList.add(user)
            } catch (e: java.lang.NullPointerException) {
                membersList.add(DELETED_USER)
            }

        }
        val groupId = result.id
        val name = result["name"] as String
        val groupImage = result["groupImage"] as Blob
        val byteArray = groupImage.toBytes()
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val lastMessageMap = result["lastMessage"] as HashMap<String, Any>
        val senderId = lastMessageMap["senderId"] as String
        val read = lastMessageMap["read"] as HashMap<String, Boolean>
        val time = (lastMessageMap["time"] as Timestamp).toDate()
        val type = (lastMessageMap["type"] as String)
        val recipientId = lastMessageMap["receiverId"] as String
        val group = if (type == MessageType.IMAGE) {
            val image = lastMessageMap["image"] as String
            Group(
                groupId,
                name,
                membersList,
                bitmap,
                ImageMessageDB(image, senderId, groupId, time, read, recipientId)
            )
        } else {
            val content = lastMessageMap["content"] as String
            Group(
                groupId,
                name,
                membersList,
                bitmap,
                TextMessageDB(content, senderId, groupId, time, read, recipientId)
            )
        }
        return Resource.Success(group)
    }

    override suspend fun renameGroupDB(docId: String, newName: String): Resource<Unit> {
        Firebase.firestore.collection("groups").document(docId).update("name", newName).await()
        return Resource.Success(Unit)
    }

    override suspend fun changeGroupImageDB(docId: String, newImage: Bitmap): Resource<Unit> {
        val outputStream = ByteArrayOutputStream()
        newImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageByteArray = outputStream.toByteArray()
        val blob = Blob.fromBytes(imageByteArray)

        Firebase.firestore.collection("groups").document(docId).update("groupImage", blob).await()
        return Resource.Success(Unit)
    }

    override suspend fun removeUserFromGroupDB(docId: String, userId: String): Resource<Unit> {
        val result = Firebase.firestore.collection("groups").document(docId).get().await()
        val members = result["membersIds"] as MutableList<DocumentReference>
        members.remove(Firebase.firestore.collection("users").document(userId))
        Firebase.firestore.collection("groups").document(docId).update("membersIds", members)
            .await()
        return Resource.Success(Unit)
    }

    override suspend fun addUserToGroupDB(docId: String, userId: String): Resource<Boolean> {
        val result = Firebase.firestore.collection("groups").document(docId).get().await()
        val members = result["membersIds"] as MutableList<DocumentReference>
        val newUserReference = Firebase.firestore.collection("users").document(userId)

        if (members.contains(newUserReference))
            return Resource.Success(false)

        members.add(newUserReference)
        val lastMessage = result["lastMessage"] as MutableMap<String, Any>
        val readMap = lastMessage["read"] as MutableMap<String, Boolean>
        readMap[userId] = false
        lastMessage["read"] = readMap
        Firebase.firestore.collection("groups").document(docId).update("membersIds", members)
            .await()
        Firebase.firestore.collection("groups").document(docId).update("lastMessage", lastMessage)
            .await()

        return Resource.Success(true)
    }

    override suspend fun updateSubscriptionsDB(
        userId: String,
        remainingSubscriptions: MutableList<String>
    ) {
        Firebase.firestore.collection("users").document(userId)
            .update("subscribedTopics", remainingSubscriptions)
    }

    override suspend fun saveImageInStorageDB(docId: String, imageBitmap: Bitmap): Resource<Uri> {
        val storageReference =
            Firebase.storage.reference.child("images/$docId/${UUID.randomUUID()}")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        storageReference.putBytes(baos.toByteArray()).await()
        val result = storageReference.downloadUrl.await()

        return Resource.Success(result)
    }
}