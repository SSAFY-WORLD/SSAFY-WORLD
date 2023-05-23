package com.ssafy.world.utils


import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.ssafy.world.databinding.CustomAlertDialogBinding

class ValidationAlertDialog(
    private val code: String,
    context: Context
): Dialog(context) {
    private lateinit var binding: CustomAlertDialogBinding
    lateinit var listener: DialogClickedListener

    interface DialogClickedListener {
        fun onConfirmClick()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = CustomAlertDialogBinding.inflate(layoutInflater)
        binding.titleText.setText(code)
        setContentView(binding.root)
        window!!.setBackgroundDrawable(ColorDrawable())
        window!!.setDimAmount(0.2f)
        setListener()
    }

    private fun setListener() = with(binding) {
        cancelBtn.setOnClickListener {
            if (this@ValidationAlertDialog.isShowing) {
                super.dismiss()
            }
        }
        confirmBtn.setOnClickListener {
            listener.onConfirmClick()
            dismiss()
        }
    }

    override fun show() {
        if(!this.isShowing) super.show()
    }
}