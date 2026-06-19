package com.hautt.playintegrity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hautt.playintegrity.ui.attestation.AttestationScreen
import com.hautt.playintegrity.ui.components.IntegrityTopBar
import com.hautt.playintegrity.ui.components.LabelCaps
import com.hautt.playintegrity.ui.history.HistoryScreen
import com.hautt.playintegrity.ui.home.CaptureScreen
import com.hautt.playintegrity.ui.home.HomeViewModel
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.PlayIntegrityTheme
import com.hautt.playintegrity.ui.theme.SecondaryContainer
import com.hautt.playintegrity.ui.theme.SecurityBlue

private enum class Tab(val label: String) { CAPTURE("Capture"), ATTESTATION("Attestation"), VAULT("Vault") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayIntegrityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val vm: HomeViewModel = viewModel()
                    val state by vm.uiState.collectAsState()
                    var tab by rememberSaveable { mutableStateOf(Tab.CAPTURE) }
                    var detailId by rememberSaveable { mutableStateOf<String?>(null) }

                    val mockToggle: @Composable () -> Unit = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LabelCaps("Mock", color = if (state.useMock) SecurityBlue else OnSurfaceVariant)
                            Switch(
                                checked = !state.useMock,
                                onCheckedChange = { vm.toggleMockMode() },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = SecurityBlue,
                                    checkedThumbColor = Color.White,
                                ),
                            )
                            LabelCaps("Real", color = if (!state.useMock) SecurityBlue else OnSurfaceVariant)
                        }
                    }

                    if (detailId != null) {
                        Scaffold(
                            topBar = { IntegrityTopBar(showBack = true, onBack = { detailId = null }) },
                        ) { pad ->
                            Box(Modifier.padding(pad)) {
                                com.hautt.playintegrity.ui.detail.ProofDetailScreen(detailId!!)
                            }
                        }
                    } else {
                        Scaffold(
                            topBar = { IntegrityTopBar(trailing = mockToggle) },
                            bottomBar = {
                                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                                    NavBarItem(Tab.CAPTURE, tab, Icons.Default.CameraAlt) { tab = it }
                                    NavBarItem(Tab.ATTESTATION, tab, Icons.Default.Gavel) { tab = it }
                                    NavBarItem(Tab.VAULT, tab, Icons.Default.Lock) { tab = it }
                                }
                            },
                        ) { pad ->
                            Box(Modifier.padding(pad)) {
                                when (tab) {
                                    Tab.CAPTURE -> CaptureScreen(vm, onCaptured = { tab = Tab.ATTESTATION })
                                    Tab.ATTESTATION -> AttestationScreen(vm, onViewProof = { tab = Tab.VAULT })
                                    Tab.VAULT -> HistoryScreen(onProofTap = { detailId = it })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavBarItem(
    item: Tab,
    selected: Tab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: (Tab) -> Unit,
) {
    NavigationBarItem(
        selected = selected == item,
        onClick = { onSelect(item) },
        icon = { Icon(icon, contentDescription = item.label) },
        label = { Text(item.label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = com.hautt.playintegrity.ui.theme.OnSecondaryContainer,
            selectedTextColor = com.hautt.playintegrity.ui.theme.OnSecondaryContainer,
            indicatorColor = SecondaryContainer,
            unselectedIconColor = OnSurfaceVariant,
            unselectedTextColor = OnSurfaceVariant,
        ),
    )
}
