package com.tini.tuner

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread

class MetronomeEngine(context: Context) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<Pair<MetronomeSoundType, Boolean>, Int>()
    private var playThread: Thread? = null
    private val running = AtomicBoolean(false)
    private var beatsPerBar = 4

    init {
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        preloadAllSounds()
    }

    private fun preloadAllSounds() {
        MetronomeSoundType.entries.forEach { type ->
            soundIds[type to false] = loadSample(type, accented = false)
            soundIds[type to true] = loadSample(type, accented = true)
        }
    }

    private fun loadSample(type: MetronomeSoundType, accented: Boolean): Int {
        val samples = MetronomeSampleFactory.generate(type, accented)
        val cacheDir = File(appContext.cacheDir, "metronome_pcm").apply { mkdirs() }
        val file = File(cacheDir, "${type.name}_${if (accented) "accent" else "beat"}.wav")
        if (!file.exists() || file.length() == 0L) {
            MetronomeWavUtil.writeMonoPcm16Wav(file, samples)
        }
        return soundPool.load(file.absolutePath, 1)
    }

    fun start(
        bpm: Int,
        soundType: MetronomeSoundType,
        onBeat: (accent: Boolean, beatIndex: Int) -> Unit
    ) {
        stop()
        if (bpm <= 0) return
        running.set(true)
        val intervalNs = 60_000_000_000L / bpm
        playThread = thread(name = "TiniMetronome", priority = Thread.NORM_PRIORITY + 1) {
            var beatIndex = 0
            var nextBeatNs = System.nanoTime() + intervalNs
            while (running.get()) {
                val accent = beatIndex % beatsPerBar == 0
                val id = soundIds[soundType to accent] ?: 0
                if (id != 0) {
                    soundPool.play(id, 1f, 1f, 1, 0, 1f)
                }
                onBeat(accent, beatIndex)
                beatIndex++
                nextBeatNs += intervalNs
                var waitNs = nextBeatNs - System.nanoTime()
                while (waitNs > 0 && running.get()) {
                    LockSupport.parkNanos(waitNs)
                    waitNs = nextBeatNs - System.nanoTime()
                }
            }
        }
    }

    fun stop() {
        running.set(false)
        playThread?.interrupt()
        playThread?.join(300)
        playThread = null
        soundPool.autoPause()
    }

    fun release() {
        stop()
        soundPool.release()
    }
}
