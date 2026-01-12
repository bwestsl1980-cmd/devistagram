package com.bethwestsl.devistagram.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.databinding.ItemCommentBinding
import com.bethwestsl.devistagram.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsAdapter(
    private val onReplyClick: (Comment) -> Unit,
    private val onLoadReplies: (Comment) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    fun submitList(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    fun updateComment(comment: Comment) {
        val index = comments.indexOfFirst { it.commentId == comment.commentId }
        if (index != -1) {
            comments[index] = comment
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                // User info
                usernameTextView.text = comment.user.username
                userAvatarImageView.load(comment.user.userIcon) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                }

                // Comment text - use body if text is null or empty
                val rawContent = when {
                    !comment.text.isNullOrBlank() -> comment.text
                    !comment.body.isNullOrBlank() -> comment.body
                    else -> "[No comment text]"
                }

                // Strip HTML tags from comment content
                val commentContent = stripHtmlTags(rawContent)
                commentTextView.text = commentContent

                android.util.Log.d("CommentsAdapter", "Binding comment ${comment.commentId}: raw='$rawContent', stripped='$commentContent'")

                // Timestamp
                timestampTextView.text = formatTimestamp(comment.posted)

                // Stats
                if (comment.likes > 0) {
                    likesTextView.visibility = View.VISIBLE
                    likesTextView.text = "♥ ${comment.likes}"
                } else {
                    likesTextView.visibility = View.GONE
                }

                // Reply count and button
                if (comment.replies > 0) {
                    repliesButton.visibility = View.VISIBLE
                    repliesButton.text = if (comment.isExpanded) {
                        "Hide ${comment.replies} replies"
                    } else {
                        "View ${comment.replies} replies"
                    }
                    repliesButton.isEnabled = !comment.isLoadingReplies

                    repliesButton.setOnClickListener {
                        onLoadReplies(comment)
                    }
                } else {
                    repliesButton.visibility = View.GONE
                }

                // Reply button
                replyButton.setOnClickListener {
                    onReplyClick(comment)
                }

                // Nested replies
                if (comment.isExpanded && !comment.repliesList.isNullOrEmpty()) {
                    repliesRecyclerView.visibility = View.VISIBLE
                    val repliesAdapter = CommentsAdapter(onReplyClick, onLoadReplies)
                    repliesAdapter.submitList(comment.repliesList!!)
                    repliesRecyclerView.adapter = repliesAdapter
                } else {
                    repliesRecyclerView.visibility = View.GONE
                }

                // Loading indicator for replies
                if (comment.isLoadingReplies) {
                    repliesProgressBar.visibility = View.VISIBLE
                } else {
                    repliesProgressBar.visibility = View.GONE
                }
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val time = timestamp.toLong()
                val date = Date(time * 1000)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(date)
            } catch (e: Exception) {
                timestamp
            }
        }

        private fun stripHtmlTags(html: String): String {
            // Remove HTML tags using regex
            return html
                .replace(Regex("<[^>]*>"), "") // Remove all HTML tags
                .replace("&nbsp;", " ")        // Replace non-breaking spaces
                .replace("&amp;", "&")          // Replace ampersand entities
                .replace("&lt;", "<")           // Replace less-than entities
                .replace("&gt;", ">")           // Replace greater-than entities
                .replace("&quot;", "\"")        // Replace quote entities
                .replace("&#39;", "'")          // Replace apostrophe entities
                .trim()                         // Remove leading/trailing whitespace
        }
    }
}

