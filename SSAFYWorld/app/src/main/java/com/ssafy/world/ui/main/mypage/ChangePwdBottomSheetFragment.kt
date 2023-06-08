package com.ssafy.world.ui.main.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.BottomChangePwdBinding
import com.ssafy.world.ui.main.auth.AuthViewModel

class ChangePwdBottomSheetFragment(private val user: User) : BottomSheetDialogFragment() {
	private var _binding: BottomChangePwdBinding? = null
	private val binding get() = _binding!!
	private var currentPwdCheck = false
	private var newPwdCheck = false
	private var confirmPwdCheck = false

	private val authViewModel: AuthViewModel by viewModels()

	override fun getTheme(): Int {
		return R.style.CustomBottomSheetDialog
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = BottomChangePwdBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initObserver()
		initButton()
		initEditTextListener()
		initObserver()
	}

	private fun initObserver() = with(authViewModel) {
		updateUserPwdSuccess.observe(viewLifecycleOwner) { success ->
			if (success) {
				changeUser()
				Toast.makeText(context, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun changeUser() {
		val updateUser = user.apply {
			pwd = binding.newPwd.text.toString()
		}
		ApplicationClass.sharedPreferences.apply {
			clearUser()
			saveUser(updateUser)
		}
		dismiss()
	}

	private fun initEditTextListener() = with(binding) {
		currentPwd.addTextChangedListener {
			checkPasswordValues()
			checkEditTextValues()
		}

		newPwd.addTextChangedListener {
			checkPasswordValues()
			checkEditTextValues()
		}

		confirmPwd.addTextChangedListener {
			checkPasswordValues()
			checkEditTextValues()
		}
	}

	private fun checkPasswordValues() = with(binding) {
		val currentPwd = currentPwd.text.toString()
		val password = newPwd.text.toString()
		val confirmPassword = confirmPwd.text.toString()

		if (currentPwd.isEmpty()) {
			currentPwdTextField.error = null
			currentPwdCheck = false
		} else if (currentPwd != user.pwd) {
			currentPwdTextField.error = "비밀번호가 일치하지 않습니다."
			currentPwdCheck = false
		} else {
			currentPwdTextField.error = null
			currentPwdCheck = true
		}

		if (password.isEmpty() || confirmPassword.isEmpty()) {
			confirmPwdTextField.error = null
			confirmPwdCheck = false
		} else if (password != confirmPassword) {
			confirmPwdTextField.error = "비밀번호가 일치하지 않습니다."
			confirmPwdCheck = false
		} else {
			confirmPwdTextField.error = null
			confirmPwdCheck = true
		}

		if (password == user.pwd) {
			pwdTextField.error = "기존과 동일한 비밀번호 입니다."
			newPwdCheck = false
		} else {
			pwdTextField.error = null
			newPwdCheck = true
		}
	}

	private fun checkEditTextValues() = with(binding) {
		val validate = !currentPwdCheck || !confirmPwdCheck || !newPwdCheck

		if (validate) {
			changePwdBtn.isEnabled = false
			changePwdBtn.setBackgroundColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.light_gray
				)
			)
			changePwdBtn.setTextColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.dark_gray
				)
			)
		} else {
			changePwdBtn.isEnabled = true
			changePwdBtn.setBackgroundColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.light_blue
				)
			)
			changePwdBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
		}
	}

	private fun initButton() = with(binding) {
		changePwdBtn.setOnClickListener {

			// 비밀번호 변경하기
			authViewModel.updateUserPwd(user.id, newPwd.text.toString())
		}
		currentPwd.setOnFocusChangeListener { view, hasFocus ->
			checkEditTextValues()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}