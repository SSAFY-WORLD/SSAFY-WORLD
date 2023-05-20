package com.ssafy.world.src.main


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.ssafy.world.R
import com.ssafy.world.config.BaseActivity
import com.ssafy.world.databinding.ActivityMainBinding
import com.ssafy.world.databinding.BottomPermissionBinding
import com.ssafy.world.src.main.auth.PermmissionBottomSheet


// :: -> method reference
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setToolbarWithNavcontroller()
        requestPermission()
    }

    private fun setToolbarWithNavcontroller() {
        //toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //bottom nav
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.mainBtmNav, navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            hideKeyboard()
            showBottomNav()
            showToolbar()
            when (destination.id)  {
                R.id.loginFragment, R.id.registerFragment -> {
                    hideBottomNav()
                    hideToolbar()
                }
                R.id.userFragment -> {
                    hideBottomNav()
                    setTitle(getString(R.string.nav_user_title))
                }
                R.id.mainFragment -> setTitle(getString(R.string.nav_home_title))
                R.id.communityFragment -> setTitle(getString(R.string.nav_community_title))
                R.id.chatFragment -> setTitle(getString(R.string.nav_chat_title))
                R.id.mypageFragment -> setTitle(getString(R.string.nav_mypage_title))
                R.id.communityWriteFragment -> {
                    setTitle("글쓰기")
                    hideBottomNav()
                }
                else -> hideBottomNav()
            }
        }
    }

    private val hideBottomNav = { binding.mainBtmNav.visibility = View.GONE }
    private val showBottomNav = { binding.mainBtmNav.visibility = View.VISIBLE }
    private val hideToolbar = { binding.mainLlToolbar.visibility = View.GONE }
    private val showToolbar = { binding.mainLlToolbar.visibility = View.VISIBLE }
    fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = this.currentFocus   // Check if no view has focus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun setTitle(title: String) {
        binding.toolbarText.text = title
    }

    private fun requestStoragePermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    // 권한이 허용된 경우 다음 작업을 수행할 수 있습니다.
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    // 권한이 거부된 경우 사용자에게 알림을 표시하거나 다른 조치를 취해야 합니다.
                }
            })
            .setDeniedMessage("권한을 허용해주세요.")
            .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 권한이 허용된 경우 다음 작업을 수행할 수 있습니다.
            } else {
                // 권한이 거부된 경우 사용자에게 알림을 표시하거나 다른 조치를 취해야 합니다.
            }
        }

    // 권한 요청 실행
    private fun requestPermission() {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasPermission = Environment.isExternalStorageManager()
            if (!hasPermission) {
                showConfirmationBottomSheet()
                return
            }
        } else {
            //showConfirmationDialog()
        }
        requestPermissionLauncher.launch(permission)
    }

    private fun showConfirmationBottomSheet() {
        val bottomSheet = PermmissionBottomSheet()
        bottomSheet.show(supportFragmentManager, "ConfirmationBottomSheet")
        bottomSheet.setOnConfirmationListener {
            openAppPermissionSettings()
            bottomSheet.dismiss()
        }
    }
    private fun openAppPermissionSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }


}