package com.hautt.playintegrity.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hautt.playintegrity.BuildConfig
import com.hautt.playintegrity.model.IntegrityProof
import com.hautt.playintegrity.model.ProofDatabase
import com.hautt.playintegrity.service.CryptoService
import com.hautt.playintegrity.service.IntegrityService
import com.hautt.playintegrity.service.MockIntegrityService
import com.hautt.playintegrity.service.PlayIntegrityService
import com.hautt.playintegrity.service.StateCapture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/** Source accounts offered in the State Capture form (matches Stitch design). */
val SOURCE_ACCOUNTS = listOf(
    "ACC-7702-X" to "Corporate Treasury (ACC-7702-X)",
    "ACC-1194-P" to "Operational Reserve (ACC-1194-P)",
    "ACC-4001-Q" to "Cold Storage Vault (ACC-4001-Q)",
)

data class HomeUiState(
    // capture form
    val amount: String = "2500.00",
    val recipientId: String = "0xA17F-CC92",
    val sourceAccount: String = SOURCE_ACCOUNTS.first().first,
    val urgent: Boolean = false,
    val useMock: Boolean = BuildConfig.USE_MOCK_INTEGRITY,
    // live-derived capture data
    val canonicalJson: String = "",
    val stateHash: String = "",
    // attestation flow
    val requestId: String? = null,
    val timestamp: String? = null,
    val nonce: String? = null,
    val isLoading: Boolean = false,
    val lastProof: IntegrityProof? = null,
    val errorMessage: String? = null,
) {
    val captureId: String
        get() = if (stateHash.length >= 8)
            "0x${stateHash.take(4)}...${stateHash.takeLast(4)}" else "0x0000...0000"
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProofDatabase.getInstance(application).proofDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        recompute()
    }

    fun updateAmount(v: String) { _uiState.value = _uiState.value.copy(amount = v); recompute() }
    fun updateRecipientId(v: String) { _uiState.value = _uiState.value.copy(recipientId = v); recompute() }
    fun updateSourceAccount(v: String) { _uiState.value = _uiState.value.copy(sourceAccount = v); recompute() }
    fun toggleUrgent() { _uiState.value = _uiState.value.copy(urgent = !_uiState.value.urgent); recompute() }
    fun toggleMockMode() { _uiState.value = _uiState.value.copy(useMock = !_uiState.value.useMock) }

    /** Recompute canonical JSON + state hash whenever a form field changes. */
    private fun recompute() {
        val s = _uiState.value
        val fields = mapOf(
            "transaction_amount" to s.amount,
            "recipient_id" to s.recipientId,
            "source_account" to s.sourceAccount,
            "urgent_processing" to s.urgent.toString(),
        )
        val (_, canonicalJson) = StateCapture.capture(
            screenName = "StateCapture",
            fields = fields,
            selections = if (s.urgent) listOf("URGENT") else emptyList(),
        )
        val hash = CryptoService.sha256Hex(canonicalJson)
        _uiState.value = _uiState.value.copy(canonicalJson = canonicalJson, stateHash = hash)
    }

    /** Build the binding nonce and request a Play Integrity token for the captured state. */
    fun requestAttestation() {
        val state = _uiState.value
        if (state.isLoading) return

        val requestId = CryptoService.generateRequestId()
        val timestamp = Instant.now().toString()
        val nonce = CryptoService.buildNonce(state.stateHash, timestamp, requestId)

        _uiState.value = state.copy(
            isLoading = true,
            errorMessage = null,
            requestId = requestId,
            timestamp = timestamp,
            nonce = nonce,
        )

        viewModelScope.launch {
            try {
                val service: IntegrityService = if (state.useMock) {
                    MockIntegrityService()
                } else {
                    PlayIntegrityService(getApplication(), BuildConfig.CLOUD_PROJECT_NUMBER)
                }
                val result = service.requestIntegrityToken(nonce)

                if (!result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorMessage ?: "Integrity check failed",
                    )
                    return@launch
                }

                val proof = IntegrityProof(
                    requestId = requestId,
                    stateHash = state.stateHash,
                    stateJson = state.canonicalJson,
                    timestamp = timestamp,
                    nonce = nonce,
                    integrityToken = result.token,
                    integrityVerdict = result.verdict,
                    isMock = state.useMock,
                )
                dao.insertProof(proof)

                _uiState.value = _uiState.value.copy(isLoading = false, lastProof = proof)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error",
                )
            }
        }
    }
}
