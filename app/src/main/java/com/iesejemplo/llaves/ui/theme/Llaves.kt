package com.iesejemplo.llaves.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenBg = Color(0xFFF2F5FA)
private val GrayText = Color(0xFF5B6778)
private val CardBorder = Color(0xFFE3E8F0)

private data class Llave(val codigo: String, val nombre: String, val disponible: Boolean)

private val llavesEjemplo = listOf(
    Llave("101", "Aula 101", true),
    Llave("102", "Aula 102", false),
    Llave("LAB", "Laboratorio", true),
    Llave("GIM", "Gimnasio", false),
    Llave("BIB", "Biblioteca", true),
    Llave("SP", "Sala profesores", true),
    Llave("201", "Aula 201", false),
    Llave("TAL", "Taller", true),
)

@Composable
fun LlavesScreen() {
    Scaffold(
        containerColor = ScreenBg,
        bottomBar = { BottomBar() }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Header() }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryCard(
                        numero = "5",
                        etiqueta = "Disponibles",
                        fondo = GreenBg,
                        color = GreenText,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        numero = "3",
                        etiqueta = "Prestadas",
                        fondo = RedBg,
                        color = RedText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            items(llavesEjemplo) { llave ->
                Box(
                    modifier = Modifier.padding(
                        start = if (llavesEjemplo.indexOf(llave) % 2 == 0) 20.dp else 0.dp,
                        end = if (llavesEjemplo.indexOf(llave) % 2 == 1) 20.dp else 0.dp
                    )
                ) {
                    KeyCard(llave)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Navy,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 18.dp)
    ) {
        Column {
            Text(
                text = "Llaves del centro",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Consulta el estado de cada llave",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun SummaryCard(
    numero: String,
    etiqueta: String,
    fondo: Color,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(fondo, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = numero,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etiqueta,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun KeyCard(llave: Llave) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = llave.codigo,
                color = Navy,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = llave.nombre,
                color = GrayText,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(10.dp))
            EstadoChip(disponible = llave.disponible)
        }
    }
}

@Composable
private fun EstadoChip(disponible: Boolean) {
    Text(
        text = if (disponible) "Disponible" else "Prestada",
        color = if (disponible) GreenText else RedText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(
                color = if (disponible) GreenBg else RedBg,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

private data class NavItem(val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem("Llaves", Icons.Outlined.GridView),
    NavItem("Préstamos", Icons.AutoMirrored.Outlined.List),
    NavItem("NFC", Icons.Outlined.Nfc),
    NavItem("Incidencias", Icons.Outlined.WarningAmber),
)

@Composable
private fun BottomBar() {
    Column {
        HorizontalDivider(color = CardBorder)
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            navItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = index == 0,
                    onClick = { },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy,
                        selectedTextColor = Navy,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LlavesScreenPreview() {
    LlavesScreen()
}