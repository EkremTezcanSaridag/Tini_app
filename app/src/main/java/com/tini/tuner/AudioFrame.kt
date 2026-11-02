package com.tini.tuner

data class AudioFrame(
    val frequency: Float?,
    val magnitudes: FloatArray? = null,
    val peaks: List<SpectrumPeak> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioFrame) return false
        return frequency == other.frequency && peaks == other.peaks
    }

    override fun hashCode(): Int = frequency.hashCode() * 31 + peaks.hashCode()
}
