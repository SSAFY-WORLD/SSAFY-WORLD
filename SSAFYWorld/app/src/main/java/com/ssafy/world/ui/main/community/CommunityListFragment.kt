package com.ssafy.world.ui.main.community

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityListBinding
import com.ssafy.world.ui.main.MainActivityViewModel

private const val TAG = "CommunityListFragment_싸피"

class CommunityListFragment : BaseFragment<FragmentCommunityListBinding>(FragmentCommunityListBinding::bind, R.layout.fragment_community_list),
    SwipeRefreshLayout.OnRefreshListener {

    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    val args: CommunityListFragmentArgs by navArgs()

    private var communityList: ArrayList<Community> = arrayListOf()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter(myContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initButton()
        initListener()

        communityViewModel.getAllCommunities(activityViewModel.entryCommunityCollection)

        binding.swipeLayout.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        communityViewModel.getAllCommunities(activityViewModel.entryCommunityCollection)
    }

    private fun initRecyclerView() = with(binding) {
        myAdapter.submitList(communityList.toMutableList())
        communityRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter

            val dividerItemDecoration = DividerItemDecoration(myContext, LinearLayoutManager.VERTICAL)
            communityRv.addItemDecoration(dividerItemDecoration)

            myAdapter.itemClickListener = object : CommunityListAdapter.ItemClickListener {
                override fun onClick(view: View, data: Community, position: Int) {
                   val bundle = bundleOf("communityId" to data.id)
                    navController.navigate(R.id.action_communityListFragment_to_communityDetailFragment, bundle)
                }
            }
        }

    }

    private fun initButton() = with(binding) {
        toolbarText.text = activityViewModel.getCommunityTitle()
        communityFab.setOnClickListener {
            val action = CommunityListFragmentDirections.actionCommunityListFragmentToCommunityWriteFragment(
                communityId = "", communityName = "")
            navController.navigate(action)
        }
        searchBtn.setOnClickListener {
            navController.navigate(R.id.action_communityListFragment_to_communitySearchFragment)
        }
    }

    private fun initListener() {
        communityViewModel.communityList.observe(viewLifecycleOwner) {
            if (!it.isEmpty()) {
                Log.d(TAG, "initListener: $communityList")
                communityList = it
                myAdapter.submitList(communityList.toMutableList())

                Handler().postDelayed({
                    binding.communityRv.scrollToPosition(0)
                }, 100)
            }
            binding.swipeLayout.isRefreshing = false
        }
    }
}
