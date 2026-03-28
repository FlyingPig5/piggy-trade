package com.piggytrade.piggytrade.ui.wallet
import com.piggytrade.piggytrade.ui.theme.*
import com.piggytrade.piggytrade.ui.common.*
import com.piggytrade.piggytrade.ui.swap.SwapState
import com.piggytrade.piggytrade.ui.swap.SwapViewModel

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggytrade.piggytrade.R
import com.piggytrade.piggytrade.ui.swap.NodeStatus

@Composable
fun AppHeader(
    isLoading: Boolean,
    onNavigateToSettings: () -> Unit,
    nodeStatus: NodeStatus = NodeStatus.Trying("")
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: App icon + wordmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Truffle",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.truffle),
                contentDescription = "Truffle",
                modifier = Modifier.height(24.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.FillHeight
            )
        }

        // Center: Subtle node status pill
        NodeStatusPill(nodeStatus = nodeStatus)

        // Right: Loading + Settings
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = ColorAccent,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            TogaIconButton(
                icon = "\uE8B8", // ICON_COG
                onClick = onNavigateToSettings,
                modifier = Modifier.size(36.dp),
                bgColor = Color.Transparent,
                radius = 10.dp
            )
        }
    }
}

/**
 * A compact, subtle pill that shows which node the app is talking to and whether it's reachable.
 * Intentionally low-contrast — it's informational, not alarming.
 */
@Composable
private fun NodeStatusPill(nodeStatus: NodeStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "nodePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val isConnecting = nodeStatus is NodeStatus.Trying

    val dotColor = when (nodeStatus) {
        is NodeStatus.Trying   -> Color(0xFF8B949E)
        is NodeStatus.Connected -> Color(0xFF3FB950)
        is NodeStatus.Failed   -> Color(0xFFE57373)
    }

    val statusLabel = when (nodeStatus) {
        is NodeStatus.Trying   -> "Connecting to node:"
        is NodeStatus.Connected -> "Connected:"
        is NodeStatus.Failed   -> "Node unreachable"
    }

    val hostLabel: String? = when (nodeStatus) {
        is NodeStatus.Trying   -> nodeStatus.url.removePrefix("https://").removePrefix("http://").substringBefore("/").ifEmpty { null }
        is NodeStatus.Connected -> nodeStatus.url.removePrefix("https://").removePrefix("http://").substringBefore("/").ifEmpty { null }
        is NodeStatus.Failed   -> null
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = if (isConnecting) pulseAlpha else 0.9f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = statusLabel,
                color = dotColor.copy(alpha = 0.55f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
            if (hostLabel != null) {
                Text(
                    text = hostLabel,
                    color = dotColor.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun WalletSelectorCard(
    uiState: SwapState,
    viewModel: SwapViewModel,
    onNavigateToAddWallet: () -> Unit
) {
    // Wallet selector card — standalone
    WalletCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .height(50.dp)
                .androidBorder(radius = 12.dp, borderWidth = 0.dp, bgColor = ColorInputBg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            var menuWidth by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onSizeChanged {
                        menuWidth = with(density) { it.width.toDp() }
                    }
                    .clickable { expanded = true }
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(menuWidth)
                        .background(ColorCard)
                ) {
                    uiState.wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet, color = Color.White) },
                            onClick = {
                                viewModel.setSelectionContext("wallet")
                                viewModel.finalizeSelection(wallet)
                                expanded = false
                            }
                        )
                    }
                    if (uiState.wallets.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Add Wallet", color = ColorAccent) },
                            onClick = {
                                expanded = false
                                onNavigateToAddWallet()
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).padding(start = 0.dp)
                ) {
                    val walletName = uiState.selectedWallet.ifEmpty { "Select Wallet" }
                    val displayAddr = if (uiState.changeAddress.isNotEmpty()) {
                        uiState.changeAddress
                    } else {
                        uiState.selectedAddress
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = walletName.replace(" (Ergopay)", ""),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (displayAddr.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${displayAddr.take(5)}...${displayAddr.takeLast(5)})",
                                color = ColorTextDim,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                    if (uiState.selectedAddress.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val numAddrs = uiState.selectedAddresses.size
                            if (numAddrs > 1) {
                                Text(
                                    text = "$numAddrs addresses active",
                                    color = ColorTextDim.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }
                            if (uiState.isLoadingWallet) {
                                if (numAddrs > 1) Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = ColorAccent,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }

                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WalletSelectorRow(
    uiState: SwapState,
    viewModel: SwapViewModel,
    onNavigateToAddWallet: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showWalletSelector: Boolean = true
) {
    // TOP: App header — outside the wallet card, on main background
    AppHeader(
        isLoading = uiState.isLoadingQuote || uiState.isLoadingHistory,
        onNavigateToSettings = onNavigateToSettings,
        nodeStatus = uiState.nodeStatus
    )

    if (showWalletSelector) {
        WalletSelectorCard(uiState, viewModel, onNavigateToAddWallet)
    }
}
