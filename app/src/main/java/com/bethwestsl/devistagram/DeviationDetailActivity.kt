package com.bethwestsl.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.databinding.ActivityDeviationDetailBinding
import com.bethwestsl.devistagram.model.Deviation
import com.bethwestsl.devistagram.network.RetrofitClient
import com.bethwestsl.devistagram.repository.UserWatchRepository
import com.bethwestsl.devistagram.repository.CommentsRepository
import com.bethwestsl.devistagram.repository.CollectionsRepository
import com.bethwestsl.devistagram.adapter.CommentsAdapter
import com.bethwestsl.devistagram.model.Comment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviationDetailBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var userWatchRepository: UserWatchRepository
    private lateinit var commentsRepository: CommentsRepository
    private lateinit var collectionsRepository: CollectionsRepository
    private lateinit var commentsAdapter: CommentsAdapter
    private var deviation: Deviation? = null
    private var isWatchingArtist: Boolean = false
    private var isLoadingWatchStatus: Boolean = false
    private var isFavorited: Boolean = false
    private val comments = mutableListOf<Comment>()

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"

        fun start(context: Context, deviation: Deviation) {
            val intent = Intent(context, DeviationDetailActivity::class.java).apply {
                putExtra(EXTRA_DEVIATION_ID, deviation.deviationId)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        tokenManager = TokenManager(this)
        userWatchRepository = UserWatchRepository()
        commentsRepository = CommentsRepository()
        collectionsRepository = CollectionsRepository()

        setupCommentsRecyclerView()
        setupToolbar()

        // Get deviation ID from intent
        val deviationId = intent.getStringExtra(EXTRA_DEVIATION_ID)

        if (deviationId != null) {
            loadDeviationDetails(deviationId)
        } else {
            Toast.makeText(this, "Error loading deviation", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.favoriteButton.setOnClickListener {
            showFavoriteFolderDialog()
        }


        binding.followButton.setOnClickListener {
            toggleWatchArtist()
        }

        binding.authorAvatarImageView.setOnClickListener {
            deviation?.author?.username?.let { username ->
                OtherUserProfileActivity.start(this, username)
            }
        }

        binding.authorNameTextView.setOnClickListener {
            deviation?.author?.username?.let { username ->
                OtherUserProfileActivity.start(this, username)
            }
        }

        binding.addCommentButton.setOnClickListener {
            showAddCommentDialog()
        }
    }

    private fun loadDeviationDetails(deviationId: String) {
        binding.progressBar.visibility = View.VISIBLE

        android.util.Log.d("DeviationDetail", "Loading deviation with ID: $deviationId")

        lifecycleScope.launch {
            try {
                val accessToken = tokenManager.getAccessToken()
                if (accessToken == null) {
                    Toast.makeText(
                        this@DeviationDetailActivity,
                        "Not logged in",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                val response = RetrofitClient.contentApi.getDeviation(
                    deviationId = deviationId,
                    authorization = "Bearer $accessToken"
                )

                android.util.Log.d("DeviationDetail", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    android.util.Log.d("DeviationDetail", "Response body: ${response.body()}")
                    android.util.Log.d("DeviationDetail", "Raw response: ${response.raw()}")
                } else {
                    android.util.Log.d("DeviationDetail", "Response error: ${response.errorBody()?.string()}")
                }

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val deviationDetail = response.body()!!.toDeviation()
                    deviation = deviationDetail
                    displayDeviation(deviationDetail)

                    // Fetch metadata for description
                    loadDeviationMetadata(deviationId, accessToken)

                    // Check if user is watching the artist
                    checkWatchingStatus(deviationDetail.author.username, accessToken)

                    // Load comments
                    loadComments(deviationId, accessToken)
                } else {
                    Toast.makeText(
                        this@DeviationDetailActivity,
                        "Failed to load deviation: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@DeviationDetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadDeviationMetadata(deviationId: String, accessToken: String) {
        lifecycleScope.launch {
            try {
                val metadataResponse = RetrofitClient.contentApi.getDeviationMetadata(
                    authorization = "Bearer $accessToken",
                    deviationIds = listOf(deviationId)
                )

                if (metadataResponse.isSuccessful && metadataResponse.body() != null) {
                    val metadata = metadataResponse.body()!!.metadata?.firstOrNull()
                    if (metadata != null && !metadata.description.isNullOrBlank()) {
                        // Remove HTML tags for simple display
                        val plainDescription = android.text.Html.fromHtml(
                            metadata.description,
                            android.text.Html.FROM_HTML_MODE_COMPACT
                        ).toString().trim()

                        binding.descriptionTextView.text = plainDescription
                    } else {
                        binding.descriptionTextView.text = "No description available"
                    }
                } else {
                    binding.descriptionTextView.text = "No description available"
                }
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Error loading metadata", e)
                binding.descriptionTextView.text = "No description available"
            }
        }
    }

    private fun displayDeviation(deviation: Deviation) {
        binding.apply {
            // Title
            titleTextView.text = deviation.title ?: "Untitled"

            // Image - use the largest available
            val imageUrl = deviation.content?.src
                ?: deviation.preview?.src
                ?: deviation.thumbs?.firstOrNull()?.src

            deviationImageView.load(imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }

            // Author
            authorNameTextView.text = deviation.author.username
            authorAvatarImageView.load(deviation.author.userIcon) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }

            // Published date
            deviation.publishedTime?.let { timeString ->
                // publishedTime is a String (Unix timestamp), convert to Long
                try {
                    val timestamp = timeString.toLong()
                    publishedDateTextView.text = formatPublishedDate(timestamp)
                } catch (e: NumberFormatException) {
                    publishedDateTextView.text = "Unknown date"
                }
            }

            // Description - will be updated when metadata loads
            descriptionTextView.text = "Loading description..."

            // Stats
            val stats = deviation.stats
            if (stats != null) {
                favoritesTextView.text = "⭐ ${formatCount(stats.favourites)}"
                commentsTextView.text = "💬 ${formatCount(stats.comments)}"
                viewsTextView.text = "👁 ${formatCount(stats.views ?: 0)}"
            } else {
                favoritesTextView.text = "⭐ 0"
                commentsTextView.text = "💬 0"
                viewsTextView.text = "👁 0"
            }

            // Set favorite status
            isFavorited = deviation.isFavourited
            updateFavoriteButton()
        }
    }

    private fun formatPublishedDate(timestamp: Long): String {
        val now = System.currentTimeMillis() / 1000
        val diff = now - timestamp

        return when {
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60} minutes ago"
            diff < 86400 -> "${diff / 3600} hours ago"
            diff < 604800 -> "${diff / 86400} days ago"
            else -> {
                val date = Date(timestamp * 1000)
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                sdf.format(date)
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

    private fun checkWatchingStatus(username: String, accessToken: String) {
        isLoadingWatchStatus = true
        updateWatchButtonState()

        lifecycleScope.launch {
            try {
                userWatchRepository.isWatchingUser(username, accessToken).fold(
                    onSuccess = { watching ->
                        isWatchingArtist = watching
                        isLoadingWatchStatus = false
                        updateWatchButtonState()
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error checking watch status", error)
                        isLoadingWatchStatus = false
                        updateWatchButtonState()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception checking watch status", e)
                isLoadingWatchStatus = false
                updateWatchButtonState()
            }
        }
    }

    private fun toggleWatchArtist() {
        val currentDeviation = deviation ?: return
        val accessToken = tokenManager.getAccessToken() ?: return

        if (isLoadingWatchStatus) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show()
            return
        }

        isLoadingWatchStatus = true
        updateWatchButtonState()

        lifecycleScope.launch {
            try {
                userWatchRepository.toggleWatchUser(
                    username = currentDeviation.author.username,
                    accessToken = accessToken,
                    currentlyWatching = isWatchingArtist
                ).fold(
                    onSuccess = { success ->
                        if (success) {
                            isWatchingArtist = !isWatchingArtist
                            isLoadingWatchStatus = false
                            updateWatchButtonState()

                            val message = if (isWatchingArtist) {
                                "Now watching ${currentDeviation.author.username}"
                            } else {
                                "Unwatched ${currentDeviation.author.username}"
                            }
                            Toast.makeText(this@DeviationDetailActivity, message, Toast.LENGTH_SHORT).show()
                        } else {
                            isLoadingWatchStatus = false
                            updateWatchButtonState()
                            Toast.makeText(this@DeviationDetailActivity, "Failed to update watch status", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error toggling watch", error)
                        isLoadingWatchStatus = false
                        updateWatchButtonState()
                        Toast.makeText(this@DeviationDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception toggling watch", e)
                isLoadingWatchStatus = false
                updateWatchButtonState()
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWatchButtonState() {
        binding.followButton.apply {
            if (isLoadingWatchStatus) {
                isEnabled = false
                text = "Loading..."
            } else {
                isEnabled = true
                text = if (isWatchingArtist) "Watching" else "Watch"
            }
        }
    }

    private fun setupCommentsRecyclerView() {
        commentsAdapter = CommentsAdapter(
            onReplyClick = { comment ->
                showReplyDialog(comment)
            },
            onLoadReplies = { comment ->
                loadReplies(comment)
            }
        )
        binding.commentsRecyclerView.adapter = commentsAdapter
    }

    private fun loadComments(deviationId: String, accessToken: String) {
        binding.commentsProgressBar.visibility = View.VISIBLE
        binding.noCommentsTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                commentsRepository.getDeviationComments(deviationId, accessToken).fold(
                    onSuccess = { commentsList ->
                        comments.clear()
                        comments.addAll(commentsList)
                        commentsAdapter.submitList(comments)

                        binding.commentsProgressBar.visibility = View.GONE
                        if (comments.isEmpty()) {
                            binding.noCommentsTextView.visibility = View.VISIBLE
                        } else {
                            binding.commentsHeaderTextView.text = "Comments (${comments.size})"
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error loading comments", error)
                        binding.commentsProgressBar.visibility = View.GONE
                        binding.noCommentsTextView.visibility = View.VISIBLE
                        Toast.makeText(this@DeviationDetailActivity, "Failed to load comments", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception loading comments", e)
                binding.commentsProgressBar.visibility = View.GONE
                binding.noCommentsTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun loadReplies(comment: Comment) {
        val accessToken = tokenManager.getAccessToken() ?: return

        // Toggle expanded state
        if (comment.isExpanded) {
            comment.isExpanded = false
            comment.repliesList = null
            commentsAdapter.updateComment(comment)
            return
        }

        // Load replies
        comment.isLoadingReplies = true
        commentsAdapter.updateComment(comment)

        lifecycleScope.launch {
            try {
                commentsRepository.getCommentReplies(comment.commentId, accessToken).fold(
                    onSuccess = { replies ->
                        comment.isLoadingReplies = false
                        comment.isExpanded = true
                        comment.repliesList = replies
                        commentsAdapter.updateComment(comment)
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error loading replies", error)
                        comment.isLoadingReplies = false
                        commentsAdapter.updateComment(comment)
                        Toast.makeText(this@DeviationDetailActivity, "Failed to load replies", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception loading replies", e)
                comment.isLoadingReplies = false
                commentsAdapter.updateComment(comment)
            }
        }
    }

    private fun showAddCommentDialog(parentComment: Comment? = null) {
        val input = android.widget.EditText(this)
        input.hint = if (parentComment != null) {
            "Reply to ${parentComment.user.username}"
        } else {
            "Write a comment..."
        }
        input.minLines = 3

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(if (parentComment != null) "Add Reply" else "Add Comment")
            .setView(input)
            .setPositiveButton("Post") { _, _ ->
                val commentText = input.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    postComment(commentText, parentComment)
                } else {
                    Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showReplyDialog(comment: Comment) {
        showAddCommentDialog(comment)
    }

    private fun postComment(commentText: String, parentComment: Comment? = null) {
        val currentDeviation = deviation ?: return
        val accessToken = tokenManager.getAccessToken() ?: return

        lifecycleScope.launch {
            try {
                commentsRepository.postComment(
                    deviationId = currentDeviation.deviationId,
                    commentText = commentText,
                    accessToken = accessToken,
                    parentCommentId = parentComment?.commentId
                ).fold(
                    onSuccess = { success ->
                        if (success) {
                            Toast.makeText(this@DeviationDetailActivity, "Comment posted!", Toast.LENGTH_SHORT).show()
                            // Reload comments to show the new one
                            loadComments(currentDeviation.deviationId, accessToken)
                        } else {
                            Toast.makeText(this@DeviationDetailActivity, "Failed to post comment", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error posting comment", error)
                        Toast.makeText(this@DeviationDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception posting comment", e)
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFavoriteFolderDialog() {
        val currentDeviation = deviation ?: return
        val accessToken = tokenManager.getAccessToken() ?: return

        // Check if already favorited
        if (isFavorited) {
            // Show unfavorite confirmation
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Remove from Favorites")
                .setMessage("Remove this deviation from your favorites?")
                .setPositiveButton("Remove") { _, _ ->
                    unfavoriteDeviation()
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        // Show loading dialog
        val loadingDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setMessage("Loading folders...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Load collection folders
        lifecycleScope.launch {
            try {
                collectionsRepository.getCollectionFolders(accessToken).fold(
                    onSuccess = { folders ->
                        loadingDialog.dismiss()

                        if (folders.isEmpty()) {
                            // No folders, show create folder dialog
                            showCreateFolderDialog()
                        } else {
                            // Show folder selection dialog
                            showFolderSelectionDialog(folders)
                        }
                    },
                    onFailure = { error ->
                        loadingDialog.dismiss()
                        android.util.Log.e("DeviationDetail", "Error loading folders", error)
                        Toast.makeText(this@DeviationDetailActivity, "Failed to load folders", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                loadingDialog.dismiss()
                android.util.Log.e("DeviationDetail", "Exception loading folders", e)
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFolderSelectionDialog(folders: List<com.bethwestsl.devistagram.model.CollectionFolder>) {
        val folderNames = folders.map { it.name }.toTypedArray()
        val folderNames2 = folderNames + "Create New Folder..."

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add to Collection")
            .setItems(folderNames2) { _, which ->
                if (which == folderNames2.size - 1) {
                    // Create new folder option
                    showCreateFolderDialog()
                } else {
                    // Selected a folder
                    val selectedFolder = folders[which]
                    favoriteDeviation(selectedFolder.folderId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreateFolderDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Folder name"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Create Collection Folder")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    createFolderAndFavorite(folderName)
                } else {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createFolderAndFavorite(folderName: String) {
        val accessToken = tokenManager.getAccessToken() ?: return

        lifecycleScope.launch {
            try {
                collectionsRepository.createCollectionFolder(folderName, accessToken).fold(
                    onSuccess = { folderId ->
                        Toast.makeText(this@DeviationDetailActivity, "Folder created!", Toast.LENGTH_SHORT).show()
                        favoriteDeviation(folderId)
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error creating folder", error)
                        Toast.makeText(this@DeviationDetailActivity, "Failed to create folder", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception creating folder", e)
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun favoriteDeviation(folderId: String? = null) {
        val currentDeviation = deviation ?: return
        val accessToken = tokenManager.getAccessToken() ?: return

        android.util.Log.d("DeviationDetail", "favoriteDeviation called with deviationId: '${currentDeviation.deviationId}', folderId: $folderId")

        lifecycleScope.launch {
            try {
                collectionsRepository.faveDeviation(
                    deviationId = currentDeviation.deviationId,
                    accessToken = accessToken,
                    folderId = folderId
                ).fold(
                    onSuccess = { success ->
                        if (success) {
                            isFavorited = true
                            updateFavoriteButton()
                            Toast.makeText(this@DeviationDetailActivity, "Added to favorites!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DeviationDetailActivity, "Failed to favorite", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error favoriting", error)
                        Toast.makeText(this@DeviationDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception favoriting", e)
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unfavoriteDeviation() {
        val currentDeviation = deviation ?: return
        val accessToken = tokenManager.getAccessToken() ?: return

        lifecycleScope.launch {
            try {
                collectionsRepository.unfaveDeviation(
                    deviationId = currentDeviation.deviationId,
                    accessToken = accessToken
                ).fold(
                    onSuccess = { success ->
                        if (success) {
                            isFavorited = false
                            updateFavoriteButton()
                            Toast.makeText(this@DeviationDetailActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@DeviationDetailActivity, "Failed to unfavorite", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("DeviationDetail", "Error unfavoriting", error)
                        Toast.makeText(this@DeviationDetailActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DeviationDetail", "Exception unfavoriting", e)
                Toast.makeText(this@DeviationDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteButton() {
        binding.favoriteButton.text = if (isFavorited) "Favorited" else "Favorite"
    }
}
