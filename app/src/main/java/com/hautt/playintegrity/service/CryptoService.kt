package com.hautt.playintegrity.service

import android.util.Base64
import java.security.MessageDigest
import java.util.UUID

object CryptoService {

    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun buildNonce(stateHash: String, timestamp: String, requestId: String): String {
        val combined = "$stateHash|$timestamp|$requestId"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun generateRequestId(): String = UUID.randomUUID().toString()
}
