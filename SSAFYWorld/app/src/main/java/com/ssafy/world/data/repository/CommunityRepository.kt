package com.ssafy.world.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.User
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "CommunityRepository"

object CommunityRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // Community
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

    suspend fun getCommunityById(collection: String, communityId: String): Community {
        val communityRef = firestore.collection(collection).document(communityId)
        return try {
            val snapshot = communityRef.get().await()
            if (snapshot.exists()) {
                val community = snapshot.toObject(Community::class.java)
                community?.id = snapshot.id
                community ?: Community()
            } else {
                Community()
            }
        } catch (e: Exception) {
            Community()
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

    suspend fun deleteCommunity(collection: String, communityId: String): Boolean {
        val communityRef = firestore.collection(collection).document(communityId)
        return try {
            // 커뮤니티 삭제
            communityRef.delete().await()
            true // 삭제 성공
        } catch (e: Exception) {
            false // 삭제 실패
        }
    }

    suspend fun updateCommunity(collection: String, community: Community): Boolean {
        val communityRef = firestore.collection(collection).document(community.id)
        return try {
            // 커뮤니티 업데이트
            communityRef.set(community).await()
            true // 업데이트 성공
        } catch (e: Exception) {
            false // 업데이트 실패
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

    // Comment
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

    suspend fun insertReply(collection: String, community: Community, comment: Comment): Boolean {
        val curCollection = firestore.collection("reply")
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

    suspend fun incrementCommunityLikeCount(
        collection: String,
        communityId: String,
        userId: String
    ): Int {
        val communityRef = firestore.collection(collection).document(communityId)
        return try {
            addLikedUserToCommunity(collection, userId, communityId)
            firestore.runTransaction { transaction ->
                val community = transaction.get(communityRef).toObject(Community::class.java)
                community?.let {
                    it.likeCount += 1
                    transaction.set(communityRef, it)
                }
                community?.likeCount
            }.await() ?: 0
        } catch (e: Exception) {
            // Return the current likeCount in case of an error
            val snapshot = communityRef.get().await()
            snapshot.toObject(Community::class.java)?.likeCount ?: 0
        }
    }

    suspend fun decrementCommunityLikeCount(
        collection: String,
        communityId: String,
        userId: String
    ): Int {
        val communityRef = firestore.collection(collection).document(communityId)
        return try {
            removeLikedUserFromCommunity(collection, userId, communityId)
            firestore.runTransaction { transaction ->
                val community = transaction.get(communityRef).toObject(Community::class.java)
                community?.let {
                    it.likeCount -= 1
                    transaction.set(communityRef, it)
                }
                community?.likeCount
            }.await() ?: 0
        } catch (e: Exception) {
            // Return the current likeCount in case of an error
            val snapshot = communityRef.get().await()
            snapshot.toObject(Community::class.java)?.likeCount ?: 0
        }
    }

    suspend fun getHotCommunities(): ArrayList<Community> {
        val collectionRefs = arrayOf(
            firestore.collection("free"),
            firestore.collection("question"),
            firestore.collection("company"),
            firestore.collection("market"),
            firestore.collection("store"),
            firestore.collection("room")
        )

        val communityList = arrayListOf<Community>()

        try {
            for (collectionRef in collectionRefs) {
                val querySnapshot = collectionRef.whereGreaterThan("likeCount", 0).get().await()
                for (document in querySnapshot) {
                    val community = document.toObject(Community::class.java)
                    communityList.add(community)
                }
            }
        } catch (e: Exception) {
            // Error handling
        }
        communityList.sortByDescending { it.time }
        return communityList
    }




    suspend fun addLikedUserToCommunity(collection: String, userId: String, communityId: String) {
        val communityRef = firestore.collection(collection).document(communityId)
        firestore.runTransaction { transaction ->
            val communitySnapshot = transaction.get(communityRef)
            val likedUserIds =
                communitySnapshot.toObject(Community::class.java)?.likedUserIds ?: ArrayList()
            likedUserIds.add(userId)
            transaction.update(communityRef, "likedUserIds", likedUserIds)
        }.await()
    }

    suspend fun removeLikedUserFromCommunity(
        collection: String,
        userId: String,
        communityId: String
    ) {
        val communityRef = firestore.collection(collection).document(communityId)
        firestore.runTransaction { transaction ->
            val communitySnapshot = transaction.get(communityRef)
            val likedUserIds = communitySnapshot.toObject(Community::class.java)?.likedUserIds
            likedUserIds?.remove(userId)
            transaction.update(communityRef, "likedUserIds", likedUserIds)
        }.await()
    }


    suspend fun deleteComment(
        collection: String,
        community: Community,
        commentId: String
    ): Boolean {
        val commentRef = firestore.collection("comment").document(commentId)
        Log.d(TAG, "deleteComment: $collection $commentId")
        return try {
            commentRef.delete().await()
            decrementCommunityCommentCount(collection, community, community.id)
            true // 삭제 성공
        } catch (e: Exception) {
            false // 삭제 실패
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

    private suspend fun decrementCommunityCommentCount(
        collection: String,
        community: Community,
        communityId: String
    ) {
        val communityRef = firestore.collection(collection).document(communityId)
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
                .orderBy("time").get().await()
            val commentList = ArrayList<Comment>()
            for (document in querySnapshot.documents) {
                val comment = document.toObject(Comment::class.java)
                comment?.let {
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

    suspend fun getRepliesByCommentId(commentId: String): ArrayList<Comment> {
        val curCollection = firestore.collection("reply")
        Log.d(TAG, "getRepliesByCommentId: $commentId")
        return try {
            val querySnapshot = curCollection.whereEqualTo("commentId", commentId)
                .orderBy("time").get().await()
            val replyList = ArrayList<Comment>()
            for (document in querySnapshot.documents) {
                val reply = document.toObject(Comment::class.java)
                reply?.let {
                    replyList.add(it)
                }
            }
            replyList
        } catch (e: Exception) {
            ArrayList()
        }
    }

    suspend fun deleteReplyById(replyId: String, commentId: String): Boolean {
        return try {
            val replyCollection = firestore.collection("reply")
            val replyDoc = replyCollection.document(replyId)
            replyDoc.delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete reply: $e")
            false
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

    suspend fun searchCommunities(searchText: String): ArrayList<Community> {
        val collectionRefs = arrayOf(
            firestore.collection("free"),
            firestore.collection("question"),
            firestore.collection("company"),
            firestore.collection("market"),
            firestore.collection("room")
        )

        val communityList = arrayListOf<Community>()

        try {
            for (collectionRef in collectionRefs) {
                val querySnapshot = collectionRef
                    .whereGreaterThanOrEqualTo("title", searchText)
                    .get()
                    .await()
                for (document in querySnapshot) {
                    val community = document.toObject(Community::class.java)
                    if (community.title.contains(searchText) || community.content.contains(searchText)) {
                        communityList.add(community)
                    }
                }
            }
        } catch (e: Exception) {
            // Error handling
        }

        communityList.sortByDescending { it.time }
        return communityList
    }

    suspend fun searchCommunitiesInCollection(collection: String, searchText: String): ArrayList<Community> {
        val collectionRef = firestore.collection(collection)
        val communityList = arrayListOf<Community>()

        try {
            val querySnapshot = collectionRef
                .whereGreaterThanOrEqualTo("title", searchText)
                .get()
                .await()
            for (document in querySnapshot) {
                val community = document.toObject(Community::class.java)
                if (community.title.contains(searchText) || community.content.contains(searchText)) {
                    communityList.add(community)
                }
            }
        } catch (e: Exception) {
            // Error handling
        }

        communityList.sortByDescending { it.time }
        return communityList
    }


}