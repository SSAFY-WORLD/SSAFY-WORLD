package com.ssafy.world.src.main.mypage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.BottomChangePwdBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePwdBottomSheetFragment(private val user: User) : BottomSheetDialogFragment() {
	private var _binding: BottomChangePwdBinding? = null
	private val binding get() = _binding!!
	private var currentPwdCheck = false
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
				Toast.makeText(context, "비밀번호가 변경되었습니다. 변경된 비빌번호로 로그인 해주세요", Toast.LENGTH_SHORT).show()
				ApplicationClass.sharedPreferences.clearUser()
				findNavController().navigate(R.id.action_changePwdBottomSheetFragment_to_loginFragment)
			}
		}
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
	}

	private fun checkEditTextValues() = with(binding) {
		val validate = !currentPwdCheck || !confirmPwdCheck

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
			val pwd = currentPwd.text.toString()
			checkEditTextValues()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}