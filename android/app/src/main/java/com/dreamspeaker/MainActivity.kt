package com.dreamspeaker

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.dreamspeaker.audio.AudioRecorder
import com.dreamspeaker.network.DreamUploader
import com.dreamspeaker.ui.RecordScreen
import com.dreamspeaker.ui.SettingsScreen
import com.dreamspeaker.ui.UploadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var audioRecorder: AudioRecorder

    companion object {
        private const val PREFS_NAME = "dreamspeaker_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_KEY = "api_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        audioRecorder = AudioRecorder(this)

        setContent {
            DreamSpeakerApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.release()
    }

    @Composable
    private fun DreamSpeakerApp() {
        val savedUrl = prefs.getString(KEY_SERVER_URL, "") ?: ""
        val savedKey = prefs.getString(KEY_API_KEY, "") ?: ""
        val hasSettings = savedUrl.isNotBlank() && savedKey.isNotBlank()

        var showSettings by remember { mutableStateOf(!hasSettings) }
        var isRecording by remember { mutableStateOf(false) }
        var uploadState by remember { mutableStateOf(UploadState.IDLE) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
            if (!granted) {
                Toast.makeText(
                    this,
                    "Microphone permission is required to record audio",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (showSettings) {
            SettingsScreen(
                initialUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
                initialKey = prefs.getString(KEY_API_KEY, "") ?: "",
                onSave = { url, key ->
                    prefs.edit()
                        .putString(KEY_SERVER_URL, url)
                        .putString(KEY_API_KEY, key)
                        .apply()
                    showSettings = false
                }
            )
        } else {
            RecordScreen(
                isRecording = isRecording,
                uploadState = uploadState,
                errorMessage = errorMessage,
                onToggleRecording = {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        return@RecordScreen
                    }

                    if (isRecording) {
                        // Stop recording and upload
                        val file = audioRecorder.stop()
                        isRecording = false

                        if (file != null && file.exists()) {
                            uploadState = UploadState.UPLOADING
                            errorMessage = null

                            val url = prefs.getString(KEY_SERVER_URL, "") ?: ""
                            val key = prefs.getString(KEY_API_KEY, "") ?: ""

                            CoroutineScope(Dispatchers.Main).launch {
                                val result = DreamUploader.upload(url, key, file)
                                when (result) {
                                    is DreamUploader.UploadResult.Success -> {
                                        uploadState = UploadState.SUCCESS
                                        delay(3000)
                                        uploadState = UploadState.IDLE
                                    }
                                    is DreamUploader.UploadResult.Error -> {
                                        uploadState = UploadState.ERROR
                                        errorMessage = result.message
                                        delay(4000)
                                        uploadState = UploadState.IDLE
                                        errorMessage = null
                                    }
                                }
                                // Clean up temp file
                                file.delete()
                            }
                        }
                    } else {
                        // Start recording
                        try {
                            audioRecorder.start()
                            isRecording = true
                            uploadState = UploadState.IDLE
                            errorMessage = null
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to start recording: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onSettingsClick = { showSettings = true }
            )
        }
    }
}
