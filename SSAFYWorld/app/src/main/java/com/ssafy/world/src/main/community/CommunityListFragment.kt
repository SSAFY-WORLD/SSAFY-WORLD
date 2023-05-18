package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.navArgs
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentCommunityListBinding

private const val TAG = "CommunityListFragment_싸피"
class CommunityListFragment : BaseFragment<FragmentCommunityListBinding>(FragmentCommunityListBinding::bind, R.layout.fragment_community_list) {
    val args: CommunityListFragmentArgs by navArgs()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        val curBoard = args.communityName
        Log.d(TAG, "onViewCreated: $curBoard")
    }
}