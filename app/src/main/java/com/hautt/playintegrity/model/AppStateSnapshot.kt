package com.hautt.playintegrity.model

data class AppStateSnapshot(
    val screenName: String,
    val visibleFields: Map<String, String>,
    val selections: List<String>,
    val capturedAtMillis: Long
)
