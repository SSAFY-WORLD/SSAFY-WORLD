package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.kakao.sdk.user.UserApiClient
import com.ssafy.world.R
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentLoginBinding

private const val TAG = "LoginFragment"
class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
    }

    private fun initButton() {
        with(binding) {
            loginBtnRegister.setOnClickListener {
                navController.navigate(R.id.action_loginFragment_to_registerFragment)
            }
            loginBtnLogin.setOnClickListener {
                navController.navigate(R.id.action_loginFragment_to_mainFragment)
            }
            loginBtnKakao.setOnClickListener {
                loginWithKAKAO()
            }
        }
    }

    private fun loginWithKAKAO() {
        UserApiClient.instance.loginWithKakaoTalk(myContext) { token, error ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
                Toast.makeText(myContext, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
            else if (token != null) {
                //TODO: 서버 조회해서 계정없으면 바텀 시트로 닉네임 정보만 받아서 바로 메인으로 이동하기
                navController.navigate(R.id.action_loginFragment_to_mainFragment)
            }
        }
    }
}