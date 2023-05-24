package com.ssafy.world.src.main.community.map

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.NotificationData
import com.ssafy.world.data.service.FCMService
import com.ssafy.world.databinding.FragmentCommunityMapDetailBinding
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.src.main.MainActivity
import com.ssafy.world.src.main.MainActivityViewModel
import com.ssafy.world.src.main.community.*
import com.ssafy.world.src.main.photo.PhotoFullDialog

private const val TAG = "CommunityMapDetailFragm"
class CommunityMapDetailFragment : BaseFragment<FragmentCommunityMapDetailBinding>(
    FragmentCommunityMapDetailBinding::bind,
    R.layout.fragment_community_map_detail
), OnMapReadyCallback {

    val args: CommunityDetailFragmentArgs by navArgs()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    private lateinit var community: Community
    private lateinit var curBoard: String
    private var curId = ""
    private var isCommunityReady = false
    private val curUser by lazy { ApplicationClass.sharedPreferences.getUser() }
    private var isReply = ""

    private val myAdapter: CommunityDetailPhotoAdapter by lazy {
        CommunityDetailPhotoAdapter(myContext)
    }
    private val commentAdapter: CommunityDetailCommentAdapter by lazy {
        CommunityDetailCommentAdapter(myContext, communityViewModel)
    }
    private var commentList = arrayListOf<Comment>()

    private var commentPosition = 0;

    private lateinit var curPlace: Place
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        curId = args.communityId
        curBoard = activityViewModel.entryCommunityCollection
        if (curId != "") {
            communityViewModel.fetchCommunityById(curBoard, curId)
        }

        mapView = binding.map
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@CommunityMapDetailFragment)

        initButton()
        initListener()
        detectKeyboard()
    }

    private fun initView() = with(binding) {
        if (community.likedUserIds.contains(curUser!!.id)) {
            likeCheckbox.isChecked = true
        }
        detailTitle.text = community.title
        nickname.text = community.userNickname
        time.text = community.getFormattedTime()
        detailContent.text = community.content
        likeCount.text = community.likeCount.toString()
        Glide.with(myContext)
            .load(community.userProfile)
            .transform(FitCenter())
            .circleCrop()
            .into(binding.userProfile)
        placeName.text = community.placeName
        // Place의 위도와 경도를 가져와서 LatLng 객체를 생성합니다.
        val latLng = LatLng(community.lat ?: 0.0, community.lng ?: 0.0)

        // 마커를 추가합니다.
        googleMap.addMarker(MarkerOptions().position(latLng).title(community.placeName))

        // 카메라를 마커의 위치로 이동합니다.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        if (curUser?.email == community?.userId) {
            detailBtnMore.visibility = View.VISIBLE
        }

        myAdapter.submitList(community.photoUrls.toMutableList())
        photoRecyclerview.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = myAdapter
        }
        myAdapter.itemClickListener = object : CommunityDetailPhotoAdapter.ItemClickListener {
            override fun onClick(data: String) {
                PhotoFullDialog(data).show(requireActivity().supportFragmentManager, "")
            }
        }


        commentAdapter.submitList(commentList.toMutableList())
        commentRecyclerview.apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
            adapter = commentAdapter
        }
        commentAdapter.itemClickListener =
            object : CommunityDetailCommentAdapter.ItemClickListener {
                override fun onClick(view: View, data: Comment, position: Int) {
                    showCommentOptionView(view, data)
                }
            }

        commentAdapter.replyShowClickListener =
            object : CommunityDetailCommentAdapter.ReplyShowClickListener {
                override fun onClick(
                    view: ItemCommunityCommentBinding,
                    data: Comment,
                    position: Int
                ) {
                    commentPosition = position
                    if (view.replyBtn.visibility == View.GONE) { // 답글 보이기
                        view.replyBtnShow.text = "답글 숨기기"
                        view.replyBtn.visibility = View.VISIBLE
                        view.replyRecyclerView.visibility = View.VISIBLE
                        communityViewModel.getRepliesByCommentId(data.id)
                    } else {
                        view.replyBtnShow.text = "답글 보기"
                        view.replyBtn.visibility = View.GONE
                        view.replyRecyclerView.visibility = View.GONE
                    }
                }
            }
        commentAdapter.replyClickListener =
            object : CommunityDetailCommentAdapter.ReplyClickListener {
                override fun onClick(view: View, data: Comment, position: Int) {
                    commentPosition = position
//                    replyTvMessage.visibility = View.VISIBLE
                    //replyTvMessage.text = "${data.userNickname} 님에게 답글 남기는 중.."
                    commentRecyclerview.scrollToPosition(position)
                    // 에딧텍스트에 포커스 설정
                    commentInput.requestFocus()
                    // 키보드 올리기
                    val imm =
                        myContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(commentInput, InputMethodManager.SHOW_IMPLICIT)
                    isReply = data.id
                }
            }
        communityViewModel.getCommentsByCommunityId(community.id)
    }


    private fun initButton() = with(binding) {
        commentBtnWrite.setOnClickListener {
            val cur = Comment().apply {
                userId = curUser!!.email
                userNickname = curUser!!.nickname
                userProfile = curUser!!.profilePhoto
                comment = commentInput.text.toString()
                time = System.currentTimeMillis()
                communityId = community.id
                fcmToken = curUser!!.token
            }

            if (isReply != "") {
                cur.commentId = isReply
                communityViewModel.insertReply(
                    activityViewModel.entryCommunityCollection,
                    community,
                    cur
                )
                val noti = NotificationData("메시지", "싸피월드", "${curUser!!.nickname} 님이 답글을 남겼습니다.")
                sendRemoteNotification(noti, cur.fcmToken)
            } else {
                communityViewModel.insertComment(
                    activityViewModel.entryCommunityCollection,
                    community,
                    cur
                )
            }

            val noti = NotificationData("메시지", "싸피월드", "${curUser!!.nickname} 님이 댓글을 남겼습니다.")
            FCMService.sendRemoteNotification(noti, community.fcmToken)
            (activity as MainActivity).hideKeyboard()
            commentInput.setText("")
            showCustomToast("댓글이 등록됐어요.")
        }

        detailBtnMore.setOnClickListener {
            showCommunityOptionView(it)
        }

        likeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !community.likedUserIds.contains(curUser!!.id)) {
                community.likedUserIds.add(curUser!!.id)
                communityViewModel.onIncrementLikeButtonClicked(
                    curBoard,
                    community.id,
                    curUser!!.id
                )
            } else if (!isChecked) {
                community.likedUserIds.remove(curUser!!.id)
                communityViewModel.onDecrementLikeButtonClicked(
                    curBoard,
                    community.id,
                    curUser!!.id
                )
            }
        }
    }

    private fun initListener() {
        communityViewModel.comments.observe(viewLifecycleOwner) {
            commentList = it
            commentAdapter.submitList(commentList.toMutableList())
        }

        communityViewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showCustomToast("커뮤니티 삭제되었습니다.")
                navController.navigate(R.id.action_communityMapDetail_to_communityMapFragment)
            } else {
                showCustomToast("커뮤니티 삭제에 실패했습니다.")
            }
        }
        communityViewModel.community.observe(viewLifecycleOwner) {
            community = it
            initView()
        }
        communityViewModel.commentDeleteSuccess.observe(viewLifecycleOwner) {
            if (it) {
                showCustomToast("댓글이 삭제되었어요.")
                communityViewModel.getCommentsByCommunityId(curId)
            }
        }

        communityViewModel.replies.observe(viewLifecycleOwner) { it ->
            commentAdapter.replyAdapters[commentPosition].submitList(it.toMutableList())
        }

        communityViewModel.likeCount.observe(viewLifecycleOwner) {
            binding.likeCount.text = it.toString()
        }
    }

    private fun showCommunityOptionView(anchorView: View) {
        val popupMenu = PopupMenu(myContext, anchorView)
        popupMenu.inflate(R.menu.comment_option_view) // option_menu는 메뉴 아이템을 정의한 리소스 파일입니다.

        // 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    showDeleteConfirmationDialog()
                    true
                }
                else -> false
            }
        }

        // 옵션 뷰 보이기
        popupMenu.show()
    }

    private fun showCommentOptionView(anchorView: View, comment: Comment) {
        val popupMenu = PopupMenu(myContext, anchorView)
        popupMenu.inflate(R.menu.comment_option_view) // option_menu는 메뉴 아이템을 정의한 리소스 파일입니다.

        // 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    showCommentDeleteConfirmationDialog(comment)
                    true
                }
                else -> false
            }
        }

        // 옵션 뷰 보이기
        popupMenu.show()
    }

    private fun showDeleteConfirmationDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                communityViewModel.deleteCommunity(curBoard, community.id)
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    private fun showCommentDeleteConfirmationDialog(comment: Comment) {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                Log.d(TAG, "showCommentDeleteConfirmationDialog: $comment")
                communityViewModel.deleteComment(curBoard, community, comment.id)
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }


    private fun detectKeyboard() {
        val rootView = requireActivity().window.decorView.rootView
        var isKeyboardVisible = false

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            val keyboardVisibleThreshold = screenHeight * 0.15 // 임의의 기준 값을 사용하여 판별

            if (keyboardHeight > keyboardVisibleThreshold) {
                // 키보드가 올라온 상태
                Log.d(TAG, "detectKeyboard: up")
                isKeyboardVisible = true
            } else if (isKeyboardVisible) {
                // 키보드가 내려간 상태 (이전에 키보드가 올라와 있었을 때만 처리)
                isKeyboardVisible = false
                // 키보드가 내려갔을 때의 작업 수행
                Log.d(TAG, "detectKeyboard: down")
                isReply = ""
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
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