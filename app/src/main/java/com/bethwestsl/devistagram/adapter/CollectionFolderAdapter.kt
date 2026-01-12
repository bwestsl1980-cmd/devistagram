package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bethwestsl.devistagram.databinding.ItemCollectionFolderBinding
import com.bethwestsl.devistagram.model.CollectionFolder

class CollectionFolderAdapter(
    private val onFolderClick: (CollectionFolder) -> Unit
) : RecyclerView.Adapter<CollectionFolderAdapter.FolderViewHolder>() {

    private val folders = mutableListOf<CollectionFolder>()

    fun submitList(newFolders: List<CollectionFolder>) {
        folders.clear()
        folders.addAll(newFolders)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemCollectionFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount(): Int = folders.size

    inner class FolderViewHolder(
        private val binding: ItemCollectionFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: CollectionFolder) {
            binding.apply {
                folderNameTextView.text = folder.name
                folderSizeTextView.text = "${folder.size} items"

                // Load folder thumbnail if available
                folder.thumb?.src?.let { thumbUrl ->
                    folderThumbnailImageView.load(thumbUrl) {
                        crossfade(true)
                        placeholder(android.R.drawable.ic_menu_gallery)
                        error(android.R.drawable.ic_menu_gallery)
                    }
                } ?: run {
                    folderThumbnailImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                root.setOnClickListener {
                    onFolderClick(folder)
                }
            }
        }
    }
}

