package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.databinding.FragmentRegisterBinding
import com.ssafy.world.data.model.User

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::bind,
    R.layout.fragment_register
) {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var curUser: User
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
        initEditTextListener()
        initObserver()
    }

    private fun initButton() = with(binding) {
        registerBtn.setOnClickListener {
            curUser = User().apply {
                id = ""
                email = idEditTextView.text.toString()
                pwd = pwdEditTextView.text.toString()
                name = nameEditTextView.text.toString()
                nickname = nicknameEditTextView.text.toString()
            }
            authViewModel.isEmailDuplicate(curUser.email)
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

    private fun initObserver() = with(authViewModel) {
        user.observe(viewLifecycleOwner) { user ->
            if (user.id != "") {
                Toast.makeText(myContext, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                ApplicationClass.sharedPreferences.saveUser(user)
                navController.navigate(R.id.action_registerFragment_to_mainFragment)
            } else {
                Toast.makeText(myContext, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        isDuplicated.observe(viewLifecycleOwner) {
            if(it) {
                Toast.makeText(myContext, "중복된 아이디 입니다.", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.insertUser(curUser)
            }
        }
    }

}