package com.tini.tuner

import kotlinx.serialization.Serializable

@Serializable
data class TuningProfile(
    val id: String,
    val name: String,
    val instrumentName: String,
    val tuningName: String,
    val a4Reference: Float,
    val themeMode: String,
    val createdAtMs: Long
)
