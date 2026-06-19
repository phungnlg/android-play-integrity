package com.hautt.playintegrity.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hautt.playintegrity.model.IntegrityProof
import com.hautt.playintegrity.model.ProofDatabase
import com.hautt.playintegrity.ui.components.LabelCaps
import com.hautt.playintegrity.ui.components.NexusCard
import com.hautt.playintegrity.ui.components.StatusChip
import com.hautt.playintegrity.ui.components.StatusKind
import com.hautt.playintegrity.ui.theme.IntegrityGreen
import com.hautt.playintegrity.ui.theme.InverseSurface
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.SecondaryContainer
import com.hautt.playintegrity.ui.theme.SecurityBlue
import com.hautt.playintegrity.ui.theme.SurfaceContainerLow

@Composable
fun ProofDetailScreen(requestId: String) {
    val context = LocalContext.current
    var proof by remember { mutableStateOf<IntegrityProof?>(null) }

    LaunchedEffect(requestId) {
        proof = ProofDatabase.getInstance(context).proofDao().getProofById(requestId)
    }

    val p = proof
    if (p == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = OnSurfaceVariant)
        }
        return
    }

    val v = p.integrityVerdict.uppercase()
    val device = v.contains("MEETS_DEVICE_INTEGRITY")
    val basic = device || v.contains("MEETS_BASIC_INTEGRITY")
    val strong = v.contains("MEETS_STRONG_INTEGRITY")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(if (device || basic) "Verified" else "Unevaluated",
                    if (device || basic) StatusKind.VERIFIED else StatusKind.UNTRUSTED)
                Spacer(Modifier.padding(start = 8.dp))
                Text(
                    "ID: ${p.requestId.take(13).uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                )
            }
            Spacer(Modifier.padding(top = 8.dp))
            Text(
                "Proof Attestation Detail",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.padding(top = 4.dp))
            Text(
                "Cryptographic breakdown of the device state integrity. Non-repudiable evidence of system validity at the time of capture.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
            )
        }

        // Google Verdict
        NexusCard {
            Column(modifier = Modifier.padding(16.dp)) {
                LabelCaps("Google Verdict")
                Spacer(Modifier.padding(top = 12.dp))
                VerdictRow("Device Integrity", if (device) "PASS" else "FAIL", device)
                VerdictRow("Basic Integrity", if (basic) "PASS" else "FAIL", basic)
                VerdictRow("Strong Integrity", if (strong) "ENFORCED" else "N/A", strong, accent = SecurityBlue)
                Spacer(Modifier.padding(top = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = IntegrityGreen)
                    Spacer(Modifier.padding(start = 6.dp))
                    Text(
                        "CERTIFIED BY GOOGLE PLAY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = IntegrityGreen,
                    )
                }
            }
        }

        // Binding Nonce
        CopyCard(title = "Binding Nonce", value = p.nonce, context = context) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Text(
                    p.nonce,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.padding(top = 8.dp))
            Text(
                "Single-use cryptographic salt prevents replay attacks by anchoring this proof to a specific session.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
            )
        }

        // Integrity Token (dark block)
        CopyCard(title = "Integrity Token", value = p.integrityToken, context = context) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .background(InverseSurface, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    p.integrityToken,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = SecondaryContainer,
                )
            }
        }

        // Raw State JSON
        CopyCard(title = "Raw State JSON", value = p.stateJson, context = context) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    p.stateJson,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = OnSurfaceVariant,
                )
            }
        }

        // Health assessment
        NexusCard(container = SecondaryContainer.copy(alpha = 0.25f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, null, tint = IntegrityGreen)
                    Spacer(Modifier.padding(start = 6.dp))
                    LabelCaps("Health Assessment", color = IntegrityGreen)
                }
                Spacer(Modifier.padding(top = 8.dp))
                Text(
                    "System state is nominal. No anomalies detected in the captured state sequence.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Meta
        NexusCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelCaps("Metadata")
                com.hautt.playintegrity.ui.components.KeyValueRow("Mode", if (p.isMock) "MOCK" else "REAL")
                com.hautt.playintegrity.ui.components.KeyValueRow("Timestamp", p.timestamp.take(19))
                com.hautt.playintegrity.ui.components.KeyValueRow(
                    "State Hash", "0x${p.stateHash.take(6)}…${p.stateHash.takeLast(4)}"
                )
            }
        }
    }
}

@Composable
private fun VerdictRow(label: String, value: String, pass: Boolean, accent: androidx.compose.ui.graphics.Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = accent ?: if (pass) IntegrityGreen else MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun CopyCard(
    title: String,
    value: String,
    context: Context,
    body: @Composable () -> Unit,
) {
    NexusCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelCaps(title)
                IconButton(onClick = { copy(context, title, value) }) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = SecurityBlue, modifier = Modifier.padding(0.dp))
                }
            }
            Spacer(Modifier.padding(top = 4.dp))
            body()
        }
    }
}

private fun copy(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}
