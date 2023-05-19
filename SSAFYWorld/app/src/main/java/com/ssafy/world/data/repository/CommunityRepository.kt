package com.ssafy.world.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.User
import kotlinx.coroutines.tasks.await

object CommunityRepository {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun insertCommunity(community: Community, collection: String) : Community {
        val curCollection = firestore.collection(collection)
        return try {
            val newCommunityRef = curCollection.add(community).await()
            community.apply { id = newCommunityRef.id }
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




}