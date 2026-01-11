package com.scottapps.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.scottapps.devistagram.auth.TokenManager
import com.scottapps.devistagram.databinding.ActivityDeviationDetailBinding
import com.scottapps.devistagram.model.Deviation
import com.scottapps.devistagram.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviationDetailBinding
    private lateinit var tokenManager: TokenManager
    private var deviation: Deviation? = null

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

        tokenManager = TokenManager(this)

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
            Toast.makeText(this, "Favorite - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.shareButton.setOnClickListener {
            deviation?.url?.let { url ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }

        binding.followButton.setOnClickListener {
            Toast.makeText(this, "Watch Artist - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.authorAvatarImageView.setOnClickListener {
            // TODO: Navigate to author profile
            Toast.makeText(this, "View Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.authorNameTextView.setOnClickListener {
            // TODO: Navigate to author profile
            Toast.makeText(this, "View Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadDeviationDetails(deviationId: String) {
        binding.progressBar.visibility = View.VISIBLE

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

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val deviationDetail = response.body()!!.deviation
                    if (deviationDetail != null) {
                        deviation = deviationDetail
                        displayDeviation(deviationDetail)
                    } else {
                        Toast.makeText(
                            this@DeviationDetailActivity,
                            "Deviation not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@DeviationDetailActivity,
                        "Failed to load deviation",
                        Toast.LENGTH_SHORT
                    ).show()
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

            // Description
            val description = deviation.description ?: "No description available"
            descriptionTextView.text = description

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
}
