package com.ssafy.world.src.main.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.BottomUserInfoBinding

class UserInfoBottomSheetFragment(
    private val user: User
): BottomSheetDialogFragment() {
    private var _binding: BottomUserInfoBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomUserInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        binding.sendMessageBtn.setOnClickListener {
            val action = UserFragmentDirections.actionUserFragmentToInChatFragment(UserFromProfile = user)
            findNavController().navigate(action)
            dismiss()
        }
    }

    private fun initView() = with(binding) {
        userName.text = user.name
        userNickName.text = user.nickname
        userEmail.text = user.email
        Glide.with(requireActivity())
            .load(user.profilePhoto)
            .error(R.drawable.default_profile_image)
            .into(profileImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}