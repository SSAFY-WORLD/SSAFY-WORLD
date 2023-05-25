package com.ssafy.world.src.main.mypage

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.DialogUserUpdateBinding
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.user.UserViewModel

class ProfileUpdateBottomSheetFragment(
	private val parent: MypageFragment,
	private val user: User,
	private val navController: NavController
): BottomSheetDialogFragment() {
	private lateinit var currentUser: User
	private lateinit var binding: DialogUserUpdateBinding
	private lateinit var mContext: Context
	private val userViewModel: UserViewModel by viewModels()

	override fun getTheme(): Int {
		return R.style.CustomBottomSheetDialog
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		mContext = context
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogUserUpdateBinding.inflate(layoutInflater, container, false)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		currentUser = ApplicationClass.sharedPreferences.getUser()!!
		initText()
		setListener()
		initEditTextListener()
		initObserver()
	}

	private fun initText() = with(binding){
		userNameEditText.setText(user.name)
		userNickNameEditText.setText(user.nickname)
		Glide.with(binding.root)
			.load(user.profilePhoto)
			.into(profileImage)

		val check = (user.name == currentUser.name &&
				user.nickname == currentUser.nickname &&
				currentUser.profilePhoto == user.profilePhoto)
		toggle(check)
	}

	private fun initEditTextListener() = with(binding) {
		userNameEditText.addTextChangedListener {
			checkEditTextValues()
		}
		userNickNameEditText.addTextChangedListener {
			checkEditTextValues()
		}
	}

	private fun toggle(check: Boolean) = with(binding) {
		if (check) {
			updateUserBtn.isEnabled = false
			updateUserBtn.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray))
			updateUserBtn.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray))
		} else {
			updateUserBtn.isEnabled = true
			updateUserBtn.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_blue))
			updateUserBtn.setTextColor(ContextCompat.getColor(mContext, R.color.white))
		}
	}

	private fun checkEditTextValues() = with(binding) {
		val duplicate = (userNameEditText.text.toString() == currentUser.name && userNickNameEditText.text.toString() == currentUser.nickname)
		toggle(duplicate)
	}

	private fun setListener() = with(binding) {
		closeBtn.setOnClickListener {
			super.dismiss()
		}
		val profile = if (currentUser.profilePhoto == user.profilePhoto) "" else user.profilePhoto
		updateUserBtn.setOnClickListener {
			val updateUser = user.apply {
				name = userNameEditText.text.toString()
				nickname = userNickNameEditText.text.toString()
				profilePhoto = profile
			}
			parent.showLoadingDialog(requireContext())
			userViewModel.updateUser(updateUser)
		}
		imageAddBtn.setOnClickListener {
			val bundle = Bundle().apply {
				putString("name", userNameEditText.text.toString())
				putString("nickname", userNickNameEditText.text.toString())
			}
			navController.navigate(R.id.action_mypageFragment_to_photoSingleFragment, bundle)
			dismiss()
		}
	}

	private fun initObserver() {
		// 회원 정보 수정
		userViewModel.user.observe(viewLifecycleOwner) { user ->
			if (user != null) {
				ApplicationClass.sharedPreferences.apply {
					clearUser()
					saveUser(user)
				}
				Toast.makeText(context, "회원정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
				parent.initUserProfile()
				dismiss()
				parent.dismissLoadingDialog()
			}
		}
		userViewModel.updateUserSuccess.observe(viewLifecycleOwner) { success ->
			if (success) {
				userViewModel.getUser(user.id)
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		arguments?.clear()
	}
}
