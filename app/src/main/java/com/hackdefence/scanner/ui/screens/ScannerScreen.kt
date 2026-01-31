package com.hackdefence.scanner.ui.screens

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.hackdefence.scanner.utils.TicketColorHelper
import com.hackdefence.scanner.viewmodel.ScanState
import com.hackdefence.scanner.viewmodel.ScannerViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState by viewModel.scanState.collectAsState()
    val staffName by viewModel.staffName.collectAsState()
    val scanCount by viewModel.scanCount.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Entry Scanner", fontSize = 18.sp)
                        Text(
                            text = staffName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    Text(
                        text = "✓ $scanCount",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted) {
                if (scanState == ScanState.Scanning || scanState == ScanState.Idle) {
                    CameraPreview(
                        onQRCodeScanned = { qrCode ->
                            viewModel.verifyEntry(qrCode)
                        }
                    )

                    // Scanning overlay
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val scanBoxSize = minOf(canvasWidth, canvasHeight) * 0.6f

                            // Dark overlay
                            drawRect(
                                color = Color.Black.copy(alpha = 0.5f),
                                size = size
                            )

                            // Clear scanning area
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = Offset(
                                    x = (canvasWidth - scanBoxSize) / 2,
                                    y = (canvasHeight - scanBoxSize) / 2
                                ),
                                size = ComposeSize(scanBoxSize, scanBoxSize),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                            )

                            // Border
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(
                                    x = (canvasWidth - scanBoxSize) / 2,
                                    y = (canvasHeight - scanBoxSize) / 2
                                ),
                                size = ComposeSize(scanBoxSize, scanBoxSize),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 100.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                )
                            ) {
                                Text(
                                    text = "Scan QR Code",
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                } else {
                    ResultScreen(
                        scanState = scanState,
                        onDismiss = { viewModel.resetState() }
                    )
                }
            } else {
                // Permission not granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera permission required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }

            if (scanState == ScanState.Verifying) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasScanned by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !hasScanned) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { qrCode ->
                                        if (!hasScanned) {
                                            hasScanned = true
                                            onQRCodeScanned(qrCode)
                                        }
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
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(hasScanned) {
        if (hasScanned) {
            kotlinx.coroutines.delay(2000)
            hasScanned = false
        }
    }
}

@Composable
fun ResultScreen(
    scanState: ScanState,
    onDismiss: () -> Unit
) {
    val details = when (scanState) {
        is ScanState.Success -> scanState.details
        is ScanState.AlreadyEntered -> scanState.details
        is ScanState.PaymentNotVerified -> scanState.details
        else -> null
    }

    // Get ticket color info based on ticket type and price
    val ticketColorInfo = TicketColorHelper.getTicketColor(details)

    // Determine background color based on state
    val backgroundColor = when (scanState) {
        is ScanState.Success -> ticketColorInfo.backgroundColor
        is ScanState.AlreadyEntered -> Color(0xFFFF9800) // Orange for duplicate
        is ScanState.PaymentNotVerified -> Color(0xFFF44336) // Red for payment not verified
        is ScanState.Error -> Color(0xFFF44336) // Red for error
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Header
                Text(
                    text = when (scanState) {
                        is ScanState.Success -> "✓ ENTRY VERIFIED"
                        is ScanState.AlreadyEntered -> "⚠ ALREADY ENTERED"
                        is ScanState.PaymentNotVerified -> "✗ PAYMENT NOT VERIFIED"
                        is ScanState.Error -> "✗ ERROR"
                        else -> ""
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Ticket Type Display (Large and Prominent)
                if (scanState is ScanState.Success) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = ticketColorInfo.displayText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            ticketColorInfo.subText?.let { subText ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Attendee Details
                details?.let {
                    Text(
                        text = it.full_name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.registration_id,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    // Show ticket info for duplicate/payment not verified states
                    if (scanState !is ScanState.Success) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ticketColorInfo.displayText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                ticketColorInfo.subText?.let { subText ->
                                    Text(
                                        text = subText,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Additional info
                    if (scanState is ScanState.AlreadyEntered) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Already verified at ${it.entry_verified_at ?: ""}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (scanState is ScanState.Error) {
                    Text(
                        text = scanState.message,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Next", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
