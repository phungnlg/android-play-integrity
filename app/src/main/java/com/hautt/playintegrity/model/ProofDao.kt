package com.hautt.playintegrity.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProofDao {

    @Query("SELECT * FROM integrity_proofs ORDER BY createdAt DESC")
    fun getAllProofs(): Flow<List<IntegrityProof>>

    @Query("SELECT * FROM integrity_proofs WHERE requestId = :requestId")
    suspend fun getProofById(requestId: String): IntegrityProof?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProof(proof: IntegrityProof)

    @Query("DELETE FROM integrity_proofs")
    suspend fun deleteAll()
}
