package com.tini.tuner

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class AudioCapture {
    
    companion object {
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val BUFFER_SIZE_MULTIPLIER = 4
    }
    
    private var audioRecord: AudioRecord? = null
    private val pitchDetector = PitchDetector(SAMPLE_RATE)
    private val smoothingWindow = mutableListOf<Float>()
    private val smoothingSize = 3
    
    fun startCapture(): Flow<AudioFrame> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        
        val bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER
        val audioBuffer = ShortArray(bufferSize)
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                emit(AudioFrame(null))
                return@flow
            }
            
            audioRecord?.startRecording()
            
            while (coroutineContext.isActive) {
                val readResult = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
                
                if (readResult > 0) {
                    val pitch = pitchDetector.detectPitch(audioBuffer)
                    val (magnitudes, peaks) = FftAnalyzer.analyze(audioBuffer, SAMPLE_RATE)
                    
                    if (pitch != null) {
                        smoothingWindow.add(pitch)
                        if (smoothingWindow.size > smoothingSize) {
                            smoothingWindow.removeAt(0)
                        }
                        
                        val smoothedPitch = smoothingWindow.average().toFloat()
                        emit(AudioFrame(smoothedPitch, magnitudes, peaks))
                    } else {
                        emit(AudioFrame(null, magnitudes, peaks))
                    }
                }
            }
        } finally {
            stopCapture()
        }
    }.flowOn(Dispatchers.IO)
    
    fun stopCapture() {
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
            }
            release()
        }
        audioRecord = null
        smoothingWindow.clear()
    }
}
