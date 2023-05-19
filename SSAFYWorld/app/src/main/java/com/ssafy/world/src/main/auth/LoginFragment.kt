package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.kakao.sdk.user.UserApiClient
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentLoginBinding
import com.ssafy.world.data.model.User

private const val TAG = "LoginFragment"

class LoginFragment :
    BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::bind, R.layout.fragment_login) {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var curUser: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        checkLogin()
        initButton()
        initEditTextListener()
        initObserver()
    }

    private fun checkLogin() {
        var user = ApplicationClass.sharedPreferences.getUser()
        if (user != null) {
            navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
    }

    private fun initButton() = with(binding) {
        loginBtnRegister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
        loginBtn.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_mainFragment)
        }
        loginBtnKakao.setOnClickListener {
            loginWithKAKAO()
        }
    }

    private fun initEditTextListener() = with(binding) {
        idEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        pwdEditTextView.addTextChangedListener {
            checkEditTextValues()
        }
    }

    private fun checkEditTextValues() = with(binding) {
        val isEmpty = idEditTextView.text.toString().isEmpty() ||
                pwdEditTextView.text.toString().isEmpty()

        if (isEmpty) {
            loginBtn.isEnabled = false
            loginBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_gray))
            loginBtn.setTextColor(ContextCompat.getColor(myContext, R.color.dark_gray))
        } else {
            loginBtn.isEnabled = true
            loginBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_blue))
            loginBtn.setTextColor(ContextCompat.getColor(myContext, R.color.white))
        }
    }

    private fun loginWithKAKAO() {
        UserApiClient.instance.loginWithKakaoTalk(myContext) { token, error ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
                Toast.makeText(myContext, "로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                //TODO: 서버 조회해서 계정없으면 바텀 시트로 닉네임 정보만 받아서 바로 메인으로 이동하기
                getKAKAOInform()
            }
        }
    }

    private fun getKAKAOInform() {
        // 사용 가능한 모든 동의 항목을 대상으로 추가 동의 필요 여부 확인 및 추가 동의를 요청하는 예제입니다.
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val scopes = mutableListOf<String>()

                if (user.kakaoAccount?.emailNeedsAgreement == true) {
                    scopes.add("account_email")
                }
                if (user.kakaoAccount?.birthdayNeedsAgreement == true) {
                    scopes.add("birthday")
                }
                if (user.kakaoAccount?.birthyearNeedsAgreement == true) {
                    scopes.add("birthyear")
                }
                if (user.kakaoAccount?.genderNeedsAgreement == true) {
                    scopes.add("gender")
                }
                if (user.kakaoAccount?.phoneNumberNeedsAgreement == true) {
                    scopes.add("phone_number")
                }
                if (user.kakaoAccount?.profileNeedsAgreement == true) {
                    scopes.add("profile")
                }
                if (user.kakaoAccount?.ageRangeNeedsAgreement == true) {
                    scopes.add("age_range")
                }
                if (user.kakaoAccount?.ciNeedsAgreement == true) {
                    scopes.add("account_ci")
                }

                if (scopes.isNotEmpty()) {
                    UserApiClient.instance.loginWithNewScopes(myContext, scopes) { token, error ->
                        if (error != null) {
                            Log.e(TAG, "사용자 추가 동의 실패", error)
                        } else {
                            insertUser(user)
                        }
                    }
                } else {
                    insertUser(user)
                }
            }
        }
    }

    private fun insertUser(user: com.kakao.sdk.user.model.User) {
        curUser = User().apply {
            email = user.kakaoAccount?.email.toString()
            nickname = user.kakaoAccount?.profile?.nickname.toString()
            profilePhoto = user.kakaoAccount?.profile?.thumbnailImageUrl.toString()
            kakaoId = user.id.toString()
        }
        authViewModel.isEmailDuplicate(curUser.email)
    }


    private fun initObserver() = with(authViewModel) {
        user.observe(viewLifecycleOwner) { user ->
            if (user.id != "") {
                Toast.makeText(myContext, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                ApplicationClass.sharedPreferences.saveUser(user)
                navController.navigate(R.id.action_loginFragment_to_mainFragment)
            } else {
                Toast.makeText(myContext, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        isDuplicated.observe(viewLifecycleOwner) {
            if(it) {
                navController.navigate(R.id.action_loginFragment_to_mainFragment)
            } else {
                // TODO: 이름, 닉네임 받아서 회원가입 시키기
                insertUser(curUser)
                navController.navigate(R.id.action_loginFragment_to_mainFragment)
            }
        }
    }
}