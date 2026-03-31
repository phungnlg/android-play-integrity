package com.hautt.playintegrity.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IntegrityProof::class], version = 1, exportSchema = false)
abstract class ProofDatabase : RoomDatabase() {

    abstract fun proofDao(): ProofDao

    companion object {
        @Volatile
        private var INSTANCE: ProofDatabase? = null

        fun getInstance(context: Context): ProofDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProofDatabase::class.java,
                    "integrity_proofs.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
