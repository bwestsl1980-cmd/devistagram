package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.databinding.ItemDeviationBinding
import com.bethwestsl.devistagram.model.Deviation

class DeviationAdapter(
    private val onDeviationClick: (Deviation) -> Unit,
    private val onAuthorClick: ((String) -> Unit)? = null
) : ListAdapter<Deviation, DeviationAdapter.DeviationViewHolder>(DeviationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviationViewHolder {
        val binding = ItemDeviationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviationViewHolder(binding, onDeviationClick, onAuthorClick)
    }
    
    override fun onBindViewHolder(holder: DeviationViewHolder, position: Int) {
        val deviation = getItem(position)
        if (deviation != null) {
            holder.bind(deviation)
        }
    }
    
    class DeviationViewHolder(
        private val binding: ItemDeviationBinding,
        private val onDeviationClick: (Deviation) -> Unit,
        private val onAuthorClick: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(deviation: Deviation) {
            binding.apply {
                val username = deviation.author.username
                
                // Author info
                authorNameTextView.text = username
                authorAvatarImageView.load(deviation.author.userIcon) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }
                
                // Make author name and avatar clickable
                val authorClickListener = View.OnClickListener {
                    onAuthorClick?.invoke(username)
                }
                authorNameTextView.setOnClickListener(authorClickListener)
                authorAvatarImageView.setOnClickListener(authorClickListener)

                // Deviation image - use content if available, otherwise preview
                val imageUrl = deviation.content?.src 
                    ?: deviation.preview?.src 
                    ?: deviation.thumbs?.firstOrNull()?.src
                
                deviationImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
                
                // Title
                titleTextView.text = deviation.title ?: "Untitled"
                
                // Stats
                val stats = deviation.stats
                if (stats != null) {
                    favoritesTextView.text = "⭐ ${formatCount(stats.favourites)}"
                    commentsTextView.text = "💬 ${formatCount(stats.comments)}"
                } else {
                    favoritesTextView.text = "⭐ 0"
                    commentsTextView.text = "💬 0"
                }
                
                // Click listener
                root.setOnClickListener {
                    onDeviationClick(deviation)
                }
            }
        }
        

        private fun formatCount(count: Int): String {
            return when {
                count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
                count >= 1000 -> String.format("%.1fK", count / 1000.0)
                else -> count.toString()
            }
        }
    }
    
    class DeviationDiffCallback : DiffUtil.ItemCallback<Deviation>() {
        override fun areItemsTheSame(oldItem: Deviation, newItem: Deviation): Boolean {
            return oldItem.deviationId == newItem.deviationId
        }
        
        override fun areContentsTheSame(oldItem: Deviation, newItem: Deviation): Boolean {
            return oldItem == newItem
        }
    }
}
