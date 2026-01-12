package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bethwestsl.devistagram.databinding.ItemFavoriteUserBinding

class FavoriteUsersAdapter(
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<String, FavoriteUsersAdapter.FavoriteUserViewHolder>(FavoriteUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteUserViewHolder {
        val binding = ItemFavoriteUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteUserViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: FavoriteUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavoriteUserViewHolder(
        private val binding: ItemFavoriteUserBinding,
        private val onRemoveClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(username: String) {
            binding.usernameText.text = username
            binding.removeButton.setOnClickListener {
                onRemoveClick(username)
            }
        }
    }

    private class FavoriteUserDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}

