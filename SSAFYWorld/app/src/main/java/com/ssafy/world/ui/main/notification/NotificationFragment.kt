package com.ssafy.world.ui.main.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.local.entity.NotificationEntity
import com.ssafy.world.databinding.FragmentNotificationBinding
import com.ssafy.world.ui.main.MainActivityViewModel

class NotificationFragment : BaseFragment<FragmentNotificationBinding>(FragmentNotificationBinding::bind, R.layout.fragment_notification) {
    private val viewModel: NotificationViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private var notificationsList: MutableList<NotificationEntity> = arrayListOf()

    private val myAdapter: NotificationListAdapter by lazy {
        NotificationListAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
        initRecyclerView()
    }

    private fun initRecyclerView() = with(binding) {
        myAdapter.submitList(notificationsList.toMutableList())
        notificationRv.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter

            val dividerItemDecoration = DividerItemDecoration(myContext, LinearLayoutManager.VERTICAL)
            notificationRv.addItemDecoration(dividerItemDecoration)

            // 알림 클릭시 해당 Community화면으로 이동
            myAdapter.itemClickListener = object: NotificationListAdapter.ItemClickListener {
                override fun onClick(view: View, data: NotificationEntity) {
                    val destination = data.destination.split("-")
                    activityViewModel.entryCommunityCollection = destination[1]
                    val bundle = Bundle().apply {
                        putString("communityId", destination[2])
                    }
                    navController.navigate(R.id.action_notificationFragment_to_communityDetailFragment, bundle)
                    // 클릭된 알람은 database에서 삭제
                    viewModel.deleteNotification(data)
                }
            }
        }
    }

    private fun initListener() = with(viewModel) {
        notifications.observe(viewLifecycleOwner) {
            notificationsList = it.toMutableList()
            myAdapter.submitList(notificationsList.toMutableList())
        }
    }
}