package com.ssafy.world.src.main.community

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.repository.CommunityRepository
import kotlinx.coroutines.launch

private const val TAG = "CommunityViewModel"

class CommunityViewModel : ViewModel() {
    private val repository = ApplicationClass.communityRepository

    private val _community = MutableLiveData<Community>()
    val community: LiveData<Community>
        get() = _community

    private val _comments = MutableLiveData<ArrayList<Comment>>()
    val comments: LiveData<ArrayList<Comment>>
        get() = _comments

    private val _replies = MutableLiveData<ArrayList<Comment>>()
    val replies: LiveData<ArrayList<Comment>>
        get() = _replies

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean>
        get() = _updateSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean>
        get() = _deleteSuccess

    private val _commentDeleteSuccess = MutableLiveData<Boolean>()
    val commentDeleteSuccess: LiveData<Boolean>
        get() = _commentDeleteSuccess

    private val _replyDeleteSuccess = MutableLiveData<Boolean>()
    val replyDeleteSuccess: LiveData<Boolean>
        get() = _replyDeleteSuccess


    fun insertCommunity(community: Community, collection: String) = viewModelScope.launch {
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

    fun insertComment(collection: String, community: Community, comment: Comment) =
        viewModelScope.launch {
            val success = repository.insertComment(collection, community, comment)
            if (success) {
                val communityId = comment.communityId
                val comments = repository.getCommentsByCommunityId(communityId)
                _comments.value = comments
            } else {
                _comments.value = ArrayList()
            }
        }

    fun insertReply(collection: String, community: Community, comment: Comment) =
        viewModelScope.launch {
            val success = repository.insertReply(collection, community, comment)
            if (success) {
                val communityId = comment.communityId
                val comments = repository.getRepliesByCommentId(comment.commentId)
                _replies.value = comments
            } else {
                _comments.value = ArrayList()
            }
        }
    fun deleteComment(collection: String, community: Community, commentId: String) =
        viewModelScope.launch {
            val success = repository.deleteComment(collection, community, commentId)
            _commentDeleteSuccess.value = success
        }

    fun getCommentsByCommunityId(communityId: String) = viewModelScope.launch {
        try {
            val comments = repository.getCommentsByCommunityId(communityId)
            _comments.value = comments
        } catch (e: Exception) {
            _comments.value = ArrayList()
        }
    }

    fun updateCommunity(collection: String, community: Community) = viewModelScope.launch {
        val success = repository.updateCommunity(collection, community)
        _updateSuccess.value = success
    }

    fun deleteCommunity(collection: String, communityId: String) = viewModelScope.launch {
        val success = repository.deleteCommunity(collection, communityId)
        _deleteSuccess.value = success
    }

    fun fetchCommunityById(collection: String, communityId: String) {
        viewModelScope.launch {
            val community = CommunityRepository.getCommunityById(collection, communityId)
            _community.value = community
        }
    }

    fun getRepliesByCommentId(commentId: String) = viewModelScope.launch {
        val replies = CommunityRepository.getRepliesByCommentId(commentId)
        Log.d(TAG, "getRepliesByCommentId22: $replies")
        _replies.value = replies
    }

    fun deleteReplyById(replyId: String, commentId: String) = viewModelScope.launch {
        val success = CommunityRepository.deleteReplyById(replyId, commentId)
        if(success) {
            getRepliesByCommentId(commentId)
        }
    }



}