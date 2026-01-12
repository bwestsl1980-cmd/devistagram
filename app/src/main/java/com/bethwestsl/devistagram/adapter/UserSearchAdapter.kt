package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.databinding.ItemUserBinding
import com.bethwestsl.devistagram.model.SearchUser

class UserSearchAdapter(
    private val onUserClick: (SearchUser) -> Unit
) : ListAdapter<SearchUser, UserSearchAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClick: (SearchUser) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: SearchUser) {
            binding.usernameTextView.text = user.username

            binding.userIconImageView.load(user.userIcon) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(android.R.drawable.ic_menu_myplaces)
                error(android.R.drawable.ic_menu_myplaces)
            }

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<SearchUser>() {
        override fun areItemsTheSame(oldItem: SearchUser, newItem: SearchUser): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: SearchUser, newItem: SearchUser): Boolean {
            return oldItem == newItem
        }
    }
}

