package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityListBinding

private const val TAG = "CommunityListFragment_싸피"
class CommunityListFragment : BaseFragment<FragmentCommunityListBinding>(FragmentCommunityListBinding::bind, R.layout.fragment_community_list) {
    val args: CommunityListFragmentArgs by navArgs()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        val curBoard = args.communityName

        initRecyclerView()
        initButton()
    }
    val test = Community().apply {
        time = 0
        title = "제목"
        content = "내용"
    }
    val arr = arrayListOf<Community>().apply {
        add(test)
        add(test)
    }
    private fun initRecyclerView() = with(binding) {
        myAdapter.submitList(arr)
        communityRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter

            val dividerItemDecoration = DividerItemDecoration(myContext, LinearLayoutManager.VERTICAL)
            communityRv.addItemDecoration(dividerItemDecoration)

            myAdapter.itemClickListener = object : CommunityListAdapter.ItemClickListener {
                override fun onClick(view: View, data: Community, position: Int) {
                    Toast.makeText(myContext, "item clicked...${data}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initButton() = with(binding) {
        communityFab.setOnClickListener {
            navController.navigate(R.id.action_communityListFragment_to_communityWriteFragment)
        }
    }
}