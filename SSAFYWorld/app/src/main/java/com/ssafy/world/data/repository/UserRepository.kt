package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ssafy.world.data.model.User
import com.ssafy.world.data.service.FCMService
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.tasks.await

private const val TAG = "UserRepository"
object UserRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val userCollection: CollectionReference
        get() = firestore.collection("users")

    suspend fun insertUser(user: User) : User {
        return try {
            val newUserRef = userCollection.add(user).await()
            user.apply { id = newUserRef.id }
            newUserRef.set(user).await()
            user
        } catch (e: Exception) {
            user
        }
    }

    suspend fun getAllUsers(currentUserEmail: String): ArrayList<User> {
        return try {
            val querySnapshot = userCollection
                .whereNotEqualTo("email", currentUserEmail)
                .get()
                .await()
            val userList = ArrayList<User>()
            for (document in querySnapshot.documents) {
                val user = document.toObject(User::class.java)
                user?.let {
                    userList.add(it)
                }
            }
            userList
        } catch (e: Exception) {
            Log.e(TAG, "getAllUsers error: ${e.message}")
            ArrayList()
        }
    }

    suspend fun getUser(userId: String): User? {
        return try {
            val userSnapshot = userCollection.document(userId).get().await()
            userSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun isEmailDuplicate(email: String): User? {
        return try {
            val querySnapshot = userCollection.whereEqualTo("email", email).get().await()
            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents.first()
                val user = userDocument.toObject(User::class.java)?.apply {
                    id = userDocument.id
                }
                Log.d(TAG, "auth: $user")
                user // 중복됨
            } else {
                null // 중복 안됨
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(email: String, password: String): User {
        Log.d(TAG, "login: $email $password")
        return try {
            val querySnapshot = userCollection
                .whereEqualTo("email", email)
                .whereEqualTo("pwd", password)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val userDocument = querySnapshot.documents.first()
                val user = userDocument.toObject(User::class.java)?.apply {
                    id = userDocument.id
                }
                user!!
            } else {
                User()
            }
        } catch (e: Exception) {
            User()
        }
    }

    suspend fun deleteUser(userEmail: String): Boolean {
        return try {
            userCollection.document(userEmail).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser error: ${e.message}")
            false
        }
    }

    // FCM Token Update
    suspend fun updateUserToken(userEmail: String): Boolean {
        val token = FCMService.getToken()
        return try {
            if (token.isNotBlank()) {
                userCollection.document(userEmail)
                    .update(Constants.KEY_TOKEN, token)
                    .await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}