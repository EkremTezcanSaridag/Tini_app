package com.tini.tuner

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
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

private data class StringPickerCell(
    val dataIndex: Int,
    @DrawableRes val imageRes: Int,
    val label: String
)

private val sixStringCloseups = listOf(
    R.drawable.string_closeup_ince_e,
    R.drawable.string_closeup_b,
    R.drawable.string_closeup_g,
    R.drawable.string_closeup_d,
    R.drawable.string_closeup_a,
    R.drawable.string_closeup_kalin_e
)

private fun stringPickerLabel(note: String, dataIndex: Int, total: Int): String {
    val isLowest = dataIndex == 0
    val isHighest = dataIndex == total - 1
    return when {
        isHighest && (note == "E" || note == "Eb") -> if (note == "Eb") "İnce Eb" else "İnce E"
        isLowest && (note == "E" || note == "Eb") -> if (note == "Eb") "Kalın Eb" else "Kalın E"
        else -> note
    }
}

private fun buildStringPickerCells(strings: List<String>): List<StringPickerCell> {
    val drawables = sixStringCloseups
    if (strings.size == 6) {
        val order = listOf(5, 4, 3, 2, 1, 0)
        return order.mapIndexed { i, dataIndex ->
            StringPickerCell(
                dataIndex = dataIndex,
                imageRes = drawables[i],
                label = stringPickerLabel(strings[dataIndex], dataIndex, strings.size)
            )
        }
    }
    return strings.indices.reversed().map { dataIndex ->
        val rank = strings.size - 1 - dataIndex
        val drawableIdx = (rank.toFloat() / (strings.size - 1).coerceAtLeast(1) * drawables.lastIndex)
            .toInt().coerceIn(0, drawables.lastIndex)
        StringPickerCell(
            dataIndex = dataIndex,
            imageRes = drawables[drawableIdx],
            label = stringPickerLabel(strings[dataIndex], dataIndex, strings.size)
        )
    }
}

/** Headstock illustration + 2×3 circular string sample pickers (mockup D). */
@Composable
fun HeadstockStringPicker(
    strings: List<String>,
    selectedIndex: Int,
    isTuned: Boolean,
    onStringSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cells = buildStringPickerCells(strings)
    val columns = if (strings.size == 6) 3 else 3

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.headstock_tuners),
            contentDescription = "Gitar burgu",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .heightIn(max = 108.dp),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(6.dp))

        cells.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                row.forEach { cell ->
                    StringSampleCircle(
                        cell = cell,
                        selected = cell.dataIndex == selectedIndex,
                        tuned = cell.dataIndex == selectedIndex && isTuned,
                        onClick = { onStringSelected(cell.dataIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StringSampleCircle(
    cell: StringPickerCell,
    selected: Boolean,
    tuned: Boolean,
    onClick: () -> Unit
) {
    val colors = AppColors
    val ringColor = when {
        selected && tuned -> colors.AccentGreen
        selected -> colors.TextPrimary
        else -> Color.Transparent
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(82.dp)
                .then(
                    if (selected) {
                        Modifier.shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            ambientColor = colors.AccentGreen.copy(alpha = 0.45f),
                            spotColor = colors.AccentGreen.copy(alpha = 0.35f)
                        )
                    } else Modifier
                )
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) ringColor else colors.Separator.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(colors.Surface)
        ) {
            Image(
                painter = painterResource(cell.imageRes),
                contentDescription = cell.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cell.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = when {
                selected && tuned -> colors.AccentGreen
                selected -> colors.TextPrimary
                else -> colors.TextSecondary
            },
            fontFamily = InterFont,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
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
