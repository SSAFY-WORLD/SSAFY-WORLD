package com.ssafy.world.ui.main.community

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ssafy.world.databinding.ItemPhotoBtnDeleteBinding

class CommunityWritePhotoAdapter(val mContext: Context) : ListAdapter<String, CommunityWritePhotoAdapter.MyViewHolder>(
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
            ItemPhotoBtnDeleteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }
    interface DeleteListener{
        fun delete( position:Int)
    }
    lateinit var deleteListener: DeleteListener

    inner class MyViewHolder(private val binding: ItemPhotoBtnDeleteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            val displayMetrics = itemView.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val itemSize = screenWidth / 3
            binding.root.layoutParams = LinearLayout.LayoutParams(itemSize, itemSize)
        }
        fun bind(data: String) = with(binding) {
            Glide.with(mContext)
                .load(data)
                .transform(CenterCrop(), RoundedCorners(30))
                .into(binding.image)
            photoBtnDelete.setOnClickListener {
                deleteListener.delete(layoutPosition)
            }
        }
    }
}