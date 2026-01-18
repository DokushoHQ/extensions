# Dokusho Extensions

Tachiyomi/Mihon extension for [Dokusho](https://github.com/dokushohq) - a self-hosted manga aggregation server.

## Installation

Add this repository to Tachiyomi/Mihon:

```
https://dokushohq.github.io/extensions
```

1. Open Tachiyomi/Mihon
2. Go to **Browse** > **Extension repos** (in the menu)
3. Tap **Add** and enter the URL above
4. Find "Dokusho" in the extension list and install it

## Configuration

After installing the extension:

1. Go to extension settings (long press on Dokusho > Settings)
2. Enter your **Server address** (e.g., `https://dokusho.example.com`)
3. Enter your **API key** (generate one in Dokusho dashboard > Account > API Keys)

## Building

### Requirements

- JDK 17
- Android SDK

### Build Commands

```bash
# Build debug APK
./gradlew :src:all:dokusho:assembleDebug

# Build release APK (requires signing key)
./gradlew :src:all:dokusho:assembleRelease
```

### Signing

For release builds, you need to set up signing:

1. Create a keystore file named `signingkey.jks`
2. Set environment variables:
   - `KEY_STORE_PASSWORD` - Keystore password
   - `KEY_PASSWORD` - Key password
   - `ALIAS` - Key alias

## Development

The extension is located in `src/all/dokusho/`.

### Structure

```
src/all/dokusho/
├── build.gradle           # Extension build config
├── AndroidManifest.xml    # Android manifest
├── res/
│   └── mipmap-xxxhdpi/
│       └── ic_launcher.png  # Extension icon
└── src/eu/kanade/tachiyomi/extension/all/dokusho/
    ├── Dokusho.kt         # Main source implementation
    ├── DokushoUtils.kt    # Utility functions
    └── dto/
        └── Dto.kt         # Data transfer objects
```

## API Endpoints Used

The extension uses the following Dokusho API endpoints:

- `GET /api/v1/serie` - List series (with pagination and search)
- `GET /api/v1/serie/{id}` - Get series details
- `GET /api/v1/serie/{id}/chapters` - Get chapter list
- `GET /api/v1/serie/{id}/chapters/{chapterId}/data` - Get chapter pages

## License

This project follows the Tachiyomi extension licensing.
