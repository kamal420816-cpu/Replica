package com.example.fluidplay

import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    private lateinit var engine: MpvEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = MpvEngine(applicationContext)

        // Video opened from another app ("Open with")
        val incoming: Uri? = intent?.data

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    FluidPlayApp(engine, incoming?.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        engine.destroy()
        super.onDestroy()
    }
}

@Composable
fun FluidPlayApp(engine: MpvEngine, initialUri: String?) {
    var settings by remember { mutableStateOf(PlayerSettings()) }
    var current by remember { mutableStateOf(initialUri) }
    var showSettings by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { current = it.toString() }
    }

    if (current == null) {
        HomeScreen(
            onOpenFile = { picker.launch(arrayOf("video/*")) },
            onOpenUrl = { showUrlDialog = true }
        )
    } else {
        PlayerScreen(
            engine = engine,
            source = current!!,
            settings = settings,
            onSettings = { showSettings = true },
            onClose = { current = null }
        )
    }

    if (showSettings) {
        SettingsDialog(
            settings = settings,
            onChange = { settings = it; engine.update(it) },
            onDismiss = { showSettings = false }
        )
    }
    if (showUrlDialog) {
        UrlDialog(
            onConfirm = { current = it; showUrlDialog = false },
            onDismiss = { showUrlDialog = false }
        )
    }
}

@Composable
fun HomeScreen(onOpenFile: () -> Unit, onOpenUrl: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FluidPlay", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Smooth video playback, free.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onOpenFile, Modifier.fillMaxWidth()) { Text("Open video file") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenUrl, Modifier.fillMaxWidth()) { Text("Open network URL") }
    }
}

@Composable
fun PlayerScreen(
    engine: MpvEngine,
    source: String,
    settings: PlayerSettings,
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(1.0) }

    Box(Modifier.fillMaxSize().clickable { controlsVisible = !controlsVisible }) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(h: SurfaceHolder) {
                            engine.start(settings)
                            engine.attachSurface(h.surface)
                            engine.load(source)
                        }
                        override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hgt: Int) {}
                        override fun surfaceDestroyed(h: SurfaceHolder) {
                            engine.detachSurface()
                        }
                    })
                }
            }
        )

        if (controlsVisible) {
            Row(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onClose) { Text("Close") }
                TextButton(onClick = onSettings) { Text("Settings") }
            }
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { engine.seekBy(-10) }) { Text("-10s") }
                Button(onClick = { engine.togglePause() }) { Text("Play / Pause") }
                TextButton(onClick = { engine.seekBy(10) }) { Text("+10s") }
                TextButton(onClick = {
                    speed = if (speed >= 2.0) 0.5 else speed + 0.25
                    engine.setSpeed(speed)
                }) { Text("${speed}x") }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    settings: PlayerSettings,
    onChange: (PlayerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Playback settings") },
        text = {
            Column {
                Text("Smoothness", style = MaterialTheme.typography.titleSmall)
                SmoothMode.values().forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onChange(settings.copy(smooth = mode)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.smooth == mode, onClick = {
                            onChange(settings.copy(smooth = mode))
                        })
                        Text(mode.label)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hardware decoding", Modifier.weight(1f))
                    Switch(
                        checked = settings.hardwareDecoding,
                        onCheckedChange = { onChange(settings.copy(hardwareDecoding = it)) }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Limit resolution (helps weak devices)", style = MaterialTheme.typography.titleSmall)
                Row {
                    listOf(0 to "None", 1080 to "1080p", 720 to "720p").forEach { (h, label) ->
                        FilterChip(
                            selected = settings.maxHeight == h,
                            onClick = { onChange(settings.copy(maxHeight = h)) },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun UrlDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Open network URL") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("https://… (mp4, mkv, m3u8)") }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) { Text("Play") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
