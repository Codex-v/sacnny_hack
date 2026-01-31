# HackDefence Scanner App

Android app for scanning QR codes and verifying participant entry at HackDefence Summit 2026.

## Features

- **Scanner Login**: Staff login using scanner code from admin panel
- **QR Code Scanning**: Real-time QR code scanning using ML Kit
- **Entry Verification**: Verify participant entry with backend validation
- **Activity Tracking**: Track successful scans and login activity
- **Offline Detection**: Detect payment verification status
- **Duplicate Detection**: Prevent duplicate entries

## Setup Instructions

### 1. Update Backend URL

Edit `scanner_app/app/src/main/java/com/hackdefence/scanner/api/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_IP_ADDRESS:5000/"
```

**For Android Emulator:**
- Use `http://10.0.2.2:5000/` to connect to localhost on your PC

**For Physical Device (same network):**
- Find your PC's IP address: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
- Use `http://YOUR_PC_IP:5000/` (e.g., `http://192.168.1.100:5000/`)

### 2. Build and Run

1. Open project in Android Studio
2. Sync Gradle files
3. Connect Android device or start emulator
4. Run the app (Shift + F10)

### 3. Get Scanner Code

1. Open admin panel at `http://localhost:5173`
2. Navigate to "Scanners" section
3. Create a new scanner with code (e.g., `SCAN0001`)
4. Use this code to login in the app

## Usage

### Login
1. Open the app
2. Enter scanner code (from admin panel)
3. Tap "Login"

### Scanning QR Codes
1. After login, camera opens automatically
2. Point camera at QR code on participant's ticket
3. App automatically scans and verifies
4. Results shown immediately:
   - **Green**: Entry verified successfully ✓
   - **Orange**: Already entered (duplicate) ⚠
   - **Red**: Payment not verified or error ✗

### Logout
- Tap the logout icon in top-right corner

## Backend API Endpoints

### Scanner Login
```
POST /api/scanner/login
Body: { "scanner_code": "SCAN0001" }
```

### Entry Verification
```
POST /api/admin/entry/verify
Body: {
  "qr_data": "encrypted_qr_data",
  "verified_by": "SCAN0001"
}
```

## Permissions Required

- **Camera**: For QR code scanning
- **Internet**: For backend API communication

## Tech Stack

- **Kotlin**
- **Jetpack Compose** - Modern UI
- **CameraX** - Camera functionality
- **ML Kit** - Barcode/QR scanning
- **Retrofit** - API communication
- **DataStore** - Local preferences storage
- **Navigation Compose** - Screen navigation

## Troubleshooting

### Camera not working
- Grant camera permission in app settings
- Check device has working camera

### Connection error
- Verify backend server is running (`npm start` or `go run cmd/main.go`)
- Check BASE_URL is correct in RetrofitClient.kt
- Ensure device and PC are on same network (for physical devices)
- Check firewall settings allow port 5000

### Scanner code invalid
- Verify scanner code exists in admin panel
- Check scanner is marked as "Active"
- Scanner code is case-sensitive

## Development

### Dependencies
All dependencies are managed in `app/build.gradle.kts`:
- CameraX
- ML Kit Barcode Scanning
- Retrofit
- Accompanist Permissions
- DataStore

### Package Structure
```
com.hackdefence.scanner/
├── data/           # Data models
├── api/            # API service and Retrofit client
├── viewmodel/      # ViewModels for business logic
├── ui/
│   ├── screens/    # Composable screens
│   ├── navigation/ # Navigation setup
│   └── theme/      # Material3 theme
└── utils/          # Utilities (PreferencesManager)
```

## Support

For issues or questions, contact the development team.
