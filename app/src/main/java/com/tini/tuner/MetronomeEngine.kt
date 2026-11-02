package com.tini.tuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

class MetronomeEngine {
    private var audioTrack: AudioTrack? = null
    private var job: Job? = null
    private val sampleRate = 44100
    
    fun start(bpm: Int) {
        stop()
        
        job = CoroutineScope(Dispatchers.Default).launch {
            val intervalMs = (60000 / bpm).toLong()
            
            while (isActive) {
                playClick()
                delay(intervalMs)
            }
        }
    }
    
    fun stop() {
        job?.cancel()
        job = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
    
    fun release() {
        stop()
    }
    
    private fun playClick() {
        try {
            // Generate a short click sound (1000 Hz tone, 50ms duration)
            val durationMs = 50
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)
            val frequency = 1000.0 // Hz
            
            // Generate sine wave with envelope
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = 1.0 - (i.toDouble() / numSamples) // Linear decay
                val sample = (sin(2.0 * Math.PI * frequency * t) * envelope * Short.MAX_VALUE * 0.5).toInt()
                buffer[i] = sample.toShort()
            }
            
            // Create and play AudioTrack
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            
            track.write(buffer, 0, numSamples)
            track.play()
            
            // Clean up after playback
            CoroutineScope(Dispatchers.Default).launch {
                delay(durationMs.toLong())
                track.stop()
                track.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
