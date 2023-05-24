package com.ssafy.world.src.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.FragmentCommunityDetailBinding
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.utils.getFormattedTime

class CommunityDetailCommentAdapter(val mContext: Context, val viewModel: CommunityViewModel) :
    ListAdapter<Comment, CommunityDetailCommentAdapter.MyViewHolder>(ItemComparator) {
    val replyAdapters: ArrayList<CommunityReplyAdapter> = arrayListOf()

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
            }
            val replyAdapter = CommunityReplyAdapter(mContext)
            replyAdapter.submitList(arrayListOf())
            replyRecyclerView.apply {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                adapter = replyAdapter
            }
            replyAdapter.replyItemClickListener =
                object : CommunityReplyAdapter.ReplyItemClickListener {
                    override fun onClick(view: View, data: Comment, position: Int) {
                        showAlertDialog(mContext, "댓글을 삭제할까요?", data.id, data.commentId)
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


    // 다이어로그를 보여주는 함수
    fun showAlertDialog(context: Context, message: String, replyId: String, commentId: String) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                viewModel.deleteReplyById(replyId, commentId)
                dialog.dismiss()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}