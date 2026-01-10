package com.scottapps.devistagram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.scottapps.devistagram.auth.OAuthManager
import com.scottapps.devistagram.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var oAuthManager: OAuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        oAuthManager = OAuthManager(this)
        
        // Check if already logged in
        if (oAuthManager.isLoggedIn()) {
            navigateToMain()
            return
        }
        
        setupViews()
    }
    
    private fun setupViews() {
        binding.loginButton.setOnClickListener {
            startLogin()
        }
    }
    
    private fun startLogin() {
        try {
            oAuthManager.startOAuthFlow()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting login: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthCallback(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Handle callback if this is a redirect
        if (intent?.data != null) {
            handleAuthCallback(intent)
        }
    }
    
    private fun handleAuthCallback(intent: Intent?) {
        val uri = intent?.data ?: return
        
        if (uri.scheme == "com.scottapps.devistagram" && uri.host == "oauth2callback") {
            showLoading(true)
            
            lifecycleScope.launch {
                val result = oAuthManager.handleAuthCallback(uri)
                
                showLoading(false)
                
                result.onSuccess {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMain()
                }.onFailure { error ->
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
