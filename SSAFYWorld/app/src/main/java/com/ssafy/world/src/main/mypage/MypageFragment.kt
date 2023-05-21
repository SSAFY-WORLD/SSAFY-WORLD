package com.ssafy.world.src.main.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentMypageBinding

class MypageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            logoutBtn.setOnClickListener {
                ApplicationClass.sharedPreferences.clearUser()
                navController.navigate(R.id.action_mypageFragment_to_loginFragment)
            }
            withdrawalBtn.setOnClickListener {
            }
        }

    }
}