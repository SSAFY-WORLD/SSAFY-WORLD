package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentCommunityBinding

private const val TAG = "CommunityFragment_싸피"

class CommunityFragment : BaseFragment<FragmentCommunityBinding>(FragmentCommunityBinding::bind, R.layout.fragment_community) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
    }

    private fun initButton() {
        with(binding) {
            communityCvFreeBoard.setOnClickListener {
                Log.d(TAG, "initButton: ")
                val action = CommunityFragmentDirections.actionCommunityFragmentToCommunityListFragment("free")
                navController.navigate(action)
            }
        }
    }
}