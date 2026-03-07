package com.piggytrade.piggytrade.ui

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.remember

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
fun TradeCard(content: @Composable ColumnScope.() -> Unit) {
    TogaColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 10.dp) // margin=10, margin_top=2
            .androidBorder(radius = 15.dp, borderWidth = 0.dp, bgColor = ColorCard)
            .padding(top = 20.dp, bottom = 15.dp) // padding_top=20, v_padding applied to top/bottom
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
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 2.dp)
            .androidBorder(radius = 15.dp, borderWidth = 0.dp, bgColor = ColorCard)
            .padding(vertical = 15.dp)
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
    borderColor: Color = Color(0xFF535C6E)
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .androidBorder(radius = radius, borderWidth = borderWidth, borderColor = borderColor, bgColor = bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, radius = radius),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontFamily = MaterialDesignIcons,
            fontSize = iconSize.value.sp,
            color = iconColor
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
    
    val assetPath = "file:///android_asset/token_logos/$finalFileName.png"
    AsyncImage(
        model = assetPath,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
