# Devistagram

<p align="center">
  <strong>A modern, clean DeviantArt image viewer for Android</strong>
</p>

## Overview

Devistagram is a native Android application that provides a streamlined, Instagram-like interface for browsing DeviantArt artwork. Built with Kotlin, it focuses on delivering an optimized image viewing experience with robust filtering and organization features.

**Package Name:** `com.bethwestsl.devistagram`  
**Author:** Beth West  
**Status:** Ready for Test  
**Minimum Android Version:** 7.0 (API 24)  
**Target Android Version:** 14 (API 36)

---

## 🎯 Key Features at a Glance

- ⭐ **Favorite Artists** - Mark your favorite artists and view only their content
- 🛡️ **Advanced Filtering** - Safe mode, artist blocking, and favorites-only feed
- 🔔 **Organized Notifications** - Separate tabs for Comments, Feedback, and Mentions
- 🖼️ **Multiple View Modes** - Grid and list layouts for different content types
- 💾 **Persistent Settings** - All your preferences and favorites are saved
- 🔐 **Secure OAuth** - Industry-standard authentication with encrypted token storage

---

## ✨ Features

### 🎨 Content Browsing
- **Feed Tab** - Browse your personalized DeviantArt feed with images from watched artists
- **Discover Tab** - Explore popular and trending artwork
- **Tagged Tab** - View deviations where you've been tagged
- **Search by Tag** - Find artwork by specific tags
- **Search by User** - Search and browse other users' profiles

### 👤 Profile & User Management
- **User Profile** - View your own profile with statistics and galleries
- **Other User Profiles** - Browse other users' profiles, galleries, and collections
- **Watch/Unwatch Users** - Follow and unfollow artists from their profile page
- **Gallery Browsing** - View user galleries with folder organization
- **Collections** - Browse and organize your favorite collections
- **Collection Detail View** - View full collection contents in grid layout

### 🖼️ Image Viewing
- **Full-Screen Deviation Detail** - Tap any image to view in full screen with zoom support
- **Image Zoom** - Pinch-to-zoom on full-resolution images
- **Metadata Display** - View titles, descriptions, artists, and statistics
- **Download Images** - Save deviations to your device
- **Grid & List Views** - Multiple viewing layouts for galleries and collections

### 💬 Social Features
- **Comments** - View and post comments on deviations
- **Favorites** - Add/remove deviations from your favorites
- **Comment Notifications** - Get notified of new comments on your deviations
- **Feedback Notifications** - Receive feedback and critique notifications
- **Mentions Notifications** - See when you're mentioned in comments
- **Organized Notification Tabs** - Separate tabs for Comments, Feedback, and Mentions

### 📬 Messaging (Legacy)
- **Notes/Mail** - View and read DeviantArt notes (deprecated by DeviantArt, but still accessible)
- **Note Detail View** - Read full note conversations
- **⚠️ Note:** DeviantArt has deprecated the Notes feature in favor of their Chat system

### ⭐ Favorites & Blocking System
- **Favorite Artists** - Mark specific artists as favorites using the ⭐ icon on their profile
- **Favorites-Only Feed** - Toggle "Show Favorites Only" to view content exclusively from favorited artists
- **Artist Blocking** - Block specific artists to hide their content from your feed
- **Persistent Filters** - All favorite and block settings are saved and persist across app restarts
- **Visual Indicators** - Filled star (⭐) shows favorited artists, outlined star shows non-favorited

### 🛡️ Content Filtering
- **Safe Mode** - Filter out mature/adult content from all feeds with toggle switch
- **Favorites Filter** - Show only content from your favorited artists
- **Artist Blocklist** - Blocked artists' content is automatically hidden from all feeds
- **Smart Filtering** - Filters apply instantly without requiring page refresh
- **Filter Persistence** - All filter settings are saved between sessions

