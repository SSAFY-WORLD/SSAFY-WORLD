package com.ssafy.world.src.main.community.map

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.Photo
import com.ssafy.world.databinding.FragmentCommunityMapWriteBinding
import com.ssafy.world.src.main.MainActivityViewModel
import com.ssafy.world.src.main.community.CommunityViewModel
import com.ssafy.world.src.main.community.CommunityWriteFragmentDirections
import com.ssafy.world.src.main.community.CommunityWritePhotoAdapter
import java.lang.reflect.Type

class CommunityMapWriteFragment : BaseFragment<FragmentCommunityMapWriteBinding>(
    FragmentCommunityMapWriteBinding::bind,
    R.layout.fragment_community_map_write
), OnMapReadyCallback {

    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()

    private val args: CommunityMapWriteFragmentArgs by navArgs()
    private lateinit var curPlace: Place
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    private val myAdapter: CommunityWritePhotoAdapter by lazy {
        CommunityWritePhotoAdapter(myContext)
    }

    private var photoUrlList: ArrayList<String> = arrayListOf()

    private var isEdit: Boolean = false
    private var communityId = ""
    private lateinit var curCommunity: Community

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        curPlace = activityViewModel.place!!
        communityId = ""

        initListener()
        if (communityId != "") {
            isEdit = true
            communityViewModel.fetchCommunityById(
                activityViewModel.entryCommunityCollection,
                communityId
            )
        }

        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@CommunityMapWriteFragment)

        initView()
        initRecyclerView()
        initButton()
        initPhoto()
    }

    private fun initEditView() = with(binding) {
        titleEditTextView.setText(curCommunity.title)
        contentEditTextView.setText(curCommunity.content)
        photoUrlList = curCommunity.photoUrls
        initRecyclerView()
    }

    private fun initView() = with(binding) {
        name.text = curPlace.name
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Place의 위도와 경도를 가져와서 LatLng 객체를 생성합니다.
        val latLng = LatLng(curPlace.latLng?.latitude ?: 0.0, curPlace.latLng?.longitude ?: 0.0)

        // 마커를 추가합니다.
        googleMap.addMarker(MarkerOptions().position(latLng).title(curPlace.name))

        // 카메라를 마커의 위치로 이동합니다.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun initButton() = with(binding) {
        writeBtnImage.setOnClickListener {
            activityViewModel.title = titleEditTextView.text.toString()
            activityViewModel.content = contentEditTextView.text.toString()
            navController.navigate(R.id.action_communityMapWriteFragment_to_photoFragment)
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
                placeName = curPlace.name
                placeAddress = curPlace.address as String
                lat = curPlace.latLng!!.latitude
                lng = curPlace.latLng!!.longitude
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
            dismissLoadingDialog()
            if (isEdit) {
                curCommunity = it
                initEditView()
                return@observe
            } else {
                if (it.id != "") {
                    showCustomToast("글이 등록되었습니다.")
                    navController.navigate(R.id.action_communityMapWriteFragment_to_communityMapFragment)
                } else {
                    Toast.makeText(myContext, "글이 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
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


    override fun onResume() {
        super.onResume()
        mapView = binding.map
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}