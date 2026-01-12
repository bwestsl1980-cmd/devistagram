package com.bethwestsl.devistagram

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bethwestsl.devistagram.auth.OAuthManager
import com.bethwestsl.devistagram.databinding.ActivityMainNewBinding
import com.bethwestsl.devistagram.fragment.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainNewBinding
    private lateinit var oAuthManager: OAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore saved theme preference
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val nightMode = prefs.getInt("night_mode", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets to bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, insets.bottom)
            windowInsets
        }

        oAuthManager = OAuthManager(this)

        // Check if logged in
        if (!oAuthManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // Set up bottom navigation
        setupBottomNavigation()

        // Load initial fragment
        if (savedInstanceState == null) {
            // Check if there's a saved tab selection
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val savedTab = prefs.getInt("selected_tab", R.id.navigation_feed)
            binding.bottomNavigation.selectedItemId = savedTab
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_feed -> FeedFragment()
                R.id.navigation_discover -> DiscoverFragment()
                R.id.navigation_tagged -> TaggedFragment()
                R.id.navigation_notifications -> NotificationsFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            
            // Save the selected tab
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putInt("selected_tab", item.itemId)
                .apply()
            
            loadFragment(fragment)
            true
        }

        // Don't set default here - it's handled in onCreate
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