### ⚙️ Additional Features
- **OAuth 2.0 Authentication** - Secure login with DeviantArt
- **Dark/Light Theme** - System-responsive theme support
- **Persistent Session** - Stay logged in across app restarts
- **Pull-to-Refresh** - Refresh content on all tabs
- **Infinite Scroll** - Seamless content loading (disabled when viewing favorites-only feed)
- **Default Gallery Selection** - Set preferred gallery for profile view
- **Optimized Pagination** - Smart content loading prevents duplicate data when scrolling

---

## 🚫 Known Limitations

### Content Type Restrictions
This app is designed as an **image viewer only**. The following content types are **not supported**:
- ❌ **Blogs** - Not displayed
- ❌ **Stories** - Not displayed
- ❌ **Journals** - Not displayed
- ❌ **Literature** - Not displayed
- ❌ **Videos** - Not displayed

### API Limitations
- ❌ **No Unread Notification Badges** - The DeviantArt API does not provide unread counts, so notifications cannot show badge indicators
- ❌ **No Chat Support** - Chat feature is not available (DeviantArt's newer messaging system is not supported by their public API)

---

## 🏗️ Project Structure

```
app/src/main/java/com/bethwestsl/devistagram/
├── adapter/              # RecyclerView adapters
├── auth/                 # OAuth authentication
│   ├── DeviantArtAuthConfig.kt
│   ├── OAuthManager.kt
│   └── TokenManager.kt
├── fragment/             # Main app fragments
│   ├── FeedFragment.kt
│   ├── DiscoverFragment.kt
│   ├── TaggedFragment.kt
│   ├── NotificationsFragment.kt
│   ├── CommentsNotificationsFragment.kt
│   ├── FeedbackNotificationsFragment.kt
│   ├── MentionsNotificationsFragment.kt
│   ├── ProfileFragment.kt
│   ├── SearchTagsFragment.kt
│   └── SearchUsersFragment.kt
├── model/                # Data models
├── network/              # Retrofit API interfaces
│   ├── DeviantArtApi.kt
│   ├── DeviantArtAuthApi.kt
│   └── RetrofitClient.kt
├── repository/           # Data repositories
├── util/                 # Utility classes
│   └── ArtistFilterManager.kt
├── viewmodel/            # MVVM ViewModels
├── LoginActivity.kt
├── MainActivity.kt
├── DeviationDetailActivity.kt
├── OtherUserProfileActivity.kt
├── CollectionDetailActivity.kt
├── NotesActivity.kt
└── NoteDetailActivity.kt
```

---

## 🔐 Security & Configuration

### Secure Credentials
OAuth credentials are stored securely in `local.properties` (not committed to version control):
- Client ID and Secret are read at build time via BuildConfig
- See `CREDENTIALS_SETUP.md` for setup instructions

### Authentication
- **OAuth 2.0** with authorization code grant flow
- **Encrypted token storage** using Android EncryptedSharedPreferences
- **Automatic token refresh** when access tokens expire
- **Secure redirect URI:** `com.bethwestsl.devistagram://oauth2callback`

### Scopes Requested
- `browse` - Browse public content
- `message` - Access notes/messages
- `note` - Read and send notes
- `user` - Access user profile
- `collection` - Manage collections
- `comment.post` - Post comments
- `user.manage` - Watch/unwatch users

---

## 🛠️ Technology Stack

### Core
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 14)

### Key Libraries
- **Retrofit 2.9.0** - HTTP client for API calls
- **Gson** - JSON serialization
- **OkHttp 4.12.0** - HTTP networking
- **Kotlin Coroutines 1.7.3** - Asynchronous operations
- **AndroidX Lifecycle** - ViewModel and LiveData
- **Material Design 3** - Modern UI components
- **Coil 2.5.0** - Image loading and caching
- **PhotoView 2.3.0** - Image zoom functionality
- **Chrome Custom Tabs** - OAuth browser integration
- **Security Crypto** - Encrypted SharedPreferences

---

## 📱 Installation & Setup

### Prerequisites
1. Android Studio (latest version recommended)
2. Android SDK with API 24-36
3. DeviantArt Developer Account

