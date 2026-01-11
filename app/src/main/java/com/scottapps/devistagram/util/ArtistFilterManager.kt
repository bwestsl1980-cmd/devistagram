package com.scottapps.devistagram.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ArtistFilterManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("artist_filters", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_FAVORITES = "favorite_artists"
        private const val KEY_BLOCKED = "blocked_artists"
        private const val KEY_FILTER_ENABLED = "favorites_filter_enabled"
        private const val KEY_SAFE_MODE = "safe_mode_enabled"
    }
    
    // Get favorite artists
    fun getFavoriteArtists(): Set<String> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }
    
    // Get blocked artists
    fun getBlockedArtists(): Set<String> {
        val json = prefs.getString(KEY_BLOCKED, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type) ?: emptySet()
    }
    
    // Toggle favorite artist
    fun toggleFavorite(username: String): Boolean {
        val favorites = getFavoriteArtists().toMutableSet()
        val added = if (favorites.contains(username)) {
            favorites.remove(username)
            false
        } else {
            favorites.add(username)
            true
        }
        saveFavorites(favorites)
        return added
    }
    
    // Toggle blocked artist
    fun toggleBlocked(username: String): Boolean {
        val blocked = getBlockedArtists().toMutableSet()
        val added = if (blocked.contains(username)) {
            blocked.remove(username)
            false
        } else {
            blocked.add(username)
            true
        }
        saveBlocked(blocked)
        return added
    }
    
    // Check if artist is favorited
    fun isFavorite(username: String): Boolean {
        return getFavoriteArtists().contains(username)
    }
    
    // Check if artist is blocked
    fun isBlocked(username: String): Boolean {
        return getBlockedArtists().contains(username)
    }
    
    // Save favorites
    private fun saveFavorites(favorites: Set<String>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }
    
    // Save blocked
    private fun saveBlocked(blocked: Set<String>) {
        val json = gson.toJson(blocked)
        prefs.edit().putString(KEY_BLOCKED, json).apply()
    }
    
    // Get filter enabled state
    fun isFavoritesFilterEnabled(): Boolean {
        return prefs.getBoolean(KEY_FILTER_ENABLED, false)
    }
    
    // Set filter enabled state
    fun setFavoritesFilterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FILTER_ENABLED, enabled).apply()
    }
    
    // Get safe mode state
    fun isSafeModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_SAFE_MODE, false)
    }
    
    // Set safe mode state
    fun setSafeModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAFE_MODE, enabled).apply()
    }
}
