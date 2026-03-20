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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.platform.LocalContext
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
