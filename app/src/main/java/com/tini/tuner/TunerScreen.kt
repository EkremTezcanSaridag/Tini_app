package com.tini.tuner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

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
    var showTuningMenu by remember { mutableStateOf(false) }
    var showA4Dialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tını",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        
        Text(
            text = "Gitar Akort Cihazı",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showTuningMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Text(uiState.currentTuning.displayName, color = Color.White)
            }
            
            Button(
                onClick = { showA4Dialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Text("A4: ${uiState.a4Reference.toInt()} Hz", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        StringSelector(
            strings = getStringNames(uiState.currentTuning),
            selectedIndex = uiState.selectedStringIndex,
            onStringSelected = onStringSelected,
            autoMode = uiState.autoMode
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Switch(
                checked = uiState.autoMode,
                onCheckedChange = onAutoModeToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4CAF50),
                    checkedTrackColor = Color(0xFF81C784)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Otomatik Algılama", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TuningGauge(
            tuningState = uiState.tuningState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FrequencyDisplay(
            frequency = uiState.detectedFrequency,
            note = uiState.tuningState?.note,
            cents = uiState.tuningState?.cents
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TuningInstruction(tuningState = uiState.tuningState)
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                if (uiState.isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isListening) Color(0xFFE53935) else Color(0xFF4CAF50)
            )
        ) {
            Text(
                text = if (uiState.isListening) "Durdur" else "Dinlemeye Başla",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    if (showTuningMenu) {
        TuningSelectionDialog(
            currentTuning = uiState.currentTuning,
            onTuningSelected = {
                onTuningChanged(it)
                showTuningMenu = false
            },
            onDismiss = { showTuningMenu = false }
        )
    }
    
    if (showA4Dialog) {
        A4ReferenceDialog(
            currentA4 = uiState.a4Reference,
            onA4Changed = {
                onA4ReferenceChanged(it)
                showA4Dialog = false
            },
            onDismiss = { showA4Dialog = false }
        )
    }
}

@Composable
fun StringSelector(
    strings: List<String>,
    selectedIndex: Int,
    onStringSelected: (Int) -> Unit,
    autoMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        strings.forEachIndexed { index, name ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onStringSelected(index) }
                    .background(
                        if (index == selectedIndex) Color(0xFF4CAF50) else Color(0xFF2A2A2A)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = (index + 1).toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TuningGauge(
    tuningState: TuningState?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.4f
            
            for (i in -50..50 step 10) {
                val angle = i * 1.8f
                val startRadius = if (i % 20 == 0) radius * 0.7f else radius * 0.8f
                val x1 = centerX + startRadius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                val y1 = centerY + startRadius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                val x2 = centerX + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                val y2 = centerY + radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()
                
                drawLine(
                    color = Color.Gray,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = if (i % 20 == 0) 3f else 1f
                )
            }
            
            val cents = tuningState?.cents ?: 0f
            val clampedCents = cents.coerceIn(-50f, 50f)
            val needleAngle = clampedCents * 1.8f
            
            rotate(needleAngle, pivot = Offset(centerX, centerY)) {
                val needleColor = when {
                    tuningState?.isInTune == true -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF5722)
                }
                
                drawLine(
                    color = needleColor,
                    start = Offset(centerX, centerY),
                    end = Offset(centerX, centerY - radius * 0.9f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
            
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = Offset(centerX, centerY)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(
                text = "${tuningState?.cents?.toInt() ?: 0} cent",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FrequencyDisplay(
    frequency: Float?,
    note: String?,
    cents: Float?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Frekans", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = if (frequency != null) "%.2f Hz".format(frequency) else "-- Hz",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nota", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = note ?: "--",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TuningInstruction(tuningState: TuningState?) {
    val (text, color) = when (tuningState?.direction) {
        TuningDirection.TOO_LOW -> "Teli sık ↑" to Color(0xFF2196F3)
        TuningDirection.TOO_HIGH -> "Teli gevşet ↓" to Color(0xFFFFC107)
        TuningDirection.IN_TUNE -> "Akortlu ✓" to Color(0xFF4CAF50)
        null -> "..." to Color.Gray
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .border(2.dp, color, RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TuningSelectionDialog(
    currentTuning: TuningType,
    onTuningSelected: (TuningType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Akort Türü Seçin") },
        text = {
            Column {
                TuningType.values().forEach { tuning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTuningSelected(tuning) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tuning == currentTuning,
                            onClick = { onTuningSelected(tuning) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tuning.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun A4ReferenceDialog(
    currentA4: Float,
    onA4Changed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentA4) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A4 Referans Frekansı") },
        text = {
            Column {
                Text("A4 = ${sliderValue.toInt()} Hz")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 430f..450f,
                    steps = 19
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onA4Changed(sliderValue) }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
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
