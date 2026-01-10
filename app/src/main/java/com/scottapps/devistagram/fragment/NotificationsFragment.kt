package com.scottapps.devistagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.scottapps.devistagram.databinding.FragmentNotificationsBinding
import com.scottapps.devistagram.databinding.ItemMessageBinding
import com.scottapps.devistagram.model.Message
import com.scottapps.devistagram.viewmodel.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            layoutParams.height = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) + insets.top
            v.layoutParams = layoutParams
            v.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationsFragment.adapter

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
        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            if (messages != null) {
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                } else {
                    binding.errorTextView.text = "No notifications yet"
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
            viewModel.loadMessages(refresh = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Message Adapter
    inner class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        private var messages = listOf<Message>()

        fun submitList(newMessages: List<Message>) {
            messages = newMessages
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MessageViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int = messages.size

        inner class MessageViewHolder(
            private val binding: ItemMessageBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(message: Message) {
                binding.apply {
                    // Avatar
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

                    // Subject - show the username from subject if available
                    val subjectText = when {
                        message.subject?.profile?.username != null -> "${message.originator?.username ?: "Someone"} is now watching ${message.subject.profile.username}"
                        message.type == "feedback.watch" -> "${message.originator?.username ?: "Someone"} is now watching you"
                        else -> message.type.replace(".", " ").capitalize()
                    }
                    subjectTextView.text = subjectText

                    // Preview
                    previewTextView.text = message.preview ?: message.type

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
                        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
                    }
                } catch (e: Exception) {
                    timestamp
                }
            }
        }
    }
}