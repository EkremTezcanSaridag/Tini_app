package com.tini.tuner

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

data class GuitarString(
    val number: Int,
    val name: String,
    val targetFrequency: Float
)

enum class TuningType(val displayName: String, val frequencies: List<Float>) {
    STANDARD("Standart", listOf(82.41f, 110.00f, 146.83f, 196.00f, 246.94f, 329.63f)),
    DROP_D("Drop D", listOf(73.42f, 110.00f, 146.83f, 196.00f, 246.94f, 329.63f)),
    HALF_STEP_DOWN("Yarım ton pes", listOf(77.78f, 103.83f, 138.59f, 185.00f, 233.08f, 311.13f)),
    OPEN_G("Open G", listOf(73.42f, 98.00f, 146.83f, 196.00f, 246.94f, 293.66f)),
    DADGAD("DADGAD", listOf(73.42f, 110.00f, 146.83f, 196.00f, 220.00f, 293.66f))
}

data class TuningState(
    val detectedFrequency: Float,
    val targetFrequency: Float,
    val cents: Float,
    val note: String,
    val isInTune: Boolean,
    val direction: TuningDirection
)

enum class TuningDirection {
    TOO_LOW,
    IN_TUNE,
    TOO_HIGH
}

object TuningUtils {
    
    fun frequencyToCents(frequency: Float, targetFrequency: Float): Float {
        return (1200 * ln(frequency / targetFrequency) / ln(2.0)).toFloat()
    }
    
    fun frequencyToNote(frequency: Float): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val a4 = 440.0
        val c0 = a4 * 2.0.pow(-4.75)
        
        val halfSteps = round(12 * ln(frequency / c0) / ln(2.0))
        val octave = (halfSteps / 12).toInt()
        val noteIndex = (halfSteps % 12).toInt()
        
        return "${noteNames[noteIndex]}$octave"
    }
    
    fun getTuningState(
        detectedFrequency: Float,
        targetFrequency: Float,
        a4Reference: Float = 440f
    ): TuningState {
        val adjustedTarget = targetFrequency * (a4Reference / 440f)
        val cents = frequencyToCents(detectedFrequency, adjustedTarget)
        val note = frequencyToNote(detectedFrequency)
        
        val direction = when {
            cents < -5 -> TuningDirection.TOO_LOW
            cents > 5 -> TuningDirection.TOO_HIGH
            else -> TuningDirection.IN_TUNE
        }
        
        val isInTune = kotlin.math.abs(cents) <= 5
        
        return TuningState(
            detectedFrequency = detectedFrequency,
            targetFrequency = adjustedTarget,
            cents = cents,
            note = note,
            isInTune = isInTune,
            direction = direction
        )
    }
    
    fun findNearestString(frequency: Float, tuning: TuningType): Int {
        var nearestIndex = 0
        var smallestDiff = Float.MAX_VALUE
        
        tuning.frequencies.forEachIndexed { index, targetFreq ->
            val diff = kotlin.math.abs(frequency - targetFreq)
            if (diff < smallestDiff) {
                smallestDiff = diff
                nearestIndex = index
            }
        }
        
        return nearestIndex
    }
}
