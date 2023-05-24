package com.ssafy.world.src.main.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import com.ssafy.world.src.main.user.UserViewModel
import com.ssafy.world.utils.Constants
import com.ssafy.world.utils.CustomAlertDialog
import com.ssafy.world.utils.ValidationAlertDialog

private const val TAG = "MypageFragment"
class MypageFragment :
    BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {
    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var isModifyClicked = false
    private var selectedPhoto = Constants.DEFAULT_PROFILE

    private lateinit var user: User

    private lateinit var validationDialog: ValidationAlertDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
        initButton()
        initEditTextListener()
    }

    private fun initEditTextListener() = with(binding) {
        userNameEditText.addTextChangedListener {
            validateChangeUser()
        }

        userNickNameEditText.addTextChangedListener {
            validateChangeUser()
        }
    }

    private fun initView() = with(binding) {
        user = ApplicationClass.sharedPreferences.getUser()!!
        setAdjustPanKeyboardType(userNameEditText)
        setAdjustPanKeyboardType(userNickNameEditText)
        initTextView()
        if (user.email == "manager") {
            validationLl.visibility = View.VISIBLE
        }
    }

    private fun Fragment.setAdjustPanKeyboardType(editText: EditText) {
        editText.setRawInputType(EditorInfo.TYPE_CLASS_TEXT)
        editText.setTextIsSelectable(true)
        editText.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun initTextView() = with(binding) {
        userEmail.text = user.email
        userName.text = user.name
        userNameEditText.setText(user.name)
        userNickName.text = user.nickname
        userNickNameEditText.setText(user.nickname)
        // 이미지에서 다시 넘어온 경우
        selectedPhoto = arguments?.getString("profilePhoto") ?: ""
        if (selectedPhoto.isNotEmpty()) {
            isModifyClicked = false
            val modifyName = arguments?.getString("name") ?: ""
            val modifyNickName = arguments?.getString("nickName") ?: ""
            if (modifyName.isNotEmpty()) {
                userName.text = modifyName
                userNameEditText.setText(modifyName)
            }
            if (modifyNickName.isNotEmpty()) {
                userNickName.text = modifyNickName
                userNickNameEditText.setText(modifyNickName)
            }
            toogle()
            setProfilePhoto(selectedPhoto)
            setButton(
                ContextCompat.getColor(myContext, R.color.light_blue),
                ContextCompat.getColor(myContext, R.color.white),
                R.string.account_tv_updateUser,
                true
            )
        } else {
            setProfilePhoto(user.profilePhoto)
        }
    }

    private fun setButton(background: Int, textColor: Int, text: Int, clickable: Boolean) = with(binding) {
        updateUserBtn.apply {
            setBackgroundColor(background)
            updateUserBtn.setTextColor(textColor)
            setText(text)
            isClickable = clickable
        }
    }

    private fun setProfilePhoto(profilePhoto: String) = with(binding) {
        Glide.with(myContext)
            .load(profilePhoto)
            .into(profileImage)
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
            val modifyName = userNameEditText.text.toString()
            val modifyNickname = userNickNameEditText.text.toString()
            val bundle = Bundle().apply {
                if (modifyName != user?.name) {
                    putString("name", modifyName)
                }
                if (modifyNickname != user?.nickname) {
                    putString("nickName", modifyNickname)
                }
            }
            navController.navigate(R.id.action_mypageFragment_to_photoSingleFragment, bundle)
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
        setUpdateUserBtn()
        // 라이선스
        licenceBtn.setOnClickListener {

        }
    }

    private fun setUpdateUserBtn() = with(binding) {
        updateUserBtn.setOnClickListener {
            if (isModifyClicked) {
                // 유저 정보 업데이트
                user.apply {
                    if (selectedPhoto != Constants.DEFAULT_PROFILE) {
                        profilePhoto = selectedPhoto
                    }
                    name = userNameEditText.text.toString()
                    nickname = userNickNameEditText.text.toString()
                }
                showLoadingDialog(myContext)
                userViewModel.updateUser(user)
            } else {
                toogle()
            }
        }
        closeBtn.setOnClickListener {
            initUserProfile()
            toogle()
        }
    }

    private fun validateChangeUser() = with(binding) {
        val newName = userNameEditText.text.toString()
        val newNickname = userNickNameEditText.text.toString()

        if (newName == user.name &&
            newNickname == user.nickname &&
            user.profilePhoto == selectedPhoto) {
            setButton(
                ContextCompat.getColor(myContext, R.color.light_blue),
                ContextCompat.getColor(myContext, R.color.white),
                R.string.account_tv_modify,
                false
            )
        } else {
            setButton(
                ContextCompat.getColor(myContext, R.color.light_blue),
                ContextCompat.getColor(myContext, R.color.white),
                R.string.account_tv_updateUser,
                true
            )
        }
    }

    private fun initUserProfile() = with(binding) {
        setProfilePhoto(user.profilePhoto)
        userName.text = user.name
        userNameEditText.setText(user.name)
        userNickName.text = user.nickname
        userNickNameEditText.setText(user.nickname)
    }

    private fun toogle() = with(binding) {
        isModifyClicked = !isModifyClicked
        if (isModifyClicked) {
            // 수정 가능하게 바꿔줌
            userName.visibility = View.GONE
            userNameEditText.visibility = View.VISIBLE
            userNickNameEditText.visibility = View.VISIBLE
            userNickName.visibility = View.GONE
            imageAddBtn.visibility = View.VISIBLE
            closeBtn.visibility = View.VISIBLE
            // 완료 버튼
            setButton(
                ContextCompat.getColor(myContext, R.color.light_gray),
                ContextCompat.getColor(myContext, R.color.dark_gray),
                R.string.account_tv_modify,
                false
            )
        } else {
            // 유저 정보를 볼 수 있게 바꿔줌
            userName.visibility = View.VISIBLE
            userNameEditText.visibility = View.GONE
            userNickNameEditText.visibility = View.GONE
            userNickName.visibility = View.VISIBLE
            imageAddBtn.visibility = View.INVISIBLE
            closeBtn.visibility = View.INVISIBLE
            // 완료 버튼
            setButton(
                ContextCompat.getColor(myContext, R.color.light_blue),
                ContextCompat.getColor(myContext, R.color.white),
                R.string.account_tv_updateUser,
                true
            )
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

        userViewModel.updateUserPwdSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                userViewModel.getUser(this@MypageFragment.user.id)
            }
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                dismissLoadingDialog()
                ApplicationClass.sharedPreferences.apply {
                    clearUser()
                    saveUser(user)
                }
                this@MypageFragment.user = ApplicationClass.sharedPreferences.getUser()!!
                initTextView()
                showCustomToast(R.string.account_tv_update)
                toogle()
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
        isModifyClicked = false
        selectedPhoto = Constants.DEFAULT_PROFILE
        userViewModel.initialize()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

}