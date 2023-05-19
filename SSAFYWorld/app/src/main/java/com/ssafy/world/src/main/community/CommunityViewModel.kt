package com.ssafy.world.src.main.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.User
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val repository = ApplicationClass.communityRepository

    private val _community = MutableLiveData<Community>()
    val community: LiveData<Community>
        get() = _community

    fun insertCommunity(community: Community, collection: String)  = viewModelScope.launch {
        try {
            _community.value = repository.insertCommunity(community, collection)
        } catch (e: Exception) {
            _community.value = community
        }
    }

    private val _communityList = MutableLiveData<ArrayList<Community>>()
    val communityList: LiveData<ArrayList<Community>>
        get() = _communityList


    fun getAllCommunities(collection: String) = viewModelScope.launch {
        try {
            val communities = repository.getAllCommunities(collection)
            _communityList.value = communities
        } catch (e: Exception) {
            _communityList.value = ArrayList()
        }
    }

}