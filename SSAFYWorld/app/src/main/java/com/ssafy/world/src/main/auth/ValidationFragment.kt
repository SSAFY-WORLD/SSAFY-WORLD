package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentValidationBinding

class ValidationFragment : BaseFragment<FragmentValidationBinding>(FragmentValidationBinding::bind, R.layout.fragment_validation) {
    private val authViewModel: AuthViewModel by viewModels()
    private var validatedCode = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListener()
    }

    private fun initView() = with(binding) {
        authViewModel.getValidationCode()
        if(ApplicationClass.sharedPreferences.getValidation() != null) {
            navController.navigate(R.id.action_validationFragment_to_loginFragment)
        }
        enterBtn.setOnClickListener {
            ApplicationClass.sharedPreferences.saveValidation(validatedCode)
            navController.navigate(R.id.action_validationFragment_to_loginFragment)
        }

        validationEditTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s?.toString()
                if (inputText == validatedCode) {
                    // 일치하는 경우
                    idTextField.error = null
                    idTextField.setHint("환영합니다!")

                    enterBtn.isEnabled = true
                    enterBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_blue))
                    enterBtn.setTextColor(ContextCompat.getColor(myContext, R.color.white))
                } else {
                    // 일치하지 않는 경우
                    idTextField.error = "인증번호가 일치하지 않습니다"
                    idTextField.setHint("인증번호를 입력해주세요")
                    enterBtn.isEnabled = false
                    enterBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_gray))
                    enterBtn.setTextColor(ContextCompat.getColor(myContext, R.color.dark_gray))
                }
            }
        })


    }

    private fun initListener() {
        authViewModel.validationSuccess.observe(viewLifecycleOwner) {
            validatedCode = it
        }
    }
}