package com.ssafy.world.src.main.mypage

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import com.ssafy.world.utils.CustomAlertDialog

class MypageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {
    private val authViewModel: AuthViewModel by viewModels()
    private var isModifyClicked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initButton()
    }

    private fun initView() = with(binding) {
        val user = ApplicationClass.sharedPreferences.getUser()!!
        userEmail.text = user.email
        userName.text = user.name
    }

    private fun initButton() = with(binding) {
        val user = ApplicationClass.sharedPreferences.getUser()
        logoutBtn.setOnClickListener {
            showAlertDialog(R.string.logout_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    ApplicationClass.sharedPreferences.clearUser()
                    showCustomToast(R.string.logout_complete)
                    navController.navigate(R.id.action_mypageFragment_to_loginFragment)
                }
            }
        }
        withdrawalBtn.setOnClickListener {
            showAlertDialog(R.string.withdrawal_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    if (user?.id != null) {
                        showLoadingDialog(myContext)
                        authViewModel.deleteUser(user.id)
                    }
                }
            }
        }
        changePwdBtn.setOnClickListener {
            if (user != null) {
                ChangePwdBottomSheetFragment(user).show(
                    parentFragmentManager,
                    "ChangePwdBottomSheet"
                )
            }
        }

        imageAddBtn.setOnClickListener {
             showAlertDialog(R.string.change_porfile_title, myContext)
        }
        setUpdateUserBtn()
    }

    private fun setUpdateUserBtn() = with(binding) {
        updateUser.setOnClickListener {
            toogleText()
        }
        closeBtn.setOnClickListener {
            toogleText()
        }
    }

    private fun toogleText() = with(binding) {
        if (!isModifyClicked) {
            // 유저 정보를 볼 수 있게 바꿔줌
            userName.visibility = View.GONE
            userNameEditText.visibility = View.VISIBLE
            userNickNameEditText.visibility = View.VISIBLE
            userNickName.visibility = View.GONE
            imageAddBtn.visibility = View.VISIBLE
            closeBtn.visibility = View.VISIBLE
            // 완료 버튼
            updateUser.apply {
                setText(R.string.account_tv_modify)
            }
        } else {
            // 수정 가능하게 바꿔줌
            userName.visibility = View.VISIBLE
            userNameEditText.visibility = View.GONE
            userNickNameEditText.visibility = View.GONE
            userNickName.visibility = View.VISIBLE
            imageAddBtn.visibility = View.INVISIBLE
            closeBtn.visibility = View.INVISIBLE
            // 완료 버튼
            updateUser.apply {
                setText(R.string.account_tv_updateUser)
            }
        }
        isModifyClicked = !isModifyClicked
    }
    private fun initObserver() = with(authViewModel) {
        isDuplicated.observe(viewLifecycleOwner) { user ->
            // 중복이면 해당 유저를 찾은 것이다
            if (user.id != ""){
                ChangePwdBottomSheetFragment(user).show(
                    parentFragmentManager,
                    "ChangePwdBottomSheet"
                )
            }
        }

        deleteUserSuccess.observe(viewLifecycleOwner) { success ->
            dismissLoadingDialog()
            if (success) {
                showCustomToast(R.string.withdrawal_complete)
                ApplicationClass.sharedPreferences.clearUser()
                navController.navigate(R.id.action_mypageFragment_to_loginFragment)
            }
        }
    }
}