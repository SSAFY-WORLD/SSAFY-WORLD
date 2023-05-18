package com.ssafy.world.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import com.ssafy.world.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogLoadingBinding
    private val MAX_PROGRESS = 3
    private val PROGRESS_DELAY_MS = 500L

    private var currentProgress = 0

    private val handler = Handler(Looper.myLooper()!!)
    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, PROGRESS_DELAY_MS)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable())
        window!!.setDimAmount(0.2f)
        handler.post(progressRunnable)
    }

    override fun show() {
        if(!this.isShowing) super.show()
    }

    private fun updateProgress() {
        currentProgress = (currentProgress + 1) % (MAX_PROGRESS + 1)
        binding.loadingText.text = ".".repeat(currentProgress)
    }
}