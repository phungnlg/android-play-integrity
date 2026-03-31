package com.hautt.playintegrity.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "integrity_proofs")
data class IntegrityProof(
    @PrimaryKey
    val requestId: String,
    val stateHash: String,
    val stateJson: String,
    val timestamp: String,
    val nonce: String,
    val integrityToken: String,
    val integrityVerdict: String,
    val isMock: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
