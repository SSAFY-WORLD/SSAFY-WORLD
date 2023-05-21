package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ssafy.world.data.model.User
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

    suspend fun isEmailDuplicate(email: String): Boolean {
        return try {
            val querySnapshot = userCollection.whereEqualTo("email", email).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
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
}