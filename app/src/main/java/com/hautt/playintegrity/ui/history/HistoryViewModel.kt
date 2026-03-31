package com.hautt.playintegrity.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hautt.playintegrity.model.IntegrityProof
import com.hautt.playintegrity.model.ProofDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProofDatabase.getInstance(application).proofDao()

    val proofs: StateFlow<List<IntegrityProof>> = dao.getAllProofs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
