package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bethwestsl.devistagram.databinding.ItemDeviationGridBinding
import com.bethwestsl.devistagram.model.Deviation

class DeviationGridAdapter(
    private val onDeviationClick: (Deviation) -> Unit
) : ListAdapter<Deviation, DeviationGridAdapter.DeviationViewHolder>(DeviationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviationViewHolder {
        val binding = ItemDeviationGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviationViewHolder(binding, onDeviationClick)
    }
    
    override fun onBindViewHolder(holder: DeviationViewHolder, position: Int) {
        val deviation = getItem(position)
        if (deviation != null) {
            holder.bind(deviation)
        }
    }
    
    class DeviationViewHolder(
        private val binding: ItemDeviationGridBinding,
        private val onDeviationClick: (Deviation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(deviation: Deviation) {
            binding.apply {
                // Deviation image - use content if available, otherwise preview
                val imageUrl = deviation.content?.src 
                    ?: deviation.preview?.src 
                    ?: deviation.thumbs?.firstOrNull()?.src
                
                deviationImageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
                
                // Click listener
                root.setOnClickListener {
                    onDeviationClick(deviation)
                }
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