### Build Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/bwestsl1980-cmd/devistagram.git
   ```

2. Set up credentials in `local.properties`:
   ```properties
   DEVIANTART_CLIENT_ID=your_client_id
   DEVIANTART_CLIENT_SECRET=your_client_secret
   ```

3. Configure your DeviantArt OAuth app:
   - Redirect URI: `com.bethwestsl.devistagram://oauth2callback`
   - Grant type: Authorization Code

4. Sync Gradle and build the project

5. Run on device or emulator

For detailed setup instructions, see `BUILD_INSTRUCTIONS.md` and `CREDENTIALS_SETUP.md`.

---

## 📚 Documentation

Additional documentation files:
- `CREDENTIALS_SETUP.md` - How credentials are securely stored
- `BUILD_INSTRUCTIONS.md` - Detailed build setup
- `QUICK_START.md` - Quick start guide
- Implementation docs for specific features:
  - `FEED_IMPLEMENTATION.md` - Feed functionality
  - `COMMENTS_IMPLEMENTATION.md` - Comments system
  - `NOTIFICATIONS_TAB_REFACTOR.md` - Notifications organization
  - `ARTIST_FILTER_FEATURE.md` - Blocking and filtering artists
  - `SAFE_MODE_FEATURE.md` - Safe mode content filtering
  - `FAVORITES_FILTER_FIX.md` - Favorites-only feed implementation
  - And more...

---

## 🌟 Using Favorites & Blocking

### How to Favorite an Artist
1. Navigate to any user's profile (tap their name anywhere in the app)
2. Look for the **star icon (⭐)** in the top-right corner of the toolbar
3. Tap the star to favorite/unfavorite the artist
   - **Filled star** = Artist is favorited
   - **Outlined star** = Artist is not favorited
4. Favorited artists are saved automatically

### How to View Favorites-Only Feed
1. Go to the **Feed** tab
2. Toggle the **"Show Favorites Only"** switch at the top
3. The feed will refresh and show only content from your favorited artists (4 latest deviations per artist)
4. This setting persists - it will stay enabled even after closing the app

### How to Block an Artist
1. Navigate to the artist's profile you want to block
2. Tap the **menu** (three dots) or **block option**
3. Confirm the block action
4. Blocked artists' content will no longer appear in any feed

### Filter Features
- **Safe Mode**: Toggle on to hide all mature/adult content
- **Favorites Filter**: When enabled, ONLY favorited artists' content is shown
- **Pagination**: Automatically disabled when viewing favorites-only feed (shows all content at once)

---

## 🐛 Known Issues & Troubleshooting

### Authentication Issues
- **Login redirects to wrong account?** - Clear app data and re-login
- **OAuth fails?** - Verify redirect URI matches exactly in DeviantArt app settings

### Content Display
- **Images not loading?** - Check internet connection and API rate limits
- **Empty feed?** - Make sure you're watching artists on DeviantArt
- **Favorites feed empty?** - You need to favorite at least one artist using the ⭐ icon on their profile
- **Still seeing blocked content?** - Try refreshing the feed (pull down to refresh)

### Filters
- **Safe Mode not working?** - Toggle it off and on again, then refresh the feed
- **Favorites filter showing wrong content?** - Make sure the toggle is ON and you have favorited artists
- **Star icon not visible on profile?** - The star is in the top-right corner of the toolbar (you may need to clean build the app)

### Notifications
- **No unread badges?** - This is a limitation of the DeviantArt API (read counts not provided)

---

## 🤝 Contributing

This is a personal project in testing phase. Bug reports and suggestions are welcome!

---

## 📄 License

**Note:** This is an independent client and is not officially affiliated with or endorsed by DeviantArt.

---

## 🙏 Acknowledgments

- **DeviantArt** for providing the public API
- **PhotoView** library by Chris Banes
- **Coil** image loading library
- All open-source contributors whose libraries made this possible

---

**Last Updated:** January 12, 2026  
**Version:** 1.0  
**Ready for Test** ✅
