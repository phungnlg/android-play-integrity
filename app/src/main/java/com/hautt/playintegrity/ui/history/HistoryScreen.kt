package com.hautt.playintegrity.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hautt.playintegrity.model.IntegrityProof
import com.hautt.playintegrity.ui.components.LabelCaps
import com.hautt.playintegrity.ui.components.NexusCard
import com.hautt.playintegrity.ui.components.StatusChip
import com.hautt.playintegrity.ui.components.StatusKind
import com.hautt.playintegrity.ui.theme.IntegrityGreen
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.Outline
import com.hautt.playintegrity.ui.theme.SurfaceContainer
import com.hautt.playintegrity.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onProofTap: (String) -> Unit = {},
) {
    val proofs by viewModel.proofs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    LabelCaps("Repository")
                    Text(
                        "Proof Vault",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (proofs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
        item {
            Text(
                "Immutable record of cryptographic attestations retrieved from the local secure enclave and Room database.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
            )
        }

        if (proofs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No proofs yet - capture one to begin.", color = OnSurfaceVariant)
                }
            }
        } else {
            items(proofs, key = { it.requestId }) { proof ->
                ProofVaultCard(proof = proof, onClick = { onProofTap(proof.requestId) })
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

private data class VerdictBadge(val label: String, val kind: StatusKind)

private fun verdictBadge(proof: IntegrityProof): VerdictBadge {
    val v = proof.integrityVerdict.uppercase()
    return when {
        v.contains("STRONG_INTEGRITY") -> VerdictBadge("STRONG_INTEGRITY", StatusKind.VERIFIED)
        v.contains("MEETS_DEVICE_INTEGRITY") -> VerdictBadge("MEETS_DEVICE_INTEGRITY", StatusKind.VERIFIED)
        v.contains("MEETS_BASIC_INTEGRITY") || v.contains("BASIC") -> VerdictBadge("BASIC_INTEGRITY", StatusKind.PENDING)
        else -> VerdictBadge("UNEVALUATED", StatusKind.UNTRUSTED)
    }
}

@Composable
private fun ProofVaultCard(proof: IntegrityProof, onClick: () -> Unit) {
    val badge = verdictBadge(proof)
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(proof.createdAt))
    val day = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(proof.createdAt))
    val iconColor = if (badge.kind == StatusKind.PENDING) WarningYellow else IntegrityGreen

    NexusCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (badge.kind == StatusKind.PENDING) Icons.Default.Pending else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = iconColor,
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(badge.label, badge.kind)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            if (proof.isMock) "mock" else "real",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Outline,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "System Attestation",
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Fingerprint,
                            null,
                            tint = Outline,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "0x${proof.stateHash.take(3)}...${proof.stateHash.takeLast(4)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                LabelCaps(day)
                Spacer(Modifier.height(2.dp))
                Text(
                    time,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Outline,
                )
            }
        }
    }
}
