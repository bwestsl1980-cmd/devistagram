# Devistagram OAuth Setup - Quick Start Guide

## ✅ What's Been Created

### Core OAuth Files
1. **DeviantArtAuthConfig.kt** - Contains your Client ID, Secret, and OAuth URLs
2. **OAuthManager.kt** - Handles the entire OAuth flow
3. **TokenManager.kt** - Securely stores and manages access/refresh tokens
4. **TokenResponse.kt** - Data models for API responses
5. **DeviantArtAuthApi.kt** - Retrofit API interface
6. **RetrofitClient.kt** - HTTP client configuration

### UI Files
1. **LoginActivity.kt** - Login screen with "Login with DeviantArt" button
2. **activity_login.xml** - Login screen layout
3. **MainActivity.kt** - Main screen (shows after successful login)
4. **activity_main.xml** - Main screen layout with logout button

### Configuration
1. **AndroidManifest.xml** - Updated with:
   - Internet permission
   - LoginActivity as launcher
   - OAuth callback intent filter
2. **build.gradle.kts** - Added dependencies for:
   - Retrofit (networking)
   - OkHttp (HTTP client)
   - Coroutines (async operations)
   - Security Crypto (encrypted storage)
   - Browser (Custom Tabs)

## 🚀 How to Run

1. **Sync Gradle** - Open project in Android Studio and sync Gradle
2. **Build** - Build the project (Build > Make Project)
3. **Run** - Run on device or emulator
4. **Test Login** - Click "Login with DeviantArt" button

## 🔐 OAuth Flow Explained

```
1. User clicks "Login with DeviantArt"
   ↓
2. App opens DeviantArt in Chrome Custom Tab
   ↓
3. User logs in and authorizes app
   ↓
4. DeviantArt redirects to: com.scottapps.devistagram://oauth2callback?code=XXXXX
   ↓
5. App receives callback, extracts authorization code
   ↓
6. App exchanges code for access token + refresh token
   ↓
7. Tokens are encrypted and stored securely
   ↓
8. User is redirected to MainActivity
```

## 📱 User Experience

### First Time User
1. App opens to LoginActivity
2. Sees "Welcome to Devistagram" with login button
3. Clicks "Login with DeviantArt"
4. Browser opens with DeviantArt login
5. After authorization, returns to app
6. Sees "You're logged in!" message

### Returning User
1. App opens directly to MainActivity (if token still valid)
2. No need to login again
3. Can click "Logout" to clear session

## 🔧 Key Features

- ✅ **Secure Token Storage** - Encrypted with AES256
- ✅ **Token Expiry Handling** - Automatically checks if token expired
- ✅ **Refresh Token Support** - Can refresh expired access tokens
- ✅ **CSRF Protection** - Uses state parameter
- ✅ **Session Persistence** - Stays logged in between app restarts
- ✅ **Clean UI** - Material Design components

## 📋 DeviantArt App Settings

Make sure your DeviantArt app is configured with:

**OAuth2 Redirect URI Whitelist:**
```
com.scottapps.devistagram://oauth2callback http://localhost
```

**OAuth2 Grant Type:**
```
Authorization Code
```

**Download URL:**
```
https://github.com/ScottMcGinn/devistagram
```

**Original URLs Whitelist:**
```
http://localhost com.scottapps.devistagram://
```

## 🐛 Common Issues & Solutions

### Issue: Build fails with "version = release(36)" error
**Solution:** Update to `compileSdk = 36` instead

### Issue: Login button doesn't do anything
**Solution:** 
- Check logcat for errors
- Ensure internet permission is in manifest
- Verify Chrome is installed on device

### Issue: After login, stays on DeviantArt page
**Solution:**
- Check intent filter in AndroidManifest.xml
- Verify redirect URI matches exactly: `com.scottapps.devistagram://oauth2callback`

### Issue: "Invalid redirect_uri" error
**Solution:**
- Go to DeviantArt app settings
- Make sure redirect URI is whitelisted EXACTLY as shown above

### Issue: Tokens not persisting
**Solution:**
- Check that Security Crypto library is properly imported
- Verify app has storage permissions

## 🎯 Next Steps

Now that OAuth is working, you can:

1. **Add API calls** - Use the access token to fetch DeviantArt content
2. **Build UI** - Create views for browsing deviations
3. **Add features** - Search, favorites, user profiles, etc.

## 💡 Using the Access Token

Once logged in, you can use the token like this:

```kotlin
val tokenManager = TokenManager(context)
val accessToken = tokenManager.getAccessToken()

// Use in API calls
val authHeader = "Bearer $accessToken"
```

## 📞 Support

If you run into issues:
1. Check the README.md for detailed documentation
2. Look at logcat output for error messages
3. Verify DeviantArt app settings match exactly

---

**Status:** OAuth MVP Complete ✅  
**Ready for:** API integration and content features
