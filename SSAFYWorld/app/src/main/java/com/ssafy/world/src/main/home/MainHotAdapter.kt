package com.ssafy.world.src.main.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.ssafy.world.data.model.Calendar
import com.ssafy.world.data.model.Community
import com.ssafy.world.databinding.ItemCalendarBinding
import com.ssafy.world.databinding.ItemCommunityListBinding
import com.ssafy.world.databinding.ItemHotCommunityBinding
import com.ssafy.world.utils.getFormattedTime

class MainHotAdapter(val mContext: Context) : ListAdapter<Community, MainHotAdapter.MyViewHolder>(ItemComparator) {

    companion object ItemComparator : DiffUtil.ItemCallback<Community>() {
        override fun areItemsTheSame(oldItem: Community, newItem: Community): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Community, newItem: Community): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHotAdapter.MyViewHolder {
        val binding = ItemHotCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHotAdapter.MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    override fun getItemCount(): Int {
        // 최신 아이템 위에 3개만 보여주기 위해 아이템 개수를 조정
        return minOf(super.getItemCount(), 3)
    }

    interface ItemClickListener {
        fun onClick(data: Community)
    }

    lateinit var itemClickListener: ItemClickListener


    inner class MyViewHolder(private val binding: ItemHotCommunityBinding) : RecyclerView.ViewHolder(binding.root) {

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
                itemClickListener.onClick(data)
            }
        }
    }
}