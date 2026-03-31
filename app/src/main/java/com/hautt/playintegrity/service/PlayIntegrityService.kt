package com.hautt.playintegrity.service

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlayIntegrityService(
    private val context: Context,
    private val cloudProjectNumber: Long
) : IntegrityService {

    override suspend fun requestIntegrityToken(nonce: String): IntegrityResult {
        return suspendCancellableCoroutine { cont ->
            val integrityManager = IntegrityManagerFactory.create(context)

            val request = com.google.android.play.core.integrity.IntegrityTokenRequest
                .builder()
                .setCloudProjectNumber(cloudProjectNumber)
                .setNonce(nonce)
                .build()

            integrityManager.requestIntegrityToken(request)
                .addOnSuccessListener { response ->
                    val token = response.token()
                    // In production, the token would be sent to your backend for decryption.
                    // The backend decrypts and returns the verdict.
                    // For this POC, we return the raw token with a placeholder verdict.
                    cont.resume(
                        IntegrityResult(
                            token = token,
                            verdict = "{\"note\": \"Token obtained. Send to backend for decryption.\"}",
                            isSuccess = true
                        )
                    )
                }
                .addOnFailureListener { e ->
                    cont.resume(
                        IntegrityResult(
                            token = "",
                            verdict = "",
                            isSuccess = false,
                            errorMessage = e.message ?: "Play Integrity request failed"
                        )
                    )
                }
        }
    }
}
