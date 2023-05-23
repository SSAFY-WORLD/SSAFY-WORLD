package com.ssafy.world.src.main.community

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunitySearchBinding
import com.ssafy.world.src.main.MainActivityViewModel

class CommunitySearchFragment : BaseFragment<FragmentCommunitySearchBinding>(
    FragmentCommunitySearchBinding::bind,
    R.layout.fragment_community_search
) {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    private var myList: ArrayList<Community> = arrayListOf()

    private val myAdapter: CommunityListAdapter by lazy {
        CommunityListAdapter(myContext)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initEditListener()
        initListener()
    }

    private fun initView() = with(binding) {
        myAdapter.submitList(myList.toMutableList())
        communityRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter
        }
    }

    private fun initEditListener() = with(binding) {
        idEditTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 검색 버튼을 눌렀을 때 실행될 동작을 여기에 작성합니다.
                if (activityViewModel.entryCommunityCollection == "") {
                    communityViewModel.getSearchCommunities(idEditTextView.text.toString())
                } else {
                    communityViewModel.getSearchCommunitiesInCollection(
                        activityViewModel.entryCommunityCollection,
                        idEditTextView.text.toString()
                    )
                }
                showLoadingDialog(myContext)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun initListener() = with(binding) {
        communityViewModel.communityList.observe(viewLifecycleOwner) {
            dismissLoadingDialog()
            if(it.isEmpty()) {
                communityRv.visibility = View.GONE
                noResultLl.visibility =View.VISIBLE
                return@observe
            }  else {
                communityRv.visibility = View.VISIBLE
                noResultLl.visibility =View.GONE
                myList = it
                myAdapter.submitList(it)
            }
        }
    }
}