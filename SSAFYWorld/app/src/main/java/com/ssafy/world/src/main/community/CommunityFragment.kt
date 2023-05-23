package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentCommunityBinding
import com.ssafy.world.src.main.MainActivityViewModel

private const val TAG = "CommunityFragment_싸피"

class CommunityFragment : BaseFragment<FragmentCommunityBinding>(
    FragmentCommunityBinding::bind,
    R.layout.fragment_community
) {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
    }

    private fun initButton() = with(binding) {

        communityFree.setOnClickListener {
            activityViewModel.entryCommunityCollection = "free"
            activityViewModel.communityTitle = "자유 게시판"
            navController.navigate(R.id.action_communityFragment_to_communityListFragment)
        }
        communityQuestion.setOnClickListener {
            activityViewModel.entryCommunityCollection = "question"
            activityViewModel.communityTitle = "질문 게시판"
            navController.navigate(R.id.action_communityFragment_to_communityListFragment)
        }
        communityMarket.setOnClickListener {
            activityViewModel.communityTitle = "장터 게시판"
            activityViewModel.entryCommunityCollection = "market"
            navController.navigate(R.id.action_communityFragment_to_communityListFragment)
        }

        communiyCompany.setOnClickListener {
            activityViewModel.communityTitle = "취업 게시판"
            activityViewModel.entryCommunityCollection = "company"
            navController.navigate(R.id.action_communityFragment_to_communityListFragment)
        }
        searchBtn.setOnClickListener {
            activityViewModel.entryCommunityCollection = ""
            navController.navigate(R.id.action_communityFragment_to_communitySearchFragment)
        }
    }

}