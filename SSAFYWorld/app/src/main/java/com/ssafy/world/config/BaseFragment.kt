package com.ssafy.world.config

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ssafy.world.utils.CustomAlertDialog
import com.ssafy.world.utils.LoadingDialog

// Fragment의 기본을 작성, 뷰 바인딩 활용
abstract class BaseFragment<B : ViewBinding>(
    private val bind: (View) -> B,
    @LayoutRes layoutResId: Int //
) : Fragment(layoutResId) {
    private var _binding: B? = null
    lateinit var mLoadingDialog: LoadingDialog
    lateinit var mCustomDialog: CustomAlertDialog

    protected val binding get() = _binding!!

    private val _navController by lazy { findNavController() }
    protected val navController get() = _navController

    private var _myContext: Context? = null
    protected val myContext get() = _myContext!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _myContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind(super.onCreateView(inflater, container, savedInstanceState)!!)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun showCustomToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
    fun showCustomToast(@StringRes message: Int) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showLoadingDialog(context: Context) {
        mLoadingDialog = LoadingDialog(context)
        mLoadingDialog.show()
    }

    fun dismissLoadingDialog() {
        if (mLoadingDialog.isShowing) {
            mLoadingDialog.dismiss()
        }
    }

    fun showAlertDialog(@StringRes title: Int, context: Context) {
        mCustomDialog = CustomAlertDialog(title, context)
        mCustomDialog.show()
    }

    fun dismissAlertDialog() {
        if (mCustomDialog.isShowing) {
            mCustomDialog.dismiss()
        }
    }

}