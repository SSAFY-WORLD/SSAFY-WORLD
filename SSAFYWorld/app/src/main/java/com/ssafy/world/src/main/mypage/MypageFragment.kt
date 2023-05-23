package com.ssafy.world.src.main.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import com.ssafy.world.utils.CustomAlertDialog
import com.ssafy.world.utils.ValidationAlertDialog

class MypageFragment :
    BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var validationDialog: ValidationAlertDialog
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
        if (user.email == "manager") {
            validationLl.visibility = View.VISIBLE
        }
    }

    private fun initButton() = with(binding) {
        val user = ApplicationClass.sharedPreferences.getUser()
        logoutBtn.setOnClickListener {
            showAlertDialog(R.string.logout_text, myContext)
            mCustomDialog.listener = object : CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    ApplicationClass.sharedPreferences.clearUser()
                    showCustomToast(R.string.logout_complete)
                    navController.navigate(R.id.action_mypageFragment_to_loginFragment)
                }
            }
        }
        withdrawalBtn.setOnClickListener {
            showAlertDialog(R.string.withdrawal_text, myContext)
            mCustomDialog.listener = object : CustomAlertDialog.DialogClickedListener {
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
        validationBtn.setOnClickListener {
            val code = generateVerificationCode()
            validationDialog = ValidationAlertDialog(code, myContext)
            validationDialog.listener = object : ValidationAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    authViewModel.saveToValidationCollection(code)
                }
            }
            validationDialog.show()
        }
    }

    private fun initObserver() = with(authViewModel) {
        isDuplicated.observe(viewLifecycleOwner) { user ->
            // 중복이면 해당 유저를 찾은 것이다
            if (user.id != "") {
                ChangePwdBottomSheetFragment(user).show(
                    parentFragmentManager,
                    "RegisterBottomSheet"
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

        validationSuccess.observe(viewLifecycleOwner) { code ->
            validationDialog.dismiss()
            showCustomToast("인증코드가 등록되었습니다")

        }
    }

    private fun generateVerificationCode(): String {
        val charPool: List<Char> = ('0'..'9') + ('A'..'Z') + ('a'..'z')
        val codeLength = 6

        return (1..codeLength)
            .map { charPool.random() }
            .joinToString("")
    }

}