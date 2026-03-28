package com.piggytrade.piggytrade.ui.wallet

import com.piggytrade.piggytrade.ui.theme.*
import com.piggytrade.piggytrade.ui.common.*
import com.piggytrade.piggytrade.ui.swap.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

/**
 * Main send screen with scrollable recipient cards.
 * Supports multiple recipients, each with address, ERG amount, and optional tokens.
 * Miner fee uses a slider (same as dex/stablecoin).
 * Token add uses an inline multiselect dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SwapViewModel,
    onBack: () -> Unit,
    onNavigateToQrScanner: (recipientIndex: Int) -> Unit,
    onNavigateToTokenSelector: (recipientIndex: Int) -> Unit,
    onNavigateToReview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track which recipient index the token picker dialog is open for (-1 = closed)
    var tokenPickerRecipientIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.clearSendState()
                onBack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Send",
                color = ColorText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            // Wallet balance info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${SwapViewModel.formatErg(uiState.walletErgBalance)} ERG",
                    color = ColorAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (uiState.changeAddress.isNotEmpty()) {
                    Text(
                        text = "Change: ${uiState.changeAddress.take(5)}...${uiState.changeAddress.takeLast(5)}",
                        color = ColorTextDim,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            // Recipient cards
            uiState.sendRecipients.forEachIndexed { index, recipient ->
                RecipientCard(
                    index = index,
                    recipient = recipient,
                    viewModel = viewModel,
                    canRemove = uiState.sendRecipients.size > 1,
                    onScanQr = { onNavigateToQrScanner(index) },
                    onAddToken = { tokenPickerRecipientIndex = index }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add recipient button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorCard)
                    .clickable { viewModel.addSendRecipient() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Recipient",
                        tint = ColorAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Recipient",
                        color = ColorAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Miner Fee (Slider) ──
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .androidBorder(radius = 12.dp, borderWidth = 0.5.dp, borderColor = Color.White.copy(alpha = 0.05f), bgColor = ColorSelectionBg.copy(alpha = 0.8f))
                    .padding(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Miner Fee:", color = ColorTextDim, fontSize = 11.sp)
                    Text(
                        text = "${SwapViewModel.formatErg(uiState.sendMinerFee)} ERG",
                        color = ColorAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = uiState.sendMinerFee.toFloat(),
                    onValueChange = { viewModel.setSendMinerFee(it.toDouble()) },
                    valueRange = 0.0011f..0.2f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = ColorAccent,
                        activeTrackColor = ColorAccent,
                        inactiveTrackColor = ColorTextDim.copy(alpha = 0.3f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Slow", color = ColorTextDim, fontSize = 9.sp)
                    Text("Fast", color = ColorTextDim, fontSize = 9.sp)
                }
            }

            // Error message
            uiState.sendError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = ColorDanger,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for button
        }

        // ── Review Transaction button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorBg)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.prepareSendTx(
                        onSuccess = { onNavigateToReview() },
                        onError = { /* error is shown via sendError state */ }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorAccent,
                    disabledContainerColor = ColorAccent.copy(alpha = 0.3f)
                ),
                enabled = !uiState.isBuildingSendTx &&
                        uiState.sendRecipients.any { it.address.isNotEmpty() && it.ergAmount.isNotEmpty() }
            ) {
                if (uiState.isBuildingSendTx) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ColorBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Building Transaction...", color = ColorBg, fontWeight = FontWeight.Bold)
                } else {
                    Text("Review Transaction", color = ColorBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    // ── Token Multiselect Dialog ──
    if (tokenPickerRecipientIndex >= 0) {
        TokenMultiselectDialog(
            viewModel = viewModel,
            recipientIndex = tokenPickerRecipientIndex,
            existingTokenIds = uiState.sendRecipients.getOrNull(tokenPickerRecipientIndex)
                ?.tokens?.map { it.tokenId }?.toSet() ?: emptySet(),
            onDismiss = { tokenPickerRecipientIndex = -1 }
        )
    }
}

/**
 * Dialog that shows all tokens the user holds and allows multiselect via checkboxes.
 * Tokens already added to this recipient are pre-checked and cannot be unchecked (remove via card).
 */
@Composable
private fun TokenMultiselectDialog(
    viewModel: SwapViewModel,
    recipientIndex: Int,
    existingTokenIds: Set<String>,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val heldTokens = uiState.walletTokens.toList() // List<(tokenId, amount)>
    val selectedIds = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = ColorCard,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.7f),
        title = {
            Text("Select Tokens", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tokens...", color = ColorInputHint, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorInputBg,
                        unfocusedContainerColor = ColorInputBg,
                        focusedBorderColor = ColorAccent,
                        unfocusedBorderColor = ColorBorder,
                        cursorColor = ColorAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                val filteredTokens = heldTokens.filter { (tokenId, _) ->
                    val name = viewModel.getTokenName(tokenId)
                    name.contains(searchQuery, ignoreCase = true) ||
                            tokenId.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredTokens) { (tokenId, amount) ->
                        val name = viewModel.getTokenName(tokenId)
                        val alreadyAdded = existingTokenIds.contains(tokenId)
                        val isChecked = alreadyAdded || selectedIds.contains(tokenId)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChecked) ColorInputBg else Color.Transparent)
                                .clickable(enabled = !alreadyAdded) {
                                    if (selectedIds.contains(tokenId)) {
                                        selectedIds.remove(tokenId)
                                    } else {
                                        selectedIds.add(tokenId)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = if (alreadyAdded) null else { checked ->
                                    if (checked) selectedIds.add(tokenId) else selectedIds.remove(tokenId)
                                },
                                enabled = !alreadyAdded,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ColorAccent,
                                    uncheckedColor = ColorTextDim,
                                    checkmarkColor = Color.White,
                                    disabledCheckedColor = ColorAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.size(36.dp)
                            )
                            TokenImage(tokenId = tokenId, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Balance: ${viewModel.formatBalance(tokenId, amount)}",
                                    color = ColorTextDim,
                                    fontSize = 11.sp
                                )
                            }
                            if (alreadyAdded) {
                                Text("added", color = ColorTextDim, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Add all newly selected tokens
                    for (tokenId in selectedIds) {
                        viewModel.addSendToken(recipientIndex, tokenId)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorAccent)
            ) {
                Text(
                    "Add ${if (selectedIds.isEmpty()) "" else "${selectedIds.size} "}Token${if (selectedIds.size != 1) "s" else ""}",
                    color = ColorBg,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ColorTextDim)
            }
        }
    )
}

