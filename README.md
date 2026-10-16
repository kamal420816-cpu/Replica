# FluidPlay

A free Android video player with smooth-motion playback, built clean-room from the public
feature description of a similar app. No code, engine, assets, name or copy from that app.

## Features (v0.1)
- Open local videos (system picker) or a network URL (http/https/m3u8)
- "Open with" support for video files from other apps
- Smoothness presets: Off / Light / Balanced / Strong (mpv built-in frame interpolation)
- Hardware decoding toggle, optional 1080p/720p cap for weak devices
- Play/pause, seek +/-10s, speed control

## Build
1. Install Android Studio (Koala or newer) with Android SDK 34.
2. Open the `FluidPlay` folder as a project and let Gradle sync.
3. Run on a device (Android 8.0+, real device recommended; libmpv's GPU output is unreliable on emulators).

## Things to verify before first run
- `app/build.gradle.kts` uses `dev.jdtech.mpv:libmpv:0.5.1`. Check Maven Central for the current
  version and API. All library calls live in `MpvEngine.kt`, so adapting is one file.
- This project was written without being compiled (no Android SDK in the authoring environment),
  so expect to fix small compile errors on first sync.
- Change `applicationId` / `namespace` (`com.example.fluidplay`) to your own before publishing.

## Limits
- mpv interpolation is frame blending, not true motion-compensated interpolation. It is free
  and smooth, but softer on fast motion than dedicated MEMC engines.
- Not included: WebDAV/SMB/DLNA/Plex browsing, desktop-streaming, recent files.
- mpv is GPL/LGPL: check license obligations (source offer, notices) before distributing.
