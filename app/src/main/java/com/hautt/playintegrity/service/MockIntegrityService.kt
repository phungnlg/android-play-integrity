package com.hautt.playintegrity.service

import kotlinx.coroutines.delay

class MockIntegrityService : IntegrityService {

    override suspend fun requestIntegrityToken(nonce: String): IntegrityResult {
        // Simulate network latency
        delay(500)

        val mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJub25jZSI6IiR7bm9uY2V9IiwicmVxdWVzdERldGFpbHMiOnsiZGV2aWNlSW50ZWdy" +
            "aXR5Ijp7ImRldmljZVJlY29nbml0aW9uVmVyZGljdCI6WyJNRUVUU19ERVZJQ0VfSU5U" +
            "RUdSSVRZIl19fX0.mock_signature_${nonce.take(8)}"

        val mockVerdict = """
            {
              "requestDetails": {
                "requestPackageName": "com.hautt.playintegrity",
                "nonce": "$nonce",
                "timestampMillis": ${System.currentTimeMillis()}
              },
              "appIntegrity": {
                "appRecognitionVerdict": "PLAY_RECOGNIZED"
              },
              "deviceIntegrity": {
                "deviceRecognitionVerdict": ["MEETS_DEVICE_INTEGRITY"]
              },
              "accountDetails": {
                "appLicensingVerdict": "LICENSED"
              }
            }
        """.trimIndent()

        return IntegrityResult(
            token = mockToken,
            verdict = mockVerdict,
            isSuccess = true
        )
    }
}
