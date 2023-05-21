package com.ssafy.world.src.main.photo


import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.ssafy.world.R
import com.ssafy.world.databinding.FragmentPhotoFullBinding

private const val TAG = "PhotoFullFragment"

class PhotoFullDialog(private val photoUrl: String) : DialogFragment() {

    private lateinit var binding: FragmentPhotoFullBinding
    private lateinit var mContext: Context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.FullScreenDialogStyle)
        binding = FragmentPhotoFullBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBuilder.setView(binding.root)
        dialogBuilder.setNegativeButton("닫기", null)
        val dialog = dialogBuilder.create()

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(photoUrl)
    }


    //다이어로그는 생성 되기 전에 영역을 잡아 먹는데 Glide는 비동기
    private fun initView(photoUrl: String) {
        Glide.with(mContext)
            .load(photoUrl)
            .fitCenter()
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.fullImage.setImageDrawable(resource)
                }
            })
    }
}

