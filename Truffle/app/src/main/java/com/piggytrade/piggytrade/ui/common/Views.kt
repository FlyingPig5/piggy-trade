package com.piggytrade.piggytrade.ui.common
import com.piggytrade.piggytrade.ui.theme.*
import com.piggytrade.piggytrade.ui.home.*
import com.piggytrade.piggytrade.ui.swap.*
import com.piggytrade.piggytrade.ui.wallet.*
import com.piggytrade.piggytrade.ui.settings.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import coil.request.ImageRequest

@Composable
fun SyncProgressPopup(progress: SyncProgress, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (progress.isFinished) onDismiss() },
        title = { 
            Text(
                if (progress.isFinished) "Sync Complete" 
                else if (progress.isFirstLaunch) "First launch, syncing trading pairs with dex contracts..."
                else "Syncing Tokens...", 
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (progress.isFirstLaunch && !progress.isFinished) 14.sp else 18.sp
            ) 
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!progress.isFinished) {
                    if (progress.total < 0) {
                        // Fetching boxes phase: spinner + batch information
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ColorAccent,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = progress.batchInfo.ifEmpty { "Connecting to node..." },
                                color = ColorTextDim,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    } else {
                        // Processing boxes phase: progress bar
                        LinearProgressIndicator(
                            progress = { if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            color = ColorAccent,
                            trackColor = ColorInputBg
                        )
                        if (progress.isFirstLaunch) {
                            Text(
                                "Processed ${progress.current} of ${progress.total} boxes", 
                                color = ColorTextDim,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    if (progress.newTokens.isNotEmpty()) {
                        Text(
                            "New pairs discovered: ${progress.newTokens.size}", 
                            color = ColorAccent, 
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    Text(
                        if (progress.newTokens.isEmpty()) "No new pairs found." else "Found ${progress.newTokens.size} new trading pairs!", 
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (progress.isFirstLaunch) {
                         Text(
                            "Processed ${progress.total} boxes.", 
                            color = ColorTextDim,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "${progress.newTokens.size} new pairs added:", 
                        color = ColorAccent, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    val list = progress.newTokens.joinToString(", ")
                    Text(
                        if (list.isEmpty()) "None" else list, 
                        color = ColorTextDim, 
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (progress.isFinished) {
                Button(
                    onClick = onDismiss, 
                    colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = ColorBg)
                }
            }
        },
        containerColor = ColorCard,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Exact replication of `apply_android_border` from `ui_views.py`.
 */
@Composable
fun Modifier.androidBorder(
    radius: Dp = 15.dp,
    borderWidth: Dp = 1.dp,
    bgColor: Color = ColorInputBg,
    borderColor: Color = Color(0xFF21262D)
): Modifier {
    val shape = RoundedCornerShape(radius)
    val finalBorderColor = if (borderColor == Color.Transparent) Color.Transparent else borderColor
    return this
        .then(if (bgColor != Color.Transparent) Modifier.background(bgColor, shape) else Modifier)
        .then(if (borderWidth > 0.dp && finalBorderColor != Color.Transparent) Modifier.border(borderWidth, finalBorderColor, shape) else Modifier)
        .clip(shape)
}

/**
 * Replicates Toga Box(direction=COLUMN) mapping.
 */
@Composable
fun TogaColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * Replicates Toga Box(direction=ROW) mapping.
 */
@Composable
fun TogaRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * Port of `trade_card` layout from `ui_views.py` showing exact padding mappings:
 * margin=10, margin_top=2, padding_top=20, radius=15, v_padding=15
 */
@Composable
fun TradeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    TogaColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 10.dp) // margin_top = 0
            .androidBorder(radius = 15.dp, borderWidth = 0.dp, bgColor = ColorCard)
            .padding(top = 10.dp, bottom = 10.dp) // internal padding reduced
    ) {
        content()
    }
}

/**
 * Port of `wallet_card` layout from `ui_views.py`.
 */
@Composable
fun WalletCard(content: @Composable ColumnScope.() -> Unit) {
    TogaColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 2.dp)
            .androidBorder(radius = 15.dp, borderWidth = 0.dp, bgColor = ColorCard)
            .padding(vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
fun TogaIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bgColor: Color = ColorInputBg,
    iconColor: Color = ColorIcon,
    iconSize: Dp = 14.dp, // Match FONT_SIZE_ICON=14
    radius: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color(0xFF535C6E),
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentAlpha = if (enabled) 1.0f else 0.4f
    Box(
        modifier = modifier
            .androidBorder(radius = radius, borderWidth = borderWidth, borderColor = borderColor, bgColor = bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, radius = radius),
                onClick = onClick,
                enabled = enabled
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontFamily = MaterialDesignIcons,
            fontSize = iconSize.value.sp,
            color = iconColor.copy(alpha = contentAlpha)
        )
    }
}

@Composable
fun TokenImage(
    tokenId: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val fileName = when {
        tokenId == "ERG" || tokenId.isEmpty() || tokenId == "Select Token" || tokenId == "ergo" -> "ergo"
        else -> tokenId
    }
    
    // Some hexes might be uppercase in the state, but files are lowercase
    val finalFileName = fileName.lowercase()
    
    var currentModel by remember(finalFileName) { 
        mutableStateOf<Any>("file:///android_asset/token_logos/$finalFileName.svg") 
    }
    
    val isFallback = currentModel == "FALLBACK"

    if (isFallback) {
        Text(
            text = "\uF0042", // mdi-block-helper
            fontFamily = MaterialDesignIcons,
            fontSize = 20.sp, // Reasonable default for a logo
            color = Color.White.copy(alpha = 0.3f),
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = currentModel,
            contentDescription = contentDescription,
            modifier = modifier,
            onError = {
                if (currentModel == "file:///android_asset/token_logos/$finalFileName.svg") {
                    currentModel = "file:///android_asset/token_logos/$finalFileName.png"
                } else {
                    currentModel = "FALLBACK"
                }
            }
        )
    }
}

/**
 * Global token info popup — shows token metadata and minting info.
 * Can be triggered from anywhere a token ID is displayed.
 */
@Composable
fun TokenInfoPopup(tokenId: String, viewModel: SwapViewModel, onDismiss: () -> Unit, onAddressClick: ((String) -> Unit)? = null) {
    var mintInfo by remember { mutableStateOf<TokenMintInfo?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    var showExplorePopup by remember { mutableStateOf(false) }
    var showTopHolders by remember { mutableStateOf(false) }

    LaunchedEffect(tokenId) {
        isLoading = true
        errorMsg = null
        try {
            mintInfo = viewModel.fetchTokenMintInfo(tokenId)
        } catch (e: Exception) {
            errorMsg = e.message ?: "Failed to load token info"
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = ColorCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TokenImage(tokenId = tokenId, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = mintInfo?.name ?: viewModel.getTokenName(tokenId),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorAccent, modifier = Modifier.size(28.dp))
                    }
                } else if (errorMsg != null) {
                    Text(errorMsg!!, color = ColorDanger, fontSize = 13.sp)
                } else if (mintInfo != null) {
                    val info = mintInfo!!

                    // Description
                    if (info.description.isNotEmpty()) {
                        Text(info.description, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(12.dp))
                    }

                    // Token ID
                    TokenInfoRow("Token ID") {
                        Text(
                            "${tokenId.take(10)}...${tokenId.takeLast(10)}",
                            color = ColorAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(tokenId))
                            }
                        )
                    }

                    // Total Supply
                    val formattedEmission = if (info.decimals > 0) {
                        val divisor = Math.pow(10.0, info.decimals.toDouble())
                        String.format("%,.${info.decimals}f", info.emissionAmount.toDouble() / divisor)
                    } else {
                        String.format("%,d", info.emissionAmount)
                    }
                    TokenInfoRow("Total Supply") {
                        Text(formattedEmission, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Decimals
                    TokenInfoRow("Decimals") {
                        Text("${info.decimals}", color = Color.White, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(8.dp))

                    Text("Minting Info", color = ColorAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    // Minting Address
                    TokenInfoRow("Address") {
                        val truncAddr = if (info.mintAddress.length > 20)
                            "${info.mintAddress.take(10)}...${info.mintAddress.takeLast(8)}"
                        else info.mintAddress
                        Text(
                            truncAddr,
                            color = ColorAccent,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable {
                                if (onAddressClick != null) {
                                    showExplorePopup = true
                                } else {
                                    clipboardManager.setText(AnnotatedString(info.mintAddress))
                                }
                            }
                        )
                    }

                    // Minting Tx
                    if (info.mintTxId.isNotEmpty()) {
                        TokenInfoRow("Transaction") {
                            Text(
                                "${info.mintTxId.take(10)}...${info.mintTxId.takeLast(8)}",
                                color = ColorAccent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(info.mintTxId))
                                }
                            )
                        }
                    }

                    // Block Height
                    info.mintBlockHeight?.let { height ->
                        TokenInfoRow("Block") {
                            Text(String.format("%,d", height), color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Minting Timestamp
                    info.mintTimestamp?.let { ts ->
                        val sdf = java.text.SimpleDateFormat("d MMM yyyy HH:mm", java.util.Locale.US)
                        TokenInfoRow("Date") {
                            Text(sdf.format(java.util.Date(ts)), color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (!isLoading && mintInfo != null) {
                    TextButton(onClick = { showTopHolders = true }) {
                        Text("Top Holders", color = ColorBlue, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = ColorAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    )

    // Explore Wallet confirmation popup (same pattern as CollapsibleBoxRow)
    if (showExplorePopup && onAddressClick != null && mintInfo != null) {
        AlertDialog(
            onDismissRequest = { showExplorePopup = false },
            containerColor = ColorCard,
            title = { Text("Explore Wallet", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("View balance & transactions for:", color = ColorTextDim, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    val addr = mintInfo!!.mintAddress
                    val truncAddr = if (addr.length > 20) "${addr.take(10)}...${addr.takeLast(8)}" else addr
                    Text(
                        truncAddr,
                        color = ColorAccent,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showExplorePopup = false
                    onDismiss()
                    onAddressClick(mintInfo!!.mintAddress)
                }) {
                    Text("Explore", color = ColorAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExplorePopup = false }) {
                    Text("Cancel", color = ColorTextDim)
                }
            }
        )
    }

    // Top Holders dialog
    if (showTopHolders && mintInfo != null) {
        TopHoldersDialog(
            tokenId = tokenId,
            tokenName = mintInfo!!.name,
            decimals = mintInfo!!.decimals,
            emissionAmount = mintInfo!!.emissionAmount,
            viewModel = viewModel,
            onDismiss = { showTopHolders = false },
            onAddressClick = onAddressClick
        )
    }
}

@Composable
private fun TokenInfoRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ColorTextDim, fontSize = 12.sp, modifier = Modifier.weight(0.4f))
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterEnd) {
            content()
        }
    }
}

@Composable
fun TopHoldersDialog(
    tokenId: String,
    tokenName: String,
    decimals: Int,
    emissionAmount: Long,
    viewModel: SwapViewModel,
    onDismiss: () -> Unit,
    onAddressClick: ((String) -> Unit)? = null
) {
    var holders by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var fetchedBoxes by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showExploreAddr by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tokenId) {
        isLoading = true
        errorMsg = null
        try {
            holders = viewModel.fetchTopHolders(
                tokenId = tokenId,
                decimals = decimals,
                onProgress = { fetched -> fetchedBoxes = fetched }
            ).holders
        } catch (e: Exception) {
            errorMsg = e.message ?: "Failed to fetch holders"
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = ColorCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Text("Top Holders \u2014 $tokenName", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = ColorAccent, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Scanning unspent boxes...", color = Color.White, fontSize = 13.sp)
                        Text(
                            "$fetchedBoxes boxes fetched",
                            color = ColorAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "This may take a while for tokens\nwith many holders.",
                            color = ColorTextDim,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (errorMsg != null) {
                    Text(errorMsg!!, color = ColorDanger, fontSize = 13.sp)
                } else {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#", color = ColorTextDim, fontSize = 10.sp, modifier = Modifier.width(28.dp))
                        Text("Address", color = ColorTextDim, fontSize = 10.sp, modifier = Modifier.weight(1f))
                        Text("Balance", color = ColorTextDim, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.width(125.dp))
                        Text("%", color = ColorTextDim, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.width(50.dp))
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Scrollable list
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        itemsIndexed(holders) { index, (address, amount) ->
                            val formattedBalance = if (decimals > 0) {
                                val divisor = Math.pow(10.0, decimals.toDouble())
                                String.format("%,.${minOf(decimals, 4)}f", amount.toDouble() / divisor)
                            } else {
                                String.format("%,d", amount)
                            }
                            val truncAddr = if (address.length > 20)
                                "${address.take(8)}...${address.takeLast(6)}"
                            else address

                            val pct = if (emissionAmount > 0)
                                String.format("%.1f%%", amount.toDouble() / emissionAmount.toDouble() * 100.0)
                            else ""

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .then(
                                        if (onAddressClick != null)
                                            Modifier.clickable { showExploreAddr = address }
                                        else Modifier
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = ColorTextDim,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    truncAddr,
                                    color = if (onAddressClick != null) ColorAccent else Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    formattedBalance,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(125.dp)
                                )
                                if (pct.isNotEmpty()) {
                                    Text(
                                        pct,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.width(50.dp)
                                    )
                                }
                            }
                            if (index < holders.size - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${holders.size} addresses \u2022 $fetchedBoxes boxes scanned",
                        color = ColorTextDim,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ColorAccent, fontWeight = FontWeight.Bold)
            }
        }
    )

    // Explore Wallet confirmation for top holder address
    showExploreAddr?.let { addr ->
        if (onAddressClick != null) {
            AlertDialog(
                onDismissRequest = { showExploreAddr = null },
                containerColor = ColorCard,
                title = { Text("Explore Wallet", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("View balance & transactions for:", color = ColorTextDim, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        val truncAddr = if (addr.length > 20) "${addr.take(10)}...${addr.takeLast(8)}" else addr
                        Text(
                            truncAddr,
                            color = ColorAccent,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExploreAddr = null
                        onDismiss()
                        onAddressClick(addr)
                    }) {
                        Text("Explore", color = ColorAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExploreAddr = null }) {
                        Text("Cancel", color = ColorTextDim)
                    }
                }
            )
        }
    }
}

