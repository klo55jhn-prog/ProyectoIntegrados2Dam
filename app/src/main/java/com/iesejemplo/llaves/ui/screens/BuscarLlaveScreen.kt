package com.iesejemplo.llaves.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.iesejemplo.llaves.ui.theme.AppLlavesTheme
import com.iesejemplo.llaves.util.NfcBroadcastReceiver
import kotlinx.coroutines.delay

enum class PasoEscaner {
    PASO_1_NFC,
    PASO_2_CAMARA_QR,
    PASO_3_RESULTADO
}

@Composable
fun BuscarLlaveScreen() {
    var pasoActual by remember { mutableStateOf(PasoEscaner.PASO_1_NFC) }
    var nfcTagId by remember { mutableStateOf<String?>(null) }
    var qrCarnetData by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val screenBackground = Color(0xFF0F264A)
    val orangeCircle = Color(0xFFF2994A)

    // Receptor NFC siempre activo para escanear cualquier tarjeta
    NfcBroadcastReceiver { tag ->
        val tagIdHex = tag.id.joinToString("") { "%02X".format(it) }
        nfcTagId = tagIdHex
        qrCarnetData = null // Resetea el carnet anterior para exigir un nuevo escaneo de carnet
        
        // Solicitar permiso de cámara si no lo tiene
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        // Transición automática al PASO 2 (Cámara QR)
        pasoActual = PasoEscaner.PASO_2_CAMARA_QR
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
            // Título
            Text(
                text = "Verificación de Acceso",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Indicador de Pasos del proceso
            IndicadorPasos(pasoActual = pasoActual)

            Spacer(modifier = Modifier.height(16.dp))

            // CONTENIDO SEGÚN EL PASO ACTUAL
            when (pasoActual) {
                PasoEscaner.PASO_1_NFC -> {
                    // PASO 1: Escanear tarjeta NFC
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .border(1.dp, orangeCircle.copy(alpha = 0.6f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(orangeCircle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "Escanear NFC",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "1. Escanea la tarjeta NFC",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Acerca la tarjeta NFC de la llave al teléfono",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                PasoEscaner.PASO_2_CAMARA_QR -> {
                    // PASO 2: Apertura automática de cámara para QR de Carnet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasCameraPermission) {
                            key(pasoActual, nfcTagId) {
                                CameraQrScannerView(
                                    onQrCodeScanned = { code ->
                                        qrCarnetData = code
                                        // Transición automática al PASO 3 (Resultado) sin tocar nada
                                        pasoActual = PasoEscaner.PASO_3_RESULTADO
                                    }
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Se requiere permiso de cámara para leer tu carnet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangeCircle)
                                ) {
                                    Text("Conceder Permiso")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Escanea tu Carnet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enfoca el código QR de tu carnet de estudiante con la cámara",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }

                PasoEscaner.PASO_3_RESULTADO -> {
                    // PASO 3: Muestra la información de la sala, edificio y número de clase
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "Acceso Autorizado",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = Color(0xFFE0E0E0)
                                )

                                ItemInfoAcceso(
                                    icono = Icons.Default.Domain,
                                    label = "EDIFICIO",
                                    valor = "Edificio Principal (Aulario U-Tad)"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                ItemInfoAcceso(
                                    icono = Icons.Default.MeetingRoom,
                                    label = "SALA / AULA",
                                    valor = "Aula 102 - Laboratorio de Desarrollo"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                ItemInfoAcceso(
                                    icono = Icons.Default.GridView,
                                    label = "NÚMERO DE CLASE",
                                    valor = "Planta 1 · Clase 102"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = Color(0xFFE0E0E0)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Tarjeta NFC",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = nfcTagId ?: "04A1B2C3",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F264A)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Carnet Estudiante",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = qrCarnetData ?: "EST-2026-948",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F264A)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¡Verificación completada!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Reiniciar
            OutlinedButton(
                onClick = {
                    pasoActual = PasoEscaner.PASO_1_NFC
                    nfcTagId = null
                    qrCarnetData = null
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (pasoActual == PasoEscaner.PASO_3_RESULTADO) "Escanear otra llave" else "Reiniciar Proceso",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** Componente de barra de estado con los pasos del flujo. */
@Composable
private fun IndicadorPasos(pasoActual: PasoEscaner) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CirculoPaso(
            numero = "1",
            titulo = "NFC",
            activo = pasoActual == PasoEscaner.PASO_1_NFC,
            completado = pasoActual != PasoEscaner.PASO_1_NFC
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.4f))
        )
        CirculoPaso(
            numero = "2",
            titulo = "QR Carnet",
            activo = pasoActual == PasoEscaner.PASO_2_CAMARA_QR,
            completado = pasoActual == PasoEscaner.PASO_3_RESULTADO
        )
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.4f))
        )
        CirculoPaso(
            numero = "3",
            titulo = "Info Aula",
            activo = pasoActual == PasoEscaner.PASO_3_RESULTADO,
            completado = pasoActual == PasoEscaner.PASO_3_RESULTADO
        )
    }
}

@Composable
private fun CirculoPaso(
    numero: String,
    titulo: String,
    activo: Boolean,
    completado: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completado -> Color(0xFF4CAF50)
                        activo -> Color(0xFFF2994A)
                        else -> Color.White.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (completado) "✓" else numero,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Text(
            text = titulo,
            color = if (activo || completado) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Componente helper para mostrar la información del aula/edificio en la tarjeta final. */
@Composable
private fun ItemInfoAcceso(
    icono: ImageVector,
    label: String,
    valor: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF2F5FA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color(0xFF0F264A),
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = valor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F264A)
            )
        }
    }
}

/** Visor de cámara en tiempo real con escáner de códigos QR usando CameraX y ML Kit. */
@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraQrScannerView(
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var lastScannedTime by remember { mutableLongStateOf(0L) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val currentTime = System.currentTimeMillis()
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (!rawValue.isNullOrEmpty() && (currentTime - lastScannedTime > 2000)) {
                                        lastScannedTime = currentTime
                                        onQrCodeScanned(rawValue)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraQrScanner", "Error al vincular cámara", e)
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true, widthDp = 392, heightDp = 800)
@Composable
fun BuscarLlaveScreenPreview() {
    AppLlavesTheme {
        BuscarLlaveScreen()
    }
}
