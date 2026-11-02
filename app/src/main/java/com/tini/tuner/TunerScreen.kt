package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.launch
import kotlin.math.abs

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
            if (now - lastHapticTime > 1000) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                lastHapticTime = now
            }
        }
    }
    
    // Auto-start listening
    LaunchedEffect(Unit) {
        if (!uiState.isListening) {
            onStartListening()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar - simple and centered
            MinimalTopBar(
                tuningName = uiState.currentTuning.displayName,
                onTuningClick = { showTuningSheet = true },
                onSettingsClick = { showSettingsSheet = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main tuner area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                MinimalTunerDisplay(
                    tuningState = uiState.tuningState,
                    note = uiState.tuningState?.note,
                    frequency = uiState.detectedFrequency,
                    cents = uiState.tuningState?.cents,
                    isListening = uiState.isListening
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // String selector
            MinimalStringSelector(
                strings = getStringNames(uiState.currentTuning),
                selectedIndex = uiState.selectedStringIndex,
                isTuned = uiState.tuningState?.isInTune == true,
                onStringSelected = onStringSelected
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // iOS-style segmented control for auto/manual
            SegmentedControl(
                isAuto = uiState.autoMode,
                onModeChanged = onAutoModeToggle
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Minimal listening status
            Text(
                text = if (uiState.isListening) "Dinleniyor..." else "Durduruldu",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont,
                modifier = Modifier.clickable {
                    if (uiState.isListening) onStopListening() else onStartListening()
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
            containerColor = AppColors.Surface
        ) {
            MinimalTuningSheet(
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
            containerColor = AppColors.Surface
        ) {
            MinimalSettingsSheet(
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
fun MinimalTopBar(
    tuningName: String,
    onTuningClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Settings icon on left
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Centered title and tuning selector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tını",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont,
                letterSpacing = 0.5.sp
            )
            
            // Tuning selector as plain text with chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTuningClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tuningName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontFamily = InterFont
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Empty space on right for balance
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun MinimalTunerDisplay(
    tuningState: TuningState?,
    note: String?,
    frequency: Float?,
    cents: Float?,
    isListening: Boolean
) {
    val needlePosition by animateFloatAsState(
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Huge thin note name - Apple style
        Text(
            text = note ?: "--",
            fontSize = 110.sp,
            fontWeight = FontWeight.Light,
            color = AppColors.TextPrimary,
            fontFamily = InterFont,
            letterSpacing = (-2).sp,
            modifier = Modifier.scale(inTuneScale)
        )
        
        // Frequency below
        if (frequency != null) {
            Text(
                text = "%.1f Hz".format(frequency),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont
            )
        }
        
        // Horizontal cent ruler - minimal Apple style
        if (tuningState != null) {
            HorizontalCentRuler(
                cents = needlePosition,
                isInTune = tuningState.isInTune
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Cents value
            val centsColor = when {
                tuningState.isInTune -> AppColors.AccentGreen
                cents != null && cents < 0 -> AppColors.AccentRed
                else -> AppColors.AccentOrange
            }
            
            Text(
                text = if (cents != null) {
                    val sign = if (cents > 0) "+" else ""
                    "$sign${cents.toInt()}"
                } else "--",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = centsColor,
                fontFamily = InterFont
            )
            
            // Status text - clear and visible
            val statusText = when (tuningState.direction) {
                TuningDirection.TOO_LOW -> "Teli sık"
                TuningDirection.TOO_HIGH -> "Teli gevşet"
                TuningDirection.IN_TUNE -> "Akortlu"
            }
            
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = centsColor,
                fontFamily = InterFont
            )
        } else {
            Spacer(modifier = Modifier.height(80.dp))
            // Placeholder
            Text(
                text = "Teli çal",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont
            )
        }
    }
}

@Composable
fun HorizontalCentRuler(
    cents: Float,
    isInTune: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(80.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2
        
        // Draw tick marks with labels - darker and longer
        for (tick in -50..50 step 10) {
            val x = centerX + (tick / 50f) * (width / 2 * 0.95f)
            val isMajor = tick % 10 == 0
            val isCenter = tick == 0
            val tickHeight = if (isCenter) 32f else if (isMajor) 24f else 16f
            val tickColor = if (isCenter) {
                AppColors.AccentGreen.copy(alpha = 0.6f)
            } else {
                AppColors.TextSecondary.copy(alpha = 0.4f)
            }
            
            drawLine(
                color = tickColor,
                start = Offset(x, centerY - tickHeight / 2),
                end = Offset(x, centerY + tickHeight / 2),
                strokeWidth = if (isCenter) 2.5f else 1.5f,
                cap = StrokeCap.Round
            )
            
            // Labels at -50, 0, +50
            if (tick == -50 || tick == 0 || tick == 50) {
                val label = when (tick) {
                    -50 -> "-50"
                    0 -> "0"
                    50 -> "+50"
                    else -> ""
                }
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#86868B")
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(label, x, centerY + tickHeight / 2 + 38f, paint)
                }
            }
        }
        
        // Draw indicator pill with soft halo
        val indicatorX = centerX + (cents / 50f) * (width / 2 * 0.95f)
        val indicatorColor = when {
            isInTune -> AppColors.AccentGreen
            cents < 0 -> AppColors.AccentRed
            else -> AppColors.AccentOrange
        }
        
        // Soft halo effect
        for (i in listOf(16f, 12f, 8f)) {
            drawCircle(
                color = indicatorColor.copy(alpha = 0.08f),
                radius = i,
                center = Offset(indicatorX, centerY)
            )
        }
        
        // Rounded pill indicator
        drawRoundRect(
            color = indicatorColor,
            topLeft = Offset(indicatorX - 4f, centerY - 18f),
            size = androidx.compose.ui.geometry.Size(8f, 36f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        
        // Indicator dot at bottom
        drawCircle(
            color = indicatorColor,
            radius = 5f,
            center = Offset(indicatorX, centerY + 24f)
        )
    }
}

@Composable
fun MinimalStringSelector(
    strings: List<String>,
    selectedIndex: Int,
    isTuned: Boolean,
    onStringSelected: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val availableWidth = maxWidth - 40.dp // Account for horizontal padding
        val buttonSize = minOf(48.dp, availableWidth / 7) // 6 buttons + spacing
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            strings.forEachIndexed { index, name ->
                MinimalStringButton(
                    number = 6 - index,
                    note = name,
                    isSelected = index == selectedIndex,
                    isTuned = index == selectedIndex && isTuned,
                    onClick = { onStringSelected(index) },
                    size = buttonSize
                )
            }
        }
    }
}

@Composable
fun MinimalStringButton(
    number: Int,
    note: String,
    isSelected: Boolean,
    isTuned: Boolean,
    onClick: () -> Unit,
    size: Dp
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
        // Circular button
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isSelected) AppColors.TextPrimary else Color.Transparent)
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else AppColors.Separator,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = note,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else AppColors.TextPrimary,
                fontFamily = InterFont
            )
            
            // Tuned indicator - small green dot at top right
            if (isTuned) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(AppColors.AccentGreen)
                )
            }
        }
        
        // String number below in grey
        Text(
            text = number.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary,
            fontFamily = InterFont
        )
    }
}

@Composable
fun SegmentedControl(
    isAuto: Boolean,
    onModeChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.SurfaceSecondary)
            .padding(2.dp)
    ) {
        // Otomatik segment
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isAuto) AppColors.Surface else Color.Transparent)
                .clickable { onModeChanged(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Otomatik",
                fontSize = 13.sp,
                fontWeight = if (isAuto) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isAuto) AppColors.TextPrimary else AppColors.TextSecondary,
                fontFamily = InterFont
            )
        }
        
        // Manuel segment
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isAuto) AppColors.Surface else Color.Transparent)
                .clickable { onModeChanged(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Manuel",
                fontSize = 13.sp,
                fontWeight = if (!isAuto) FontWeight.SemiBold else FontWeight.Medium,
                color = if (!isAuto) AppColors.TextPrimary else AppColors.TextSecondary,
                fontFamily = InterFont
            )
        }
    }
}

