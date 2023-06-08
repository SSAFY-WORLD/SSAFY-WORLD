package com.ssafy.world.ui.main.community.map

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityMapListBinding
import com.ssafy.world.ui.main.MainActivityViewModel
import com.ssafy.world.ui.main.community.CommunityListAdapter
import com.ssafy.world.ui.main.community.CommunityViewModel

class CommunityMapFragment : BaseFragment<FragmentCommunityMapListBinding>(
    FragmentCommunityMapListBinding::bind,
    R.layout.fragment_community_map_list
), SwipeRefreshLayout.OnRefreshListener {

    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()

    private var communityList: ArrayList<Community> = arrayListOf()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter(myContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRecyclerView()
        initListener()

        communityViewModel.getAllCommunities(activityViewModel.entryCommunityCollection)
    }

    private fun initView() = with(binding) {
        swipeLayout.setOnRefreshListener(this@CommunityMapFragment) // SwipeRefreshLayout에 리스너 설정

        toolbarText.text = activityViewModel.getCommunityTitle()
        mapBtn.setOnClickListener {
            navController.navigate(R.id.action_communityMapFragment_to_mapFragment)
        }
        searchBtn.setOnClickListener {
            navController.navigate(R.id.action_communityMapFragment_to_communityMapSearchFragment)
        }
        communityFab.setOnClickListener {
            navController.navigate(R.id.action_communityMapFragment_to_communityMapSearchFragment)
        }
    }

    private fun initRecyclerView() = with(binding) {
        myAdapter.submitList(communityList.toMutableList())
        communityRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter

            val dividerItemDecoration =
                DividerItemDecoration(myContext, LinearLayoutManager.VERTICAL)
            communityRv.addItemDecoration(dividerItemDecoration)

            myAdapter.itemClickListener = object : CommunityListAdapter.ItemClickListener {
                override fun onClick(view: View, data: Community, position: Int) {
                    val bundle = bundleOf("communityId" to data.id)
                    navController.navigate(R.id.action_communityMapFragment_to_communityMapDetail, bundle)
                }
            }
        }
    }

    private fun initListener() {
        communityViewModel.communityList.observe(viewLifecycleOwner) {
            if (!it.isEmpty()) {
                communityList = it
                myAdapter.submitList(communityList.toMutableList())
                Handler().postDelayed({
                    binding.communityRv.scrollToPosition(0)
                }, 100)
            }
            // 새로 고침 완료 후에 SwipeRefreshLayout의 로딩 상태 제거
            binding.swipeLayout.isRefreshing = false
        }
    }

    // SwipeRefreshLayout의 새로 고침 이벤트 처리
    override fun onRefresh() {
        communityViewModel.getAllCommunities(activityViewModel.entryCommunityCollection)
    }
}
