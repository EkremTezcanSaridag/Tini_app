package com.tini.tuner

import kotlinx.serialization.Serializable

@Serializable
data class TuningHistoryEntry(
    val timestamp: Long,
    val tuningType: String,
    val stringNumber: Int,
    val stringName: String,
    val targetFrequency: Float,
    val actualFrequency: Float,
    val cents: Float,
    val wasInTune: Boolean
)
