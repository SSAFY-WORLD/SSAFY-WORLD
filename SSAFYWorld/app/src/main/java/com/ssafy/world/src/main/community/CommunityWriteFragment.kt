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

    private var isEdit: Boolean = false
    private var communityId = ""
    private lateinit var curCommunity: Community

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        communityId = args.communityId

        initListener()
        if (communityId != "") {
            isEdit = true
            communityViewModel.fetchCommunityById(args.communityName, communityId)
        }

        initRecyclerView()
        initButton()
        initPhoto()
    }

    private fun initView() = with(binding) {
        titleEditTextView.setText(curCommunity.title)
        contentEditTextView.setText(curCommunity.content)
        photoUrlList = curCommunity.photoUrls
        initRecyclerView()
    }

    private fun initButton() = with(binding) {
        writeBtnImage.setOnClickListener {
            activityViewModel.title = titleEditTextView.text.toString()
            activityViewModel.content = contentEditTextView.text.toString()
            navController.navigate(R.id.action_communityWriteFragment_to_photoFragment)
        }
        writeBtnComplete.setOnClickListener {
            val curTitle = titleEditTextView.text.toString().trim()
            val curContent = contentEditTextView.text.toString().trim()

            if (curTitle.isEmpty()) {
                titleEditTextView.error = "제목을 입력해주세요."
                titleEditTextView.requestFocus()
                return@setOnClickListener
            }

            if (curContent.isEmpty()) {
                contentEditTextView.error = "내용을 입력해주세요."
                contentEditTextView.requestFocus()
                return@setOnClickListener
            }

            showLoadingDialog(myContext)
            val curUser = ApplicationClass.sharedPreferences.getUser()
            val curPost = Community().apply {
                userId = curUser!!.email
                userNickname = curUser!!.nickname
                userProfile = curUser!!.profilePhoto
                title = curTitle
                content = curContent
                likeCount = 0
                fcmToken = curUser!!.token
                collection = activityViewModel.entryCommunityCollection
                time = System.currentTimeMillis()
                photoUrls = photoUrlList
            }
            if (!isEdit) {
                communityViewModel.insertCommunity(
                    curPost,
                    activityViewModel.entryCommunityCollection
                )
            } else {
                curPost.id = curCommunity.id
                communityViewModel.updateCommunity(
                    activityViewModel.entryCommunityCollection,
                    curPost
                )
            }
        }
    }

    private fun initPhoto() = with(binding) {
        titleEditTextView.setText(activityViewModel.title)
        contentEditTextView.setText(activityViewModel.content)
        activityViewModel.title = ""
        activityViewModel.content = ""
        val photoListJson = arguments?.getString("photoListJson") ?: return
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<Photo>>() {}.type
        val photoList: ArrayList<Photo> = gson.fromJson(photoListJson, listType)
        photoUrlList.clear()
        photoUrlList.apply {
            photoList.forEach {
                add(it.url)
            }
        }
        initRecyclerView()
    }

    private fun initRecyclerView() = with(binding) {
        if(photoUrlList.size > 0) {
            writeCv.visibility = View.VISIBLE
        }
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
            if (isEdit) {
                curCommunity = it
                Log.d(TAG, "initListener: $it")
                initView()
                return@observe
            } else {
                if (it.id != "") {
                    showCustomToast("글이 등록되었습니다.")
                    navController.navigate(R.id.action_communityWriteFragment_to_communityListFragment)
                } else {
                    Toast.makeText(myContext, "글이 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
                dismissLoadingDialog()
            }
        }
        communityViewModel.updateSuccess.observe(viewLifecycleOwner) {
            dismissLoadingDialog()
            if (it) {
                showCustomToast("수정에 성공했습니다.")
                val action =
                    CommunityWriteFragmentDirections.actionCommunityWriteFragmentToCommunityDetailFragment(
                        communityId
                    )
                navController.navigate(action)
            } else {
                showCustomToast("수정에 성공했습니다.")
            }
        }
    }

}