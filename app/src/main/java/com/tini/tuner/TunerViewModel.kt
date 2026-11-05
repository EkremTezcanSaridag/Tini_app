package com.tini.tuner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TunerUiState(
    val isListening: Boolean = false,
    val selectedStringIndex: Int = 0,
    val autoMode: Boolean = true,
    val currentInstrument: Instrument = Instrument.GUITAR,
    val currentTuning: TuningType = TuningType.STANDARD,
    val a4Reference: Float = 440f,
    val tuningState: TuningState? = null,
    val detectedFrequency: Float? = null,
    val spectrumMagnitudes: FloatArray? = null,
    val detectedPeaks: List<SpectrumPeak> = emptyList(),
    val multiStringMatches: List<Pair<Int, Float>> = emptyList(),
    val polyphonicMode: Boolean = false
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()
    
    private val audioCapture = AudioCapture()
    private val inTuneChime = InTuneChimePlayer(application)
    /** Latch: true while the current stretch is in the ±5 cent window (edge = false→true). */
    private var inTuneLatched = false
    /** Minimum gap between chimes to ignore boundary jitter. */
    private var lastInTuneChimeAtMs = 0L
    private companion object {
        const val MIN_CHIME_GAP_MS = 450L
    }
    private var captureJob: Job? = null
    private var historyRepository: TuningHistoryRepository? = null
    private var lastInTuneTime: Long = 0
    
    // Pitch smoothing with EMA (Exponential Moving Average)
    private var smoothedFrequency: Float? = null
    private val smoothingFactor = 0.3f // Lower = smoother but slower response
    
    fun setHistoryRepository(repository: TuningHistoryRepository) {
        historyRepository = repository
    }
    
    fun startListening() {
        if (_uiState.value.isListening) return
        
        _uiState.value = _uiState.value.copy(isListening = true)
        smoothedFrequency = null // Reset smoothing on start
        
        captureJob = viewModelScope.launch {
            audioCapture.startCapture().collect { frame ->
                if (frame.frequency != null) {
                    smoothedFrequency = if (smoothedFrequency == null) {
                        frame.frequency
                    } else {
                        smoothedFrequency!! * (1 - smoothingFactor) + frame.frequency * smoothingFactor
                    }
                    processFrequency(smoothedFrequency!!, frame)
                } else {
                    smoothedFrequency = null
                    resetInTuneLatch()
                    _uiState.value = _uiState.value.copy(
                        tuningState = null,
                        detectedFrequency = null,
                        spectrumMagnitudes = frame.magnitudes,
                        detectedPeaks = frame.peaks,
                        multiStringMatches = if (_uiState.value.polyphonicMode) {
                            FftAnalyzer.matchPeaksToStrings(frame.peaks, _uiState.value.currentTuning)
                        } else emptyList()
                    )
                }
            }
        }
    }
    
    fun stopListening() {
        captureJob?.cancel()
        captureJob = null
        audioCapture.stopCapture()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            tuningState = null,
            detectedFrequency = null
        )
        resetInTuneLatch()
    }

    /** Edge-triggered chime: entering in-tune only (not idle, not while staying in tune). */
    private fun onTuningStateUpdated(tuningState: TuningState?) {
        if (tuningState == null) {
            resetInTuneLatch()
            return
        }
        val inTune = tuningState.isInTune
        if (inTune && !inTuneLatched) {
            val now = System.currentTimeMillis()
            if (now - lastInTuneChimeAtMs >= MIN_CHIME_GAP_MS) {
                inTuneChime.play()
                lastInTuneChimeAtMs = now
            }
            inTuneLatched = true
        } else if (!inTune) {
            inTuneLatched = false
        }
    }

    private fun resetInTuneLatch() {
        inTuneLatched = false
    }
    
    private fun processFrequency(frequency: Float, frame: AudioFrame) {
        val currentState = _uiState.value
        val targetFrequency = currentState.currentTuning.frequencies[currentState.selectedStringIndex]
        
        if (currentState.autoMode && !currentState.polyphonicMode) {
            val nearestStringIndex = TuningUtils.findNearestString(frequency, currentState.currentTuning)
            if (nearestStringIndex != currentState.selectedStringIndex) {
                _uiState.value = currentState.copy(selectedStringIndex = nearestStringIndex)
            }
        }
        
        val tuningState = TuningUtils.getTuningState(
            frequency,
            targetFrequency,
            currentState.a4Reference
        )
        
        val multiMatches = if (currentState.polyphonicMode) {
            FftAnalyzer.matchPeaksToStrings(frame.peaks, currentState.currentTuning)
        } else emptyList()
        
        // Save to history when in-tune (max once every 3 seconds)
        if (tuningState.isInTune && historyRepository != null) {
            val now = System.currentTimeMillis()
            if (now - lastInTuneTime > 3000) {
                lastInTuneTime = now
                val stringNames = getStringNamesForTuning(currentState.currentTuning)
                viewModelScope.launch {
                    historyRepository?.addEntry(
                        TuningHistoryEntry(
                            timestamp = now,
                            tuningType = currentState.currentTuning.displayName,
                            stringNumber = currentState.selectedStringIndex + 1,
                            stringName = stringNames[currentState.selectedStringIndex],
                            targetFrequency = targetFrequency,
                            actualFrequency = frequency,
                            cents = tuningState.cents,
                            wasInTune = true
                        )
                    )
                }
            }
        }
        
        onTuningStateUpdated(tuningState)
        
        _uiState.value = _uiState.value.copy(
            tuningState = tuningState,
            detectedFrequency = frequency,
            spectrumMagnitudes = frame.magnitudes,
            detectedPeaks = frame.peaks,
            multiStringMatches = multiMatches
        )
    }
    
    fun setPolyphonicMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(polyphonicMode = enabled, autoMode = if (enabled) false else _uiState.value.autoMode)
    }
    
    private fun getStringNamesForTuning(tuning: TuningType): List<String> {
        return when (tuning) {
            TuningType.STANDARD -> listOf("E", "A", "D", "G", "B", "E")
            TuningType.DROP_D -> listOf("D", "A", "D", "G", "B", "E")
            TuningType.DROP_C -> listOf("C", "G", "C", "F", "A", "D")
            TuningType.HALF_STEP_DOWN -> listOf("Eb", "Ab", "Db", "Gb", "Bb", "Eb")
            TuningType.DROP_Db -> listOf("Db", "Ab", "Db", "Gb", "Bb", "Eb")
            TuningType.OPEN_G -> listOf("D", "G", "D", "G", "B", "D")
            TuningType.OPEN_D -> listOf("D", "A", "D", "F#", "A", "D")
            TuningType.DADGAD -> listOf("D", "A", "D", "G", "A", "D")
            TuningType.DOUBLE_DROP_D -> listOf("D", "A", "D", "G", "B", "D")
            TuningType.BASS_STANDARD -> listOf("E", "A", "D", "G")
            TuningType.BASS_DROP_D -> listOf("D", "A", "D", "G")
            TuningType.BASS_5STRING -> listOf("B", "E", "A", "D", "G")
            TuningType.UKULELE_C -> listOf("G", "C", "E", "A")
            TuningType.UKULELE_D -> listOf("A", "D", "F#", "B")
        }
    }
    
    fun selectString(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedStringIndex = index,
            autoMode = false
        )
    }
    
    fun setAutoMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoMode = enabled)
    }
    
    fun setTuning(tuning: TuningType) {
        _uiState.value = _uiState.value.copy(
            currentTuning = tuning,
            currentInstrument = tuning.instrument,
            selectedStringIndex = 0
        )
    }
    
    fun setInstrument(instrument: Instrument) {
        // Find first tuning for this instrument
        val firstTuning = TuningType.values().first { it.instrument == instrument }
        _uiState.value = _uiState.value.copy(
            currentInstrument = instrument,
            currentTuning = firstTuning,
            selectedStringIndex = 0
        )
    }
    
    fun setA4Reference(reference: Float) {
        _uiState.value = _uiState.value.copy(a4Reference = reference)
    }
    
    override fun onCleared() {
        super.onCleared()
        inTuneChime.release()
        stopListening()
    }
}
