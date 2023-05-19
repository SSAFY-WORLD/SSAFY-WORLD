package com.ssafy.world.src.main.photo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.ssafy.world.data.model.Photo
import com.ssafy.world.databinding.ItemPhotoCheckboxBinding

class PhotoGridAdapter(val mContext: Context) : ListAdapter<Photo, PhotoGridAdapter.MyViewHolder>(
    PhotoGridAdapter
) {

    companion object ItemComparator : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.url == newItem.url
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemPhotoCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    inner class MyViewHolder(private val binding: ItemPhotoCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val displayMetrics = itemView.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val itemSize = screenWidth / 3
            binding.root.layoutParams = LinearLayout.LayoutParams(itemSize, itemSize)
        }
        fun bind(data: Photo) = with(binding) {

            Glide.with(mContext)
                .load(data.url)
                .transform(CenterCrop())
                .into(binding.image)

            // 체크박스 클릭 리스너 설정
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
            }
        }
    }
}