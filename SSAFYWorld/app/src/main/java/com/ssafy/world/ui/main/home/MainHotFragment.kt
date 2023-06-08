package com.ssafy.world.ui.main.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentHotBinding
import com.ssafy.world.ui.main.MainActivityViewModel
import com.ssafy.world.ui.main.community.CommunityListAdapter
import com.ssafy.world.ui.main.community.CommunityViewModel

class MainHotFragment : BaseFragment<FragmentHotBinding>(FragmentHotBinding::bind, R.layout.fragment_hot) {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val viewModel: CommunityViewModel by viewModels()
    private var hotList: ArrayList<Community> = arrayListOf()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter(myContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getHotCommunities()
        initListener()
        initRecyclerView()
    }

    private fun initRecyclerView() = with(binding) {
        myAdapter.submitList(hotList.toMutableList())
        communityRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter

            val dividerItemDecoration = DividerItemDecoration(myContext, LinearLayoutManager.VERTICAL)
            communityRv.addItemDecoration(dividerItemDecoration)

            myAdapter.itemClickListener = object : CommunityListAdapter.ItemClickListener {
                override fun onClick(view: View, data: Community, position: Int) {
                    activityViewModel.entryCommunityCollection = data.collection
                    val action = MainHotFragmentDirections.actionMainHotFragmentToCommunityDetailFragment(data.id)
                    navController.navigate(action)
                }
            }
        }
    }

    private fun initListener() = with(viewModel) {
        communityList.observe(viewLifecycleOwner) {
            hotList = it

            myAdapter.submitList(hotList.toMutableList())
        }
    }


}