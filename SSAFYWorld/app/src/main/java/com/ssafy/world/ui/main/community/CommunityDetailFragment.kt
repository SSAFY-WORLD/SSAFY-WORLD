package com.ssafy.world.ui.main.community

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.config.BaseFragment
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.data.model.NotificationData
import com.ssafy.world.data.model.User
import com.ssafy.world.data.service.FCMService
import com.ssafy.world.databinding.FragmentCommunityDetailBinding
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.ui.main.MainActivity
import com.ssafy.world.ui.main.MainActivityViewModel
import com.ssafy.world.ui.main.photo.PhotoFullDialog
import com.ssafy.world.ui.main.user.UserInfoBottomSheetFragment
import com.ssafy.world.utils.CustomAlertDialog

private const val TAG = "CommunityDetailFragment"

class CommunityDetailFragment : BaseFragment<FragmentCommunityDetailBinding>(
    FragmentCommunityDetailBinding::bind,
    R.layout.fragment_community_detail
), SwipeRefreshLayout.OnRefreshListener {
    val args: CommunityDetailFragmentArgs by navArgs()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val communityViewModel: CommunityViewModel by viewModels()
    private lateinit var community: Community
    private lateinit var curBoard: String
    private var curId = ""
    private val curUser by lazy { ApplicationClass.sharedPreferences.getUser() }
    private var isReply = ""
    private var isNew = false
    private var replyComment = Comment()
    private var replyList = arrayListOf<Comment>()

    private val myAdapter: CommunityDetailPhotoAdapter by lazy {
        CommunityDetailPhotoAdapter(myContext)
    }
    private lateinit var commentAdapter : CommunityDetailCommentAdapter


    private var commentList = arrayListOf<Comment>()

    private var commentPosition = 0;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        curId = arguments?.getString("communityId") ?: ""
        curBoard = activityViewModel.entryCommunityCollection
        if (curId != "") {
            communityViewModel.fetchCommunityById(curBoard, curId)
        }
        communityViewModel.fetchCommunityById(curBoard, curId)

        commentAdapter = CommunityDetailCommentAdapter(
            myContext,
            communityViewModel,
            childFragmentManager,
            "community"
        )
        initButton()
        initListener()
        initWatcher()
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
                    showCommentOptionView(view, data, position)
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
                    replyComment = data
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

                    val commentItemView =
                        commentRecyclerview.layoutManager?.findViewByPosition(position)
                    val scrollY = commentItemView?.top ?: 0
                    scrollView.smoothScrollTo(0, scrollY)
                }
            }
        commentAdapter.profileClickListener =
            object : CommunityDetailCommentAdapter.ProfileClickListener {
                override fun onClick(
                    view: ItemCommunityCommentBinding,
                    data: Comment,
                    position: Int
                ) {
                    val user = User().apply {
                        id = data.userId
                        nickname = data.userNickname
                        profilePhoto = data.userProfile
                        email = data.userEmail
                        name = data.userName
                    }
                    val bottomSheetDialogFragment = UserInfoBottomSheetFragment(user, "community")
                    bottomSheetDialogFragment.show(
                        childFragmentManager,
                        bottomSheetDialogFragment.tag
                    )
                }
            }
        communityViewModel.getCommentsByCommunityId(community.id)
    }


    private fun initButton() = with(binding) {
        commentBtnWrite.setOnClickListener {
            if (commentInput.text.toString().trim() == "") {
                commentInput.error = "내용을 입력해주세요."
                commentBtnWrite.isEnabled = false
                return@setOnClickListener;
            }
            val cur = Comment().apply {
                userId = curUser!!.email
                userNickname = curUser!!.nickname
                userProfile = curUser!!.profilePhoto
                userName = curUser!!.name
                userEmail = curUser!!.email
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
                if (replyComment.userId != curUser!!.email) {
                    val noti = NotificationData(
                        "커뮤니티-${activityViewModel.entryCommunityCollection}-${community.id}",
                        activityViewModel.getCommunityTitle(),
                        "${curUser!!.nickname} 님이 답글을 남겼습니다."
                    )
                    sendRemoteNotification(noti, replyComment.fcmToken)
                }
            } else {
                communityViewModel.insertComment(
                    activityViewModel.entryCommunityCollection,
                    community,
                    cur
                )
            }
            if (community.userId != curUser!!.email) {
                val noti = NotificationData(
                    "커뮤니티-${activityViewModel.entryCommunityCollection}-${community.id}",
                    activityViewModel.getCommunityTitle(),
                    "${curUser!!.nickname} 님이 댓글을 남겼습니다."
                )
                FCMService.sendRemoteNotification(noti, community.fcmToken)
            }

            (activity as MainActivity).hideKeyboard()
            commentInput.setText("")
            showCustomToast("댓글이 등록됐어요.")
            isNew = true
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

            if (isNew) {
                Handler().postDelayed({
                    binding.scrollView.fullScroll(View.FOCUS_DOWN)
                }, 100)
            }
        }
        communityViewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                // 수정에 성공한 경우, 커뮤니티를 새로고침하여 변경된 내용 반영
                showCustomToast("커뮤니티가 수정되었습니다.")
            } else {
                showCustomToast("커뮤니티 수정에 실패했습니다.")
            }
        }

        communityViewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showCustomToast("커뮤니티 삭제되었습니다.")
                navController.navigate(R.id.action_communityDetailFragment_to_communityListFragment)
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
            commentAdapter.replyList[commentPosition] = it
            commentAdapter.replyAdapters[commentPosition].submitList(commentAdapter.replyList[commentPosition].toMutableList())
            Log.d(TAG, "initListener: ${commentAdapter.replyAdapters[commentPosition].currentList}")
        }

        communityViewModel.likeCount.observe(viewLifecycleOwner) {
            binding.likeCount.text = it.toString()
        }
    }

    private fun initWatcher() = with(binding) {
        commentInput.addTextChangedListener {
            if (commentInput.lineCount > 3) {
                commentInput.error = "2줄까지 허용됩니다."
                commentBtnWrite.isEnabled = false
            } else {
                commentInput.error = null
                commentBtnWrite.isEnabled = true
            }
        }
        commentInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                commentInput.setText("")
                commentInput.error = null
                commentBtnWrite.isEnabled = false
            }
        }
    }

    private fun showCommunityOptionView(anchorView: View) {
        val popupMenu = PopupMenu(myContext, anchorView)
        popupMenu.inflate(R.menu.more_option_view) // option_menu는 메뉴 아이템을 정의한 리소스 파일입니다.

        // 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    val action =
                        CommunityDetailFragmentDirections.actionCommunityDetailFragmentToCommunityWriteFragment(
                            community.id,
                            curBoard
                        )
                    navController.navigate(action)
                    true
                }
                R.id.menu_delete -> {
                    showAlertDialog(R.string.delete_text, myContext)
                    mCustomDialog.listener = object : CustomAlertDialog.DialogClickedListener {
                        override fun onConfirmClick() {
                            communityViewModel.deleteCommunity(curBoard, community.id)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        // 옵션 뷰 보이기
        popupMenu.show()
    }

    private fun showCommentOptionView(anchorView: View, comment: Comment, position: Int) {
        val popupMenu = PopupMenu(myContext, anchorView)
        popupMenu.inflate(R.menu.comment_option_view) // option_menu는 메뉴 아이템을 정의한 리소스 파일입니다.

        // 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    isNew = false
                    showAlertDialog(R.string.delete_text, myContext)
                    mCustomDialog.listener = object : CustomAlertDialog.DialogClickedListener {
                        override fun onConfirmClick() {
                            communityViewModel.deleteComment(curBoard, community, comment.id)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        // 옵션 뷰 보이기
        popupMenu.show()
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


    override fun onRefresh() {
        // 새로고침을 수행할 동작을 여기에 작성하십시오.
        commentAdapter = CommunityDetailCommentAdapter(
            myContext,
            communityViewModel,
            childFragmentManager,
            "community"
        )
        communityViewModel.fetchCommunityById(curBoard, curId)
        communityViewModel.getCommentsByCommunityId(community.id)
        binding.swipeRefreshLayout.isRefreshing = false
    }
}