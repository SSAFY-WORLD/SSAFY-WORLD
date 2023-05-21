package com.ssafy.world.src.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssafy.world.R
import com.ssafy.world.config.ApplicationClass
import com.ssafy.world.data.model.Comment
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.utils.getFormattedTime

class CommunityReplyAdapter(val mContext: Context) :
    ListAdapter<Comment, CommunityReplyAdapter.MyViewHolder>(ItemComparator) {
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

    interface ReplyItemClickListener {
        fun onClick(view: View, data: Comment, position: Int)
    }
    lateinit var replyItemClickListener: ReplyItemClickListener

    inner class MyViewHolder(private val binding: ItemCommunityCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Comment) = with(binding) {
            Glide.with(mContext)
                .load(data.userProfile)
                .transform(FitCenter())
                .circleCrop()
                .into(binding.profileImage)
            if(data.userId == ApplicationClass.sharedPreferences.getUser()!!.email) {
                commentMore.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_close_gray))
                commentMore.visibility = View.VISIBLE
                commentMore.setOnClickListener {
                    replyItemClickListener.onClick(commentMore, data, layoutPosition)
                }

            }
            replyBtnShow.visibility = View.GONE
            username.text = data.userNickname
            content.text = data.comment
            time.text = getFormattedTime(data.time)
        }
    }
}