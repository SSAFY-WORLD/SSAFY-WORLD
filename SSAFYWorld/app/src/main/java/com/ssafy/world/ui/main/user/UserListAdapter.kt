package com.ssafy.world.ui.main.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.world.R
import com.ssafy.world.data.model.User
import com.ssafy.world.databinding.ItemUsersBinding

class UserListAdapter :
    ListAdapter<User, UserListAdapter.MyViewHolder>(ItemComparator) {
    companion object ItemComparator : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            // Item의 id 값으로 비교한다. object에 id가 있으면 그 id를 사용하여 비교.
            return oldItem.id == newItem.id
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemUsersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            bind(getItem(position))
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, user: User, position: Int)
    }

    lateinit var itemClickListener: ItemClickListener

    inner class MyViewHolder(private val binding: ItemUsersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) = with(binding) {
            userName.text = user.name
            Glide.with(binding.root)
                .load(user.profilePhoto)
                .error(R.drawable.default_profile_image)
                .into(profileImage)
            itemView.setOnClickListener {
                itemClickListener.onClick(it, user, layoutPosition)
            }
        }
    }
}