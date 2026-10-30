package com.iesejemplo.llaves

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iesejemplo.llaves.ui.screens.*
import com.iesejemplo.llaves.ui.theme.AppLlavesTheme
import com.iesejemplo.llaves.ui.theme.Navy
import com.iesejemplo.llaves.ui.theme.Orange
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
                MainLanzadera()
            }
        }
    }

    private fun enableNfc() {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                // 1. Habilitar ReaderMode para callbacks directos instantáneos en cada toque
                try {
                    adapter.enableReaderMode(
                        this,
                        { tag: Tag ->
                            sendBroadcast(Intent(INTENT_ACTION_NFC_READ).apply {
                                putExtra(NfcAdapter.EXTRA_TAG, tag)
                                setPackage(packageName)
                            })
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

                // 2. Habilitar ForegroundDispatch como respaldo
                try {
                    val nfcIntentFilter = arrayOf(
                        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                        IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                    )
                    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    } else {
                        PendingIntent.getActivity(
                            this,
                            0,
                            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
                    adapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null)
                } catch (e: Exception) {
                    Log.e("NFC", "Error enabling foreground dispatch", e)
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
            try {
                adapter.disableForegroundDispatch(this)
            } catch (e: Exception) {
                Log.e("NFC", "Error disabling foreground dispatch", e)
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
        enableNfc()
    }
}

enum class Pantalla {
    MENU,
    BUSCAR_LLAVE,
    LLAVES,
    LOANS,
    LOGIN,
    INCIDENCIAS
}

@Composable
fun MainLanzadera() {
    var pantallaActual by remember { mutableStateOf(Pantalla.MENU) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (pantallaActual) {
            Pantalla.MENU -> MenuLanzadera(onSeleccionar = { pantallaActual = it })
            Pantalla.BUSCAR_LLAVE -> BuscarLlaveScreen()
            Pantalla.LLAVES -> LlavesScreen()
            Pantalla.LOANS -> LoansScreen()
            Pantalla.LOGIN -> LoginScreen()
            Pantalla.INCIDENCIAS -> IncidentsScreen(onSend = { _, _, _ -> })
        }

        // Botón flotante para regresar a la Lanzadera desde cualquier pantalla
        if (pantallaActual != Pantalla.MENU) {
            FloatingActionButton(
                onClick = { pantallaActual = Pantalla.MENU },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                containerColor = Orange,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    Text("Lanzadera", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MenuLanzadera(onSeleccionar: (Pantalla) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F264A))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = "🚀 Lanzadera de Pantallas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Selecciona una ventana para probarla",
            color = Color(0xFFC4D3E7),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BotonLanzadera(
                titulo = "1. BuscarLlaveScreen",
                subtitulo = "Buscador y escáner NFC",
                icono = Icons.Default.Search,
                onClick = { onSeleccionar(Pantalla.BUSCAR_LLAVE) }
            )

            BotonLanzadera(
                titulo = "2. Llaves",
                subtitulo = "Estado de llaves del centro",
                icono = Icons.Default.VpnKey,
                onClick = { onSeleccionar(Pantalla.LLAVES) }
            )

            BotonLanzadera(
                titulo = "3. LoansScreen",
                subtitulo = "Gestión de préstamos activos",
                icono = Icons.AutoMirrored.Filled.FormatListBulleted,
                onClick = { onSeleccionar(Pantalla.LOANS) }
            )

            BotonLanzadera(
                titulo = "4. Login",
                subtitulo = "Pantalla de inicio de sesión",
                icono = Icons.Default.Lock,
                onClick = { onSeleccionar(Pantalla.LOGIN) }
            )

            BotonLanzadera(
                titulo = "5. PantallaIncidencias",
                subtitulo = "Reportar llaves rotas o perdidas",
                icono = Icons.Default.Warning,
                onClick = { onSeleccionar(Pantalla.INCIDENCIAS) }
            )
        }
    }
}

@Composable
private fun BotonLanzadera(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Navy
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Orange,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = Color(0xFF5B6B82)
                )
            }
        }
    }
}
