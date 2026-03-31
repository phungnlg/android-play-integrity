package com.hautt.playintegrity.service

data class IntegrityResult(
    val token: String,
    val verdict: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

interface IntegrityService {
    suspend fun requestIntegrityToken(nonce: String): IntegrityResult
}
