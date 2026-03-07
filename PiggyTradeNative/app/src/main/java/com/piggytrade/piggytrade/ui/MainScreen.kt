package com.piggytrade.piggytrade.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggytrade.piggytrade.R

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun MainScreen(
    viewModel: SwapViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTokenSelector: (String) -> Unit,
    onNavigateToWalletSelector: () -> Unit,
    onNavigateToAddWallet: () -> Unit,
    onNavigateToWalletInfo: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (uiState.syncProgress != null) {
        SyncProgressPopup(uiState.syncProgress!!, onDismiss = { viewModel.dismissSyncPopup() })
    }

    Scaffold(
        topBar = {
            PiggyTopBar(
                isLoading = uiState.isLoadingQuote,
                onSettingsClick = onNavigateToSettings
            )
        },
        containerColor = ColorBg
    ) { paddingValues ->
        TogaColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ColorBg)
                .verticalScroll(scrollState)
        ) {
            // Wallet Card
            WalletCard {
                TogaRow(
                    modifier = Modifier.padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "WALLET",
                        color = ColorText,
                        style = Typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TogaRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // btn_wallet_sel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .androidBorder(radius = 12.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = ColorInputBg)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 12.dp),
                                onClick = onNavigateToWalletSelector
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.selectedWallet.ifEmpty { "Select Wallet" },
                            color = Color.White,
                            fontSize = 12.sp, // FONT_SIZE_MD from theme.py
                            modifier = Modifier.padding(start = 25.dp) // h_padding=25 from Python
                        )
                    }
                    // btn_add_w
                    TogaIconButton(
                        icon = "\uE145", // ICON_PLUS
                        onClick = onNavigateToAddWallet,
                        modifier = Modifier.padding(start = 8.dp).size(50.dp),
                        radius = 10.dp,
                        borderColor = Color(0xFF535C6E)
                    )
                }

                TogaRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // inp_address
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .androidBorder(radius = 10.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = ColorInputBg),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = uiState.selectedAddress,
                            onValueChange = { if (uiState.selectedWallet == "ErgoPay") viewModel.setSelectedAddress(it) },
                            modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                            textStyle = TextStyle(fontSize = 10.sp, color = ColorInputText),
                            cursorBrush = SolidColor(Color.White),
                            readOnly = uiState.selectedWallet != "ErgoPay",
                            decorationBox = { innerTextField ->
                                if (uiState.selectedAddress.isEmpty()) {
                                    Text("Enter wallet Address", color = ColorInputHint, fontSize = 10.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                    // btn_view_w
                    TogaIconButton(
                        icon = "\uF8FF", // ICON_WALLET
                        onClick = { 
                            if (uiState.selectedWallet != "Select Wallet" && uiState.selectedWallet.isNotEmpty()) {
                                onNavigateToWalletInfo(uiState.selectedWallet)
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp).size(50.dp),
                        radius = 10.dp,
                        borderColor = Color(0xFF535C6E)
                    )
                }
            }

            // Trade Card
            TradeCard {
                // Favorites Grid: 2 rows of 4
                TogaColumn(
                    modifier = Modifier.fillMaxWidth().padding(top = 1.dp, bottom = 1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val rows = uiState.favorites.chunked(4)
                    rows.forEachIndexed { rowIndex, rowItems ->
                        TogaRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            rowItems.forEachIndexed { colIndex, fav ->
                                val index = rowIndex * 4 + colIndex
                                FavoriteButton(
                                    index = index,
                                    fav = fav,
                                    isSelected = uiState.firstFavoriteSelectedIndex == index,
                                    onClick = { 
                                        if (uiState.isEditFavoritesMode && index != 0) {
                                            viewModel.startEditingFavorite(index)
                                            onNavigateToTokenSelector("fav")
                                        } else {
                                            viewModel.handleFavClick(index, fav)
                                        }
                                    },
                                    vm = viewModel
                                )
                            }
                        }
                    }
                }

                // Edit Favorites Switch
                TogaRow(
                    modifier = Modifier.padding(start = 22.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Favorites",
                        color = ColorText,
                        fontSize = 10.sp // FONT_SIZE_BASE
                    )
                    Switch(
                        checked = uiState.isEditFavoritesMode,
                        onCheckedChange = { viewModel.toggleEditFavoritesMode() },
                        modifier = Modifier.scale(0.8f).padding(start = 5.dp)
                    )

                    if (uiState.debugMode) {
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = "Wallet",
                            color = ColorText,
                            fontSize = 10.sp
                        )
                        Switch(
                            checked = uiState.useMempool,
                            onCheckedChange = { viewModel.setUseMempool(it) },
                            modifier = Modifier.scale(0.8f).padding(start = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LP",
                            color = ColorText,
                            fontSize = 10.sp
                        )
                        Switch(
                            checked = uiState.useLpMempool,
                            onCheckedChange = { viewModel.setUseLpMempool(it) },
                            modifier = Modifier.scale(0.8f).padding(start = 2.dp)
                        )
                    }
                }

                // FROM section
                TogaColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                     TogaRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 5.dp).padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Balance: ${uiState.fromBalance}", color = ColorTextDim, fontSize = 8.sp, modifier = Modifier.padding(end = 10.dp))
                        Box(
                            modifier = Modifier
                                .androidBorder(radius = 10.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = ColorInputBg)
                                .clickable { viewModel.setMaxAmount() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "MAX", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // pulse logic
                    var fromFlash by remember { mutableStateOf(false) }
                    LaunchedEffect(uiState.fromPulseTrigger) {
                        if (uiState.fromPulseTrigger > 0) {
                            fromFlash = true
                            kotlinx.coroutines.delay(200)
                            fromFlash = false
                        }
                    }
                    val fromBgColor by animateColorAsState(
                        targetValue = if (fromFlash) Color(0xFF1B4D3E) else ColorInputBg,
                        animationSpec = tween(durationMillis = 200)
                    )

                    // from_top_row (Merged Amount + Token Selector)
                    TogaRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .androidBorder(radius = 10.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = fromBgColor),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = uiState.fromAmount,
                            onValueChange = { viewModel.setFromAmount(it) },
                            modifier = Modifier.weight(1f).padding(start = 15.dp),
                            textStyle = TextStyle(fontSize = 16.sp, color = Color.White),
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (uiState.fromAmount.isEmpty()) {
                                        Text("0.0", color = ColorInputHint, fontSize = 16.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        // btn_from
                        TogaRow(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 10.dp),
                                    onClick = { onNavigateToTokenSelector("from") }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val status = viewModel.getVerificationStatus(uiState.fromAsset)
                            TokenImage(tokenId = viewModel.getTokenId(uiState.fromAsset), modifier = Modifier.size(24.dp).padding(end = 5.dp))
                            val baseName = if (uiState.fromAsset.isNotEmpty()) viewModel.getTokenName(viewModel.getTokenId(uiState.fromAsset)) else "SELECT"
                            
                             val (nameColor, labelText) = when (status) {
                                 0 -> Color.White to ""
                                 1 -> Color.White to " (user added)"
                                 else -> Color.White to "" // Hide "unverified" for selected asset
                             }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = baseName,
                                    color = nameColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (labelText.isNotEmpty()) {
                                    Text(
                                        text = labelText,
                                        color = ColorOrange,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // MID ROW (Swap Arrow)
                TogaRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TogaIconButton(
                        icon = String(Character.toChars(0xFFFA3)), // ICON_SWAP_VERT from theme.py
                        onClick = { viewModel.swapDirection() },
                        modifier = Modifier.size(42.dp),
                        radius = 21.dp, // Circle
                        borderColor = Color(0xFF535C6E)
                    )
                }

                TogaColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 2.dp)
                ) {
                    var toFlash by remember { mutableStateOf(false) }
                    LaunchedEffect(uiState.toPulseTrigger) {
                        if (uiState.toPulseTrigger > 0) {
                            toFlash = true
                            kotlinx.coroutines.delay(200)
                            toFlash = false
                        }
                    }
                    val toBgColor by animateColorAsState(
                        targetValue = if (toFlash) Color(0xFF1B4D3E) else ColorInputBg,
                        animationSpec = tween(durationMillis = 200)
                    )

                    // to_top_row
                    TogaRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .androidBorder(radius = 12.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = toBgColor),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.toQuote,
                            color = ColorAccent, // LBL_QUOTE_FONT_COLOR
                            fontSize = 16.sp, // FONT_SIZE_XL
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 15.dp)
                        )

                        TogaRow(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 10.dp),
                                    onClick = { onNavigateToTokenSelector("to") }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val status = viewModel.getVerificationStatus(uiState.toAsset)
                            TokenImage(tokenId = viewModel.getTokenId(uiState.toAsset), modifier = Modifier.size(24.dp).padding(end = 5.dp))
                            val baseName = if (uiState.toAsset.isNotEmpty()) viewModel.getTokenName(viewModel.getTokenId(uiState.toAsset)) else "SELECT"
                            
                             val (nameColor, labelText) = when (status) {
                                 0 -> Color.White to ""
                                 1 -> Color.White to " (user added)"
                                 else -> Color.White to "" // Hide "unverified" for selected asset
                             }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = baseName,
                                    color = nameColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (labelText.isNotEmpty()) {
                                    Text(
                                        text = labelText,
                                        color = ColorOrange,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    TogaRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Impact: ${uiState.priceImpact}%", 
                            color = if (uiState.priceImpact > 5.0) Color.Red else ColorTextDim, 
                            fontSize = 8.sp,
                            fontWeight = if (uiState.priceImpact > 5.0) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(text = "Balance: ${uiState.toBalance}", color = ColorTextDim, fontSize = 8.sp)
                    }
                }

                // Miner Fee
                TogaRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Miner Fee:", color = ColorText, fontSize = 10.sp)
                    Slider(
                        value = uiState.minerFee.toFloat(),
                        onValueChange = { viewModel.setMinerFee(it.toDouble()) },
                        valueRange = 0.001f..0.5f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )
                    Text(
                        text = "${String.format("%.3f", uiState.minerFee)} ERG",
                        color = ColorText,
                        fontSize = 10.sp,
                        modifier = Modifier.width(80.dp)
                    )
                }

                if (uiState.debugMode) {
                    TogaRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.setSimulationMode(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isSimulation) ColorAccent else ColorSelectionBg,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Check TX", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { viewModel.setSimulationMode(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!uiState.isSimulation) Color(0xFF9E1F1F) else ColorSelectionBg,
                                contentColor = Color.White
                            )
                        ) {
                            Text("LIVE", fontSize = 14.sp)
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        viewModel.prepareSwap(
                            onSuccess = {
                                onSubmit()
                            },
                            onError = { err ->
                                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(55.dp),
                    enabled = !uiState.isBuildingTx,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D18B))
                ) {
                    Text(
                        text = when {
                            uiState.isBuildingTx -> "Building transaction..."
                            uiState.isSimulation -> "SIMULATE"
                            else -> "SUBMIT"
                        },
                        color = ColorBg, // BTN_SUBMIT_FONT_COLOR
                        fontSize = 16.sp, // FONT_SIZE_XL
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PiggyTopBar(isLoading: Boolean, onSettingsClick: () -> Unit) {
    TogaRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ColorAccent,
                    strokeWidth = 2.dp
                )
            }
        }

        TogaRow(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Piggy", color = ColorText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Image(
                painter = painterResource(id = R.drawable.piggytrade),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(32.dp)
                    .padding(horizontal = 2.dp)
            )
            Text(text = "Trade", color = ColorText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        TogaRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TogaIconButton(
                icon = "\uE8B8", // ICON_COG
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp),
                bgColor = ColorBg, // BTN_SETTINGS_COLOR
                radius = 10.dp
            )
        }
    }
}

@Composable
fun FavoriteButton(index: Int, fav: String, isSelected: Boolean, onClick: () -> Unit, vm: SwapViewModel) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ColorAccent else ColorInputBg,
        animationSpec = tween(durationMillis = 200)
    )
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(width = 88.dp, height = 45.dp)
            .androidBorder(radius = 10.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 10.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TokenImage(tokenId = vm.getTokenId(fav), modifier = Modifier.size(if (index == 0) 36.dp else 30.dp))
            if (fav != "ERG" && fav != "?") {
                val displayName = vm.getTokenName(vm.getTokenId(fav))
                Text(
                    text = if (displayName.length > 5) displayName.take(5) + ".." else displayName,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
