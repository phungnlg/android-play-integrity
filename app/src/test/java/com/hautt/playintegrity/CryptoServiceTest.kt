package com.hautt.playintegrity

import com.hautt.playintegrity.service.CryptoService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoServiceTest {

    @Test
    fun sha256Hex_deterministic() {
        val input = "hello world"
        val hash1 = CryptoService.sha256Hex(input)
        val hash2 = CryptoService.sha256Hex(input)
        assertEquals(hash1, hash2)
    }

    @Test
    fun sha256Hex_knownValue() {
        // SHA-256 of "hello world" is well-known
        val expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
        val actual = CryptoService.sha256Hex("hello world")
        assertEquals(expected, actual)
    }

    @Test
    fun sha256Hex_differentInputs() {
        val hash1 = CryptoService.sha256Hex("input1")
        val hash2 = CryptoService.sha256Hex("input2")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun sha256Hex_length() {
        val hash = CryptoService.sha256Hex("test")
        assertEquals(64, hash.length) // 256 bits = 64 hex chars
    }

    @Test
    fun buildNonce_deterministic() {
        val nonce1 = CryptoService.buildNonce("hash1", "2024-01-01T00:00:00Z", "req-1")
        val nonce2 = CryptoService.buildNonce("hash1", "2024-01-01T00:00:00Z", "req-1")
        assertEquals(nonce1, nonce2)
    }

    @Test
    fun buildNonce_differentWithDifferentTimestamp() {
        val nonce1 = CryptoService.buildNonce("hash1", "2024-01-01T00:00:00Z", "req-1")
        val nonce2 = CryptoService.buildNonce("hash1", "2024-01-01T00:00:01Z", "req-1")
        assertNotEquals(nonce1, nonce2)
    }

    @Test
    fun buildNonce_differentWithDifferentHash() {
        val nonce1 = CryptoService.buildNonce("hash1", "2024-01-01T00:00:00Z", "req-1")
        val nonce2 = CryptoService.buildNonce("hash2", "2024-01-01T00:00:00Z", "req-1")
        assertNotEquals(nonce1, nonce2)
    }

    @Test
    fun buildNonce_isBase64UrlSafe() {
        val nonce = CryptoService.buildNonce("hash", "ts", "req")
        // Base64URL: only [A-Za-z0-9_-] (no +, /, or =)
        assertTrue(nonce.matches(Regex("[A-Za-z0-9_-]+")))
    }

    @Test
    fun generateRequestId_isUUID() {
        val id = CryptoService.generateRequestId()
        // UUID format: 8-4-4-4-12
        assertTrue(id.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }

    @Test
    fun generateRequestId_unique() {
        val ids = (1..100).map { CryptoService.generateRequestId() }.toSet()
        assertEquals(100, ids.size)
    }
}
