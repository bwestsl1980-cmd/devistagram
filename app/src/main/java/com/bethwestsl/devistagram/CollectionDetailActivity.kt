package com.bethwestsl.devistagram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bethwestsl.devistagram.adapter.DeviationGridAdapter
import com.bethwestsl.devistagram.auth.TokenManager
import com.bethwestsl.devistagram.databinding.ActivityCollectionDetailBinding
import com.bethwestsl.devistagram.network.RetrofitClient
import kotlinx.coroutines.launch

class CollectionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollectionDetailBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var deviationsAdapter: DeviationGridAdapter
    private var currentUsername: String? = null
    private var currentFolderId: String? = null
    private var currentFolderName: String? = null

    companion object {
        private const val EXTRA_USERNAME = "username"
        private const val EXTRA_FOLDER_ID = "folder_id"
        private const val EXTRA_FOLDER_NAME = "folder_name"

        fun start(context: Context, username: String, folderId: String, folderName: String? = null) {
            val intent = Intent(context, CollectionDetailActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
                putExtra(EXTRA_FOLDER_ID, folderId)
                putExtra(EXTRA_FOLDER_NAME, folderName)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        val folderId = intent.getStringExtra(EXTRA_FOLDER_ID)
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME)

        if (username == null || folderId == null) {
            Toast.makeText(this, "Error: Invalid collection data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUsername = username
        currentFolderId = folderId
        currentFolderName = folderName

        tokenManager = TokenManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            layoutParams.height = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material) + insets.top
            v.layoutParams = layoutParams
            v.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        setupToolbar()
        setupRecyclerView()
        loadCollectionContent()
    }

    private fun setupToolbar() {
        binding.toolbar.title = currentFolderName ?: "Collection"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        deviationsAdapter = DeviationGridAdapter { deviation ->
            DeviationDetailActivity.start(this, deviation)
        }

        binding.deviationsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@CollectionDetailActivity, 3)
            adapter = deviationsAdapter
        }
    }

    private fun loadCollectionContent() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val token = tokenManager.getAccessToken()
                if (token == null) {
                    showError("Not authenticated")
                    return@launch
                }

                val response = RetrofitClient.contentApi.getCollectionFolder(
                    folderId = currentFolderId!!,
                    authorization = "Bearer $token",
                    username = currentUsername!!,
                    matureContent = true
                )

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val deviations = response.body()?.results ?: emptyList<com.bethwestsl.devistagram.model.Deviation>()
                    if (deviations.isNotEmpty()) {
                        deviationsAdapter.submitList(deviations)
                        binding.deviationsRecyclerView.visibility = View.VISIBLE
                        binding.errorTextView.visibility = View.GONE
                    } else {
                        binding.deviationsRecyclerView.visibility = View.GONE
                        binding.errorTextView.text = "This collection is empty"
                        binding.errorTextView.visibility = View.VISIBLE
                    }
                } else {
                    showError("Failed to load collection: ${response.code()}")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showError("Error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.visibility = View.VISIBLE
        binding.deviationsRecyclerView.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

