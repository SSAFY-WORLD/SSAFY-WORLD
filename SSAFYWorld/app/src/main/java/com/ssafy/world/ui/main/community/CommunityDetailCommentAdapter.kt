package com.ssafy.world.ui.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.ui.main.user.UserInfoBottomSheetFragment
import com.ssafy.world.utils.CustomAlertDialog
import com.ssafy.world.utils.getFormattedTime

class CommunityDetailCommentAdapter(
    val mContext: Context,
    val viewModel: CommunityViewModel,
    val manager: FragmentManager,
    val from: String
) :
    ListAdapter<Comment, CommunityDetailCommentAdapter.MyViewHolder>(ItemComparator) {
    val replyAdapters: ArrayList<CommunityReplyAdapter> = arrayListOf()
    val replyList: ArrayList<ArrayList<Comment>> = arrayListOf()

    companion object ItemComparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCommunityCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, data: Comment, position: Int)
    }

    lateinit var itemClickListener: ItemClickListener

    interface ReplyClickListener {
        fun onClick(view: View, data: Comment, position: Int)
    }

    lateinit var replyClickListener: ReplyClickListener

    interface ReplyShowClickListener {
        fun onClick(view: ItemCommunityCommentBinding, data: Comment, position: Int)
    }

    lateinit var replyShowClickListener: ReplyShowClickListener


    interface ProfileClickListener {
        fun onClick(view: ItemCommunityCommentBinding, data: Comment, position: Int)
    }

    lateinit var profileClickListener: ProfileClickListener


    inner class MyViewHolder(private val binding: ItemCommunityCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Comment) = with(binding) {
            Glide.with(mContext)
                .load(data.userProfile)
                .transform(FitCenter())
                .circleCrop()
                .into(binding.profileImage)
            if (data.userId == ApplicationClass.sharedPreferences.getUser()!!.email) {
                commentMore.visibility = View.VISIBLE
                commentMore.setOnClickListener {
                    itemClickListener.onClick(commentMore, data, layoutPosition)
                }
            } else {
                profileImage.setOnClickListener {
                    profileClickListener.onClick(binding, data, layoutPosition)
                }
            }
            val replyAdapter = CommunityReplyAdapter(mContext)
            val cur = arrayListOf<Comment>()
            replyAdapter.submitList(cur.toMutableList())
            replyList.add(cur)
            replyRecyclerView.apply {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                adapter = replyAdapter
            }
            replyAdapter.replyItemClickListener =
                object : CommunityReplyAdapter.ReplyItemClickListener {
                    override fun onClick(view: View, data: Comment, position: Int) {
                        showCommunityOptionView(view, data)
                    }
                }
            replyAdapter.profileClickListener =
                object : CommunityReplyAdapter.ProfileClickListener {
                    override fun onClick(view: View, data: Comment, position: Int) {
                        val user = User().apply {
                            id = data.userId
                            nickname = data.userNickname
                            profilePhoto = data.userProfile
                            email = data.userEmail
                            name = data.userName
                        }
                        val bottomSheetDialogFragment =
                            UserInfoBottomSheetFragment(user, from)
                        bottomSheetDialogFragment.show(manager, bottomSheetDialogFragment.tag)
                    }
                }
            replyAdapters.add(replyAdapter)

            replyBtn.setOnClickListener {
                replyClickListener.onClick(replyBtn, data, layoutPosition)
            }
            replyBtnShow.setOnClickListener {
                replyShowClickListener.onClick(binding, data, layoutPosition)
            }
            username.text = data.userNickname
            content.text = data.comment
            time.text = getFormattedTime(data.time)

        }
    }

    private fun showCommunityOptionView(anchorView: View, data: Comment) {
        val popupMenu = PopupMenu(mContext, anchorView)
        popupMenu.inflate(R.menu.comment_option_view) // option_menu는 메뉴 아이템을 정의한 리소스 파일입니다.

        // 메뉴 아이템 클릭 리스너 설정
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    val mDialog = CustomAlertDialog(R.string.delete_text, mContext)
                    mDialog.listener = object : CustomAlertDialog.DialogClickedListener {
                        override fun onConfirmClick() {
                            viewModel.deleteReplyById(data.id, data.commentId)
                            Toast.makeText(mContext, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    mDialog.show()
                    true
                }
                else -> false
            }
        }

        // 옵션 뷰 보이기
        popupMenu.show()
    }
}