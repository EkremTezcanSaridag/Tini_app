package com.tini.tuner

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    uiState: TunerUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStringSelected: (Int) -> Unit,
    onAutoModeToggle: (Boolean) -> Unit,
    onTuningChanged: (TuningType) -> Unit,
    onA4ReferenceChanged: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showTuningSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    
    val view = LocalView.current
    var lastHapticTime by remember { mutableStateOf(0L) }
    
    // Haptic feedback on in-tune
    LaunchedEffect(uiState.tuningState?.isInTune) {
        if (uiState.tuningState?.isInTune == true) {
            val now = System.currentTimeMillis()
            if (now - lastHapticTime > 1000) { // Limit to once per second
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                lastHapticTime = now
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.Background, AppColors.Surface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            ProfessionalTopBar(
                tuningName = uiState.currentTuning.displayName,
                onTuningClick = { showTuningSheet = true },
                onSettingsClick = { showSettingsSheet = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main tuner gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ProfessionalGauge(
                    tuningState = uiState.tuningState,
                    note = uiState.tuningState?.note,
                    frequency = uiState.detectedFrequency,
                    cents = uiState.tuningState?.cents,
                    isListening = uiState.isListening
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // String selector
            ProfessionalStringSelector(
                strings = getStringNames(uiState.currentTuning),
                selectedIndex = uiState.selectedStringIndex,
                isTuned = uiState.tuningState?.isInTune == true,
                onStringSelected = onStringSelected
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Auto mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "Otomatik Algılama",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp,
                    fontFamily = ManropeFont
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.autoMode,
                    onCheckedChange = onAutoModeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.AccentGreen,
                        checkedTrackColor = AppColors.AccentGreen.copy(alpha = 0.5f),
                        uncheckedThumbColor = AppColors.TextSecondary,
                        uncheckedTrackColor = AppColors.Surface
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Start/Stop button
            ProfessionalButton(
                text = if (uiState.isListening) "Durdur" else "Başla",
                isListening = uiState.isListening,
                onClick = {
                    if (uiState.isListening) {
                        onStopListening()
                    } else {
                        onStartListening()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Tuning selection bottom sheet
    if (showTuningSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTuningSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.SurfaceElevated
        ) {
            TuningBottomSheet(
                currentTuning = uiState.currentTuning,
                onTuningSelected = {
                    onTuningChanged(it)
                    scope.launch {
                        sheetState.hide()
                        showTuningSheet = false
                    }
                }
            )
        }
    }
    
    // Settings bottom sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.SurfaceElevated
        ) {
            SettingsBottomSheet(
                currentA4 = uiState.a4Reference,
                onA4Changed = onA4ReferenceChanged,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showSettingsSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun ProfessionalTopBar(
    tuningName: String,
    onTuningClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App name
        Text(
            text = "Tını",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            fontFamily = ManropeFont,
            modifier = Modifier.weight(1f, fill = false)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            // Tuning selector
            Surface(
                onClick = onTuningClick,
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Surface,
                border = BorderStroke(1.dp, AppColors.PrimaryIndigo.copy(alpha = 0.3f)),
                modifier = Modifier.wrapContentWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tuning",
                        tint = AppColors.PrimaryIndigo,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = tuningName,
                        color = AppColors.PrimaryIndigo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        fontFamily = ManropeFont,
                        maxLines = 1
                    )
                }
            }
            
            // Settings button
            Surface(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalGauge(
    tuningState: TuningState?,
    note: String?,
    frequency: Float?,
    cents: Float?,
    isListening: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val needleRotation by animateFloatAsState(
        targetValue = (tuningState?.cents ?: 0f).coerceIn(-50f, 50f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "needle"
    )
    
    val inTuneScale by animateFloatAsState(
        targetValue = if (tuningState?.isInTune == true) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Arc gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.8f),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Pulse effect when listening
            if (isListening && tuningState != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height * 0.75f
                    val maxRadius = min(size.width, size.height * 0.75f) * 0.9f
                    
                    for (i in 1..2) {
                        drawCircle(
                            color = AppColors.PrimaryIndigo.copy(alpha = pulseAlpha / (i * 2)),
                            radius = maxRadius * (1f + i * 0.08f),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
            
            // Main arc gauge canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height * 0.75f
                val radius = min(size.width, size.height * 0.75f) * 0.85f
                
                val startAngle = 150f
                val sweepAngle = 240f
                
                // Draw colored arc zones
                for (segment in 0..100) {
                    val segmentAngle = startAngle + segment * sweepAngle / 100f
                    val centsPos = -50 + segment
                    
                    val segmentColor = when {
                        abs(centsPos) < 8 -> AppColors.AccentGreen
                        abs(centsPos) < 25 -> AppColors.PrimaryAmber
                        else -> AppColors.AccentRed
                    }
                    
                    val rad = Math.toRadians(segmentAngle.toDouble())
                    val innerR = radius - 50f
                    val outerR = radius
                    
                    val x1 = centerX + innerR * cos(rad).toFloat()
                    val y1 = centerY + innerR * sin(rad).toFloat()
                    val x2 = centerX + outerR * cos(rad).toFloat()
                    val y2 = centerY + outerR * sin(rad).toFloat()
                    
                    drawLine(
                        color = segmentColor.copy(alpha = 0.35f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 10f
                    )
                }
                
                // Draw tick marks
                for (tick in -50..50 step 10) {
                    val tickAngle = startAngle + (tick + 50) * sweepAngle / 100
                    val rad = Math.toRadians(tickAngle.toDouble())
                    val isMainTick = (tick % 20 == 0)
                    val tickStartR = radius - if (isMainTick) 28f else 18f
                    val tickEndR = radius
                    
                    val x1 = centerX + tickStartR * cos(rad).toFloat()
                    val y1 = centerY + tickStartR * sin(rad).toFloat()
                    val x2 = centerX + tickEndR * cos(rad).toFloat()
                    val y2 = centerY + tickEndR * sin(rad).toFloat()
                    
                    drawLine(
                        color = Color.White.copy(alpha = if (isMainTick) 0.5f else 0.25f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = if (isMainTick) 4f else 2f,
                        cap = StrokeCap.Round
                    )
                }
                
                // Draw needle
                val needleAngle = startAngle + (needleRotation + 50) * sweepAngle / 100
                val needleLength = radius * 0.7f
                val needleRad = Math.toRadians(needleAngle.toDouble())
                
                val needleColor = when {
                    tuningState?.isInTune == true -> AppColors.AccentGreen
                    (tuningState?.cents ?: 0f) < 0 -> AppColors.AccentRed
                    else -> AppColors.PrimaryAmber
                }
                
                val needleEndX = centerX + needleLength * cos(needleRad).toFloat()
                val needleEndY = centerY + needleLength * sin(needleRad).toFloat()
                
                // Glow effect for in-tune
                if (tuningState?.isInTune == true) {
                    for (glowR in listOf(40f, 28f, 16f)) {
                        drawCircle(
                            color = needleColor.copy(alpha = 0.12f),
                            radius = glowR,
                            center = Offset(needleEndX, needleEndY)
                        )
                    }
                }
                
                // Needle line with gradient
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            needleColor
                        ),
                        start = Offset(centerX, centerY),
                        end = Offset(needleEndX, needleEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                
                // Center knob - proper pivot point
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.8f))
                    ),
                    radius = 16f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = AppColors.Surface,
                    radius = 10f,
                    center = Offset(centerX, centerY)
                )
            }
        }
        
        // Note and status display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(inTuneScale),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Large note display
            Text(
                text = note ?: "--",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                fontFamily = ManropeFont,
                letterSpacing = 2.sp
            )
            
            if (frequency != null) {
                Text(
                    text = "%.1f Hz".format(frequency),
                    fontSize = 18.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = ManropeFont
                )
            }
            
            // Cents display
            val centsColor = when {
                tuningState?.isInTune == true -> AppColors.AccentGreen
                (cents ?: 0f) < 0 -> AppColors.AccentRed
                else -> AppColors.PrimaryAmber
            }
            
            Text(
                text = if (cents != null) {
                    val sign = if (cents > 0) "+" else ""
                    "$sign${cents.toInt()} cent"
                } else "-- cent",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = centsColor,
                fontFamily = ManropeFont
            )
            
            // Status text - ALWAYS visible with high contrast
            if (tuningState != null) {
                val (statusText, statusColor) = when (tuningState.direction) {
                    TuningDirection.TOO_LOW -> "Teli sık ↑" to AppColors.AccentRed
                    TuningDirection.TOO_HIGH -> "Teli gevşet ↓" to AppColors.PrimaryAmber
                    TuningDirection.IN_TUNE -> "Akortlu ✓" to AppColors.AccentGreen
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontFamily = ManropeFont,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp)
                    )
                }
            } else {
                // Placeholder when no tuning state
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.Surface,
                    border = BorderStroke(1.dp, AppColors.TextSecondary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Dinleniyor...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary,
                        fontFamily = ManropeFont,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalStringSelector(
    strings: List<String>,
    selectedIndex: Int,
    isTuned: Boolean,
    onStringSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            strings.forEachIndexed { index, name ->
                StringButton(
                    number = 6 - index,
                    note = name,
                    isSelected = index == selectedIndex,
                    isTuned = index == selectedIndex && isTuned,
                    onClick = { onStringSelected(index) }
                )
            }
        }
    }
}

@Composable
fun StringButton(
    number: Int,
    note: String,
    isSelected: Boolean,
    isTuned: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // String number label above the circle
        Text(
            text = number.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) AppColors.PrimaryIndigo else AppColors.TextSecondary.copy(alpha = 0.7f),
            fontFamily = ManropeFont
        )
        
        // Circle with note letter
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .scale(scale),
            shape = CircleShape,
            color = if (isSelected) {
                AppColors.PrimaryIndigo
            } else {
                AppColors.SurfaceElevated
            },
            border = if (isSelected) {
                BorderStroke(2.dp, AppColors.PrimaryAmber.copy(alpha = 0.5f))
            } else {
                BorderStroke(1.dp, AppColors.TextSecondary.copy(alpha = 0.2f))
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = note,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else AppColors.TextPrimary,
                    fontFamily = ManropeFont
                )
                
                if (isTuned) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tuned",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp)
                            .size(18.dp)
                            .background(AppColors.AccentGreen, CircleShape)
                            .padding(3.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProfessionalButton(
    text: String,
    isListening: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isListening) AppColors.AccentRed else AppColors.PrimaryIndigo,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = ManropeFont,
            color = Color.White
        )
    }
}

@Composable
fun TuningBottomSheet(
    currentTuning: TuningType,
    onTuningSelected: (TuningType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Akort Türü Seçin",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            fontFamily = ManropeFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        TuningType.values().forEach { tuning ->
            Surface(
                onClick = { onTuningSelected(tuning) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (tuning == currentTuning) {
                    AppColors.PrimaryIndigo.copy(alpha = 0.15f)
                } else {
                    AppColors.Surface
                },
                border = BorderStroke(
                    width = if (tuning == currentTuning) 2.dp else 1.dp,
                    color = if (tuning == currentTuning) {
                        AppColors.PrimaryIndigo
                    } else {
                        AppColors.TextSecondary.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tuning.displayName,
                        fontSize = 16.sp,
                        fontWeight = if (tuning == currentTuning) FontWeight.Bold else FontWeight.Normal,
                        color = AppColors.TextPrimary,
                        fontFamily = ManropeFont
                    )
                    
                    if (tuning == currentTuning) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AppColors.PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsBottomSheet(
    currentA4: Float,
    onA4Changed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentA4) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Ayarlar",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            fontFamily = ManropeFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = AppColors.Surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "A4 Referans Frekansı",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = ManropeFont
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "${sliderValue.toInt()} Hz",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryIndigo,
                    fontFamily = ManropeFont,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 430f..450f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.PrimaryIndigo,
                        activeTrackColor = AppColors.PrimaryIndigo,
                        inactiveTrackColor = AppColors.TextSecondary.copy(alpha = 0.2f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("430 Hz", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = ManropeFont)
                    Text("450 Hz", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = ManropeFont)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = {
                onA4Changed(sliderValue)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.PrimaryIndigo
            )
        ) {
            Text(
                text = "Kaydet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = ManropeFont
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getStringNames(tuning: TuningType): List<String> {
    return when (tuning) {
        TuningType.STANDARD -> listOf("E", "A", "D", "G", "B", "E")
        TuningType.DROP_D -> listOf("D", "A", "D", "G", "B", "E")
        TuningType.HALF_STEP_DOWN -> listOf("Eb", "Ab", "Db", "Gb", "Bb", "Eb")
        TuningType.OPEN_G -> listOf("D", "G", "D", "G", "B", "D")
        TuningType.DADGAD -> listOf("D", "A", "D", "G", "A", "D")
    }
}
