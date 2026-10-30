package com.tini.tuner

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class TuningUtilsTest {
    
    @Test
    fun testFrequencyToCents() {
        val cents = TuningUtils.frequencyToCents(440f, 440f)
        assertEquals(0f, cents, 0.1f)
    }
    
    @Test
    fun testFrequencyToCentsSharp() {
        val cents = TuningUtils.frequencyToCents(466.16f, 440f)
        assertTrue(abs(cents - 100f) < 1f)
    }
    
    @Test
    fun testFrequencyToCentsFlat() {
        val cents = TuningUtils.frequencyToCents(415.30f, 440f)
        assertTrue(abs(cents + 100f) < 1f)
    }
    
    @Test
    fun testFrequencyToNote() {
        assertEquals("A4", TuningUtils.frequencyToNote(440f))
        assertEquals("E2", TuningUtils.frequencyToNote(82.41f))
        assertEquals("E4", TuningUtils.frequencyToNote(329.63f))
    }
    
    @Test
    fun testTuningStateInTune() {
        val state = TuningUtils.getTuningState(110f, 110f)
        assertTrue(state.isInTune)
        assertEquals(TuningDirection.IN_TUNE, state.direction)
    }
    
    @Test
    fun testTuningStateTooLow() {
        val state = TuningUtils.getTuningState(105f, 110f)
        assertFalse(state.isInTune)
        assertEquals(TuningDirection.TOO_LOW, state.direction)
    }
    
    @Test
    fun testTuningStateTooHigh() {
        val state = TuningUtils.getTuningState(115f, 110f)
        assertFalse(state.isInTune)
        assertEquals(TuningDirection.TOO_HIGH, state.direction)
    }
    
    @Test
    fun testFindNearestStringStandard() {
        val nearestIndex = TuningUtils.findNearestString(85f, TuningType.STANDARD)
        assertEquals(0, nearestIndex)
        
        val nearestIndex2 = TuningUtils.findNearestString(330f, TuningType.STANDARD)
        assertEquals(5, nearestIndex2)
    }
    
    @Test
    fun testA4ReferenceAdjustment() {
        val state442 = TuningUtils.getTuningState(442f, 440f, 442f)
        assertTrue(abs(state442.cents) < 1f)
    }
    
    @Test
    fun testDADGADTuningFrequencies() {
        val dadgad = TuningType.DADGAD
        assertEquals("DADGAD should have 6 strings", 6, dadgad.frequencies.size)
        assertEquals("6th string should be D2 (73.42 Hz)", 73.42f, dadgad.frequencies[0], 0.01f)
        assertEquals("5th string should be A2 (110.00 Hz)", 110.00f, dadgad.frequencies[1], 0.01f)
        assertEquals("4th string should be D3 (146.83 Hz)", 146.83f, dadgad.frequencies[2], 0.01f)
        assertEquals("3rd string should be G3 (196.00 Hz)", 196.00f, dadgad.frequencies[3], 0.01f)
        assertEquals("2nd string should be A3 (220.00 Hz)", 220.00f, dadgad.frequencies[4], 0.01f)
        assertEquals("1st string should be D4 (293.66 Hz)", 293.66f, dadgad.frequencies[5], 0.01f)
    }
}
