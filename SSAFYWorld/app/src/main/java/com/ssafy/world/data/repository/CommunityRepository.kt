package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "CommunityRepository"

object CommunityRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    suspend fun insertCommunity(community: Community, collection: String): Community {
        val curCollection = firestore.collection(collection)
        return try {
            val newCommunityRef = curCollection.document()
            val newCommunityId = newCommunityRef.id // 새로운 커뮤니티의 ID 값

            // 이미지 업로드
            val imageUrls = ArrayList<String>()
            for (imageUrl in community.photoUrls) {
                val imageFile = File(imageUrl)
                val imageRef = getCommunityImageRef(newCommunityId)
                val imageBytes = readFileBytes(imageFile)
                val imageUrl = uploadImageAsync(imageBytes, imageRef)
                imageUrl?.let {
                    imageUrls.add(it)
                }
            }

            community.photoUrls = imageUrls
            Log.d(TAG, "insertCommunity: $imageUrls")
            // 커뮤니티 추가
            community.id = newCommunityId // 커뮤니티의 ID 필드에 새로운 ID 할당
            newCommunityRef.set(community).await()
            community
        } catch (e: Exception) {
            community
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

    suspend fun getAllCommunities(collection: String): ArrayList<Community> {
        val curCollection = firestore.collection(collection)
        return try {
            val querySnapshot =
                curCollection.orderBy("time", Query.Direction.DESCENDING).get().await()
            val communityList = ArrayList<Community>()
            for (document in querySnapshot.documents) {
                val community = document.toObject(Community::class.java)
                community?.let {
                    it.id = document.id
                    communityList.add(it)
                }
            }
            communityList
        } catch (e: Exception) {
            ArrayList()
        }
    }

    suspend fun insertComment(collection: String, community: Community, comment: Comment): Boolean {
        val curCollection = firestore.collection("comment")
        val newCommentRef = curCollection.document()
        comment.id = newCommentRef.id
        return try {
            newCommentRef.set(comment).await()
            incrementCommunityCommentCount(collection, community, comment.communityId)
            true // 삽입 성공
        } catch (e: Exception) {
            false // 삽입 실패
        }
    }

    private suspend fun incrementCommunityCommentCount(
        collection: String,
        community: Community,
        communityId: String
    ) {
        val communityRef = firestore.collection(collection).document(communityId)
        val newCommentCount = community.commentCount + 1
        Log.d(TAG, "incrementCommunityCommentCount: $newCommentCount")
        firestore.runTransaction { transaction ->
            transaction.update(communityRef, "commentCount", newCommentCount)
        }.await()
    }

    private suspend fun decrementCommunityCommentCount(communityId: String) {
        val communityRef = firestore.collection("community").document(communityId)
        firestore.runTransaction { transaction ->
            val community = transaction.get(communityRef).toObject(Community::class.java)
            community?.let {
                it.commentCount -= 1
                transaction.set(communityRef, it)
            }
        }.await()
    }

    suspend fun getCommentsByCommunityId(communityId: String): ArrayList<Comment> {
        val curCollection = firestore.collection("comment")
        Log.d(TAG, "getCommentsByCommunityId: $communityId")
        return try {
            val querySnapshot = curCollection.whereEqualTo("communityId", communityId)
                .orderBy("time", Query.Direction.DESCENDING).get().await()
            val commentList = ArrayList<Comment>()
            for (document in querySnapshot.documents) {
                val comment = document.toObject(Comment::class.java)
                comment?.let {
                    it.id = communityId
                    commentList.add(it)
                }
            }

            Log.d(TAG, "getCommentsByCommunityId2: $commentList")
            commentList
        } catch (e: Exception) {
            Log.e(TAG, "getCommentsByCommunityId: $e")
            ArrayList()
        }
    }


    private fun getCommunityImageRef(communityId: String): StorageReference {
        val time = System.currentTimeMillis()
        val imagesRef = storage.reference.child("community_images")
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