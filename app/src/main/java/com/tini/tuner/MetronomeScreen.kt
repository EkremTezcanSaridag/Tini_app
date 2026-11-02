package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MetronomeScreen(
    onBackClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var bpm by remember { mutableFloatStateOf(120f) }
    var beatCount by remember { mutableIntStateOf(0) }
    
    val metronome = remember { MetronomeEngine() }
    
    LaunchedEffect(isPlaying, bpm) {
        if (isPlaying) {
            metronome.start(bpm.toInt())
            while (isPlaying) {
                delay((60000L / bpm.toLong()))
                beatCount++
            }
        } else {
            metronome.stop()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            metronome.release()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(24.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = AppColors.TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Metronom",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                fontFamily = InterFont
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // BPM Display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${bpm.toInt()}",
                fontSize = 72.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                fontFamily = InterFont
            )
            Text(
                text = "BPM",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Visual metronome
        MetronomePulse(isPlaying = isPlaying, beatCount = beatCount)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // BPM Slider
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Tempo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont
            )
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = bpm,
                onValueChange = { bpm = it },
                valueRange = 40f..240f,
                steps = 199,
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.AccentBlue,
                    activeTrackColor = AppColors.AccentBlue,
                    inactiveTrackColor = AppColors.Separator
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("40", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
                Text("240", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Play/Stop button
        Button(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) AppColors.AccentRed else AppColors.AccentGreen,
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
fun MetronomePulse(isPlaying: Boolean, beatCount: Int) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying && beatCount > 0) 1.2f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pulse"
    )
    
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
                    if (isPlaying) AppColors.AccentBlue else AppColors.Separator
                )
        )
    }
}
