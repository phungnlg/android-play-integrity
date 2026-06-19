package com.hautt.playintegrity.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hautt.playintegrity.ui.theme.IntegrityGreen
import com.hautt.playintegrity.ui.theme.OnSurfaceVariant
import com.hautt.playintegrity.ui.theme.OutlineVariant
import com.hautt.playintegrity.ui.theme.SecurityBlue
import com.hautt.playintegrity.ui.theme.SurfaceContainer
import com.hautt.playintegrity.ui.theme.SurfaceContainerLow
import com.hautt.playintegrity.ui.theme.WarningYellow

/** Top app bar matching the IntegrityGuard header (security icon + brand). */
@Composable
fun IntegrityTopBar(
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showBack) {
                        IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp)) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = SecurityBlue,
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = SecurityBlue,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text(
                        text = "IntegrityGuard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SecurityBlue,
                    )
                }
                if (trailing != null) {
                    trailing()
                } else {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                    )
                }
            }
            HairlineDivider()
        }
    }
}

@Composable
fun HairlineDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(OutlineVariant)
    )
}

/** All-caps monospace micro-label ("label-caps" token). */
@Composable
fun LabelCaps(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = OnSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
    )
}

/** White surface card with 1px outline + soft lift (Surface Level 1). */
@Composable
fun NexusCard(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) { content() }
}

enum class StatusKind { VERIFIED, PENDING, UNTRUSTED }

/** Pill-shaped status chip: green / yellow / red at 12% tint. */
@Composable
fun StatusChip(text: String, kind: StatusKind, modifier: Modifier = Modifier) {
    val color = when (kind) {
        StatusKind.VERIFIED -> IntegrityGreen
        StatusKind.PENDING -> WarningYellow
        StatusKind.UNTRUSTED -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 0.6.sp,
        )
    }
}

/** Monospace data block on a tinted background, for hashes / nonces / tokens. */
@Composable
fun MonoBlock(
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Key on the left, monospace value chip on the right. */
@Composable
fun KeyValueRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .background(SurfaceContainer, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
