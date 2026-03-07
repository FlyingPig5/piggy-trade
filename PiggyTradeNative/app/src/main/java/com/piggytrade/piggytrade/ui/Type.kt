package com.piggytrade.piggytrade.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.piggytrade.piggytrade.R

val MaterialDesignIcons = FontFamily(
    Font(R.font.materialdesignicons, FontWeight.Normal)
)

val RobotoFamily = FontFamily(
    Font(R.font.roboto_variablefont_wdth_wght, FontWeight.Normal),
    Font(R.font.roboto_italic_variablefont_wdth_wght, FontWeight.Normal)
)

val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        color = ColorText
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = ColorText
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = ColorText
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = ColorText
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = ColorText
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        color = ColorTextDim
    )
)
