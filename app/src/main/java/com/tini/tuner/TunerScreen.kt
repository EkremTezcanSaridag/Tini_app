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
    onInstrumentChanged: (Instrument) -> Unit = {},
    onA4ReferenceChanged: (Float) -> Unit,
    onPolyphonicToggle: (Boolean) -> Unit = {},
    onMetronomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onChordsClick: () -> Unit = {},
    onProfilesClick: () -> Unit = {},
    themeRepository: ThemeRepository? = null
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MinimalTopBar(
                tuningName = uiState.currentTuning.displayName,
                onTuningClick = { showTuningSheet = true },
                onSettingsClick = { showSettingsSheet = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SpectrumWashBackground(magnitudes = uiState.spectrumMagnitudes)
                PremiumTunerDisplay(
                    tuningState = uiState.tuningState,
                    note = uiState.tuningState?.note,
                    frequency = uiState.detectedFrequency,
                    cents = uiState.tuningState?.cents
                )
            }

            if (uiState.polyphonicMode && uiState.multiStringMatches.isNotEmpty()) {
                Text(
                    text = "Çoklu tel: " + uiState.multiStringMatches.joinToString { (idx, freq) ->
                        val names = getStringNames(uiState.currentTuning)
                        "${names.getOrElse(idx) { "?" }} ${"%.0f".format(freq)}Hz"
                    },
                    fontSize = 11.sp,
                    color = AppColors.TextSecondary,
                    fontFamily = InterFont,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            GuitarStringSelector(
                strings = getStringNames(uiState.currentTuning),
                selectedIndex = uiState.selectedStringIndex,
                isTuned = uiState.tuningState?.isInTune == true,
                onStringSelected = onStringSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            SegmentedControl(
                isAuto = uiState.autoMode,
                onModeChanged = onAutoModeToggle
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.isListening) "Dinleniyor…" else "Durduruldu",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                fontFamily = InterFont,
                modifier = Modifier.clickable {
                    if (uiState.isListening) onStopListening() else onStartListening()
                }
            )
        }

        TiniBottomNavBar(
            onMetronomeClick = onMetronomeClick,
            onHistoryClick = onHistoryClick,
            onChordsClick = onChordsClick,
            onProfilesClick = onProfilesClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                },
                themeRepository = themeRepository,
                polyphonicMode = uiState.polyphonicMode,
                onPolyphonicToggle = onPolyphonicToggle
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
    val colors = AppColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ayarlar",
                tint = colors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Tını",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.TextSecondary,
                fontFamily = InterFont,
                letterSpacing = 0.5.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTuningClick)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tuningName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.TextPrimary,
                    fontFamily = InterFont
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun PremiumTunerDisplay(
    tuningState: TuningState?,
    note: String?,
    frequency: Float?,
    cents: Float?
) {
    val colors = AppColors
    val needleCents = (tuningState?.cents ?: 0f).coerceIn(-50f, 50f)
    val inTuneScale by animateFloatAsState(
        targetValue = if (tuningState?.isInTune == true) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "noteScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 72.dp)
    ) {
        Text(
            text = note ?: "—",
            fontSize = 88.sp,
            fontWeight = FontWeight.Light,
            color = colors.TextPrimary,
            fontFamily = InterFont,
            letterSpacing = (-2).sp,
            modifier = Modifier.scale(inTuneScale)
        )

        if (frequency != null) {
            Text(
                text = "%.1f Hz".format(frequency),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.TextSecondary,
                fontFamily = InterFont
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (tuningState != null) {
            ArcTunerGauge(
                cents = needleCents,
                isInTune = tuningState.isInTune,
                modifier = Modifier.padding(top = 4.dp)
            )

            val centsColor = when {
                tuningState.isInTune -> colors.AccentGreen
                cents != null && cents < 0 -> colors.AccentRed
                else -> colors.AccentOrange
            }

            Text(
                text = if (cents != null) {
                    val sign = if (cents > 0) "+" else ""
                    "$sign${cents.toInt()} ¢"
                } else "—",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = centsColor,
                fontFamily = InterFont,
                modifier = Modifier.padding(top = 4.dp)
            )

            val statusText = when (tuningState.direction) {
                TuningDirection.TOO_LOW -> "Pes"
                TuningDirection.TOO_HIGH -> "Tiz"
                TuningDirection.IN_TUNE -> "Akortlu"
            }

            Text(
                text = statusText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = centsColor,
                fontFamily = InterFont
            )
        } else {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = "Teli çalın",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.TextSecondary,
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
    val colors = AppColors
    val accentGreen = colors.AccentGreen
    val accentRed = colors.AccentRed
    val accentOrange = colors.AccentOrange
    val textSecondary = colors.TextSecondary
    
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
                accentGreen.copy(alpha = 0.6f)
            } else {
                textSecondary.copy(alpha = 0.4f)
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
            isInTune -> accentGreen
            cents < 0 -> accentRed
            else -> accentOrange
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
        val buttonSize = minOf(48.dp, availableWidth / (strings.size + 1))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            strings.forEachIndexed { index, name ->
                MinimalStringButton(
                    number = strings.size - index,
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
    var selectedInstrument by remember { mutableStateOf(currentTuning.instrument) }
    val colors = AppColors
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Enstrüman & Akort",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.TextPrimary,
            fontFamily = InterFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Instrument selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.SurfaceSecondary)
                .padding(2.dp)
        ) {
            Instrument.values().forEach { instrument ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedInstrument == instrument) colors.Surface else Color.Transparent)
                        .clickable { selectedInstrument = instrument }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = instrument.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (selectedInstrument == instrument) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selectedInstrument == instrument) colors.TextPrimary else colors.TextSecondary,
                        fontFamily = InterFont
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Tunings for selected instrument
        val tunings = TuningType.values().filter { it.instrument == selectedInstrument }
        tunings.forEach { tuning ->
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
                    color = colors.TextPrimary,
                    fontFamily = InterFont
                )
                
                if (tuning == currentTuning) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.AccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            if (tuning != tunings.last()) {
                Divider(
                    color = colors.Separator,
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
    onDismiss: () -> Unit,
    themeRepository: ThemeRepository? = null,
    polyphonicMode: Boolean = false,
    onPolyphonicToggle: (Boolean) -> Unit = {}
) {
    var sliderValue by remember { mutableFloatStateOf(currentA4) }
    val currentTheme by (themeRepository?.themeModeFlow?.collectAsState(initial = ThemeMode.LIGHT) ?: remember { mutableStateOf(ThemeMode.LIGHT) })
    val scope = rememberCoroutineScope()
    val colors = AppColors
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Ayarlar",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.TextPrimary,
            fontFamily = InterFont,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Theme Selection
        Text(
            text = "Tema",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.TextSecondary,
            fontFamily = InterFont
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.SurfaceSecondary)
                .padding(2.dp)
        ) {
            ThemeMode.values().forEach { mode ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (currentTheme == mode) colors.Surface else Color.Transparent)
                        .clickable {
                            scope.launch {
                                themeRepository?.setThemeMode(mode)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (mode) {
                            ThemeMode.LIGHT -> "Açık"
                            ThemeMode.DARK -> "Koyu"
                            ThemeMode.SYSTEM -> "Sistem"
                        },
                        fontSize = 13.sp,
                        fontWeight = if (currentTheme == mode) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (currentTheme == mode) colors.TextPrimary else colors.TextSecondary,
                        fontFamily = InterFont
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = colors.Separator, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Çoklu tel analizi", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.TextPrimary, fontFamily = InterFont)
                Text("FFT ile birden fazla tel", fontSize = 12.sp, color = colors.TextSecondary, fontFamily = InterFont)
            }
            Switch(
                checked = polyphonicMode,
                onCheckedChange = onPolyphonicToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = colors.AccentBlue)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = colors.Separator, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "A4 Referans Frekansı",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.TextSecondary,
            fontFamily = InterFont
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "${sliderValue.toInt()} Hz",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.TextPrimary,
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
                thumbColor = colors.AccentBlue,
                activeTrackColor = colors.AccentBlue,
                inactiveTrackColor = colors.Separator
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("430 Hz", color = colors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
            Text("450 Hz", color = colors.TextSecondary, fontSize = 12.sp, fontFamily = InterFont)
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
                containerColor = colors.AccentBlue,
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
        TuningType.BASS_STANDARD -> listOf("E", "A", "D", "G")
        TuningType.BASS_DROP_D -> listOf("D", "A", "D", "G")
        TuningType.BASS_5STRING -> listOf("B", "E", "A", "D", "G")
        TuningType.UKULELE_C -> listOf("G", "C", "E", "A")
        TuningType.UKULELE_D -> listOf("A", "D", "F#", "B")
    }
}

@Composable
fun SpectrumVisualization(
    magnitudes: FloatArray?,
    peaks: List<SpectrumPeak>
) {
    val colors = AppColors
    if (magnitudes == null) return
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val barCount = 64
        val step = magnitudes.size / barCount
        val maxMag = magnitudes.maxOrNull()?.coerceAtLeast(0.001f) ?: 1f
        val barWidth = size.width / barCount
        
        for (i in 0 until barCount) {
            val mag = magnitudes[i * step]
            val h = (mag / maxMag) * size.height * 0.9f
            drawRect(
                color = colors.AccentBlue.copy(alpha = 0.35f),
                topLeft = Offset(i * barWidth, size.height - h),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, h)
            )
        }
        
        peaks.forEach { peak ->
            val bin = (peak.frequency * magnitudes.size / 44100f).toInt().coerceIn(0, magnitudes.size - 1)
            val x = (bin.toFloat() / magnitudes.size) * size.width
            drawLine(
                color = colors.AccentGreen,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f
            )
        }
    }
}
