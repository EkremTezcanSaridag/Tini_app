package com.tini.tuner

import kotlin.math.*

data class SpectrumPeak(
    val frequency: Float,
    val magnitude: Float
)

object FftAnalyzer {
    private const val FFT_SIZE = 4096

    fun analyze(samples: ShortArray, sampleRate: Int): Pair<FloatArray, List<SpectrumPeak>> {
        val n = min(samples.size, FFT_SIZE)
        val real = DoubleArray(FFT_SIZE)
        val imag = DoubleArray(FFT_SIZE)
        for (i in 0 until n) {
            real[i] = samples[i].toDouble() / Short.MAX_VALUE
        }
        fft(real, imag)

        val half = FFT_SIZE / 2
        val magnitudes = FloatArray(half)
        for (i in 0 until half) {
            magnitudes[i] = sqrt(real[i] * real[i] + imag[i] * imag[i]).toFloat()
        }

        val peaks = findPeaks(magnitudes, sampleRate, maxPeaks = 6)
        return magnitudes to peaks
    }

    private fun findPeaks(magnitudes: FloatArray, sampleRate: Int, maxPeaks: Int): List<SpectrumPeak> {
        val minBin = (40.0 * FFT_SIZE / sampleRate).toInt().coerceAtLeast(1)
        val maxBin = (1000.0 * FFT_SIZE / sampleRate).toInt().coerceAtMost(magnitudes.size - 2)

        val candidates = mutableListOf<SpectrumPeak>()
        for (i in minBin until maxBin) {
            val m = magnitudes[i]
            if (m > magnitudes[i - 1] && m > magnitudes[i + 1] && m > 0.02f) {
                val freq = i.toFloat() * sampleRate / FFT_SIZE
                candidates.add(SpectrumPeak(freq, m))
            }
        }
        return candidates
            .sortedByDescending { it.magnitude }
            .take(maxPeaks)
            .sortedBy { it.frequency }
    }

    /** Match peaks to tuning string frequencies for multi-string hint. */
    fun matchPeaksToStrings(peaks: List<SpectrumPeak>, tuning: TuningType): List<Pair<Int, Float>> {
        val matches = mutableListOf<Pair<Int, Float>>()
        tuning.frequencies.forEachIndexed { index, target ->
            val best = peaks.minByOrNull { abs(it.frequency - target) }
            if (best != null && abs(best.frequency - target) < target * 0.08f) {
                matches.add(index to best.frequency)
            }
        }
        return matches
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                real[i] = real[j].also { real[j] = real[i] }
                imag[i] = imag[j].also { imag[j] = imag[i] }
            }
        }
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wlenRe = cos(ang)
            val wlenIm = sin(ang)
            var i = 0
            while (i < n) {
                var wRe = 1.0
                var wIm = 0.0
                for (k in 0 until len / 2) {
                    val uRe = real[i + k]
                    val uIm = imag[i + k]
                    val vRe = real[i + k + len / 2] * wRe - imag[i + k + len / 2] * wIm
                    val vIm = real[i + k + len / 2] * wIm + imag[i + k + len / 2] * wRe
                    real[i + k] = uRe + vRe
                    imag[i + k] = uIm + vIm
                    real[i + k + len / 2] = uRe - vRe
                    imag[i + k + len / 2] = uIm - vIm
                    val nextWRe = wRe * wlenRe - wIm * wlenIm
                    wIm = wRe * wlenIm + wIm * wlenRe
                    wRe = nextWRe
                }
                i += len
            }
            len *= 2
        }
    }
}
