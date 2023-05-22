package com.ssafy.world.src.main.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.NotificationData
import com.ssafy.world.databinding.FragmentMypageBinding
import com.ssafy.world.src.main.auth.AuthViewModel
import com.ssafy.world.src.main.community.CommunityViewModel
import com.ssafy.world.utils.CustomAlertDialog

class MypageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initButton()
    }

    private fun initButton() = with(binding) {
        logoutBtn.setOnClickListener {
            showAlertDialog(R.string.logout_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    ApplicationClass.sharedPreferences.clearUser()
                    showCustomToast(R.string.logout_complete)
                    navController.navigate(R.id.action_mypageFragment_to_loginFragment)
                }
            }
        }
        withdrawalBtn.setOnClickListener {
            val userEmail = ApplicationClass.sharedPreferences.getUser()?.email
            showAlertDialog(R.string.withdrawal_text, myContext)
            mCustomDialog.listener = object: CustomAlertDialog.DialogClickedListener {
                override fun onConfirmClick() {
                    if (userEmail != null) {
                        showLoadingDialog(myContext)
                        authViewModel.deleteUser(userEmail)
                    }
                }
            }
        }
        fcmTestBtn.setOnClickListener {
            val token = "fjVdQ9J9T_SbEw10zBAZht:APA91bHft71KnRDlcwuataGnGql6krKM35OLExO0aoPBdMH4HR-xfL9YiuGm5NLHcMfI5hVkY9CBvTACwcaHV9HGQ75cJ_oRZwiMsYGuLm7m4IX8ChVx-MX8tJ1kBUATRpn4Sjjle77n"
            val data = NotificationData("채팅", "알람 타이틀", "메시지")
            sendRemoteNotification(data, token)
        }
    }

    private fun initObserver() = with(authViewModel) {
        deleteUserSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                dismissLoadingDialog()
                ApplicationClass.sharedPreferences.clearUser()
                showCustomToast(R.string.withdrawal_complete)
                navController.navigate(R.id.action_mypageFragment_to_loginFragment)
            }
        }
    }
}