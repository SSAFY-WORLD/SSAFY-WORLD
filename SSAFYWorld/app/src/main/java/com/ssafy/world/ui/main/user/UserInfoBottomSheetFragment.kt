package com.ssafy.world.ui.main.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.BottomUserInfoBinding
import com.ssafy.world.ui.main.community.CommunityDetailFragmentDirections
import com.ssafy.world.ui.main.community.map.CommunityMapDetailFragmentDirections

class UserInfoBottomSheetFragment(
    private val user: User,
    private val from: String
) : BottomSheetDialogFragment() {
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
            var action: NavDirections? = null
            when (from) {
                "community" -> action =
                    CommunityDetailFragmentDirections.actionCommunityDetailFragmentToInChatFragment(UserFromProfile = user, user = null)
                "user" -> action =
                    UserFragmentDirections.actionUserFragmentToInChatFragment(UserFromProfile = user, user = null)
                "map" -> action =
                    CommunityMapDetailFragmentDirections.actionCommunityMapDetailToInChatFragment(UserFromProfile = user, user = null)

            }
            findNavController().navigate(action!!)
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