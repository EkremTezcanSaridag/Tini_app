package com.tini.tuner

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MetronomeWavUtil {
    fun writeMonoPcm16Wav(file: File, samples: ShortArray, sampleRate: Int = MetronomeSampleFactory.SAMPLE_RATE) {
        val dataSize = samples.size * 2
        val totalSize = 36 + dataSize
        RandomAccessFile(file, "rw").use { raf ->
            raf.setLength(0)
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(totalSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(1) // PCM
            header.putShort(1) // mono
            header.putInt(sampleRate)
            header.putInt(sampleRate * 2)
            header.putShort(2)
            header.putShort(16)
            header.put("data".toByteArray())
            header.putInt(dataSize)
            raf.write(header.array())
            val pcm = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            samples.forEach { pcm.putShort(it) }
            raf.write(pcm.array())
        }
    }
}