@Composable
private fun RecipientCard(
    index: Int,
    recipient: SendRecipientState,
    viewModel: SwapViewModel,
    canRemove: Boolean,
    onScanQr: () -> Unit,
    onAddToken: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    TradeCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (uiState.sendRecipients.size > 1) "Recipient ${index + 1}" else "Recipient",
                    color = ColorText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (canRemove) {
                    IconButton(
                        onClick = { viewModel.removeSendRecipient(index) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = ColorDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Address input with QR button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .androidBorder(radius = 10.dp, borderWidth = 1.dp, bgColor = ColorInputBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = recipient.address,
                    onValueChange = { viewModel.setSendRecipientAddress(index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Ergo address (9...)", color = ColorInputHint, fontSize = 13.sp)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = ColorText,
                        fontSize = 13.sp
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = ColorAccent
                    )
                )
                IconButton(onClick = onScanQr) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = ColorAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ERG amount with MAX button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .androidBorder(radius = 10.dp, borderWidth = 1.dp, bgColor = ColorInputBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = recipient.ergAmount,
                    onValueChange = { viewModel.setSendRecipientAmount(index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("ERG Amount", color = ColorInputHint, fontSize = 13.sp)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = ColorText,
                        fontSize = 13.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = ColorAccent
                    )
                )
                Text(
                    text = "ERG",
                    color = ColorTextDim,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                TextButton(
                    onClick = { viewModel.setSendMaxErg(index) },
                    modifier = Modifier.padding(end = 4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("MAX", color = ColorAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Token rows
            recipient.tokens.forEachIndexed { tokenIndex, tokenState ->
                Spacer(modifier = Modifier.height(8.dp))
                SendTokenRow(
                    tokenState = tokenState,
                    viewModel = viewModel,
                    recipientIndex = index,
                    tokenIndex = tokenIndex
                )
            }

            // Add token button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAddToken() }
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Token",
                    tint = ColorBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Token",
                    color = ColorBlue,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SendTokenRow(
    tokenState: SendTokenState,
    viewModel: SwapViewModel,
    recipientIndex: Int,
    tokenIndex: Int
) {
    val tokenName = viewModel.getTokenName(tokenState.tokenId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .androidBorder(radius = 10.dp, borderWidth = 1.dp, bgColor = ColorInputBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Token icon
        TokenImage(
            tokenId = tokenState.tokenId,
            modifier = Modifier
                .size(24.dp)
                .padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Token name label
        Text(
            text = tokenName,
            color = ColorText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(60.dp)
        )

        // Amount input
        OutlinedTextField(
            value = tokenState.amount,
            onValueChange = {
                viewModel.setSendTokenAmount(recipientIndex, tokenIndex, it)
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Amount", color = ColorInputHint, fontSize = 12.sp) },
            textStyle = LocalTextStyle.current.copy(
                color = ColorText,
                fontSize = 12.sp
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ColorText,
                unfocusedTextColor = ColorText,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = ColorAccent
            )
        )

        // MAX button
        TextButton(
            onClick = { viewModel.setSendMaxToken(recipientIndex, tokenIndex) },
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("MAX", color = ColorAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        // Remove button
        IconButton(
            onClick = { viewModel.removeSendToken(recipientIndex, tokenIndex) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Token",
                tint = ColorDanger,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
