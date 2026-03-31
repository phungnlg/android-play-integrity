package com.hautt.playintegrity.service

import com.google.gson.GsonBuilder
import com.hautt.playintegrity.model.AppStateSnapshot
import java.util.TreeMap

object StateCapture {

    private val gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    /**
     * Captures the current screen state and serializes it to canonical JSON.
     * Returns the snapshot and its canonical JSON string.
     */
    fun capture(
        screenName: String,
        fields: Map<String, String>,
        selections: List<String>
    ): Pair<AppStateSnapshot, String> {
        val snapshot = AppStateSnapshot(
            screenName = screenName,
            visibleFields = fields,
            selections = selections,
            capturedAtMillis = System.currentTimeMillis()
        )
        val canonicalJson = toCanonicalJson(snapshot)
        return Pair(snapshot, canonicalJson)
    }

    /**
     * Serializes an AppStateSnapshot to canonical JSON with sorted keys.
     * Uses TreeMap to guarantee deterministic key ordering.
     */
    fun toCanonicalJson(snapshot: AppStateSnapshot): String {
        val sorted = TreeMap<String, Any>()
        sorted["screenName"] = snapshot.screenName
        sorted["visibleFields"] = TreeMap(snapshot.visibleFields)
        sorted["selections"] = snapshot.selections
        sorted["capturedAtMillis"] = snapshot.capturedAtMillis
        return gson.toJson(sorted)
    }
}
