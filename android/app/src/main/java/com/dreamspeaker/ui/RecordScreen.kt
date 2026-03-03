package com.dreamspeaker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val RecordingRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF22C55E)

enum class UploadState {
    IDLE, UPLOADING, SUCCESS, ERROR
}

@Composable
fun RecordScreen(
    isRecording: Boolean,
    uploadState: UploadState,
    errorMessage: String?,
    onToggleRecording: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Pulsing animation when recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor by animateColorAsState(
        targetValue = if (isRecording) RecordingRed else Purple,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    val outerGlowColor by animateColorAsState(
        targetValue = if (isRecording) RecordingRed.copy(alpha = 0.3f) else Purple.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "glowColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Settings gear icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Center content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "DreamSpeaker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Purple
            )

            // Record button with glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .then(
                        if (isRecording) Modifier.scale(pulseScale) else Modifier
                    )
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(outerGlowColor, CircleShape)
                )

                // Main button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(8.dp, CircleShape)
                        .background(buttonColor, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleRecording
                        )
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Start recording",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Status text
            val statusText = when {
                isRecording -> "Recording…"
                uploadState == UploadState.UPLOADING -> "Uploading…"
                uploadState == UploadState.SUCCESS -> "Sent!"
                uploadState == UploadState.ERROR -> errorMessage ?: "Upload failed"
                else -> "Tap to record your dream"
            }

            val statusColor = when {
                isRecording -> RecordingRed
                uploadState == UploadState.SUCCESS -> SuccessGreen
                uploadState == UploadState.ERROR -> RecordingRed
                uploadState == UploadState.UPLOADING -> Purple
                else -> TextSecondary
            }

            Text(
                text = statusText,
                fontSize = 16.sp,
                color = statusColor,
                textAlign = TextAlign.Center,
                fontWeight = if (uploadState != UploadState.IDLE || isRecording)
                    FontWeight.SemiBold else FontWeight.Normal
            )

            // Uploading progress indicator
            if (uploadState == UploadState.UPLOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Purple,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
