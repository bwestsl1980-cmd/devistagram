# Devistagram - OAuth Implementation

## Overview
This is the initial OAuth 2.0 authentication implementation for the Devistagram app - a DeviantArt content viewer for Android.

## What's Implemented

### OAuth 2.0 Flow
- **Authorization Code Grant** - Full OAuth flow with refresh tokens
- **Custom Tab Integration** - Opens DeviantArt authorization in an in-app browser
- **Secure Token Storage** - Uses EncryptedSharedPreferences for storing access/refresh tokens
- **Automatic Token Expiry** - Tracks token expiration and handles refresh

### Features
✅ Login with DeviantArt  
✅ Secure token storage  
✅ OAuth callback handling  
✅ Token refresh capability  
✅ Logout functionality  
✅ Session persistence  

## Project Structure

```
app/src/main/java/com/scottapps/devistagram/
├── auth/
│   ├── DeviantArtAuthConfig.kt    # OAuth configuration
│   ├── OAuthManager.kt             # Handles OAuth flow
│   └── TokenManager.kt             # Secure token storage
├── model/
│   └── TokenResponse.kt            # API response models
├── network/
│   ├── DeviantArtAuthApi.kt        # Retrofit API interface
│   └── RetrofitClient.kt           # HTTP client setup
├── LoginActivity.kt                # Login screen
└── MainActivity.kt                 # Main app (placeholder)
```

## How It Works

### 1. User clicks "Login with DeviantArt"
- App generates a unique state parameter for CSRF protection
- Opens DeviantArt authorization URL in Chrome Custom Tab
- User logs in and authorizes the app

### 2. DeviantArt redirects back to app
- Redirect URI: `com.scottapps.devistagram://oauth2callback`
- App receives authorization code and state
- State is verified to prevent CSRF attacks

### 3. Exchange code for tokens
- App sends authorization code to DeviantArt's token endpoint
- Receives:
  - Access token (expires in 1 hour)
  - Refresh token (expires in 3 months)
- Tokens are encrypted and stored securely

### 4. Session management
- On app restart, checks if access token exists and isn't expired
- If expired, can use refresh token to get new access token
- If no valid token, redirects to login

## API Configuration

**DeviantArt Credentials:**
- Client ID: `59601`
- Client Secret: `ccd16fd197588957c0bd74939057fa9d`
- Redirect URI: `com.scottapps.devistagram://oauth2callback`
- Scope: `browse` (basic public access)

**Endpoints:**
- Authorization: `https://www.deviantart.com/oauth2/authorize`
- Token: `https://www.deviantart.com/oauth2/token`

## How to Test

1. **Build and install** the app on your device/emulator
2. **Click "Login with DeviantArt"**
3. **Authorize** the app in the browser
4. **You'll be redirected back** to the app
5. **Success!** You should see "You're logged in!"

## Next Steps

- [ ] Implement DeviantArt API calls (browse deviations, user profile, etc.)
- [ ] Add proper UI for viewing content
- [ ] Implement image loading and caching
- [ ] Add search functionality
- [ ] Add favorites/collections
- [ ] Improve error handling and user feedback

## Dependencies

```kotlin
// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Browser
implementation("androidx.browser:browser:1.7.0")
```

## Security Notes

- Tokens are encrypted using AES256_GCM encryption
- Client secret is included in app (note: this is normal for mobile apps, but keep the APK secure)
- State parameter prevents CSRF attacks
- HTTPS is used for all network communication

## Troubleshooting

**Login button does nothing:**
- Check logcat for errors
- Ensure internet permission is granted
- Verify redirect URI matches DeviantArt app settings

**Redirect doesn't work:**
- Check AndroidManifest.xml has correct intent filter
- Verify redirect URI: `com.scottapps.devistagram://oauth2callback`
- Ensure activity has `launchMode="singleTask"`

**Token exchange fails:**
- Check Client ID and Secret are correct
- Verify network connectivity
- Check logcat for API error responses

---

**Author:** Scott McGinn  
**Date:** January 2026  
**Status:** MVP OAuth Implementation Complete ✅
