package com.ssafy.world.src.main


import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.ssafy.world.R
import com.ssafy.world.config.BaseActivity
import com.ssafy.world.databinding.ActivityMainBinding


// :: -> method reference
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarWithNavcontroller()
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
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> hideBottomNav()
                R.id.mainFragment -> { setTitle(getString(R.string.nav_home_title)) }
                R.id.communityFragment -> setTitle(getString(R.string.nav_community_title))
                R.id.chatFragment -> setTitle(getString(R.string.nav_chat_title))
                R.id.mypageFragment -> setTitle(getString(R.string.nav_mypage_title))
                else -> hideBottomNav()
            }
        }
    }

    private val hideBottomNav = { binding.mainBtmNav.visibility = View.GONE }
    private val showBottomNav = { binding.mainBtmNav.visibility = View.VISIBLE }
    private fun hideKeyboard() {
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

    private fun setTitle(title: String) {
        binding.toolbarText.text = title
    }
}