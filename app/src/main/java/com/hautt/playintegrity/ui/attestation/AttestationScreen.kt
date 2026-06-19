package com.hautt.playintegrity.ui.attestation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hautt.playintegrity.ui.components.LabelCaps
import com.hautt.playintegrity.ui.components.MonoBlock
import com.hautt.playintegrity.ui.components.NexusCard
import com.hautt.playintegrity.ui.components.StatusChip
import com.hautt.playintegrity.ui.components.StatusKind
import com.hautt.playintegrity.ui.home.HomeViewModel
import com.hautt.playintegrity.ui.theme.IntegrityGreen
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.Outline
import com.hautt.playintegrity.ui.theme.OutlineVariant
import com.hautt.playintegrity.ui.theme.SecurityBlue
import com.hautt.playintegrity.ui.theme.SurfaceContainer

private enum class StepState { PENDING, ACTIVE, COMPLETED }

@Composable
fun AttestationScreen(
    viewModel: HomeViewModel,
    onViewProof: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    val step1 = StepState.COMPLETED
    val step2 = when {
        state.lastProof != null -> StepState.COMPLETED
        state.nonce != null -> StepState.ACTIVE
        else -> StepState.PENDING
    }
    val step3 = when {
        state.lastProof != null -> StepState.COMPLETED
        state.isLoading -> StepState.ACTIVE
        else -> StepState.PENDING
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                "Integrity Attestation",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Cryptographically bind the current application state to the Play Integrity API. " +
                    "This ensures the environment is secure and the payload is tamper-proof.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
            )
        }

        // Step 1
        StepCard(
            number = 1,
            title = "SHA-256 State Hashing",
            state = step1,
            description = "Summarizing current app state into a fixed-length cryptographic fingerprint.",
        ) {
            LabelCaps("Current State Hash", modifier = Modifier.padding(bottom = 4.dp))
            MonoBlock(state.stateHash, maxLines = 2)
        }

        // Step 2
        StepCard(
            number = 2,
            title = "Nonce Construction",
            state = step2,
            description = "Building the binding token: Base64URL(SHA-256(stateHash | ts | id)).",
        ) {
            LabelCaps("Nonce Parameters", modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NonceParam("Timestamp", state.timestamp?.take(10) ?: "—", Modifier.weight(1f))
                NonceParam("Request ID", state.requestId?.take(8) ?: "—", Modifier.weight(1f))
                NonceParam("Algorithm", "SHA-256", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            LabelCaps("Constructed Nonce", modifier = Modifier.padding(bottom = 4.dp))
            MonoBlock(state.nonce ?: "Pending attestation request…", maxLines = 3)
        }

        // Step 3
        StepCard(
            number = 3,
            title = "Integrity API Call",
            state = step3,
            description = "Transmitting the nonce to Google Play services for remote attestation.",
        ) {
            when (step3) {
                StepState.COMPLETED -> LinearProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = IntegrityGreen,
                    trackColor = SurfaceContainer,
                )
                StepState.ACTIVE -> LinearProgressIndicator(
                    progress = 0.66f,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = SecurityBlue,
                    trackColor = SurfaceContainer,
                )
                StepState.PENDING -> LinearProgressIndicator(
                    progress = 0f,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = SecurityBlue,
                    trackColor = SurfaceContainer,
                )
            }
        }

        // Execution context
        NexusCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LabelCaps("Execution Context")
                ContextRow("API Target", "v1.2 (Standard)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Enforcement", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    StatusChip("Strict", StatusKind.VERIFIED)
                }
                ContextRow("Mode", if (state.useMock) "MOCK" else "REAL")
            }
        }

        // Action button
        val success = state.lastProof != null
        Button(
            onClick = { if (success) onViewProof() else viewModel.requestAttestation() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (success) IntegrityGreen else SecurityBlue
            ),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        progress = 0.8f,
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Text("  PROCESSING…", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                success -> {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                    Text("  ATTESTATION SUCCESS - VIEW PROOF", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                else -> {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                    Text("  REQUEST ATTESTATION", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Why attest info
        NexusCard(container = SurfaceContainer) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = SecurityBlue)
                    Spacer(Modifier.width(8.dp))
                    LabelCaps("Why Attest?", color = SecurityBlue)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Attestation prevents replay attacks and ensures the app environment hasn't been " +
                        "modified by root or malicious injection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    title: String,
    state: StepState,
    description: String,
    content: @Composable () -> Unit,
) {
    val accent = when (state) {
        StepState.COMPLETED -> IntegrityGreen
        StepState.ACTIVE -> SecurityBlue
        StepState.PENDING -> Outline
    }
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (state == StepState.PENDING) SurfaceContainer else accent,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            number.toString(),
                            color = if (state == StepState.PENDING) OnSurfaceVariant else Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            StatusChip(
                                text = when (state) {
                                    StepState.COMPLETED -> "Completed"
                                    StepState.ACTIVE -> "Active"
                                    StepState.PENDING -> "Pending"
                                },
                                kind = when (state) {
                                    StepState.COMPLETED -> StatusKind.VERIFIED
                                    StepState.ACTIVE -> StatusKind.PENDING
                                    StepState.PENDING -> StatusKind.UNTRUSTED
                                },
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun NonceParam(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SurfaceContainer, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                label.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Outline,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ContextRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(
            value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
