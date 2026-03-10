package com.piggytrade.piggytrade.ui.swap
import com.piggytrade.piggytrade.ui.theme.*
import com.piggytrade.piggytrade.ui.common.*
import com.piggytrade.piggytrade.ui.home.*
import com.piggytrade.piggytrade.ui.wallet.*
import com.piggytrade.piggytrade.ui.settings.*

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FromTokenPanel(uiState: SwapState, viewModel: SwapViewModel) {
    TogaColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        // Favorites: Single Line Scrollable
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(uiState.favorites.take(uiState.numFavorites)) { index, fav ->
                FavoriteButton(
                    index = index,
                    fav = fav,
                    isSelected = uiState.firstFavoriteSelectedIndex == index,
                    onClick = { viewModel.handleFavClick(index, fav) },
                    onLongClick = {
                        if (index != 0) {
                            viewModel.startEditingFavorite(index)
                            viewModel.setActiveSelector("fav")
                        }
                    },
                    vm = viewModel
                )
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

        // from_top_row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .androidBorder(radius = 16.dp, borderWidth = 0.5.dp, borderColor = Color.White.copy(alpha = 0.1f), bgColor = fromBgColor),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            BasicTextField(
                                value = uiState.fromAmount,
                                onValueChange = { viewModel.setFromAmount(it) },
                                textStyle = TextStyle(fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(Color.White),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (uiState.fromAmount.isEmpty()) {
                                            Text("0.0", color = ColorInputHint, fontSize = 26.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        if (uiState.fromBalance.isNotEmpty()) {
                            Text(
                                text = "Balance: ${uiState.fromBalance}",
                                color = ColorTextDim,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.setMaxAmount() }
                    ) {
                        Text(
                            text = "MAX",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    TogaRow(
                        modifier = Modifier
                            .width(140.dp)
                            .fillMaxHeight()
                            .androidBorder(radius = 12.dp, borderWidth = 0.dp, bgColor = ColorSelectionBg)
                            .clickable { viewModel.setActiveSelector("from") }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.fromAsset.isNotEmpty()) {
                            TokenImage(tokenId = viewModel.getTokenId(uiState.fromAsset), modifier = Modifier.size(40.dp).padding(end = 5.dp))
                        }
                        val baseName = if (uiState.fromAsset.isNotEmpty()) viewModel.getTokenName(viewModel.getTokenId(uiState.fromAsset)) else "SELECT"
                        Text(text = baseName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Icon(painter = painterResource(id = android.R.drawable.arrow_down_float), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp).padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ToTokenPanel(uiState: SwapState, viewModel: SwapViewModel) {
    TogaColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .androidBorder(radius = 16.dp, borderWidth = 0.5.dp, borderColor = Color.White.copy(alpha = 0.1f), bgColor = toBgColor),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = uiState.toQuote,
                                color = ColorAccent,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (uiState.toBalance.isNotEmpty()) {
                            Text(
                                text = "Balance: ${uiState.toBalance}",
                                color = ColorTextDim,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    TogaRow(
                        modifier = Modifier
                            .width(140.dp)
                            .fillMaxHeight()
                            .androidBorder(radius = 12.dp, borderWidth = 0.dp, bgColor = ColorSelectionBg)
                            .clickable { viewModel.setActiveSelector("to") }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (uiState.toAsset.isNotEmpty()) {
                            TokenImage(tokenId = viewModel.getTokenId(uiState.toAsset), modifier = Modifier.size(40.dp).padding(end = 5.dp))
                        }
                        val baseName = if (uiState.toAsset.isNotEmpty()) viewModel.getTokenName(viewModel.getTokenId(uiState.toAsset)) else "SELECT"
                        Text(text = baseName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Icon(painter = painterResource(id = android.R.drawable.arrow_down_float), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp).padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailsPanel(uiState: SwapState, viewModel: SwapViewModel) {
    TogaColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .androidBorder(radius = 12.dp, borderWidth = 0.dp, bgColor = ColorSelectionBg.copy(alpha = 0.8f))
            .padding(15.dp)
    ) {
        Text(text = "ORDER DETAILS", color = ColorAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        TogaRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Rate:", color = ColorText, fontSize = 11.sp)
            val fromAmt = uiState.fromAmount.replace(",", ".").toDoubleOrNull() ?: 1.0
            val toQuoteAmt = uiState.toQuote.replace(",", ".").toDoubleOrNull() ?: 0.0
            val rate = if (fromAmt > 0) toQuoteAmt / fromAmt else 0.0
            Text(text = "1 ${uiState.fromAsset} = ${String.format("%.4f", rate)} ${uiState.toAsset}", color = Color.White, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TogaRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price Impact:", color = ColorText, fontSize = 11.sp)
            val impactColor = if (uiState.priceImpact < 1.0) Color(0xFF00D18B) else if (uiState.priceImpact > 5.0) Color.Red else Color.White
            Text(
                text = "${String.format("%.2f", uiState.priceImpact)}%",
                color = impactColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TogaRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "LP Fee:", color = ColorText, fontSize = 11.sp)
            Text(
                text = "${String.format("%.1f", uiState.lpFee * 100)}%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TogaRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Miner Fee:", color = ColorText, fontSize = 11.sp)
            Text(
                text = "${String.format("%.3f", uiState.minerFee)} ERG",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = uiState.minerFee.toFloat(),
            onValueChange = { viewModel.setMinerFee(it.toDouble()) },
            valueRange = 0.001f..0.2f,
            modifier = Modifier.fillMaxWidth()
        )
        TogaRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Slow", color = ColorTextDim, fontSize = 9.sp)
            Text(text = "Fast", color = ColorTextDim, fontSize = 9.sp)
        }
    }
}

@Composable
fun DebugModeButtons(uiState: SwapState, viewModel: SwapViewModel) {
    if (uiState.debugMode) {
        TogaRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
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
}

@Composable
fun SwapButton(
    uiState: SwapState,
    isSwapValid: Boolean,
    fromAmountValue: Double,
    fromBalanceValue: Double,
    onShowDisclaimer: () -> Unit
) {
    Button(
        onClick = {
            onShowDisclaimer()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(55.dp),
        enabled = !uiState.isBuildingTx && isSwapValid,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSwapValid) Color(0xFF00D18B) else ColorSelectionBg,
            disabledContainerColor = ColorSelectionBg.copy(alpha = 0.5f)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState.isBuildingTx) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp).padding(end = 8.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = when {
                    uiState.isBuildingTx -> "Building transaction..."
                    uiState.isSimulation -> "SIMULATE"
                    !isSwapValid && fromAmountValue > fromBalanceValue -> "INSUFFICIENT BALANCE"
                    else -> "SWAP"
                },
                color = if (uiState.isBuildingTx) Color.White.copy(alpha = 0.7f) else if (isSwapValid) ColorBg else ColorTextDim,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
