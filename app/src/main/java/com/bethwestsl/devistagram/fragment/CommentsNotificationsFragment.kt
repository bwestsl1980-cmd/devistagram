package com.bethwestsl.devistagram.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.DeviationDetailActivity
import com.bethwestsl.devistagram.R
import com.bethwestsl.devistagram.databinding.FragmentCommentsNotificationsBinding
import com.bethwestsl.devistagram.databinding.ItemCommentNotificationBinding
import com.bethwestsl.devistagram.model.Author
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.viewmodel.CommentsNotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsNotificationsFragment : Fragment() {

    private var _binding: FragmentCommentsNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CommentsNotificationsViewModel
    private lateinit var adapter: CommentNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter = CommentNotificationAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CommentsNotificationsFragment.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CommentsNotificationsViewModel::class.java]

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications != null) {
                adapter.submitList(notifications)
                if (notifications.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                } else {
                    binding.errorTextView.text = "No comment notifications yet"
                    binding.errorTextView.visibility = View.VISIBLE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading && adapter.itemCount == 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.swipeRefreshLayout.isRefreshing = isLoading && adapter.itemCount > 0
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorTextView.text = error
                binding.errorTextView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadNotifications(refresh = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Adapter
    inner class CommentNotificationAdapter : RecyclerView.Adapter<CommentNotificationAdapter.NotificationViewHolder>() {

        private var notifications = listOf<Message>()

        fun submitList(newNotifications: List<Message>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val binding = ItemCommentNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NotificationViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            holder.bind(notifications[position])
        }

        override fun getItemCount(): Int = notifications.size

        inner class NotificationViewHolder(
            private val binding: ItemCommentNotificationBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(message: Message) {
                binding.apply {
                    // User Avatar
                    message.originator?.userIcon?.let { iconUrl: String ->
                        avatarImageView.load(iconUrl) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(android.R.drawable.ic_menu_myplaces)
                            error(android.R.drawable.ic_menu_myplaces)
                        }
                    }

                    // New indicator
                    newIndicator.visibility = if (message.isNew) View.VISIBLE else View.GONE

                    // Build notification text based on type
                    val username = message.originator?.username ?: "Someone"
                    val isReply = message.type.contains("reply", ignoreCase = true)

                    val (fullText, clickableRanges) = if (isReply) {
                        // For replies: "{{originator.userid}} has replied to your comment"
                        val text = "$username has replied to your comment"
                        val ranges = mutableListOf<ClickableRange>()

                        // Username clickable
                        ranges.add(ClickableRange(0, username.length, ClickType.USER, username))

                        // "comment" clickable (links to the comment)
                        val commentStart = text.indexOf("comment")
                        if (commentStart >= 0 && message.subject?.comment?.commentId != null) {
                            ranges.add(ClickableRange(
                                commentStart,
                                commentStart + "comment".length,
                                ClickType.COMMENT,
                                message.subject.comment.commentId
                            ))
                        }

                        Pair(text, ranges)
                    } else {
                        // For comments: "{{originator.userid}} has commented on your deviation {{subject.title}}"
                        val deviationTitle = message.subject?.title ?: message.subject?.deviation?.title ?: "your deviation"
                        val text = "$username has commented on your deviation $deviationTitle"
                        val ranges = mutableListOf<ClickableRange>()

                        // Username clickable
                        ranges.add(ClickableRange(0, username.length, ClickType.USER, username))

                        // Deviation title clickable
                        val deviationStart = text.indexOf(deviationTitle)
                        if (deviationStart >= 0 && message.subject?.deviation?.deviationId != null) {
                            ranges.add(ClickableRange(
                                deviationStart,
                                deviationStart + deviationTitle.length,
                                ClickType.DEVIATION,
                                message.subject.deviation.deviationId
                            ))
                        }

                        Pair(text, ranges)
                    }

                    // Apply clickable spans
                    val spannableString = SpannableString(fullText)
                    clickableRanges.forEach { range ->
                        spannableString.setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    when (range.type) {
                                        ClickType.USER -> {
                                            Toast.makeText(widget.context, "Navigate to ${range.data}'s profile", Toast.LENGTH_SHORT).show()
                                        }
                                        ClickType.DEVIATION -> {
                                            navigateToDeviation(range.data)
                                        }
                                        ClickType.COMMENT -> {
                                            Toast.makeText(widget.context, "Navigate to comment ${range.data}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun updateDrawState(ds: android.text.TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = ContextCompat.getColor(root.context, R.color.link_color)
                                    ds.isUnderlineText = false
                                }
                            },
                            range.start,
                            range.end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    notificationTextView.text = spannableString
                    notificationTextView.movementMethod = LinkMovementMethod.getInstance()

                    // Timestamp
                    timestampTextView.text = formatTimestamp(message.timestamp)
                }
            }

            private fun formatTimestamp(timestamp: String): String {
                return try {
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(timestamp)
                    val now = Date()
                    val diff = now.time - (date?.time ?: 0)

                    when {
                        diff < 60000 -> "Just now"
                        diff < 3600000 -> "${diff / 60000}m ago"
                        diff < 86400000 -> "${diff / 3600000}h ago"
                        diff < 604800000 -> "${diff / 86400000}d ago"
                        else -> date?.let { SimpleDateFormat("MMM d", Locale.getDefault()).format(it) } ?: timestamp
                    }
                } catch (_: Exception) {
                    timestamp
                }
            }

            private fun navigateToDeviation(deviationId: String) {
                val deviation = Deviation(
                    deviationId = deviationId,
                    title = "",
                    url = "",
                    publishedTime = null,
                    author = Author(
                        userId = "",
                        username = "",
                        userIcon = "",
                        type = ""
                    ),
                    stats = null,
                    preview = null,
                    content = null,
                    thumbs = null,
                    excerpt = null,
                    isFavourited = false,
                    isMature = false,
                    allowsComments = false,
                    category = null,
                    categoryPath = null,
                    isDeleted = false,
                    description = null
                )
                DeviationDetailActivity.start(binding.root.context, deviation)
            }
        }
    }

    // Helper classes for clickable ranges
    private data class ClickableRange(
        val start: Int,
        val end: Int,
        val type: ClickType,
        val data: String
    )

    private enum class ClickType {
        USER,
        DEVIATION,
        COMMENT
    }

    companion object {
        fun newInstance() = CommentsNotificationsFragment()
    }
}

