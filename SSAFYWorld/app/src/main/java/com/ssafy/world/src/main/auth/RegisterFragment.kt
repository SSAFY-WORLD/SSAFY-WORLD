package com.ssafy.world.src.main.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.User
import com.ssafy.world.data.service.FCMService
import com.ssafy.world.databinding.FragmentRegisterBinding
import com.ssafy.world.utils.Constants
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "RegisterFragment"

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(
    FragmentRegisterBinding::bind,
    R.layout.fragment_register
) {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var curUser: User

    private var isIdValid = false
    private var isPwdValid = false
    private var isPwdCheckValid = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToken()
        initButton()
        initEditTextListener()
        initObserver()
    }

    private fun initToken() = lifecycleScope.launch {
        curUser = User()
        curUser.token = FCMService.getToken()
    }

    private fun initButton() = with(binding) {
        registerBtn.setOnClickListener {
            curUser.apply {
                id = ""
                email = idEditTextView.text.toString()
                pwd = pwdEditTextView.text.toString()
                name = nameEditTextView.text.toString()
                nickname = nicknameEditTextView.text.toString()
                profilePhoto =
                    Constants.DEFAULT_PROFILE
            }
            authViewModel.insertUser(curUser)
        }
    }

    private fun initEditTextListener() = with(binding) {
        idEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        idEditTextView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                val id = idEditTextView.text.toString()
                authViewModel.isEmailDuplicate(id)
            }
        }


        pwdEditTextView.addTextChangedListener {
            checkPasswordValues()
            checkEditTextValues()
        }

        pwdEditTextView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(!hasFocus) {
                checkPwdValues()
            } else {
                pwdCheckEditTextView.setText("")
            }
        }

        pwdCheckEditTextView.addTextChangedListener {
            checkPasswordValues()
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

        Log.d(TAG, "checkEditTextValues: $isIdValid $isPwdCheckValid")
        if (isEmpty || !isIdValid || !isPwdValid || !isPwdCheckValid) {
            registerBtn.isEnabled = false
            registerBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_gray))
            registerBtn.setTextColor(ContextCompat.getColor(myContext, R.color.dark_gray))
        } else {
            registerBtn.isEnabled = true
            registerBtn.setBackgroundColor(ContextCompat.getColor(myContext, R.color.light_blue))
            registerBtn.setTextColor(ContextCompat.getColor(myContext, R.color.white))

        }
    }

    private fun checkPasswordValues() = with(binding) {
        val password = pwdEditTextView.text.toString()
        val confirmPassword = pwdCheckEditTextView.text.toString()

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            pwdCheckTextField.error = null
            pwdCheckTextField.startIconDrawable = null
            isPwdCheckValid = false
        } else if (password != confirmPassword) {
            pwdCheckTextField.error = "비밀번호가 일치하지 않습니다."
            pwdCheckTextField.startIconDrawable = null
            isPwdCheckValid = false
        } else {
            pwdCheckTextField.error = null
            pwdCheckTextField.setStartIconTintList(null)
            pwdCheckTextField.setStartIconDrawable(R.drawable.ic_check)
            isPwdCheckValid = true
        }
    }

    private fun checkPwdValues() = with(binding) {
        val pwd = pwdEditTextView.text.toString().trim()
        if(pwd.length < 6) {
            pwdTextField.error = "비밀번호는 최소 6자 이상입니다."
            pwdTextField.startIconDrawable = null
            isPwdValid = false
        } else {
            pwdTextField.error = null
            pwdTextField.setStartIconTintList(null)
            pwdTextField.setStartIconDrawable(R.drawable.ic_check)
            isPwdValid = true
        }
    }



    private fun initObserver() = with(authViewModel) {
        user.observe(viewLifecycleOwner) { user ->
            if (user.id != "") {
                ApplicationClass.sharedPreferences.saveUser(user!!)
                navController.navigate(R.id.action_registerFragment_to_mainFragment)
                Toast.makeText(myContext, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(myContext, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        isDuplicated.observe(viewLifecycleOwner)  {
            if (it.id == "") {
                binding.idTextField.error = null
                binding.idTextField.setStartIconTintList(null)
                binding.idTextField.setStartIconDrawable(R.drawable.ic_check)
                isIdValid = true
            } else {
                binding.idTextField.error = "중복된 아이디입니다."
                binding.idTextField.startIconDrawable = null
                isIdValid = false
            }
        }

    }
}