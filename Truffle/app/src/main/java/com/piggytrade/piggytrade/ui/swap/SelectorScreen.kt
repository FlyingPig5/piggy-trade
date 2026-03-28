package com.piggytrade.piggytrade.ui.swap
import com.piggytrade.piggytrade.ui.theme.*
import com.piggytrade.piggytrade.ui.common.*
import com.piggytrade.piggytrade.ui.home.*
import com.piggytrade.piggytrade.ui.wallet.*
import com.piggytrade.piggytrade.ui.settings.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun <T> SelectorScreen(
    title: String,
    items: List<T>,
    onSelect: (T) -> Unit,
    onBack: () -> Unit,
    getName: (T) -> String,
    getId: (T) -> String,
    getBalance: ((T) -> String?)? = null,
    getVerificationStatus: ((T) -> Int)? = null,
    showFullId: Boolean = false,
    showSearch: Boolean = true,
    idLabel: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = items.filter { 
        getName(it).contains(searchQuery, ignoreCase = true) || 
        getId(it).contains(searchQuery, ignoreCase = true)
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
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TogaIconButton(
                icon = "\uEF7D", // BACK
                onClick = onBack,
                modifier = Modifier.size(36.dp),
                radius = 10.dp,
                bgColor = ColorBlue
            )
            if (showSearch) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...", color = ColorInputHint, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                        .height(52.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorInputBg,
                        unfocusedContainerColor = ColorInputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                    )
                )
            } else {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
        }

        TogaColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .androidBorder(radius = 30.dp, borderWidth = 0.dp, bgColor = ColorCard)
                .padding(20.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredItems) { item ->
                    TogaRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .height(60.dp)
                            .androidBorder(radius = 12.dp, borderWidth = 1.dp, borderColor = Color(0xFF535C6E), bgColor = ColorInputBg)
                            .clickable { onSelect(item) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TokenImage(tokenId = getId(item), modifier = Modifier.size(40.dp).padding(start = 12.dp))
                        
                        val status = getVerificationStatus?.invoke(item) ?: 0
                        val baseName = getName(item)
                        
                        val (nameColor, labelText) = when (status) {
                            0 -> Color.White to "" // Verified
                            1 -> Color.White to " (user added)" // User added
                            3 -> Color(0xFFE57373) to " (offline/dead)" // Dead node
                            else -> ColorOrange to " (unverified)" // Unverified
                        }

                        Column(modifier = Modifier.padding(start = 15.dp).weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = baseName,
                                    color = nameColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (labelText.isNotEmpty()) {
                                    Text(
                                        text = labelText,
                                        color = if (status == 1) ColorTextDim else if (status == 3) Color(0xFFE57373) else ColorOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                            if (getId(item).isNotEmpty()) {
                                 if (showFullId) {
                                    Text(
                                        text = getId(item),
                                        color = ColorTextDim,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                } else {
                                    val id = getId(item)
                                    val isAddress = id.length > 30
                                    
                                    // Use explicit label if provided, otherwise infer
                                    val label = idLabel ?: if (isAddress) "Addr: " else "ID: "
                                    
                                    val displayId = if (isAddress) {
                                        id.take(12) + "..." + id.takeLast(6)
                                    } else {
                                        id.take(10)
                                    }
                                    Text(
                                        text = "$label$displayId",
                                        color = ColorTextDim,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        if (getBalance != null) {
                            getBalance(item)?.let { balance ->
                                Text(
                                    text = balance,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
