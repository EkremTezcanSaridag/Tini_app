package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/** Upper semicircle arc gauge −50…+50 ¢ with zone colors and animated needle. */
@Composable
fun ArcTunerGauge(
    cents: Float,
    isInTune: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AppColors
    val animatedCents by animateFloatAsState(
        targetValue = cents.coerceIn(-50f, 50f),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 280f),
        label = "arcCents"
    )
    val needleColor = when {
        isInTune -> colors.AccentGreen
        animatedCents < 0 -> colors.AccentRed
        else -> colors.AccentOrange
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h - 8f
        val radius = minOf(w, h * 2f) * 0.42f
        val stroke = 14f
        val arcTopLeft = Offset(cx - radius, cy - radius)
        val arcSize = Size(radius * 2, radius * 2)

        // Track
        drawArc(
            color = colors.Separator.copy(alpha = 0.55f),
            startAngle = 180f,
            sweepAngle = -180f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        fun drawZone(cStart: Float, cEnd: Float, color: Color, alpha: Float = 0.35f) {
            val a0 = centToAngle(cStart)
            val a1 = centToAngle(cEnd)
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = a0,
                sweepAngle = a1 - a0,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }

        drawZone(-50f, -12f, colors.AccentRed)
        drawZone(-12f, -5f, colors.AccentOrange, 0.4f)
        drawZone(-5f, 5f, colors.AccentGreen, 0.55f)
        drawZone(5f, 12f, colors.AccentOrange, 0.4f)
        drawZone(12f, 50f, colors.AccentRed)

        // Tick marks
        for (tick in listOf(-50, -25, 0, 25, 50)) {
            val rad = centToRad(tick.toFloat())
            val inner = radius - stroke / 2 - 6f
            val outer = radius + stroke / 2 + 4f
            val sx = cx + inner * cos(rad).toFloat()
            val sy = cy - inner * sin(rad).toFloat()
            val ex = cx + outer * cos(rad).toFloat()
            val ey = cy - outer * sin(rad).toFloat()
            drawLine(
                color = colors.TextSecondary.copy(alpha = 0.35f),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = if (tick == 0) 2f else 1f,
                cap = StrokeCap.Round
            )
        }

        val needleRad = centToRad(animatedCents)
        val tipX = cx + (radius - 4f) * cos(needleRad).toFloat()
        val tipY = cy - (radius - 4f) * sin(needleRad).toFloat()

        for (r in listOf(22f, 16f, 10f)) {
            drawCircle(
                color = needleColor.copy(alpha = 0.07f),
                radius = r,
                center = Offset(tipX, tipY)
            )
        }

        drawLine(
            color = needleColor,
            start = Offset(cx, cy),
            end = Offset(tipX, tipY),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
        drawCircle(color = colors.Surface, radius = 10f, center = Offset(cx, cy))
        drawCircle(color = needleColor, radius = 6f, center = Offset(cx, cy))

        // Labels −50, 0, +50
        listOf(-50 to "-50", 0 to "0", 50 to "+50").forEach { (c, label) ->
            val rad = centToRad(c.toFloat())
            val lx = cx + (radius + 28f) * cos(rad).toFloat()
            val ly = cy - (radius + 28f) * sin(rad).toFloat()
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = colors.TextSecondary.toArgb()
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(label, lx, ly + 8f, paint)
            }
        }
    }
}

private fun centToAngle(cents: Float): Float =
    180f - (cents + 50f) / 100f * 180f

private fun centToRad(cents: Float): Double =
    Math.PI * (1.0 - (cents + 50.0) / 100.0)

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

@Composable
fun GuitarStringSelector(
    strings: List<String>,
    selectedIndex: Int,
    isTuned: Boolean,
    onStringSelected: (Int) -> Unit
) {
    val colors = AppColors
    val gaugeWidths = listOf(14f, 12f, 11f, 10f, 9f, 8f) // low → high if 6 strings

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        strings.forEachIndexed { index, note ->
            val stringNum = strings.size - index
            val widthDp = gaugeWidths.getOrElse(index) { 10f }.dp
            val label = stringLabel(note, index, strings.size)
            val selected = index == selectedIndex
            val tuned = selected && isTuned

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onStringSelected(index) }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(widthDp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(widthDp / 2))
                        .background(
                            when {
                                tuned -> colors.AccentGreen.copy(alpha = 0.25f)
                                selected -> colors.TextPrimary
                                else -> colors.SurfaceSecondary
                            }
                        )
                        .then(
                            if (!selected && !tuned) Modifier
                            else Modifier
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) colors.TextPrimary else colors.TextSecondary,
                    fontFamily = InterFont
                )
                Text(
                    text = "$stringNum",
                    fontSize = 10.sp,
                    color = colors.TextSecondary.copy(alpha = 0.7f),
                    fontFamily = InterFont
                )
            }
        }
    }
}

private fun stringLabel(note: String, index: Int, total: Int): String {
    if (note == "E" || note == "Eb") {
        if (index == 0) return if (note == "Eb") "Kalın Eb" else "Kalın E"
        if (index == total - 1) return if (note == "Eb") "İnce Eb" else "İnce E"
    }
    return note
}

@Composable
fun TiniBottomNavBar(
    onMetronomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChordsClick: () -> Unit,
    onProfilesClick: () -> Unit,
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
            TiniNavItem("Metronom", Icons.Default.Timer, onMetronomeClick)
            TiniNavItem("Geçmiş", Icons.Default.History, onHistoryClick)
            TiniNavItem("Akorlar", Icons.Default.LibraryMusic, onChordsClick)
            TiniNavItem("Profil", Icons.Default.Person, onProfilesClick)
        }
    }
}

@Composable
private fun TiniNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = AppColors
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
            tint = colors.TextPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = colors.TextSecondary,
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
