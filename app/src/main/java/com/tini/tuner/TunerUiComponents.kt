package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class TiniNavTab { Metronome, History, Chords, Profiles }

/** Almost-full circular dial (open at bottom) with note readout inside the ring. */
@Composable
fun CircularAnalogDial(
    note: String?,
    frequency: Float?,
    tuningState: TuningState?,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    val cents = (tuningState?.cents ?: 0f).coerceIn(-50f, 50f)
    val isInTune = tuningState?.isInTune == true
    val animatedCents by animateFloatAsState(
        targetValue = cents,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 280f),
        label = "dialCents"
    )
    val accentColor = when {
        tuningState == null -> colors.TextSecondary
        isInTune -> colors.AccentGreen
        animatedCents < 0 -> colors.AccentRed
        else -> colors.AccentOrange
    }
    val statusText = when (tuningState?.direction) {
        TuningDirection.TOO_LOW -> "Pes"
        TuningDirection.TOO_HIGH -> "Tiz"
        TuningDirection.IN_TUNE -> "Akortlu"
        null -> ""
    }
    val inTuneScale by animateFloatAsState(
        targetValue = if (isInTune) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "noteScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.05f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.52f
            val radius = minOf(w, h) * 0.44f
            val trackW = 18f
            val arcTopLeft = Offset(cx - radius, cy - radius)
            val arcSize = Size(radius * 2, radius * 2)
            val arcStart = 135f
            val arcSweep = 270f

            // Light outer track
            drawArc(
                color = colors.Separator.copy(alpha = 0.45f),
                startAngle = arcStart,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = trackW, cap = StrokeCap.Round)
            )

            fun drawCentZone(c0: Float, c1: Float, color: Color, alpha: Float) {
                val a0 = centToDialAngle(c0)
                val a1 = centToDialAngle(c1)
                drawArc(
                    color = color.copy(alpha = alpha),
                    startAngle = a0,
                    sweepAngle = a1 - a0,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = trackW, cap = StrokeCap.Butt)
                )
            }

            drawCentZone(-50f, -8f, colors.AccentRed, 0.85f)
            drawCentZone(-8f, -4f, colors.AccentOrange, 0.75f)
            drawCentZone(-4f, 4f, colors.AccentGreen, 0.9f)
            drawCentZone(4f, 8f, colors.AccentOrange, 0.75f)
            drawCentZone(8f, 50f, colors.AccentRed, 0.85f)

            // Fine tick marks around the ring
            for (tick in -50..50 step 5) {
                val rad = Math.toRadians(centToDialAngle(tick.toFloat()).toDouble())
                val inner = radius - trackW / 2 - 10f
                val outer = radius + trackW / 2 + (if (tick % 10 == 0) 8f else 4f)
                val sx = cx + inner * cos(rad).toFloat()
                val sy = cy + inner * sin(rad).toFloat()
                val ex = cx + outer * cos(rad).toFloat()
                val ey = cy + outer * sin(rad).toFloat()
                drawLine(
                    color = colors.TextSecondary.copy(alpha = if (tick == 0) 0.55f else 0.28f),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = if (tick == 0) 2.5f else 1.2f,
                    cap = StrokeCap.Round
                )
            }

            // Cent labels at arc ends and top
            listOf(
                Triple(-50f, "-50", false),
                Triple(0f, "0", true),
                Triple(50f, "+50", false)
            ).forEach { (c, label, emphasize) ->
                val rad = Math.toRadians(centToDialAngle(c).toDouble())
                val lx = cx + (radius + trackW + 22f) * cos(rad).toFloat()
                val ly = cy + (radius + trackW + 22f) * sin(rad).toFloat()
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = (if (emphasize && isInTune) colors.AccentGreen else colors.TextSecondary)
                            .toArgb()
                        textSize = if (emphasize) 30f else 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        if (emphasize) isFakeBoldText = true
                    }
                    canvas.nativeCanvas.drawText(label, lx, ly + 10f, paint)
                }
            }

            // Needle from center toward rim
            val needleRad = Math.toRadians(centToDialAngle(animatedCents).toDouble())
            val tipX = cx + (radius - 6f) * cos(needleRad).toFloat()
            val tipY = cy + (radius - 6f) * sin(needleRad).toFloat()
            if (tuningState != null) {
                for (r in listOf(26f, 18f, 12f)) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.08f),
                        radius = r,
                        center = Offset(tipX, tipY)
                    )
                }
                drawLine(
                    color = accentColor,
                    start = Offset(cx, cy),
                    end = Offset(tipX, tipY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = colors.Surface, radius = 11f, center = Offset(cx, cy))
            drawCircle(
                color = if (tuningState != null) accentColor else colors.Separator,
                radius = 6.5f,
                center = Offset(cx, cy)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            if (tuningState != null) {
                Text(
                    text = note ?: "—",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary,
                    fontFamily = InterFont,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.scale(inTuneScale)
                )
                if (frequency != null) {
                    Text(
                        text = "%.1f Hz".format(frequency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.TextSecondary,
                        fontFamily = InterFont
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    fontFamily = InterFont
                )
            } else {
                Text(
                    text = "—",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary.copy(alpha = 0.35f),
                    fontFamily = InterFont
                )
                Spacer(modifier = Modifier.height(8.dp))
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
}

private fun centToDialAngle(cents: Float): Float =
    270f + cents * (135f / 50f)

@Composable
fun SpectrumWashBackground(
    magnitudes: FloatArray?,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    if (magnitudes == null) return
    Canvas(modifier = modifier.fillMaxSize()) {
        val barCount = 48
        val step = (magnitudes.size / barCount).coerceAtLeast(1)
        val maxMag = magnitudes.maxOrNull()?.coerceAtLeast(0.001f) ?: 1f
        val barW = size.width / barCount
        for (i in 0 until barCount) {
            val mag = magnitudes[i * step]
            val h = (mag / maxMag) * size.height * 0.35f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.AccentBlue.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    startY = size.height - h,
                    endY = size.height
                ),
                topLeft = Offset(i * barW, size.height - h),
                size = Size(barW * 1.1f, h)
            )
        }
    }
}

/**
 * Horizontal neck strings — display top→bottom: high string (thin) to low string (thick).
 * Data [selectedIndex] uses index 0 = lowest / thickest string (same as tuning model).
 */
@Composable
fun HorizontalNeckStringSelector(
    strings: List<String>,
    selectedIndex: Int,
    isTuned: Boolean,
    onStringSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    val dataIndicesTopToBottom = strings.indices.reversed().toList()
    val thicknessSteps = listOf(1.2f, 1.6f, 2.0f, 2.6f, 3.2f, 4.0f)
    fun thicknessForDataIndex(dataIndex: Int): Float {
        val rankFromThin = strings.size - 1 - dataIndex
        val step = thicknessSteps.getOrElse(rankFromThin) { 2f }
        return step
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height((strings.size * 26 + 16).dp)
            .padding(horizontal = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fretCount = 8
                for (i in 1 until fretCount) {
                    val x = size.width * i / fretCount
                    drawLine(
                        color = colors.Separator.copy(alpha = 0.35f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                dataIndicesTopToBottom.forEach { dataIndex ->
                    val note = strings[dataIndex]
                    val selected = dataIndex == selectedIndex
                    val tuned = selected && isTuned
                    val label = neckStringLabel(note, dataIndex, strings.size)
                    val thickness = thicknessForDataIndex(dataIndex).dp
                    val stringNum = strings.size - dataIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clickable { onStringSelected(dataIndex) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(52.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (tuned) colors.AccentGreen else colors.TextPrimary
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = note.take(2),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = InterFont,
                                        maxLines = 1
                                    )
                                }
                            } else if (label.contains(" ")) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.TextSecondary.copy(alpha = 0.85f),
                                    fontFamily = InterFont,
                                    maxLines = 1
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(thickness.coerceAtLeast(1.dp))
                                .drawBehind {
                                    if (selected) {
                                        val glowH = size.height + 14f
                                        drawRoundRect(
                                            color = colors.AccentGreen.copy(alpha = 0.22f),
                                            topLeft = Offset(0f, (size.height - glowH) / 2),
                                            size = Size(size.width, glowH),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(glowH / 2)
                                        )
                                    }
                                    drawLine(
                                        color = when {
                                            tuned -> colors.AccentGreen
                                            selected -> colors.TextPrimary
                                            else -> colors.TextSecondary.copy(alpha = 0.55f)
                                        },
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width, size.height / 2),
                                        strokeWidth = thickness.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                        )
                        Text(
                            text = "$stringNum",
                            fontSize = 9.sp,
                            color = colors.TextSecondary.copy(alpha = 0.6f),
                            fontFamily = InterFont,
                            modifier = Modifier.width(16.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun neckStringLabel(note: String, dataIndex: Int, total: Int): String {
    val isLowest = dataIndex == 0
    val isHighest = dataIndex == total - 1
    return when {
        isHighest && (note == "E" || note == "Eb") -> if (note == "Eb") "İnce Eb" else "İnce E"
        isLowest && (note == "E" || note == "Eb") -> if (note == "Eb") "Kalın Eb" else "Kalın E"
        else -> note
    }
}

@Composable
fun TiniBottomNavBar(
    onMetronomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChordsClick: () -> Unit,
    onProfilesClick: () -> Unit,
    selectedTab: TiniNavTab? = null,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = colors.Surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TiniNavItem("Metronom", Icons.Default.Timer, onMetronomeClick, selectedTab == TiniNavTab.Metronome)
            TiniNavItem("Geçmiş", Icons.Default.History, onHistoryClick, selectedTab == TiniNavTab.History)
            TiniNavItem("Akorlar", Icons.Default.LibraryMusic, onChordsClick, selectedTab == TiniNavTab.Chords)
            TiniNavItem("Profil", Icons.Default.Person, onProfilesClick, selectedTab == TiniNavTab.Profiles)
        }
    }
}

@Composable
private fun TiniNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    selected: Boolean
) {
    val colors = AppColors
    val iconTint = if (selected) colors.AccentBlue else colors.TextPrimary
    val labelColor = if (selected) colors.AccentBlue else colors.TextSecondary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            fontFamily = InterFont
        )
    }
}

@Composable
fun TiniSubScreenTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, "Geri", tint = colors.TextPrimary)
        }
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.TextPrimary,
            fontFamily = InterFont
        )
    }
}
