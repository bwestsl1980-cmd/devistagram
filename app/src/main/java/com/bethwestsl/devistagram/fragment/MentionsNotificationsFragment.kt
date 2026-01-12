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
import com.bethwestsl.devistagram.OtherUserProfileActivity
import com.bethwestsl.devistagram.R
import com.bethwestsl.devistagram.databinding.FragmentMentionsNotificationsBinding
import com.bethwestsl.devistagram.databinding.ItemMentionNotificationBinding
import com.bethwestsl.devistagram.model.Author
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.model.Message
import com.bethwestsl.devistagram.viewmodel.MentionsNotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MentionsNotificationsFragment : Fragment() {

    private var _binding: FragmentMentionsNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MentionsNotificationsViewModel
    private lateinit var adapter: MentionNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMentionsNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter = MentionNotificationAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MentionsNotificationsFragment.adapter

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
        viewModel = ViewModelProvider(this)[MentionsNotificationsViewModel::class.java]

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications != null) {
                adapter.submitList(notifications)
                if (notifications.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                } else {
                    binding.errorTextView.text = "No mention notifications yet"
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
    inner class MentionNotificationAdapter : RecyclerView.Adapter<MentionNotificationAdapter.NotificationViewHolder>() {

        private var notifications = listOf<Message>()

        fun submitList(newNotifications: List<Message>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val binding = ItemMentionNotificationBinding.inflate(
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
            private val binding: ItemMentionNotificationBinding
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

                    // Build notification text: "{{originator.userid}} has mentioned you in {{subject.title}}"
                    val username = message.originator?.username ?: "Someone"
                    val title = message.subject?.title ?: "a comment"
                    val fullText = "$username has mentioned you in $title"

                    val spannableString = SpannableString(fullText)

                    // Make username clickable
                    val usernameStart = 0
                    val usernameEnd = username.length
                    spannableString.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                // Navigate to user profile
                                message.originator?.username?.let { user ->
                                    OtherUserProfileActivity.start(widget.context, user)
                                }
                            }

                            override fun updateDrawState(ds: android.text.TextPaint) {
                                super.updateDrawState(ds)
                                ds.color = ContextCompat.getColor(root.context, R.color.link_color)
                                ds.isUnderlineText = false
                            }
                        },
                        usernameStart,
                        usernameEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // Make title clickable
                    val titleStart = fullText.indexOf(title)
                    val titleEnd = titleStart + title.length
                    if (titleStart >= 0) {
                        spannableString.setSpan(
                            object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    // Navigate to the deviation containing the mention
                                    message.subject?.deviation?.deviationId?.let { deviationId ->
                                        navigateToDeviation(deviationId)
                                    }
                                }

                                override fun updateDrawState(ds: android.text.TextPaint) {
                                    super.updateDrawState(ds)
                                    ds.color = ContextCompat.getColor(root.context, R.color.link_color)
                                    ds.isUnderlineText = false
                                }
                            },
                            titleStart,
                            titleEnd,
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
                // Create a minimal Deviation object for navigation
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

    companion object {
        fun newInstance() = MentionsNotificationsFragment()
    }
}

