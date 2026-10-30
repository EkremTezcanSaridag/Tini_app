package com.tini.tuner

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.*

class PitchDetectorTest {
    
    private lateinit var pitchDetector: PitchDetector
    private val sampleRate = 44100
    
    @Before
    fun setup() {
        pitchDetector = PitchDetector(sampleRate)
    }
    
    private fun generateSineWave(frequency: Float, durationSeconds: Float = 0.5f): ShortArray {
        val numSamples = (sampleRate * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)
        
        for (i in 0 until numSamples) {
            val sample = sin(2.0 * PI * frequency * i / sampleRate)
            buffer[i] = (sample * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        
        return buffer
    }
    
    private fun assertFrequencyWithinCents(expected: Float, actual: Float, maxCents: Float) {
        val cents = abs(1200 * ln(actual / expected) / ln(2.0)).toFloat()
        assertTrue(
            "Expected $expected Hz, got $actual Hz (${cents.toInt()} cents difference, max $maxCents cents)",
            cents <= maxCents
        )
    }
    
    @Test
    fun testDetectE2String() {
        val targetFreq = 82.41f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect E2 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectA2String() {
        val targetFreq = 110.00f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect A2 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectD3String() {
        val targetFreq = 146.83f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect D3 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectG3String() {
        val targetFreq = 196.00f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect G3 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectB3String() {
        val targetFreq = 246.94f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect B3 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectE4String() {
        val targetFreq = 329.63f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect E4 frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectDropDString() {
        val targetFreq = 73.42f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect Drop D frequency", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDetectA440() {
        val targetFreq = 440.00f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect A4 (440 Hz)", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testSilenceReturnsNull() {
        val silence = ShortArray(sampleRate / 2) { 0 }
        val detected = pitchDetector.detectPitch(silence)
        
        assertNull("Silence should return null", detected)
    }
    
    @Test
    fun testVeryLowAmplitudeReturnsNull() {
        val targetFreq = 110.00f
        val numSamples = (sampleRate * 0.5f).toInt()
        val buffer = ShortArray(numSamples)
        
        for (i in 0 until numSamples) {
            val sample = sin(2.0 * PI * targetFreq * i / sampleRate)
            buffer[i] = (sample * Short.MAX_VALUE * 0.005).toInt().toShort()
        }
        
        val detected = pitchDetector.detectPitch(buffer)
        assertNull("Very low amplitude should return null", detected)
    }
    
    @Test
    fun testSlightlyFlatNote() {
        val targetFreq = 110.00f
        val flatFreq = targetFreq * 0.995f
        val sineWave = generateSineWave(flatFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect slightly flat note", detected)
        assertTrue("Detected frequency should be close to $flatFreq", 
            abs(detected!! - flatFreq) < 2f)
    }
    
    @Test
    fun testSlightlySharpNote() {
        val targetFreq = 110.00f
        val sharpFreq = targetFreq * 1.005f
        val sineWave = generateSineWave(sharpFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect slightly sharp note", detected)
        assertTrue("Detected frequency should be close to $sharpFreq", 
            abs(detected!! - sharpFreq) < 2f)
    }
    
    @Test
    fun testDADGADString1_D2() {
        val targetFreq = 73.42f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect DADGAD 6th string (D2)", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDADGADString5_A3() {
        val targetFreq = 220.00f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect DADGAD 2nd string (A3)", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
    
    @Test
    fun testDADGADString6_D4() {
        val targetFreq = 293.66f
        val sineWave = generateSineWave(targetFreq)
        val detected = pitchDetector.detectPitch(sineWave)
        
        assertNotNull("Should detect DADGAD 1st string (D4)", detected)
        assertFrequencyWithinCents(targetFreq, detected!!, 1f)
    }
}
