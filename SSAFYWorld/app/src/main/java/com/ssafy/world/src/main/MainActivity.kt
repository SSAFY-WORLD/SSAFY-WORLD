package com.ssafy.world.src.main

import android.os.Bundle
import com.ssafy.world.config.BaseActivity
import com.ssafy.world.databinding.ActivityMainBinding

// :: -> method reference
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}