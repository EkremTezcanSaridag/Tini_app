package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.*

val GradientStart = Color(0xFF6366F1)
val GradientEnd = Color(0xFF8B5CF6)
val DarkBackground = Color(0xFF0F0F14)
val CardBackground = Color(0xFF1A1A24)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)
val AccentYellow = Color(0xFFF59E0B)

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
    
    val brush = remember {
        Brush.verticalGradient(
            colors = listOf(DarkBackground, Color(0xFF1A1A28))
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                tuningName = uiState.currentTuning.displayName,
                a4Reference = uiState.a4Reference,
                onTuningClick = { showTuningSheet = true },
                onSettingsClick = { showSettingsSheet = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PremiumGauge(
                tuningState = uiState.tuningState,
                note = uiState.tuningState?.note,
                frequency = uiState.detectedFrequency,
                cents = uiState.tuningState?.cents,
                isListening = uiState.isListening,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HeadstockStringSelector(
                strings = getStringNames(uiState.currentTuning),
                selectedIndex = uiState.selectedStringIndex,
                tuningState = uiState.tuningState,
                onStringSelected = onStringSelected,
                autoMode = uiState.autoMode
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    "Otomatik Algılama",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = uiState.autoMode,
                    onCheckedChange = onAutoModeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentGreen,
                        checkedTrackColor = AccentGreen.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PremiumButton(
                text = if (uiState.isListening) "Durdur" else "Dinlemeye Başla",
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
    
    if (showTuningSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTuningSheet = false },
            sheetState = sheetState,
            containerColor = CardBackground
        ) {
            TuningBottomSheet(
                currentTuning = uiState.currentTuning,
                onTuningSelected = {
                    onTuningChanged(it)
                    scope.launch { showTuningSheet = false }
                }
            )
        }
    }
    
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = CardBackground
        ) {
            SettingsBottomSheet(
                currentA4 = uiState.a4Reference,
                autoMode = uiState.autoMode,
                onA4Changed = onA4ReferenceChanged,
                onAutoModeChanged = onAutoModeToggle,
                onDismiss = { scope.launch { showSettingsSheet = false } }
            )
        }
    }
}

@Composable
fun TopBar(
    tuningName: String,
    a4Reference: Float,
    onTuningClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Tını",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        alpha = 0.15f
                    )
                }
            )
            Text(
                text = "Profesyonel Gitar Akort",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        Row {
            Surface(
                onClick = onTuningClick,
                modifier = Modifier.padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, GradientStart.copy(alpha = 0.3f))
            ) {
                Text(
                    text = tuningName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = GradientStart,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            
            Surface(
                onClick = onSettingsClick,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ayarlar",
                    modifier = Modifier.padding(10.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PremiumGauge(
    tuningState: TuningState?,
    note: String?,
    frequency: Float?,
    cents: Float?,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isListening) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height * 0.7f
                    val maxRadius = min(size.width, size.height * 0.7f) * 0.9f
                    
                    for (i in 1..3) {
                        drawCircle(
                            color = GradientStart.copy(alpha = pulseAlpha / (i * 2)),
                            radius = maxRadius * (1f + i * 0.1f),
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                val centerX = size.width / 2
                val centerY = size.height * 0.7f
                val radius = min(size.width, size.height * 0.7f) * 0.85f
                
                val startAngle = 150f
                val sweepAngle = 240f
                
                for (segment in 0..100) {
                    val segmentAngle = startAngle + segment * sweepAngle / 100f
                    val nextAngle = startAngle + (segment + 1) * sweepAngle / 100f
                    
                    val normalizedPos = (segment - 50f) / 50f
                    val segmentColor = when {
                        abs(normalizedPos) < 0.2f -> AccentGreen
                        abs(normalizedPos) < 0.6f -> AccentYellow
                        else -> AccentRed
                    }
                    
                    val innerRadius = radius - 40f
                    val outerRadius = radius
                    
                    val x1 = centerX + innerRadius * cos(Math.toRadians(segmentAngle.toDouble())).toFloat()
                    val y1 = centerY + innerRadius * sin(Math.toRadians(segmentAngle.toDouble())).toFloat()
                    val x2 = centerX + outerRadius * cos(Math.toRadians(segmentAngle.toDouble())).toFloat()
                    val y2 = centerY + outerRadius * sin(Math.toRadians(segmentAngle.toDouble())).toFloat()
                    
                    drawLine(
                        color = segmentColor.copy(alpha = 0.4f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 5f
                    )
                }
                
                for (i in -50..50 step 10) {
                    val angle = startAngle + (i + 50) * sweepAngle / 100
                    val isMainTick = i % 20 == 0
                    val startRadius = if (isMainTick) radius - 30f else radius - 18f
                    val endRadius = radius
                    
                    val startX = centerX + startRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                    val startY = centerY + startRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                    val endX = centerX + endRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                    val endY = centerY + endRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                    
                    drawLine(
                        color = Color.White.copy(alpha = if (isMainTick) 0.6f else 0.3f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMainTick) 4f else 2f,
                        cap = StrokeCap.Round
                    )
                }
                
                val needleAngle = startAngle + (needleRotation + 50) * sweepAngle / 100
                val needleLength = radius * 0.75f
                
                val needleColor = when {
                    tuningState?.isInTune == true -> AccentGreen
                    (tuningState?.cents ?: 0f) < 0 -> AccentRed
                    else -> AccentYellow
                }
                
                val needleEndX = centerX + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat()
                val needleEndY = centerY + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
                
                if (tuningState?.isInTune == true) {
                    for (glowRadius in listOf(50f, 35f, 20f)) {
                        drawCircle(
                            color = needleColor.copy(alpha = 0.15f),
                            radius = glowRadius,
                            center = Offset(needleEndX, needleEndY)
                        )
                    }
                }
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.8f), needleColor),
                        start = Offset(centerX, centerY),
                        end = Offset(needleEndX, needleEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(needleEndX, needleEndY),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.7f))
                    ),
                    radius = 20f,
                    center = Offset(centerX, centerY)
                )
                
                drawCircle(
                    color = CardBackground,
                    radius = 14f,
                    center = Offset(centerX, centerY)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(inTuneScale)
        ) {
            Text(
                text = note ?: "--",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            
            if (frequency != null) {
                Text(
                    text = "%.1f Hz".format(frequency),
                    fontSize = 22.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val centsColor = when {
                tuningState?.isInTune == true -> AccentGreen
                (cents ?: 0f) < 0 -> AccentRed
                else -> AccentYellow
            }
            
            Text(
                text = if (cents != null) {
                    val sign = if (cents > 0) "+" else ""
                    "$sign${cents.toInt()} cent"
                } else "-- cent",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = centsColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (tuningState != null) {
                val instruction = when (tuningState.direction) {
                    TuningDirection.TOO_LOW -> "Teli sık ↑"
                    TuningDirection.TOO_HIGH -> "Teli gevşet ↓"
                    TuningDirection.IN_TUNE -> "Akortlu ✓"
                }
                
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = centsColor.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, centsColor),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = instruction,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = centsColor,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HeadstockStringSelector(
    strings: List<String>,
    selectedIndex: Int,
    tuningState: TuningState?,
    onStringSelected: (Int) -> Unit,
    autoMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            strings.forEachIndexed { index, name ->
                StringPeg(
                    number = index + 1,
                    note = name,
                    isSelected = index == selectedIndex,
                    isTuned = index == selectedIndex && tuningState?.isInTune == true,
                    onClick = { onStringSelected(index) }
                )
            }
        }
    }
}

@Composable
fun StringPeg(
    number: Int,
    note: String,
    isSelected: Boolean,
    isTuned: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val checkScale by animateFloatAsState(
        targetValue = if (isTuned) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                brush = if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A3A),
                            Color(0xFF1E1E2E)
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number.toString(),
                fontSize = 13.sp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
            )
        }
        
        if (isTuned && checkScale > 0) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Akortlu",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .scale(checkScale)
                    .size(20.dp)
                    .background(AccentGreen, CircleShape)
                    .padding(2.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    isListening: Boolean,
    onClick: () -> Unit
) {
    val gradient = if (isListening) {
        Brush.horizontalGradient(colors = listOf(AccentRed, AccentRed.copy(alpha = 0.8f)))
    } else {
        Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd))
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TuningType.values().forEach { tuning ->
            Surface(
                onClick = { onTuningSelected(tuning) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (tuning == currentTuning) {
                    GradientStart.copy(alpha = 0.2f)
                } else {
                    Color.White.copy(alpha = 0.05f)
                },
                border = BorderStroke(
                    width = if (tuning == currentTuning) 2.dp else 1.dp,
                    color = if (tuning == currentTuning) {
                        GradientStart
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tuning.displayName,
                        fontSize = 18.sp,
                        fontWeight = if (tuning == currentTuning) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White
                    )
                    
                    if (tuning == currentTuning) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = GradientStart,
                            modifier = Modifier.size(24.dp)
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
    autoMode: Boolean,
    onA4Changed: (Float) -> Unit,
    onAutoModeChanged: (Boolean) -> Unit,
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White.copy(alpha = 0.05f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "A4 Referans Frekansı",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${sliderValue.toInt()} Hz",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 430f..450f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = GradientStart,
                        activeTrackColor = GradientStart,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("430 Hz", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text("450 Hz", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                onA4Changed(sliderValue)
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Kaydet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
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
