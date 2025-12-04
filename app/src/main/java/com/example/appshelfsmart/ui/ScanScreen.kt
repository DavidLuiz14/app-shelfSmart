package com.example.appshelfsmart.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appshelfsmart.camera.BarcodeAnalyzer
import com.example.appshelfsmart.camera.CameraPreview
import com.example.appshelfsmart.camera.TextRecognitionAnalyzer
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.net.Uri

enum class ScanMode {
    BARCODE, TEXT, NUTRITION
}



@Composable
fun ScanScreen(
    onProductScanned: (String) -> Unit,
    onDateScanned: (String, String?, String?, String?) -> Unit,
    onManualEntry: () -> Unit,
    onFinish: () -> Unit,
    initialMode: ScanMode = ScanMode.BARCODE
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        ScanContent(
            onProductScanned = onProductScanned,
            onDateScanned = onDateScanned,
            onManualEntry = onManualEntry,
            onFinish = onFinish,
            initialMode = initialMode
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to scan products.")
        }
    }
}

@Composable
fun ScanContent(
    onProductScanned: (String) -> Unit,
    onDateScanned: (String, String?, String?, String?) -> Unit,
    onManualEntry: () -> Unit,
    onFinish: () -> Unit,
    initialMode: ScanMode = ScanMode.BARCODE
) {
    val context = LocalContext.current
    var scanMode by remember { mutableStateOf(initialMode) }
    var lastScannedText by remember { mutableStateOf("") }
    
    // Pause scanning after a successful scan to avoid multiple triggers
    var isScanning by remember { mutableStateOf(true) }
    
    val imageCapture = remember { ImageCapture.Builder().build() }

    val analyzer: ImageAnalysis.Analyzer = remember(scanMode, isScanning) {
        if (!isScanning && scanMode != ScanMode.NUTRITION) {
            ImageAnalysis.Analyzer { it.close() }
        } else {
            when (scanMode) {
                ScanMode.BARCODE -> BarcodeAnalyzer { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { code ->
                        if (isScanning) {
                            isScanning = false
                            lastScannedText = code
                            onProductScanned(code)
                        }
                    }
                }
                ScanMode.TEXT -> TextRecognitionAnalyzer { text ->
                    val rawText = text.text
                    // Numeric dates: DD/MM/YYYY, MM/DD/YYYY, DD/MM/YY, MM/DD/YY
                    val numericDatePattern = Regex("""\b\d{1,2}[/.-]\d{1,2}[/.-](\d{2}|\d{4})\b""")
                    
                    // ISO Date: YYYY-MM-DD
                    val isoDatePattern = Regex("""\b\d{4}[/.-]\d{2}[/.-]\d{2}\b""")
                    
                    // MM/YYYY or MM-YYYY
                    val monthYearPattern = Regex("""\b\d{1,2}[/.-]\d{4}\b""")

                    // Alphanumeric dates (English and Spanish)
                    val alphaMonthPattern = Regex(
                        """\b\d{1,2}[/.\-\s]+(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|ENE|ABR|AGO|DIC)[a-z]*[/.\-\s]+(\d{2}|\d{4})\b""", 
                        RegexOption.IGNORE_CASE
                    )

                    val patterns = listOf(numericDatePattern, isoDatePattern, alphaMonthPattern, monthYearPattern)
                    
                    var foundDate: String? = null
                    for (pattern in patterns) {
                        val match = pattern.find(rawText)
                        if (match != null) {
                            foundDate = match.value
                            break
                        }
                    }

                    if (foundDate != null && isScanning) {
                        isScanning = false
                        lastScannedText = foundDate
                        onDateScanned(foundDate, null, null, null)
                    }
                }
                ScanMode.NUTRITION -> ImageAnalysis.Analyzer { image ->
                    // No-op for analysis in nutrition mode, we use ImageCapture
                    image.close()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            analyzer = analyzer,
            imageCapture = if (scanMode == ScanMode.NUTRITION) imageCapture else null
        )

        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            // Center Focus Area
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f)
                    .height(if (scanMode == ScanMode.NUTRITION) 400.dp else 200.dp) // Taller for nutrition
                    .background(Color.Transparent)
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (scanMode) {
                        ScanMode.BARCODE -> "Scan Product Barcode"
                        ScanMode.TEXT -> "Scan Expiration Date"
                        ScanMode.NUTRITION -> "Scan Nutrition Label"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (scanMode == ScanMode.NUTRITION) {
                    // Multi-shot logic
                    val capturedCount = lastScannedText.split(",").filter { it.isNotBlank() }.size
                    
                    Text(
                        text = if (capturedCount > 0) "$capturedCount photos taken" else "Take clear photos of the label (overlap if needed)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val photoFile = File(context.externalCacheDir, "nutrition_scan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                
                                imageCapture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            val savedUri = Uri.fromFile(photoFile).toString()
                                            // Append to list (comma separated)
                                            lastScannedText = if (lastScannedText.isBlank()) savedUri else "$lastScannedText,$savedUri"
                                        }
                                        override fun onError(exc: ImageCaptureException) {
                                            // Handle error
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Capture")
                        }
                        
                        if (lastScannedText.isNotBlank()) {
                            Button(
                                onClick = {
                                    // Finish and send all URIs
                                    onDateScanned(lastScannedText, null, null, null)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Analyze (${lastScannedText.split(",").size})")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (initialMode != ScanMode.NUTRITION) {
                        Button(
                            onClick = { 
                                scanMode = ScanMode.BARCODE 
                                isScanning = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (scanMode == ScanMode.BARCODE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("Barcode")
                        }
                        Button(
                            onClick = { 
                                scanMode = ScanMode.TEXT 
                                isScanning = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (scanMode == ScanMode.TEXT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text("Date")
                        }
                    }
                }
                
                if (scanMode != ScanMode.NUTRITION) {
                    Button(
                        onClick = onManualEntry,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Enter Manually")
                    }
                    
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Finish Registration")
                    }
                } else {
                     Button(
                        onClick = onFinish, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
