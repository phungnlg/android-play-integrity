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

data class HomeUiState(
    val inputText: String = "Sample transaction data",
    val selectedType: TransactionType = TransactionType.PAYMENT,
    val useMock: Boolean = BuildConfig.USE_MOCK_INTEGRITY,
    val isLoading: Boolean = false,
    val lastProof: IntegrityProof? = null,
    val errorMessage: String? = null
)

enum class TransactionType { PAYMENT, TRANSFER, QUERY }

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ProofDatabase.getInstance(application).proofDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun updateSelectedType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun toggleMockMode() {
        _uiState.value = _uiState.value.copy(useMock = !_uiState.value.useMock)
    }

    fun captureAndVerify() {
        val state = _uiState.value
        if (state.isLoading) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // 1. Capture screen state
                val fields = mapOf(
                    "inputText" to state.inputText,
                    "transactionType" to state.selectedType.name
                )
                val selections = listOf(state.selectedType.name)
                val (snapshot, canonicalJson) = StateCapture.capture(
                    screenName = "HomeScreen",
                    fields = fields,
                    selections = selections
                )

                // 2. Hash the state
                val stateHash = CryptoService.sha256Hex(canonicalJson)

                // 3. Generate request ID and timestamp
                val requestId = CryptoService.generateRequestId()
                val timestamp = Instant.now().toString()

                // 4. Build nonce (binds state + time + request)
                val nonce = CryptoService.buildNonce(stateHash, timestamp, requestId)

                // 5. Request integrity token
                val service: IntegrityService = if (state.useMock) {
                    MockIntegrityService()
                } else {
                    PlayIntegrityService(
                        getApplication(),
                        BuildConfig.CLOUD_PROJECT_NUMBER
                    )
                }
                val result = service.requestIntegrityToken(nonce)

                if (!result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.errorMessage ?: "Integrity check failed"
                    )
                    return@launch
                }

                // 6. Build proof object
                val proof = IntegrityProof(
                    requestId = requestId,
                    stateHash = stateHash,
                    stateJson = canonicalJson,
                    timestamp = timestamp,
                    nonce = nonce,
                    integrityToken = result.token,
                    integrityVerdict = result.verdict,
                    isMock = state.useMock
                )

                // 7. Persist to Room
                dao.insertProof(proof)

                // 8. Update UI
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastProof = proof
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
}
