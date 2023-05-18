package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentRegisterBinding
import com.ssafy.world.data.model.User

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::bind,
    R.layout.fragment_register
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
        initEditTextListener()
    }

    private fun initButton() = with(binding) {
        registerBtn.setOnClickListener {
            // TODO: 서버 등록 + 아이디 중복 체크
            val curUser = User().apply {
                id = idEditTextView.text.toString()
                email = idEditTextView.text.toString()
                pwd = pwdEditTextView.text.toString()
                name = nameEditTextView.text.toString()
                nickname = nicknameEditTextView.text.toString()
            }

            ApplicationClass.sharedPreferences.saveUser(curUser)
        }
    }

    private fun initEditTextListener() = with(binding) {
        idEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        pwdEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        pwdCheckEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        nameEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        nicknameEditTextView.addTextChangedListener {
            checkEditTextValues()
        }
    }

    private fun checkEditTextValues() = with(binding) {
        val isEmpty = idEditTextView.text.toString().isEmpty() ||
                pwdEditTextView.text.toString().isEmpty() ||
                pwdCheckEditTextView.text.toString().isEmpty() ||
                nameEditTextView.text.toString().isEmpty() ||
                nicknameEditTextView.text.toString().isEmpty()

        if (isEmpty) {
            registerBtn.isEnabled = false
            registerBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_gray))
            registerBtn.setTextColor(ContextCompat.getColor(myContext, R.color.dark_gray))
        } else {
            registerBtn.isEnabled = true
            registerBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_blue))
            registerBtn.setTextColor(ContextCompat.getColor(myContext, R.color.white))

        }
    }

}