package com.iesejemplo.llaves.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iesejemplo.llaves.ui.theme.AppLlavesTheme
import com.iesejemplo.llaves.util.NfcBroadcastReceiver

@Composable
fun BuscarLlaveScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var lastScannedTag by remember { mutableStateOf<String?>(null) }
    val screenBackground = Color(0xFF0F264A)
    val orangeCircle = Color(0xFFF2994A)

    NfcBroadcastReceiver { tag ->
        val tagIdHex = tag.id.joinToString("") { "%02X".format(it) }
        lastScannedTag = tagIdHex
        searchQuery = tagIdHex
    }

    Scaffold(
        containerColor = screenBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Llaves") },
                    label = { Text("Llaves") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = screenBackground,
                        selectedTextColor = screenBackground,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Préstamos") },
                    label = { Text("Préstamos") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = screenBackground,
                        selectedTextColor = screenBackground,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Nfc, contentDescription = "NFC") },
                    label = { Text("NFC") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = screenBackground,
                        selectedTextColor = screenBackground,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Incidencias") },
                    label = { Text("Incidencias") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = screenBackground,
                        selectedTextColor = screenBackground,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Buscar llave",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o aula", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.Gray)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Static NFC Concentric circles (no animation)
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring 3
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                )
                // Outer ring 2
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                )
                // Outer ring 1
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .border(1.dp, orangeCircle.copy(alpha = 0.6f), CircleShape)
                )
                // Inner orange circle (static)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(orangeCircle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC Scanning",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scanning texts
            Text(
                text = if (lastScannedTag != null) "¡Tarjeta leída!" else "Escaneando...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (lastScannedTag != null) Color(0xFF81C784) else Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Acerca el móvil a la etiqueta NFC de la llave",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            OutlinedButton(
                onClick = { searchQuery = "" },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 392, heightDp = 800)
@Composable
fun BuscarLlaveScreenPreview() {
    AppLlavesTheme {
        BuscarLlaveScreen()
    }
}
