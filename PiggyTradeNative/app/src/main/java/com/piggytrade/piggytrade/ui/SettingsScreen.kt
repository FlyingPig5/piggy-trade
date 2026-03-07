package com.piggytrade.piggytrade.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggytrade.piggytrade.R

@Composable
fun SettingsScreen(
    viewModel: SwapViewModel, 
    onBack: () -> Unit, 
    onNavigateToAddNode: () -> Unit,
    onNavigateToManagePairs: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (uiState.syncProgress != null) {
        SyncProgressPopup(uiState.syncProgress!!, onDismiss = { viewModel.dismissSyncPopup() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        // Header
        TogaRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TogaIconButton(
                icon = "\uEF7D", // ICON_BACK
                onClick = onBack,
                modifier = Modifier.size(36.dp),
                radius = 10.dp,
                bgColor = ColorBlue, // BTN_BACK_SET_COLOR is COLOR_BLUE
                iconColor = Color.White
            )
            Text(
                text = "Settings",
                color = ColorText,
                fontSize = 18.sp, // FONT_SIZE_TITLE ?? theme.py says FONT_SIZE_TITLE=18
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f)
            )
        }

        TogaColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp)
                .verticalScroll(scrollState)
                .androidBorder(radius = 30.dp, borderWidth = 0.dp, bgColor = ColorCard)
                .padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            // Logo & Credits
            TogaColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Large Logo",
                    modifier = Modifier
                        .width(120.dp)
                        .height(98.dp)
                        .padding(bottom = 10.dp)
                )
                Text(
                    text = "Ergo trading shouldn't be a desk job.\nSwap on the go!",
                    color = ColorTextDim,
                    fontSize = 10.sp, // FONT_SIZE_BASE
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                // GitHub Link
                TogaRow(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable { /* TODO open github */ }
                        .padding(horizontal = 15.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(24.dp).padding(end = 10.dp)
                    )
                    Text(
                        text = "This app is open source!",
                        color = Color.White,
                        fontSize = 12.sp // FONT_SIZE_MD
                    )
                }
            }

            // Node Section
            Text(
                text = "NODE",
                color = ColorText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 15.dp, bottom = 5.dp, start = 10.dp)
            )

            TogaRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Node Selection Dropdown
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = ColorSelectionBg)
                        .clickable { expanded = true },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = uiState.nodes.getOrNull(uiState.selectedNodeIndex) ?: "Select Node",
                        color = Color.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(ColorSelectionBg)
                    ) {
                        uiState.nodes.forEachIndexed { index, node ->
                            DropdownMenuItem(
                                text = { Text(node, color = Color.Black) },
                                onClick = {
                                    viewModel.setSelectedNodeIndex(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                TogaIconButton(
                    icon = "\uE145", // ICON_PLUS
                    onClick = onNavigateToAddNode,
                    modifier = Modifier.padding(start = 5.dp).size(32.dp),
                    radius = 8.dp,
                    borderColor = Color(0xFF535C6E),
                    bgColor = ColorInputBg
                )
                TogaIconButton(
                    icon = "\uE872", // ICON_TRASH
                    onClick = { /* delete node */ },
                    modifier = Modifier.padding(start = 5.dp).size(32.dp),
                    radius = 8.dp,
                    bgColor = Color(0xFF9E1F1F) // BTN_DEL_N_COLOR
                )
            }

            // Debug Section
            TogaRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 10.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debug/Advanced",
                    color = ColorText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.debugMode,
                    onCheckedChange = { viewModel.setDebugMode(it) },
                    modifier = Modifier.scale(0.8f)
                )
            }

            // Service Fees Section
            Text(
                text = "SERVICE FEES",
                color = ColorText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 5.dp, start = 10.dp)
            )
            val feeDesc = "• Token to Token trades: FREE!\n• Under 10 ERG: 0.0001 ERG\n• Over 10 ERG: 0.05%\n\nYeah, it's CHEAP!!"
            Text(
                text = feeDesc,
                color = ColorTextDim,
                fontSize = 8.sp,
                modifier = Modifier.padding(start = 15.dp),
                lineHeight = 12.sp
            )

            // Token Management Section
            Text(
                text = "TOKEN LIST MANAGEMENT",
                color = ColorText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 10.dp)
            )

            if (uiState.debugMode) {
                TogaRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = ColorBlue)
                            .clickable { /* import */ }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Import", color = Color.White, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp)
                            .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFF535C6E))
                            .clickable { /* export */ }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Export", color = Color.White, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 5.dp)
                        .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFFF2A332))
                        .clickable { /* import all */ }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Import All Trading Pairs", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 5.dp)
                    .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = ColorAccent)
                    .clickable { viewModel.syncTokenList() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Check for new trading pairs", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 5.dp)
                    .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFF6200EE))
                    .clickable { onNavigateToManagePairs() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Manage Trading Pairs", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 5.dp)
                    .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFF6200EE))
                    .clickable { viewModel.exportTokens(context) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Export Sync'd Tokens", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
                            progress = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            color = ColorAccent,
                            trackColor = ColorInputBg
                        )
                        val batchText = if (progress.batchInfo.isNotEmpty()) "\n${progress.batchInfo}" else ""
                        Text(
                            "Processed ${progress.current} of ${progress.total} boxes$batchText", 
                            color = ColorTextDim,
                            fontSize = 12.sp
                        )
                    }
                    
                    if (progress.newTokens.isNotEmpty()) {
                        Text(
                            "New pairs found: ${progress.newTokens.size}", 
                            color = ColorAccent, 
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                } else {
                    Text(
                        "Sync finished! Processed ${progress.total} boxes.", 
                        color = ColorText,
                        fontSize = 14.sp
                    )
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
