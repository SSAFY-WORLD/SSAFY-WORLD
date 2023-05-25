package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ssafy.world.data.model.User
import com.ssafy.world.data.service.FCMService
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "UserRepository"

object UserRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val userCollection: CollectionReference
        get() = firestore.collection("users")

    suspend fun insertUser(user: User): User {
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

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            userCollection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteUser error: ${e.message}")
            false
        }
    }

    suspend fun updateUserPwd(userId: String, userPwd: String): Boolean {
        return try {
            userCollection.document(userId)
                .update("pwd", userPwd)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "updateUserPwd error: ${e.message}")
            false
        }
    }

    suspend fun updateUserDetails(updateUser: User): Boolean {
        return try {
            val userSnapshot = userCollection.document(updateUser.id).get().await()
            val user = userSnapshot.toObject(User::class.java)
            if (user != null) {
                val updatedFields = mutableMapOf<String, Any>()
                updatedFields["name"] = updateUser.name
                updatedFields["nickname"] = updateUser.nickname
                if (updateUser.profilePhoto.isNotEmpty() && updateUser.profilePhoto != user.profilePhoto) {
                    // 이미지 업로드
                    val imageFile = File(updateUser.profilePhoto)
                    val imageRef = getUserImageRef()
                    val imageBytes = readFileBytes(imageFile)
                    val imageUrl = uploadImageAsync(imageBytes, imageRef)
                    updatedFields["profilePhoto"] = imageUrl
                }
                userCollection.document(updateUser.id).update(updatedFields).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "updateUserDetails error: ${e.message}")
            false
        }
    }

    // FCM Token Update
    suspend fun updateUserToken(userId: String): Boolean {
        val token = FCMService.getToken()
        return try {
            if (token.isNotBlank()) {
                userCollection.document(userId)
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

    suspend fun saveToValidationCollection(value: String): Boolean {
        return try {
            val validationCollection = firestore.collection("validation")
            val querySnapshot = validationCollection.get().await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.reference.update("code", value).await()
            } else {
                validationCollection.add(mapOf("code" to value)).await()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save value: ${e.message}")
            false
        }
    }

    suspend fun getValidationCode(): String? {
        return try {
            val validationCollection = firestore.collection("validation")
            val querySnapshot = validationCollection.get().await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val code = document.getString("code")
                code
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve validation code: ${e.message}")
            null
        }
    }

    private suspend fun uploadImageAsync(
        imageBytes: ByteArray,
        imageRef: StorageReference
    ): String = suspendCoroutine { continuation ->
        val uploadTask = imageRef.putBytes(imageBytes)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                continuation.resume(uri.toString())
            }.addOnFailureListener { error ->
                continuation.resumeWithException(error)
            }
        }.addOnFailureListener { error ->
            continuation.resumeWithException(error)
        }
    }

    private fun getUserImageRef(): StorageReference {
        val time = System.currentTimeMillis()
        val imagesRef = storage.reference.child("user_profile_images")
        return imagesRef.child("$time.jpg")
    }

    private fun readFileBytes(file: File): ByteArray {
        val inputStream = FileInputStream(file)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        inputStream.close()
        outputStream.close()
        return outputStream.toByteArray()
    }


}