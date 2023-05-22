package com.ssafy.world.src.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.ItemCommunityListBinding
import com.ssafy.world.utils.getFormattedTime

class CommunityListAdapter(val mContext: Context) :
    ListAdapter<Community, CommunityListAdapter.MyViewHolder>(ItemComparator) {
    companion object ItemComparator : DiffUtil.ItemCallback<Community>() {
        override fun areItemsTheSame(oldItem: Community, newItem: Community): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Community, newItem: Community): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCommunityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class MyViewHolder(private val binding: ItemCommunityListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Community) = with(binding) {
            Glide.with(mContext)
                .load(data.userProfile)
                .transform(FitCenter())
                .circleCrop()
                .into(binding.userProfile)
            title.text = data.title
            content.text = data.content
            nickname.text = data.userNickname
            photo.text = data.photoUrls.size.toString()
            comment.text = data.commentCount.toString()
            time.text = getFormattedTime(data.time)
            likeCount.text = data.likeCount.toString()
            itemView.setOnClickListener {
                itemClickListener.onClick(it, data, layoutPosition)
            }
        }
    }
}