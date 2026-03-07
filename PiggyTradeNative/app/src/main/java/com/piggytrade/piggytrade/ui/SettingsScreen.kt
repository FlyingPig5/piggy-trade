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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggytrade.piggytrade.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                .padding(top = 5.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            TogaColumn(
                modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Large Logo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(55.dp))
                    )
                    
                    // The "Feather" Overlay: Fades from transparent in the center 
                    // to the card background color at the edges.
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0.5f to Color.Transparent,
                                        0.65f to Color.Transparent,
                                        0.95f to ColorCard
                                    )
                                )
                            )
                    )
                }
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
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TogaRow(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .androidBorder(radius = 12.dp, borderWidth = 0.dp, bgColor = ColorInputBg)
                        .clickable { viewModel.setActiveSelector("node") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_compass),
                        contentDescription = null,
                        tint = ColorAccent,
                        modifier = Modifier.padding(start = 12.dp).size(24.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        val nodeName = uiState.nodes.getOrNull(uiState.selectedNodeIndex) ?: "Select Node"
                        Text(
                            text = nodeName.substringBefore(":"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = nodeName.substringAfter(": "),
                            color = ColorTextDim,
                            fontSize = 10.sp
                        )
                    }
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                TogaIconButton(
                    icon = "\uE145", // ICON_PLUS
                    onClick = onNavigateToAddNode,
                    modifier = Modifier.size(40.dp),
                    radius = 8.dp,
                    borderColor = ColorBorder,
                    bgColor = ColorInputBg
                )
                Spacer(modifier = Modifier.width(6.dp))
                TogaIconButton(
                    icon = "\uE872", // ICON_TRASH
                    onClick = { viewModel.deleteNode() },
                    modifier = Modifier.size(40.dp),
                    radius = 8.dp,
                    bgColor = Color(0xFF9E1F1F)
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

            TogaRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Include unconfirmed",
                    color = ColorText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.includeUnconfirmed,
                    onCheckedChange = { viewModel.setIncludeUnconfirmed(it) },
                    modifier = Modifier.scale(0.8f)
                )
            }

            // Favorites Setting
            Text(
                text = "FAVORITES",
                color = ColorText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 5.dp, start = 10.dp)
            )
            TogaRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Count:", color = ColorText, fontSize = 12.sp)
                Slider(
                    value = uiState.numFavorites.toFloat(),
                    onValueChange = { viewModel.setNumFavorites(it.toInt()) },
                    valueRange = 4f..20f,
                    steps = 15,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
                )
                Text(text = "${uiState.numFavorites}", color = ColorText, fontSize = 12.sp, modifier = Modifier.width(30.dp))
            }


            // Token Management Section
            Text(
                text = "TOKEN LIST MANAGEMENT",
                color = ColorText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 10.dp)
            )

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

            if (uiState.debugMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 5.dp)
                        .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFF24336B))
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
                        .androidBorder(radius = 10.dp, borderWidth = 0.dp, bgColor = Color(0xFF24336B))
                        .clickable { viewModel.exportTokens(context) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Export token list", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
        }
    }
}

    // Animated Selector Overlay for Node Selection
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.activeSelector == "node",
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { viewModel.setActiveSelector(null) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .align(Alignment.BottomCenter)
                        .background(ColorBg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* Consume click */ }
                ) {
                    SelectorScreen(
                        title = "Select Node",
                        items = uiState.nodes,
                        onSelect = { node ->
                            viewModel.finalizeSelection(node)
                            viewModel.setActiveSelector(null)
                        },
                        onBack = { viewModel.setActiveSelector(null) },
                        getName = { it.substringBefore(":") },
                        getId = { it.substringAfter(": ") },
                        getBalance = { null },
                        showFullId = true,
                        showSearch = false
                    )
                }
            }
        }
    }
}
}

