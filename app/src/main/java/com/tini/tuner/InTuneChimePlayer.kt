package com.tini.tuner

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

/**
 * Short success "tick" when a string enters the in-tune window — not metronome PCM.
 */
class InTuneChimePlayer(context: Context) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private var soundId: Int = 0
    private var loaded = false

    init {
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }
        val cacheDir = File(appContext.cacheDir, "tuner_sfx").apply { mkdirs() }
        val file = File(cacheDir, "in_tune_chime.wav")
        if (!file.exists() || file.length() == 0L) {
            MetronomeWavUtil.writeMonoPcm16Wav(file, InTuneChimeSample.generate())
        }
        soundId = soundPool.load(file.absolutePath, 1)
    }

    fun play() {
        if (soundId == 0) return
        soundPool.play(soundId, 0.72f, 0.72f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

private object InTuneChimeSample {
    private const val SAMPLE_RATE = 44100
    private const val DURATION_SEC = 0.22f

    fun generate(): ShortArray {
        val n = (SAMPLE_RATE * DURATION_SEC).toInt()
        val samples = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toFloat() / SAMPLE_RATE
            val env = kotlin.math.exp(-t * 16.0).toFloat()
            val wave = 0.5f * sin(2.0 * PI * 880.0 * t).toFloat() +
                0.22f * sin(2.0 * PI * 1760.0 * t).toFloat()
            val amp = (env * wave * 32767 * 0.9f).toInt().coerceIn(-32768, 32767)
            samples[i] = amp.toShort()
        }
        return samples
    }
}
