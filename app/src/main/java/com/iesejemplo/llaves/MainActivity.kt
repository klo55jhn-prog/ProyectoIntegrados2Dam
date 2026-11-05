package com.iesejemplo.llaves

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iesejemplo.llaves.ui.screens.*
import com.iesejemplo.llaves.ui.theme.AppLlavesTheme
import com.iesejemplo.llaves.util.INTENT_ACTION_NFC_READ
import com.iesejemplo.llaves.util.getParcelableCompatibility

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            AppLlavesTheme {
                AppPrincipal()
            }
        }
    }

    private fun enableNfc() {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                // Habilitar ReaderMode para callbacks silenciosos sin relanzar la Activity
                try {
                    adapter.enableReaderMode(
                        this,
                        { tag: Tag ->
                            runOnUiThread {
                                sendBroadcast(Intent(INTENT_ACTION_NFC_READ).apply {
                                    putExtra(NfcAdapter.EXTRA_TAG, tag)
                                    setPackage(packageName)
                                })
                            }
                        },
                        NfcAdapter.FLAG_READER_NFC_A or
                                NfcAdapter.FLAG_READER_NFC_B or
                                NfcAdapter.FLAG_READER_NFC_F or
                                NfcAdapter.FLAG_READER_NFC_V or
                                NfcAdapter.FLAG_READER_NFC_BARCODE,
                        null
                    )
                } catch (e: Exception) {
                    Log.e("NFC", "Error enabling reader mode", e)
                }
            }
        }
    }

    private fun disableNfc() {
        nfcAdapter?.let { adapter ->
            try {
                adapter.disableReaderMode(this)
            } catch (e: Exception) {
                Log.e("NFC", "Error disabling reader mode", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfc()
    }

    override fun onPause() {
        super.onPause()
        disableNfc()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tag = intent.getParcelableCompatibility(NfcAdapter.EXTRA_TAG, Tag::class.java)
        if (tag != null) {
            sendBroadcast(Intent(INTENT_ACTION_NFC_READ).apply {
                putExtra(NfcAdapter.EXTRA_TAG, tag)
                setPackage(packageName)
            })
        }
    }
}

enum class SeccionApp(val titulo: String, val icono: ImageVector) {
    BUSCAR_NFC("Escáner NFC", Icons.Default.Nfc),
    LLAVES("Llaves", Icons.Default.GridView),
    PRESTAMOS("Préstamos", Icons.AutoMirrored.Filled.List),
    INCIDENCIAS("Incidencias", Icons.Default.Warning),
    LOGIN("Perfil", Icons.Default.Person)
}

@Composable
fun AppPrincipal() {
    var seccionActual by rememberSaveable { mutableStateOf(SeccionApp.BUSCAR_NFC) }
    val darkBlue = Color(0xFF0F264A)

    Scaffold(
        containerColor = darkBlue,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Black,
                tonalElevation = 8.dp
            ) {
                SeccionApp.entries.forEach { seccion ->
                    val selected = seccionActual == seccion
                    NavigationBarItem(
                        selected = selected,
                        onClick = { seccionActual = seccion },
                        icon = { Icon(seccion.icono, contentDescription = seccion.titulo) },
                        label = {
                            Text(
                                text = seccion.titulo,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = darkBlue,
                            selectedTextColor = darkBlue,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE2E9F3)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (seccionActual) {
                SeccionApp.BUSCAR_NFC -> BuscarLlaveScreen()
                SeccionApp.LLAVES -> LlavesScreen()
                SeccionApp.PRESTAMOS -> LoansScreen()
                SeccionApp.INCIDENCIAS -> IncidentsScreen(onSend = { _, _, _ -> })
                SeccionApp.LOGIN -> LoginScreen()
            }
        }
    }
}
