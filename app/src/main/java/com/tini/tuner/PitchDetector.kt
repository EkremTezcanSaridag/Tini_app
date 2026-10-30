package com.tini.tuner

import kotlin.math.abs
import kotlin.math.sqrt

class PitchDetector(private val sampleRate: Int) {
    
    private val threshold = 0.15
    
    fun detectPitch(audioBuffer: ShortArray): Float? {
        val floatBuffer = audioBuffer.map { it.toFloat() / Short.MAX_VALUE }.toFloatArray()
        
        val rms = sqrt(floatBuffer.map { it * it }.average().toFloat())
        if (rms < 0.01f) {
            return null
        }
        
        return yinPitchDetection(floatBuffer)
    }
    
    private fun yinPitchDetection(buffer: FloatArray): Float? {
        val bufferSize = buffer.size
        val halfSize = bufferSize / 2
        val yinBuffer = FloatArray(halfSize)
        
        yinBuffer[0] = 1f
        
        var runningSum = 0f
        for (tau in 1 until halfSize) {
            var sum = 0f
            for (i in 0 until halfSize) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
            runningSum += yinBuffer[tau]
            
            if (runningSum != 0f) {
                yinBuffer[tau] *= tau / runningSum
            } else {
                yinBuffer[tau] = 1f
            }
        }
        
        var tauEstimate = -1
        var tau = 2
        while (tau < halfSize) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < halfSize && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                tauEstimate = tau
                break
            }
            tau++
        }
        
        if (tauEstimate == -1) {
            return null
        }
        
        val betterTau = parabolicInterpolation(yinBuffer, tauEstimate)
        
        val pitchHz = sampleRate / betterTau
        
        if (pitchHz < 50f || pitchHz > 1000f) {
            return null
        }
        
        return pitchHz
    }
    
    private fun parabolicInterpolation(array: FloatArray, tau: Int): Float {
        if (tau < 1 || tau >= array.size - 1) {
            return tau.toFloat()
        }
        
        val s0 = array[tau - 1]
        val s1 = array[tau]
        val s2 = array[tau + 1]
        
        val adjustment = (s2 - s0) / (2 * (2 * s1 - s2 - s0))
        
        return if (adjustment.isNaN() || adjustment.isInfinite()) {
            tau.toFloat()
        } else {
            tau + adjustment
        }
    }
}
