package com.tini.tuner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChordLibraryScreen(onBackClick: () -> Unit) {
    var selectedChord by remember { mutableStateOf(ChordLibrary.chords.first()) }
    val colors = AppColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.Background)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        TiniSubScreenTopBar(title = "Akor Kütüphanesi", onBackClick = onBackClick)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.Surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    selectedChord.displayName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.TextPrimary,
                    fontFamily = InterFont
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChordDiagramCanvas(
                    chord = selectedChord,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(ChordLibrary.chords) { chord ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (chord == selectedChord) colors.AccentBlue.copy(alpha = 0.15f)
                            else colors.SurfaceSecondary
                        )
                        .clickable { selectedChord = chord }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chord.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (chord == selectedChord) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (chord == selectedChord) colors.AccentBlue else colors.TextPrimary,
                        fontFamily = InterFont
                    )
                }
            }
        }
    }
}

@Composable
fun ChordDiagramCanvas(chord: ChordDiagram, modifier: Modifier = Modifier) {
    val colors = AppColors
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val left = w * 0.15f
        val right = w * 0.85f
        val top = h * 0.12f
        val bottom = h * 0.88f
        val stringCount = 6
        val fretCount = 5
        val stringSpacing = (right - left) / (stringCount - 1)
        val fretSpacing = (bottom - top) / fretCount

        val lineColor = colors.TextPrimary.copy(alpha = 0.35f)
        val nutColor = colors.TextPrimary.copy(alpha = 0.8f)

        // Nut or fret number
        if (chord.baseFret > 1) {
            // skip drawing thick nut
        } else {
            drawLine(nutColor, Offset(left - 4, top), Offset(right + 4, top), strokeWidth = 6f)
        }

        for (s in 0 until stringCount) {
            val x = left + s * stringSpacing
            drawLine(lineColor, Offset(x, top), Offset(x, bottom), strokeWidth = 2f)
        }
        for (f in 0..fretCount) {
            val y = top + f * fretSpacing
            drawLine(lineColor, Offset(left, y), Offset(right, y), strokeWidth = if (f == 0) 1f else 2f)
        }

        chord.frets.forEachIndexed { stringIndex, fret ->
            if (fret < 0) {
                val x = left + stringIndex * stringSpacing
                drawLine(
                    color = colors.TextPrimary,
                    start = Offset(x - 8, top - 20),
                    end = Offset(x + 8, top - 8),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            } else if (fret == 0) {
                val x = left + stringIndex * stringSpacing
                drawCircle(
                    color = colors.TextPrimary,
                    radius = 10f,
                    center = Offset(x, top - 14),
                    style = Stroke(width = 3f)
                )
            } else {
                val displayFret = fret - (chord.baseFret - 1)
                val x = left + stringIndex * stringSpacing
                val y = top + (displayFret - 0.5f) * fretSpacing
                drawCircle(
                    color = colors.AccentBlue,
                    radius = 14f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
