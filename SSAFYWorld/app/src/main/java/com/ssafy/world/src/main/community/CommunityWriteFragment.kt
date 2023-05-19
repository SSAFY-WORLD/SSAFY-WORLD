package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Photo
import com.ssafy.world.databinding.FragmentCommunityWriteBinding
import java.lang.reflect.Type
import kotlin.math.log

private const val TAG = "CommunityWriteFragment_싸피"
class CommunityWriteFragment : BaseFragment<FragmentCommunityWriteBinding>(FragmentCommunityWriteBinding::bind, R.layout.fragment_community_write) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()
        initPhoto()
    }

    private fun initButton() = with(binding) {
        writeBtn.setOnClickListener {
            navController.navigate(R.id.action_communityWriteFragment_to_photoFragment)
        }
    }

    private fun initPhoto() {
        val photoListJson = arguments?.getString("photoListJson") ?: return
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<Photo>>() {}.type
        val photoList: ArrayList<Photo> = gson.fromJson(photoListJson, listType)

    }
}