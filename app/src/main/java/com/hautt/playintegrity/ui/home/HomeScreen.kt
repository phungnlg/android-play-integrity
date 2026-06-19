package com.hautt.playintegrity.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hautt.playintegrity.ui.components.LabelCaps
import com.hautt.playintegrity.ui.components.MonoBlock
import com.hautt.playintegrity.ui.components.NexusCard
import com.hautt.playintegrity.ui.components.StatusChip
import com.hautt.playintegrity.ui.components.StatusKind
import com.hautt.playintegrity.ui.theme.IntegrityGreen
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.SecurityBlue
import com.hautt.playintegrity.ui.theme.SurfaceContainer
import com.hautt.playintegrity.ui.theme.SurfaceContainerHigh

@Composable
fun CaptureScreen(
    viewModel: HomeViewModel,
    onCaptured: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // --- State Capture form card ---
        NexusCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "State Capture",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusChip("New Entry", StatusKind.VERIFIED)
                }
                Box(Modifier.padding(top = 20.dp))

                LabeledField(label = "Transaction Amount") {
                    OutlinedTextField(
                        value = state.amount,
                        onValueChange = viewModel::updateAmount,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("0.00") },
                        trailingIcon = { LabelCaps("USD") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors(),
                    )
                }

                LabeledField(label = "Recipient ID") {
                    OutlinedTextField(
                        value = state.recipientId,
                        onValueChange = viewModel::updateRecipientId,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter secure hash or ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Fingerprint, null, tint = OnSurfaceVariant)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors(),
                    )
                }

                LabeledField(label = "Source Account") {
                    SourceAccountDropdown(
                        selected = state.sourceAccount,
                        onSelect = viewModel::updateSourceAccount,
                    )
                }

                // Urgent processing toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(SurfaceContainer, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Urgent Processing",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        LabelCaps("Prioritize in next block")
                    }
                    Switch(
                        checked = state.urgent,
                        onCheckedChange = { viewModel.toggleUrgent() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = SecurityBlue,
                            checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                }

                Button(
                    onClick = onCaptured,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityBlue),
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                    Text(
                        "  Capture State",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }

        // --- Canonical JSON preview ---
        NexusCard(container = SurfaceContainer) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHigh)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, null, tint = OnSurfaceVariant)
                        Box(Modifier.padding(start = 8.dp))
                        LabelCaps("Canonical JSON Preview")
                    }
                    LabelCaps("● Live Sync", color = IntegrityGreen)
                }
                Box(Modifier.padding(16.dp)) {
                    Text(
                        text = prettyJson(state.canonicalJson),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabelCaps("Keys are sorted alphabetically for canonical hashing.")
                }
            }
        }

        // --- Runtime context ---
        NexusCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, null, tint = SecurityBlue)
                    Box(Modifier.padding(start = 8.dp))
                    LabelCaps("Runtime Context")
                }
                com.hautt.playintegrity.ui.components.KeyValueRow("Capture ID", state.captureId)
                com.hautt.playintegrity.ui.components.KeyValueRow(
                    "State Hash", "0x${state.stateHash.take(12)}…"
                )
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, field: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        LabelCaps(label, modifier = Modifier.padding(bottom = 4.dp))
        field()
    }
}

@Composable
private fun SourceAccountDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = SOURCE_ACCOUNTS.firstOrNull { it.first == selected }?.second ?: selected
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown, null,
                    modifier = Modifier.clickableNoRipple { expanded = true },
                    tint = OnSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors(),
        )
        // transparent overlay to open the menu on tap
        Box(
            Modifier
                .matchParentSize()
                .clickableNoRipple { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SOURCE_ACCOUNTS.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SecurityBlue,
    unfocusedBorderColor = com.hautt.playintegrity.ui.theme.OutlineVariant,
)

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
    )
}

/** Lightweight pretty-printer: the canonical JSON is single-line; add line breaks. */
private fun prettyJson(json: String): String {
    if (json.isBlank()) return "{}"
    val sb = StringBuilder()
    var indent = 0
    var inString = false
    for (c in json) {
        when (c) {
            '"' -> { sb.append(c); inString = !inString }
            '{', '[' -> if (inString) sb.append(c) else {
                indent++; sb.append(c).append('\n').append("  ".repeat(indent))
            }
            '}', ']' -> if (inString) sb.append(c) else {
                indent--; sb.append('\n').append("  ".repeat(indent)).append(c)
            }
            ',' -> if (inString) sb.append(c) else {
                sb.append(c).append('\n').append("  ".repeat(indent))
            }
            ':' -> sb.append(if (inString) ":" else ": ")
            else -> sb.append(c)
        }
    }
    return sb.toString()
}
