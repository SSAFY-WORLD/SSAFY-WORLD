package com.ssafy.world.src.main.community

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.Photo
import com.ssafy.world.databinding.FragmentCommunityWriteBinding
import com.ssafy.world.src.main.MainActivityViewModel
import java.lang.reflect.Type

private const val TAG = "CommunityWriteFragment_싸피"

class CommunityWriteFragment : BaseFragment<FragmentCommunityWriteBinding>(
    FragmentCommunityWriteBinding::bind,
    R.layout.fragment_community_write
) {
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    val args: CommunityWriteFragmentArgs by navArgs()

    private val myAdapter: CommunityWritePhotoAdapter by lazy {
        CommunityWritePhotoAdapter(myContext)
    }

    private var photoUrlList: ArrayList<String> = arrayListOf()

    private var isEdit : Boolean = false
    private var communityId = ""
    private lateinit var curCommunity: Community

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        communityId = args.communityId

        if(communityId != "") {
            isEdit = true
            communityViewModel.fetchCommunityById(args.communityName, communityId)
        }
        initRecyclerView()
        initButton()
        initPhoto()
        initListener()
    }

    private fun initView() = with(binding) {
        titleEditTextView.setText(curCommunity.title)
        contentEditTextView.setText(curCommunity.content)
        photoUrlList = curCommunity.photoUrls
        initRecyclerView()
    }

    private fun initButton() = with(binding) {
        writeBtnImage.setOnClickListener {
            navController.navigate(R.id.action_communityWriteFragment_to_photoFragment)
        }
        writeBtnComplete.setOnClickListener {
            showLoadingDialog(myContext)
            val curUser = ApplicationClass.sharedPreferences.getUser()
            val curPost = Community().apply {
                userId = curUser!!.email
                userNickname = curUser!!.nickname
                userProfile = curUser!!.profilePhoto
                title = titleEditTextView.text.toString()
                content = contentEditTextView.text.toString()
                time = System.currentTimeMillis()
                photoUrls = photoUrlList
            }
            if(!isEdit) {
                communityViewModel.insertCommunity(curPost, activityViewModel.entryCommunityCollection)
            } else {
                curPost.id = curCommunity.id
                communityViewModel.updateCommunity(activityViewModel.entryCommunityCollection, curPost)
            }
        }
    }

    private fun initPhoto() {
        val photoListJson = arguments?.getString("photoListJson") ?: return
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<Photo>>() {}.type
        val photoList: ArrayList<Photo> = gson.fromJson(photoListJson, listType)
        photoUrlList.apply {
            photoList.forEach {
                add(it.url)
            }
        }
        initRecyclerView()
    }

    private fun initRecyclerView() = with(binding) {
        writeCv.visibility = View.VISIBLE
        myAdapter.submitList(photoUrlList.toMutableList())
        writeRvPhoto.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = myAdapter
        }
        myAdapter.deleteListener = object : CommunityWritePhotoAdapter.DeleteListener {
            override fun delete(position: Int) {
                photoUrlList.removeAt(position)
                myAdapter.submitList(photoUrlList.toMutableList())
            }
        }
    }

    private fun initListener() = with(communityViewModel) {
        communityViewModel.community.observe(viewLifecycleOwner) {
            Log.d(TAG, "initListener: $it")
            Log.d(TAG, "initListener: $isEdit")
            if(isEdit) {
                curCommunity = it
                initView()
                return@observe
            }
            else {
                if (it.id != "") {
                    Toast.makeText(myContext, "글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_communityWriteFragment_to_communityListFragment)
                } else {
                    Toast.makeText(myContext, "글이 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                dismissLoadingDialog()
            }
        }
        communityViewModel.updateSuccess.observe(viewLifecycleOwner) {
            dismissLoadingDialog()
            if(it) {
                showCustomToast("수정에 성공했습니다.")
                val action = CommunityWriteFragmentDirections.actionCommunityWriteFragmentToCommunityDetailFragment(communityId)
                navController.navigate(action)
            } else {
                showCustomToast("수정에 성공했습니다.")
            }
        }
    }
}