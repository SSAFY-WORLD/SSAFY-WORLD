package com.ssafy.world.src.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssafy.world.data.model.Comment
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.ItemCommunityCommentBinding
import com.ssafy.world.utils.getFormattedTime

class CommunityDetailCommentAdapter(val mContext: Context) :
    ListAdapter<Comment, CommunityDetailCommentAdapter.MyViewHolder>(ItemComparator) {
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
        fun onClick(view: View, data: Community, position: Int)
    }

    lateinit var itemClickListener: ItemClickListener

    inner class MyViewHolder(private val binding: ItemCommunityCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Comment) = with(binding) {
            Glide.with(mContext)
                .load(data)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(binding.profileImage)

            username.text = data.userId
            content.text = data.comment
            time.text = getFormattedTime(data.time)
        }
    }
}