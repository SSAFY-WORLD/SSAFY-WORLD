package com.ssafy.world.src.main.community

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssafy.world.databinding.ItemCommunityDetailPhotoBinding
import com.ssafy.world.databinding.ItemPhotoBtnDeleteBinding

class CommunityDetailPhotoAdapter(val mContext: Context) : ListAdapter<String, CommunityDetailPhotoAdapter.MyViewHolder>(
    ItemComparator
) {
    companion object ItemComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemCommunityDetailPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    inner class MyViewHolder(private val binding: ItemCommunityDetailPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val displayMetrics = itemView.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val itemSize = screenWidth / 2
            val widthSize = screenWidth
            binding.root.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemSize).apply {
                gravity = Gravity.CENTER
            }
        }
        fun bind(data: String) = with(binding) {
            Glide.with(mContext)
                .load(data)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(binding.image)
        }
    }
}