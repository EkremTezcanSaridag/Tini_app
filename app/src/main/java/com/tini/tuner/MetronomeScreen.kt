package com.tini.tuner

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetronomeScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var bpm by remember { mutableFloatStateOf(120f) }
    var beatCount by remember { mutableIntStateOf(0) }
    var lastAccent by remember { mutableStateOf(false) }
    var soundType by remember { mutableStateOf(MetronomeSoundType.WOOD) }
    
    val metronome = remember { MetronomeEngine(context) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    
    LaunchedEffect(isPlaying, bpm, soundType) {
        if (isPlaying) {
            beatCount = 0
            metronome.start(bpm.toInt(), soundType) { accent, index ->
                mainHandler.post {
                    lastAccent = accent
                    beatCount = index + 1
                }
            }
        } else {
            metronome.stop()
            beatCount = 0
            lastAccent = false
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            metronome.release()
        }
    }
    
    val colors = AppColors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        TiniSubScreenTopBar(title = "Metronom", onBackClick = onBackClick)
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${bpm.toInt()}",
                fontSize = 72.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.TextPrimary,
                fontFamily = InterFont
            )
            Text(
                text = "BPM",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.TextSecondary,
                fontFamily = InterFont
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Ses",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = colors.TextSecondary,
            fontFamily = InterFont
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.SurfaceSecondary)
                .padding(2.dp)
        ) {
            MetronomeSoundType.entries.forEach { type ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (soundType == type) colors.Surface else Color.Transparent)
                        .clickable(enabled = !isPlaying) { soundType = type }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (soundType == type) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (soundType == type) colors.TextPrimary else colors.TextSecondary,
                        fontFamily = InterFont
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        MetronomePulse(isPlaying = isPlaying, beatCount = beatCount, accent = lastAccent)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Tempo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.TextSecondary,
                fontFamily = InterFont
            )
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = bpm,
                onValueChange = { bpm = it },
                valueRange = 40f..240f,
                steps = 199,
                colors = SliderDefaults.colors(
                    thumbColor = colors.AccentBlue,
                    activeTrackColor = colors.AccentBlue,
                    inactiveTrackColor = colors.Separator
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("40", color = colors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
                Text("240", color = colors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) colors.AccentRed else colors.AccentGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isPlaying) "Durdur" else "Başlat",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFont
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetronomePulse(isPlaying: Boolean, beatCount: Int, accent: Boolean) {
    val targetScale = when {
        !isPlaying || beatCount == 0 -> 1f
        accent -> 1.28f
        else -> 1.12f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 80),
        label = "pulse"
    )
    val colors = AppColors
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((80 * scale).dp)
                .clip(CircleShape)
                .background(
                    when {
                        !isPlaying -> colors.Separator
                        accent -> colors.AccentGreen
                        else -> colors.AccentBlue
                    }
                )
        )
    }
}
