package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityListBinding
import com.ssafy.world.src.main.MainActivityViewModel

private const val TAG = "CommunityListFragment_싸피"
class CommunityListFragment : BaseFragment<FragmentCommunityListBinding>(FragmentCommunityListBinding::bind, R.layout.fragment_community_list) {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    val args: CommunityListFragmentArgs by navArgs()

    private var communityList: ArrayList<Community> = arrayListOf()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initRecyclerView()
        initButton()
        initListener()

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
                    Toast.makeText(myContext, "item clicked...${data}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initButton() = with(binding) {
        communityFab.setOnClickListener {
            val action = CommunityListFragmentDirections.actionCommunityListFragmentToCommunityWriteFragment("free")
            navController.navigate(action)
        }
    }

    private fun initListener() {
        communityViewModel.communityList.observe(viewLifecycleOwner) {
            if(!it.isEmpty()) {
                communityList = it
                myAdapter.submitList(communityList.toMutableList())
            }
        }
    }
}