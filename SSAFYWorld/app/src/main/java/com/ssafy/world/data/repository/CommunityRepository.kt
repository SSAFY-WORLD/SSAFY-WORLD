package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.User
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

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
                uploadImage(imageBytes, imageRef)
                imageUrls.add(imageRef.downloadUrl.await().toString())
            }
            community.photoUrls = imageUrls

            // 커뮤니티 추가
            community.id = newCommunityId // 커뮤니티의 ID 필드에 새로운 ID 할당
            newCommunityRef.set(community).await()
            community
        } catch (e: Exception) {
            community
        }
    }

    suspend fun getAllCommunities(collection: String): ArrayList<Community> {
        val curCollection = firestore.collection(collection)
        return try {
            val querySnapshot = curCollection.get().await()
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

    suspend fun insertComment(comment: Comment): Boolean {
        val curCollection = firestore.collection("comment")
        val newCommentRef = curCollection.document()
        comment.id = newCommentRef.id
        return try {
            newCommentRef.set(comment).await()
            true // 삽입 성공
        } catch (e: Exception) {
            false // 삽입 실패
        }
    }

    suspend fun getCommentsByCommunityId(communityId: String): ArrayList<Comment> {
        val curCollection = firestore.collection("comment")
        Log.d(TAG, "getCommentsByCommunityId: $communityId")
        return try {
            val querySnapshot = curCollection.whereEqualTo("communityId", communityId).get().await()
            val commentList = ArrayList<Comment>()
            for (document in querySnapshot.documents) {
                val comment = document.toObject(Comment::class.java)
                comment?.let {
                    it.id = document.id
                    commentList.add(it)
                }
            }
            Log.d(TAG, "getCommentsByCommunityId: $commentList")
            commentList
        } catch (e: Exception) {
            ArrayList()
        }
    }

    suspend fun setCommunityComments(communityId: String, comments: ArrayList<Comment>, collection: String) {
        val curCollection = firestore.collection(collection)
        val communityRef = curCollection.document(communityId)

        try {
            communityRef.update("comments", comments).await()
        } catch (e: Exception) {
            // Handle exception if necessary
        }
    }


    private fun getCommunityImageRef(communityId: String): StorageReference {
        val imagesRef = storage.reference.child("community_images")
        return imagesRef.child("$communityId.jpg")
    }

    private suspend fun uploadImage(imageBytes: ByteArray, imageRef: StorageReference): UploadTask.TaskSnapshot {
        return imageRef.putBytes(imageBytes).await()
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