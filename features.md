# FluidPlay: clean-room feature map

Source: the public Play Store listing of a motion-smoothing Android video player.
Only functionality and UX patterns are reproduced. No code, engine, assets, name or copy from the original.

| # | Feature | Priority | Plan |
|---|---------|----------|------|
| 1 | Play local video (all common formats), hardware decoding | Must | libmpv via an open-source Android wrapper |
| 2 | Open from file browser (system picker) | Must | Storage Access Framework, so the user chooses what appears |
| 3 | Open a network URL (http/https/m3u8) | Must | `loadfile <url>` |
| 4 | Smooth-motion playback (frame interpolation) | Must | mpv built-in `interpolation` + `video-sync=display-resample` + `tscale` (free, open source) |
| 5 | Smoothness presets: Off / Light / Balanced / Strong | Must | Map to mpv `tscale` options |
| 6 | HW decoder toggle | Should | `hwdec` = `mediacodec` or `no` |
| 7 | Downscale cap (1080p / 720p) for weak devices | Should | `vf=scale` or `vd-lavc-*` option |
| 8 | Basic controls: play/pause, seek, speed, tap-to-toggle UI | Must | Compose overlay |
| 9 | Recent files | Could | DataStore |
| 10 | Streaming from WebDAV / SMB / DLNA / Plex | Out of scope v1 | Network-share protocols are a separate project |
| 11 | Streaming from a desktop SVP app | Out of scope | Belongs to the original vendor's ecosystem |

Honest note on "smoothing": the original uses a dedicated motion-estimation engine (true MEMC).
mpv's built-in interpolation is frame blending, which is free and open but looks different:
less sharp than true motion-compensated interpolation, with some blur on fast motion.
