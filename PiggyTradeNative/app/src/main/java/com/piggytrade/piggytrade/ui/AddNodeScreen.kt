package com.piggytrade.piggytrade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddNodeScreen(onBack: () -> Unit) {
    var nodeUrl by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg)
    ) {
        // Header
        TogaRow(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TogaIconButton(icon = "\uEF7D", onClick = onBack, modifier = Modifier.size(36.dp), radius = 10.dp, bgColor = ColorBlue)
            Text(text = "Add Custom Node", color = ColorText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
        }

        TogaColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .androidBorder(radius = 30.dp, borderWidth = 0.dp, bgColor = ColorCard)
                .padding(30.dp)
        ) {
            Text("NODE URL", color = ColorText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
            OutlinedTextField(
                value = nodeUrl,
                onValueChange = { nodeUrl = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                placeholder = { Text("https://...", color = ColorInputHint) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorInputBg,
                    unfocusedContainerColor = ColorInputBg,
                    focusedBorderColor = Color(0xFF535C6E),
                    unfocusedBorderColor = Color(0xFF535C6E)
                )
            )

            Text("NAME (OPTIONAL)", color = ColorText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
            OutlinedTextField(
                value = nodeName,
                onValueChange = { nodeName = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorInputBg,
                    unfocusedContainerColor = ColorInputBg,
                    focusedBorderColor = Color(0xFF535C6E),
                    unfocusedBorderColor = Color(0xFF535C6E)
                )
            )

            Button(
                onClick = { /* TODO add node and back */ onBack() },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBlue)
            ) {
                Text("Add Node", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
