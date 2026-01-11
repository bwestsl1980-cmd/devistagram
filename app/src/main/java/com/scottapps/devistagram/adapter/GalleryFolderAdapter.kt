package com.scottapps.devistagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.scottapps.devistagram.databinding.ItemGalleryFolderBinding
import com.scottapps.devistagram.model.GalleryFolder

class GalleryFolderAdapter(
    private val onFolderClick: (GalleryFolder) -> Unit
) : ListAdapter<GalleryFolder, GalleryFolderAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onFolderClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemGalleryFolderBinding,
        private val onFolderClick: (GalleryFolder) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: GalleryFolder) {
            binding.folderNameTextView.text = folder.name ?: "Unnamed Folder"
            binding.folderSizeTextView.text = "${folder.size ?: 0} items"

            // Load thumbnail
            val thumbnailUrl = folder.thumb?.src
            if (thumbnailUrl != null) {
                binding.folderThumbImageView.load(thumbnailUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }
            } else {
                binding.folderThumbImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.root.setOnClickListener {
                onFolderClick(folder)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GalleryFolder>() {
        override fun areItemsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder): Boolean {
            return oldItem.folderId == newItem.folderId
        }

        override fun areContentsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder): Boolean {
            return oldItem == newItem
        }
    }
}
