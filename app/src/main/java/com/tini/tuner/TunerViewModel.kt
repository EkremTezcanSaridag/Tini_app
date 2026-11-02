package com.tini.tuner

import androidx.lifecycle.ViewModel
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
    val currentTuning: TuningType = TuningType.STANDARD,
    val a4Reference: Float = 440f,
    val tuningState: TuningState? = null,
    val detectedFrequency: Float? = null
)

class TunerViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()
    
    private val audioCapture = AudioCapture()
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
            audioCapture.startCapture().collect { frequency ->
                if (frequency != null) {
                    // Apply EMA smoothing
                    smoothedFrequency = if (smoothedFrequency == null) {
                        frequency
                    } else {
                        smoothedFrequency!! * (1 - smoothingFactor) + frequency * smoothingFactor
                    }
                    processFrequency(smoothedFrequency!!)
                } else {
                    smoothedFrequency = null
                    _uiState.value = _uiState.value.copy(
                        tuningState = null,
                        detectedFrequency = null
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
    }
    
    private fun processFrequency(frequency: Float) {
        val currentState = _uiState.value
        val targetFrequency = currentState.currentTuning.frequencies[currentState.selectedStringIndex]
        
        if (currentState.autoMode) {
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
        
        _uiState.value = _uiState.value.copy(
            tuningState = tuningState,
            detectedFrequency = frequency
        )
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
            selectedStringIndex = 0
        )
    }
    
    fun setA4Reference(reference: Float) {
        _uiState.value = _uiState.value.copy(a4Reference = reference)
    }
    
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
