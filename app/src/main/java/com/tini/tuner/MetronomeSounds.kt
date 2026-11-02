package com.tini.tuner

import kotlin.math.*
import kotlin.random.Random

enum class MetronomeSoundType(val displayName: String) {
    WOOD("Tahta"),
    CLASSIC("Klasik"),
    HI_HAT("Hi-hat")
}

object MetronomeSampleFactory {
    const val SAMPLE_RATE = 44100

    fun generate(type: MetronomeSoundType, accented: Boolean): ShortArray {
        return when (type) {
            MetronomeSoundType.WOOD -> woodblock(accented)
            MetronomeSoundType.CLASSIC -> softBeep(accented)
            MetronomeSoundType.HI_HAT -> hiHat(accented)
        }
    }

    /** Clave / woodblock: ~1.5–2 kHz sine + short noise transient, ~20 ms, exp decay. */
    private fun woodblock(accented: Boolean): ShortArray {
        val durationMs = if (accented) 22 else 18
        val n = SAMPLE_RATE * durationMs / 1000
        val freq = if (accented) 2100.0 else 1750.0
        val gain = if (accented) 0.62 else 0.52
        return synthTick(n, freq, gain, decayMs = if (accented) 14.0 else 11.0, noiseAmount = 0.18)
    }

    private fun softBeep(accented: Boolean): ShortArray {
        val durationMs = if (accented) 28 else 22
        val n = SAMPLE_RATE * durationMs / 1000
        val freq = if (accented) 988.0 else 784.0 // B5 / G5 — soft, not harsh
        val gain = if (accented) 0.45 else 0.38
        return synthTick(n, freq, gain, decayMs = 20.0, noiseAmount = 0.04)
    }

    private fun hiHat(accented: Boolean): ShortArray {
        val durationMs = if (accented) 35 else 28
        val n = SAMPLE_RATE * durationMs / 1000
        val out = ShortArray(n)
        val decaySamples = (SAMPLE_RATE * (if (accented) 0.025 else 0.02)).toInt().coerceAtLeast(1)
        val gain = if (accented) 0.55 else 0.42
        var prev = 0.0
        for (i in 0 until n) {
            val env = exp(-i.toDouble() / decaySamples * 4.5)
            val attack = min(1.0, i / (SAMPLE_RATE * 0.0015))
            val white = Random.nextDouble(-1.0, 1.0)
            val hp = white - prev * 0.85
            prev = white
            val tick = hp * env * attack * gain
            out[i] = (tick * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return applyEdgeFade(out)
    }

    private fun synthTick(
        numSamples: Int,
        frequency: Double,
        gain: Double,
        decayMs: Double,
        noiseAmount: Double
    ): ShortArray {
        val out = ShortArray(numSamples)
        val decaySamples = (SAMPLE_RATE * decayMs / 1000.0).coerceAtLeast(1.0)
        val attackSamples = (SAMPLE_RATE * 0.002).toInt().coerceAtLeast(1)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = min(1.0, i.toDouble() / attackSamples)
            val env = exp(-i / decaySamples * 5.0) * attack
            val tone = sin(2.0 * PI * frequency * t)
            val noise = if (i < SAMPLE_RATE * 0.004) Random.nextDouble(-1.0, 1.0) else 0.0
            val mixed = (tone * (1.0 - noiseAmount * 0.5) + noise * noiseAmount) * env * gain
            out[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return applyEdgeFade(out)
    }

    /** Remove DC offset and fade last samples to zero — avoids buffer-edge pops. */
    private fun applyEdgeFade(samples: ShortArray): ShortArray {
        if (samples.isEmpty()) return samples
        val fadeOut = min(32, samples.size / 4)
        for (i in 0 until fadeOut) {
            val idx = samples.size - 1 - i
            val factor = (fadeOut - i).toDouble() / fadeOut
            samples[idx] = (samples[idx] * factor).toInt().toShort()
        }
        return samples
    }
}
