package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityDetailBinding
import com.ssafy.world.src.main.MainActivity
import com.ssafy.world.src.main.MainActivityViewModel

private const val TAG = "CommunityDetailFragment"
class CommunityDetailFragment : BaseFragment<FragmentCommunityDetailBinding>(FragmentCommunityDetailBinding::bind, R.layout.fragment_community_detail) {
    val args: CommunityDetailFragmentArgs by navArgs()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    private lateinit var community: Community


    private val myAdapter: CommunityDetailPhotoAdapter by lazy {
        CommunityDetailPhotoAdapter(myContext)
    }
    private val commentAdapter: CommunityDetailCommentAdapter by lazy {
        CommunityDetailCommentAdapter(myContext)
    }
    private var commentList = arrayListOf<Comment>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        community = args.community
        initView()
        initButton()
        initListener()
    }

    private fun initView() = with(binding) {
        detailTitle.text = community.title
        nickname.text = community.userNickname
        time.text = community.getFormattedTime()
        detailContent.text = community.content
    
        myAdapter.submitList(community.photoUrls.toMutableList())
        photoRecyclerview.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter
        }
        commentAdapter.submitList(commentList.toMutableList())
        commentRecyclerview.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = commentAdapter
        }

        communityViewModel.getCommentsByCommunityId(community.id)
    }


    private fun initButton() = with(binding) {
        val curUser = ApplicationClass.sharedPreferences.getUser()
        commentBtnWrite.setOnClickListener {
            val cur = Comment().apply {
                userId = curUser!!.nickname
                userProfile = curUser!!.profilePhoto
                comment = commentInput.text.toString()
                time = System.currentTimeMillis()
                communityId = community.id

            }
            communityViewModel.insertComment(activityViewModel.entryCommunityCollection, community, cur)
            (activity as MainActivity).hideKeyboard()
            commentInput.setText("")
            showCustomToast("댓글이 등록됐어요.")
        }
    }

    private fun initListener() {
        communityViewModel.comments.observe(viewLifecycleOwner) {
            Log.d(TAG, "initListener: $it")
            commentList = it
            commentAdapter.submitList(commentList.toMutableList())
        }
    }
}