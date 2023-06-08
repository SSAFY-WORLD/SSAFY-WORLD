package com.ssafy.world.ui.main.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.databinding.BottomPermissionBinding

class PermmissionBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomPermissionBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private var onConfirmationListener: (() -> Unit)? = null

    fun setOnConfirmationListener(listener: () -> Unit) {
        onConfirmationListener = listener
    }

    private var onCancelListener: (() -> Unit)? = null
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }


    private fun initButton() = with(binding) {
        permissionComplete.setOnClickListener {
            onConfirmationListener?.invoke()
            dismiss()
        }
        permissionCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}