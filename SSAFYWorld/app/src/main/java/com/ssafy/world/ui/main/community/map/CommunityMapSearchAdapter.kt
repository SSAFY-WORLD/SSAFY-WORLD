package com.ssafy.world.ui.main.community.map

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.Place
import com.ssafy.world.databinding.ItemMapSearchBinding

class CommunityMapSearchAdapter : ListAdapter<Place, CommunityMapSearchAdapter.MyViewHolder>(ItmeComparator) {
    companion object ItmeComparator: DiffUtil.ItemCallback<Place>() {
        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemMapSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    interface ItemClickListener {
        fun onClick(data: Place)
    }

    lateinit var itemClickListener: ItemClickListener

    inner class MyViewHolder(private val binding: ItemMapSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Place) = with(binding) {
            name.text = data.name
            address.text = data.address
            itemView.setOnClickListener {
                itemClickListener.onClick(data)
            }
        }
    }
}