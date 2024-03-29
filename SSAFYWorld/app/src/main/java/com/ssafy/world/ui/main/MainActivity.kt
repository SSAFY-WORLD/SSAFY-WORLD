package com.ssafy.world.ui.main


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.ssafy.world.R
import com.ssafy.world.config.BaseActivity
import com.ssafy.world.databinding.ActivityMainBinding
import com.ssafy.world.utils.Constants


private const val TAG = "MainActivity_메인"

// :: -> method reference
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val activityViewModel: MainActivityViewModel by viewModels()
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    // Android Oreo 이상에서는 알림 채널을 생성해야 한다
    private fun createNotificationChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra(Constants.DESTINATION)?.let { destination ->
            moveFragment(destination)
        }
        createNotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME)
        setToolbarWithNavcontroller()


        requestPermission()
    }

    private fun moveFragment(destination: String) {
        Log.d(TAG, "moveFragment: ")
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val cur = destination.split("-")
        Log.d(TAG, "moveFragment: $cur")
        when (cur[0]) {
            Constants.CHAT -> {
                setTitle(getString(R.string.nav_chat_title))
                navController.navigate(R.id.chatFragment)
            }
            Constants.COMMUNITY -> {
                activityViewModel.entryCommunityCollection = cur[1]
                val bundle = bundleOf("communityId" to cur[2])
                navController.navigate(R.id.communityDetailFragment, bundle)
            }
            else -> hideBottomNav()
        }
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
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    hideBottomNav()
                    hideToolbar()
                    unblockBackPressed()
                }
                R.id.userFragment -> {
                    hideBottomNav()
                    setTitle(getString(R.string.nav_user_title))
                    unblockBackPressed()
                }
                R.id.mainFragment -> {
                    setTitle(getString(R.string.nav_home_title))
                    blockBackPressed()
                    showBottomNav()
                }
                R.id.mainHotFragment -> {
                    setTitle("인기 게시글")
                    unblockBackPressed()
                    hideBottomNav()
                }
                R.id.communityFragment -> {
                    setTitle(getString(R.string.nav_community_title))
                    blockBackPressed()
                    hideToolbar()
                }
                R.id.communityListFragment, R.id.communityMapFragment -> {
                    hideToolbar()
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.chatFragment -> {
                    setTitle(getString(R.string.nav_chat_title))
                    blockBackPressed()
                }
                R.id.inChatFragment -> {
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.mypageFragment -> {
                    setTitle(getString(R.string.nav_mypage_title))
                    blockBackPressed()
                }
                R.id.communityWriteFragment, R.id.communityMapWriteFragment -> {
                    setTitle("글쓰기")
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.mapFragment -> {
                    setTitle("지도")
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.communitySearchFragment, R.id.communityMapSearchFragment -> {
                    hideBottomNav()
                    unblockBackPressed()
                    hideToolbar()
                }
                R.id.communityDetailFragment, R.id.communityMapDetail -> {
                    hideBottomNav()
                    unblockBackPressed()
                    setTitle(activityViewModel.getCommunityTitle())
                }
                R.id.photoFragment, R.id.photoFullFragment, R.id.photoSingleFragment -> {
                    hideToolbar()
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.communityMapFragment -> {
                    hideToolbar()
                    hideBottomNav()
                    unblockBackPressed()
                }
                R.id.notificationFragment -> {
                    setTitle("알림 목록")
                    unblockBackPressed()
                    hideBottomNav()
                }
                else -> {
                    hideToolbar()
                    hideBottomNav()
                    unblockBackPressed()
                }
            }
        }
    }

    private val hideBottomNav = { binding.mainBtmNav.visibility = View.GONE }
    private val showBottomNav = { binding.mainBtmNav.visibility = View.VISIBLE }
    private val hideToolbar = { binding.mainLlToolbar.visibility = View.GONE }
    private val showToolbar = { binding.mainLlToolbar.visibility = View.VISIBLE }

    private var backPressedBlocked = false
    private val blockBackPressed = { backPressedBlocked = true }
    private val unblockBackPressed = { backPressedBlocked = false }

    override fun onBackPressed() {
        if (!backPressedBlocked) {
            super.onBackPressed()
        }
    }

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

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                101
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
        intent.getStringExtra(Constants.DESTINATION)?.let { destination ->
            moveFragment(destination)
        }
    }
}