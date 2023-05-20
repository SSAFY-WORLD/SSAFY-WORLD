package com.ssafy.world.src.main.auth

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
import com.ssafy.world.databinding.BottomRegisterBinding

class RegisterBottomSheetFragment(private val user: User) : BottomSheetDialogFragment() {
    private var _binding: BottomRegisterBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initEditTextListener()
    }

    private fun initEditTextListener() = with(binding) {
        nameEditTextView.addTextChangedListener {
            checkEditTextValues()
        }

        nickEditTextView.addTextChangedListener {
            checkEditTextValues()
        }
    }

    private fun checkEditTextValues() = with(binding) {
        val isEmpty = nameEditTextView.text.toString().isEmpty() ||
                nickEditTextView.text.toString().isEmpty()

        if (isEmpty) {
            registerComplete.isEnabled = false
            registerComplete.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_gray
                )
            )
            registerComplete.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_gray
                )
            )
        } else {
            registerComplete.isEnabled = true
            registerComplete.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.light_blue
                )
            )
            registerComplete.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun initButton() = with(binding) {
        registerComplete.setOnClickListener {
            user.apply {
                name = nameEditTextView.text.toString()
                nickname = nickEditTextView.text.toString()
            }
            ApplicationClass.sharedPreferences.saveUser(user)
            authViewModel.insertUser(user)
        }
    }

    private fun initListener() = with(authViewModel) {
        user.observe(viewLifecycleOwner) { user ->
            if (user.id != "") {
                Toast.makeText(context, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show()
                ApplicationClass.sharedPreferences.saveUser(user)
                findNavController().navigate(R.id.action_registerBottomSheetFragment_to_mainFragment)
            } else {
                dismiss()
                Toast.makeText(context, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}