@Composable
fun MinimalTuningSheet(
    currentTuning: TuningType,
    onTuningSelected: (TuningType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Akort Türü",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            fontFamily = InterFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        TuningType.values().forEach { tuning ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTuningSelected(tuning) }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tuning.displayName,
                    fontSize = 17.sp,
                    fontWeight = if (tuning == currentTuning) FontWeight.SemiBold else FontWeight.Normal,
                    color = AppColors.TextPrimary,
                    fontFamily = InterFont
                )
                
                if (tuning == currentTuning) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.AccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            if (tuning != TuningType.values().last()) {
                Divider(
                    color = AppColors.Separator,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MinimalSettingsSheet(
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
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            fontFamily = InterFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        Text(
            text = "A4 Referans Frekansı",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary,
            fontFamily = InterFont
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "${sliderValue.toInt()} Hz",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            fontFamily = InterFont,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 430f..450f,
            steps = 19,
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
            Text("430 Hz", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
            Text("450 Hz", color = AppColors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                onA4Changed(sliderValue)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.AccentBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Kaydet",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFont
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getStringNames(tuning: TuningType): List<String> {
    return when (tuning) {
        TuningType.STANDARD -> listOf("E", "A", "D", "G", "B", "E")
        TuningType.DROP_D -> listOf("D", "A", "D", "G", "B", "E")
        TuningType.DROP_C -> listOf("C", "G", "C", "F", "A", "D")
        TuningType.HALF_STEP_DOWN -> listOf("Eb", "Ab", "Db", "Gb", "Bb", "Eb")
        TuningType.DROP_Db -> listOf("Db", "Ab", "Db", "Gb", "Bb", "Eb")
        TuningType.OPEN_G -> listOf("D", "G", "D", "G", "B", "D")
        TuningType.OPEN_D -> listOf("D", "A", "D", "F#", "A", "D")
        TuningType.DADGAD -> listOf("D", "A", "D", "G", "A", "D")
        TuningType.DOUBLE_DROP_D -> listOf("D", "A", "D", "G", "B", "D")
    }
}
