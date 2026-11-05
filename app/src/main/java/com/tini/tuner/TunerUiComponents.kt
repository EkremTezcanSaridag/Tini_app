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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
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

private val WireEdge = Color(0xFF9A886C)
private val WireBody = Color(0xFFC9B08A)
private val WireShine = Color(0xFFE8D5B0)
private val WireSpec = Color(0xFFFDFBF7)
private val WireCoilShadow = Color(0xFF8A7558)
private val NeedleDark = Color(0xFF1D1D1F)

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
    val statusColor = when {
        tuningState == null -> colors.TextSecondary
        isInTune -> colors.AccentGreen
        animatedCents < 0 -> colors.AccentRed
        else -> colors.AccentOrange
    }
    val statusText = tuningState?.direction?.let(::tuningStatusLabel) ?: ""
    val inTuneScale by animateFloatAsState(
        targetValue = if (isInTune) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "noteScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(228.dp)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h * 0.50f
            val radius = minOf(w, h) * 0.46f
            val trackW = 24f
            val arcTopLeft = Offset(cx - radius, cy - radius)
            val arcSize = Size(radius * 2, radius * 2)

            fun drawCentZone(c0: Float, c1: Float, color: Color) {
                val a0 = centToDialAngle(c0)
                val a1 = centToDialAngle(c1)
                drawArc(
                    color = color,
                    startAngle = a0,
                    sweepAngle = a1 - a0,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = trackW, cap = StrokeCap.Butt)
                )
            }

            // Saturated track: red sides, green top center
            drawCentZone(-50f, -7f, colors.AccentRed)
            drawCentZone(-7f, 7f, colors.AccentGreen)
            drawCentZone(7f, 50f, colors.AccentRed)

            // Dense fine ticks along full arc (−50…+50 mapped to 135°…405°)
            for (centi in -500..500) {
                val c = centi / 10f
                val rad = Math.toRadians(centToDialAngle(c).toDouble())
                val isMajor = (centi % 50 == 0)
                val isMid = (centi % 10 == 0)
                val inner = radius - trackW / 2 - (if (isMajor) 14f else if (isMid) 10f else 8f)
                val outer = radius + trackW / 2 + (if (isMajor) 10f else if (isMid) 6f else 4f)
                drawLine(
                    color = colors.TextPrimary.copy(
                        alpha = when {
                            c == 0f -> 0.75f
                            isMajor -> 0.55f
                            isMid -> 0.35f
                            else -> 0.22f
                        }
                    ),
                    start = Offset(cx + inner * cos(rad).toFloat(), cy + inner * sin(rad).toFloat()),
                    end = Offset(cx + outer * cos(rad).toFloat(), cy + outer * sin(rad).toFloat()),
                    strokeWidth = when {
                        c == 0f -> 2.2f
                        isMajor -> 1.6f
                        else -> 1f
                    },
                    cap = StrokeCap.Round
                )
            }

            listOf(
                Triple(-50f, "-50", colors.TextSecondary),
                Triple(0f, "0", if (isInTune) colors.AccentGreen else colors.TextPrimary),
                Triple(50f, "+50", colors.TextSecondary)
            ).forEach { (c, label, labelColor) ->
                val rad = Math.toRadians(centToDialAngle(c).toDouble())
                val lx = cx + (radius + trackW + 26f) * cos(rad).toFloat()
                val ly = cy + (radius + trackW + 26f) * sin(rad).toFloat()
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textSize = if (c == 0f) 32f else 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = c == 0f
                    }
                    canvas.nativeCanvas.drawText(label, lx, ly + 10f, paint)
                }
            }

            val needleRad = Math.toRadians(centToDialAngle(animatedCents).toDouble())
            val tipX = cx + (radius - trackW * 0.35f) * cos(needleRad).toFloat()
            val tipY = cy + (radius - trackW * 0.35f) * sin(needleRad).toFloat()
            if (tuningState != null) {
                drawLine(
                    color = NeedleDark,
                    start = Offset(cx, cy),
                    end = Offset(tipX, tipY),
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(color = colors.Surface, radius = 13f, center = Offset(cx, cy))
            drawCircle(color = colors.Separator, radius = 13f, center = Offset(cx, cy), style = Stroke(1.5f))
            drawCircle(color = NeedleDark, radius = 5f, center = Offset(cx, cy))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 8.dp)
        ) {
            if (tuningState != null) {
                Text(
                    text = note ?: "—",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary,
                    fontFamily = InterFont,
                    letterSpacing = (-2).sp,
                    modifier = Modifier.scale(inTuneScale)
                )
                if (frequency != null) {
                    Text(
                        text = "%.1f Hz".format(frequency),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.TextSecondary,
                        fontFamily = InterFont
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    fontFamily = InterFont,
                    maxLines = 1
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

/** Slim wire diameters (dp): thinnest high string → thickest low E. */
private fun stringDiameterDp(rankFromThin: Int, stringCount: Int): Dp {
    val presets = listOf(2.5f, 3f, 3.5f, 4.5f, 5f, 6.5f)
    if (stringCount <= 1) return presets.first().dp
    val idx = (rankFromThin.toFloat() / (stringCount - 1) * presets.lastIndex).toInt()
        .coerceIn(0, presets.lastIndex)
    return presets[idx].dp
}

private fun rowHeightForString(diameterDp: Dp, hasCaption: Boolean): Dp {
    val base = diameterDp + 12.dp
    return if (hasCaption) base + 11.dp else base
}

/** Horizontal neck — top→bottom: İnce E (high) … Kalın E (low). Data index 0 = lowest. */
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
    val rowHeights = dataIndicesTopToBottom.map { dataIndex ->
        val rank = strings.size - 1 - dataIndex
        val caption = neckCaption(strings[dataIndex], dataIndex, strings.size)
        rowHeightForString(stringDiameterDp(rank, strings.size), caption != null)
    }
    val totalHeight = rowHeights.fold(0.dp) { acc, h -> acc + h } + 6.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight.coerceAtLeast(188.dp))
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val fretCount = 9
                for (i in 1 until fretCount) {
                    val x = size.width * i / fretCount
                    drawLine(
                        color = colors.Separator.copy(alpha = 0.45f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }
            Column(modifier = Modifier.fillMaxSize()) {
                dataIndicesTopToBottom.forEachIndexed { rowIndex, dataIndex ->
                    val note = strings[dataIndex]
                    val selected = dataIndex == selectedIndex
                    val tuned = selected && isTuned
                    val caption = neckCaption(note, dataIndex, strings.size)
                    val rankFromThin = strings.size - 1 - dataIndex
                    val diameterDp = stringDiameterDp(rankFromThin, strings.size)
                    val wound = rankFromThin >= 2

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeights[rowIndex])
                            .clickable { onStringSelected(dataIndex) }
                    ) {
                        if (caption != null) {
                            Text(
                                text = caption,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected && tuned) colors.AccentGreen
                                else if (selected) colors.TextPrimary
                                else colors.TextSecondary.copy(alpha = 0.85f),
                                fontFamily = InterFont,
                                modifier = Modifier.padding(start = 4.dp, bottom = 1.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StringNoteBadge(
                                note = badgeText(note),
                                selected = selected,
                                tuned = tuned
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                drawSlimMetalStringWire(
                                    left = 0f,
                                    right = size.width,
                                    centerY = size.height / 2f,
                                    diameterPx = diameterDp.toPx(),
                                    wound = wound,
                                    selected = selected,
                                    tuned = tuned,
                                    green = colors.AccentGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StringNoteBadge(
    note: String,
    selected: Boolean,
    tuned: Boolean
) {
    val colors = AppColors
    val bg = when {
        selected && tuned -> colors.AccentGreen
        selected -> colors.TextPrimary
        else -> colors.Surface
    }
    val border = when {
        selected -> Color.Transparent
        else -> colors.Separator.copy(alpha = 0.9f)
    }
    val textColor = when {
        selected -> Color.White
        else -> colors.TextPrimary
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (!selected) Modifier.border(1.dp, border, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = note,
            fontSize = if (note.length > 1) 9.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = InterFont,
            maxLines = 1
        )
    }
}

private fun badgeText(note: String): String =
    if (note.length <= 2) note else note.take(1)

private fun neckCaption(note: String, dataIndex: Int, total: Int): String? {
    val isLowest = dataIndex == 0
    val isHighest = dataIndex == total - 1
    return when {
        isHighest && (note == "E" || note == "Eb") -> if (note == "Eb") "İnce Eb" else "İnce E"
        isLowest && (note == "E" || note == "Eb") -> if (note == "Eb") "Kalın Eb" else "Kalın E"
        else -> null
    }
}

private fun DrawScope.drawSlimMetalStringWire(
    left: Float,
    right: Float,
    centerY: Float,
    diameterPx: Float,
    wound: Boolean,
    selected: Boolean,
    tuned: Boolean,
    green: Color
) {
    val h = diameterPx.coerceIn(2f, 22f)
    val top = centerY - h / 2f
    val width = (right - left).coerceAtLeast(1f)
    val corner = CornerRadius(h / 2f, h / 2f)

    if (selected) {
        val bloom = if (tuned) 0.14f else 0.10f
        listOf(8f, 5f, 3f).forEach { pad ->
            drawRoundRect(
                color = green.copy(alpha = bloom * (8f / pad)),
                topLeft = Offset(left, centerY - (h + pad) / 2f),
                size = Size(width, h + pad),
                cornerRadius = CornerRadius((h + pad) / 2f)
            )
        }
    }

    drawRoundRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to WireEdge.copy(alpha = 0.65f),
                0.25f to WireBody,
                0.45f to WireShine,
                0.5f to WireSpec,
                0.55f to WireShine,
                0.75f to WireBody,
                1f to WireEdge.copy(alpha = 0.65f)
            ),
            startY = top,
            endY = top + h
        ),
        topLeft = Offset(left, top),
        size = Size(width, h),
        cornerRadius = corner
    )

    drawLine(
        color = Color.White.copy(alpha = 0.75f),
        start = Offset(left + width * 0.04f, centerY),
        end = Offset(left + width * 0.96f, centerY),
        strokeWidth = (h * 0.22f).coerceAtLeast(0.6f),
        cap = StrokeCap.Round
    )

    if (wound) {
        val clip = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = left,
                    top = top,
                    right = left + width,
                    bottom = top + h,
                    cornerRadius = corner
                )
            )
        }
        clipPath(clip) {
            val pitch = 1.65f
            var x = left
            var n = 0
            while (x < left + width) {
                drawLine(
                    color = if (n % 2 == 0) WireCoilShadow.copy(alpha = 0.22f)
                    else Color.White.copy(alpha = 0.16f),
                    start = Offset(x, top + 1f),
                    end = Offset(x, top + h - 1f),
                    strokeWidth = 0.55f,
                    cap = StrokeCap.Round
                )
                x += pitch
                n++
            }
        }
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
        shadowElevation = 6.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TiniNavItem("Metronom", Icons.Outlined.Timer, onMetronomeClick, selectedTab == TiniNavTab.Metronome)
            TiniNavItem("Geçmiş", Icons.Outlined.History, onHistoryClick, selectedTab == TiniNavTab.History)
            TiniNavItem("Akorlar", Icons.Outlined.LibraryMusic, onChordsClick, selectedTab == TiniNavTab.Chords)
            TiniNavItem("Profil", Icons.Outlined.Person, onProfilesClick, selectedTab == TiniNavTab.Profiles)
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
    val iconTint = if (selected) colors.AccentBlue else colors.TextSecondary
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
