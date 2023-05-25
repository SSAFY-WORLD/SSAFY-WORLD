package com.ssafy.world.src.main.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ssafy.world.BuildConfig
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import com.ssafy.world.src.main.user.UserViewModel
import com.ssafy.world.utils.CustomAlertDialog
import com.ssafy.world.utils.ValidationAlertDialog

private const val TAG = "MypageFragment"
class MypageFragment :
    BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    lateinit var user: User

    private lateinit var validationDialog: ValidationAlertDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initButton()
        checkArguments()
    }

    private fun checkArguments() {
        val name = arguments?.getString("name") ?: ""
        val nickname = arguments?.getString("nickname") ?: ""
        val photo = arguments?.getString("photo")?: user.profilePhoto
        if (name.isNotEmpty() && nickname.isNotEmpty()) {
            val user = User().apply {
                id = user.id
                this.name = name
                this.nickname = nickname
                this.profilePhoto = photo
            }
            ProfileUpdateBottomSheetFragment(this@MypageFragment, user, navController)
                .show(parentFragmentManager, "Profile")
        }
    }

    private fun initView() = with(binding) {
        user = ApplicationClass.sharedPreferences.getUser()!!
        initUserProfile()
        if (user.email == "manager") {
            validationLl.visibility = View.VISIBLE
        }
        versionName.text = BuildConfig.VERSION_NAME
    }

    private fun openAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", myContext.packageName, null)
        }
        myContext.startActivity(intent)
    }


    private fun initButton() = with(binding) {
        logoutBtn.setOnClickListener {
            showAlertDialog(R.string.logout_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    val token = user.token
                    ApplicationClass.sharedPreferences.clearUser()
                    ApplicationClass.sharedPreferences.setUserToken(token)
                    showCustomToast(R.string.logout_complete)
                    navController.navigate(R.id.action_mypageFragment_to_loginFragment)
                }
            }
        }
        withdrawalBtn.setOnClickListener {
            showAlertDialog(R.string.withdrawal_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    showLoadingDialog(myContext)
                    authViewModel.deleteUser(user.id)
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
        // 회원정보 수정 Dialog
        updateUserBtn.setOnClickListener {
            user?.let {
                ProfileUpdateBottomSheetFragment(this@MypageFragment, it, navController)
                    .show(parentFragmentManager, "ProfileDialog")
            }
        }
        permissionBtn.setOnClickListener {
            openAppSettings()
        }
    }


    fun initUserProfile() = with(binding) {
        user = ApplicationClass.sharedPreferences.getUser()!!
        userEmail.text = user.email
        userName.text = user.name
        userNickName.text = user.nickname
        Glide.with(myContext)
            .load(user.profilePhoto)
            .into(profileImage)
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
            if (success) {
                dismissLoadingDialog()
                showCustomToast(R.string.withdrawal_complete)
                ApplicationClass.sharedPreferences.clearUser()
                navController.navigate(R.id.action_mypageFragment_to_loginFragment)
            } else {
                dismissLoadingDialog()
            }
        }

        validationSuccess.observe(viewLifecycleOwner) { code ->
            validationDialog.dismiss()
            showCustomToast("인증코드가 등록되었습니다")

        }

        userViewModel.updateUserSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                userViewModel.getUser(this@MypageFragment.user.id)
            }
        }
    }

    private fun generateVerificationCode(): String {
        val charPool: List<Char> = ('0'..'9') + ('A'..'Z') + ('a'..'z')
        val codeLength = 6

        return (1..codeLength)
            .map { charPool.random() }
            .joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.clear()
    }

}