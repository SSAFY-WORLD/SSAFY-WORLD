package com.ssafy.world.src.main.user

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentUserBinding
import com.ssafy.world.utils.LineDividerItemDecoration


class UserFragment : BaseFragment<FragmentUserBinding>(FragmentUserBinding::bind, R.layout.fragment_user) {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var userListAdapter: UserListAdapter
    private var userList: ArrayList<User> = arrayListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUsers()
        initAdapter()
        initObserver()

    }

    private fun initAdapter() = with(binding) {
        userListAdapter = UserListAdapter()
        userListAdapter.submitList(userList)
        usersRecyclerView.apply {
            adapter = userListAdapter

            usersRecyclerView.addItemDecoration(LineDividerItemDecoration(myContext, R.drawable.recyclerview_divider, 10, 10))

            userListAdapter.itemClickListener = object : UserListAdapter.ItemClickListener {
                override fun onClick(view: View, user: User, position: Int) {
                    val bottomSheetDialogFragment = UserInfoBottomSheetFragment(user)
                    // 유저 정보 보여주기
                    bottomSheetDialogFragment.show(childFragmentManager, bottomSheetDialogFragment.tag)
                }
            }
        }
    }

    private fun initObserver() {
        userViewModel.users.observe(viewLifecycleOwner) {
            userList = it
            userListAdapter.submitList(userList.toMutableList())
        }
    }

    private fun initUsers() {
        var currentUserEmail = ApplicationClass.sharedPreferences.getUser()?.email
        if (currentUserEmail == null) {
            currentUserEmail = ""
        }
        userViewModel.getAllUsers(currentUserEmail)
    }
}
