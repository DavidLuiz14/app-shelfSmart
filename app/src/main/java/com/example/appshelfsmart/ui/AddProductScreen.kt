package com.example.appshelfsmart.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.appshelfsmart.data.Product
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    productId: String?,
    initialBarcode: String,
    initialDate: String,
    initialName: String,
    initialBrand: String,
    initialManufacturer: String,
    initialCategory: String,
    initialQuantityValue: Double,
    initialQuantityUnit: String,
    initialNutritionalInfoRaw: String,
    initialNutritionalInfoSimplified: String,
    initialExpirationDates: List<String> = emptyList(),
    initialPhotoUri: Uri? = null,
    onSave: (List<Product>) -> Unit,
    onCancel: () -> Unit,
    onScanDate: (Int) -> Unit,
    onScanNutrition: () -> Unit,
    onStateChanged: (String, String, String, String, Double, String, List<String>, Uri?) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var name by remember { mutableStateOf(initialName) }
    var brand by remember { mutableStateOf(initialBrand) }
    var manufacturer by remember { mutableStateOf(initialManufacturer) }
    var category by remember { mutableStateOf(initialCategory) }
    var units by remember { mutableStateOf("1") }
    var quantityValue by remember { mutableStateOf(if (initialQuantityValue > 0) initialQuantityValue.toString() else "") }
    var quantityUnit by remember { mutableStateOf(initialQuantityUnit.ifBlank { "g" }) }
    var photoUri by remember { mutableStateOf(initialPhotoUri) }

    // Initialize expiration dates from list if available, otherwise use initialDate
    var expirationDates by remember { 
        mutableStateOf(
            if (initialExpirationDates.isNotEmpty()) initialExpirationDates 
            else List(1) { initialDate }
        ) 
    }

    // Sync state back to parent
    LaunchedEffect(name, brand, manufacturer, category, quantityValue, quantityUnit, expirationDates, photoUri) {
        val qValue = quantityValue.toDoubleOrNull() ?: 0.0
        onStateChanged(name, brand, manufacturer, category, qValue, quantityUnit, expirationDates, photoUri)
    }

    // Update expirationDates list size when units changes
    LaunchedEffect(units) {
        val count = units.toIntOrNull() ?: 1
        if (count > 0) {
            val currentList = expirationDates.toMutableList()
            if (count > currentList.size) {
                // Add empty dates
                repeat(count - currentList.size) { currentList.add("") }
            } else if (count < currentList.size) {
                // Remove dates
                // Don't remove if we want to preserve data, but UI implies matching units.
                // Let's keep it simple and match units.
            }
            
            if (currentList.size != count) {
                 expirationDates = if (count > currentList.size) {
                     currentList + List(count - currentList.size) { "" }
                 } else {
                     currentList.take(count)
                 }
            }
        }
    }
    
    // If initialDate is passed (from scan), update the first empty slot
    LaunchedEffect(initialDate) {
        if (initialDate.isNotBlank()) {
             val newList = expirationDates.toMutableList()
             // Find first empty
             val firstEmpty = newList.indexOfFirst { it.isBlank() }
             if (firstEmpty != -1) {
                 newList[firstEmpty] = initialDate
                 expirationDates = newList
             } else if (newList.isNotEmpty()) {
                 // If no empty, update first? Or do nothing?
                 // User might have scanned to overwrite. Let's overwrite first for now.
                 newList[0] = initialDate
                 expirationDates = newList
             }
        }
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Camera Logic for Photo
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            photoUri = null
        }
    }

    fun takePhoto() {
        val photoFile = File(context.cacheDir, "product_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
        photoUri = uri
        cameraLauncher.launch(uri)
    }

    // Date Picker Logic
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerIndex by remember { mutableStateOf(0) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null) {
                        // Adjust for timezone to prevent off-by-one error
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = selectedDate
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        val dateString = formatter.format(calendar.time)
                        
                        val newList = expirationDates.toMutableList()
                        if (datePickerIndex < newList.size) {
                            newList[datePickerIndex] = dateString
                            expirationDates = newList
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Validation
    val isNameValid = name.isNotBlank()
    val isUnitsValid = units.toIntOrNull() != null && units.toInt() > 0
    val isQuantityValid = quantityValue.toDoubleOrNull() != null && quantityValue.toDouble() > 0
    val areDatesValid = expirationDates.all { it.isNotBlank() }
    val isFormValid = isNameValid && isUnitsValid && isQuantityValid && areDatesValid

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productId == null) "Add Product" else "Edit Product") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { takePhoto() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Product Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay button to retake photo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            FloatingActionButton(
                                onClick = { takePhoto() },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt, 
                                    contentDescription = "Retake Photo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap to Take Photo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = initialBarcode,
                onValueChange = {},
                label = { Text("Barcode") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name *") },
                isError = !isNameValid,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand (Auto)") },
                modifier = Modifier.fillMaxWidth()
            )



            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            val categories = listOf(
                "Lácteos y derivados",
                "Carnes y pescados",
                "Frutas Verduras Granos y cereales",
                "Bebidas Condimentos y especias",
                "Snacks y dulces",
                "Productos de limpieza",
                "Otros"
            )
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Quantity and Units Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantityValue,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quantityValue = it },
                    label = { Text("Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.6f),
                    isError = !isQuantityValid
                )
                
                // Unit Selector
                var unitExpanded by remember { mutableStateOf(false) }
                val unitsList = listOf("g", "kg", "ml", "L", "oz", "lb")
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded },
                    modifier = Modifier.weight(0.4f)
                ) {
                    OutlinedTextField(
                        value = quantityUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitsList.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    quantityUnit = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = units,
                onValueChange = { if (it.all { char -> char.isDigit() }) units = it },
                label = { Text("Units (Count) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = !isUnitsValid
            )

            // Multiple Expiration Dates
            Text("Expiration Dates", style = MaterialTheme.typography.titleMedium)
            expirationDates.forEachIndexed { index, date ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Item ${index + 1}") },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                datePickerIndex = index
                                showDatePicker = true
                            },
                        isError = date.isBlank()
                    )
                    IconButton(onClick = { 
                        datePickerIndex = index
                        showDatePicker = true 
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                    // Camera Scan for Date (Optional, might navigate away)
                    IconButton(onClick = { onScanDate(index) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan Date")
                    }
                }
            }

            // Nutritional Info Section
            Text("Nutritional Information", style = MaterialTheme.typography.titleMedium)
            
            var nutritionalInfoRaw by remember { mutableStateOf(initialNutritionalInfoRaw) }
            var nutritionalInfoSimplified by remember { mutableStateOf(initialNutritionalInfoSimplified) }
            var isSimplifying by remember { mutableStateOf(false) }
            val geminiService = remember { com.example.appshelfsmart.data.ai.GeminiService(com.example.appshelfsmart.BuildConfig.GEMINI_API_KEY) }
            val scope = rememberCoroutineScope()
            
            // Trigger simplification if raw info is present but simplified is not (e.g. returning from scan)
            // Trigger simplification if raw info is present but simplified is not (e.g. returning from scan)
            LaunchedEffect(initialNutritionalInfoRaw) {
                if (initialNutritionalInfoRaw.isNotBlank() && initialNutritionalInfoSimplified.isBlank()) {
                    isSimplifying = true
                    scope.launch {
                        try {
                            // Check if it's a URI (or list of URIs)
                            if (initialNutritionalInfoRaw.contains("content://") || initialNutritionalInfoRaw.contains("file://")) {
                                val uris = initialNutritionalInfoRaw.split(",")
                                val bitmaps = uris.mapNotNull { uriString ->
                                    if (uriString.isNotBlank()) {
                                        val uri = Uri.parse(uriString)
                                        if (android.os.Build.VERSION.SDK_INT < 28) {
                                            @Suppress("DEPRECATION")
                                            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                        } else {
                                            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                                            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                                                decoder.isMutableRequired = true
                                            }
                                        }
                                    } else null
                                }
                                
                                if (bitmaps.isNotEmpty()) {
                                    val simplified = geminiService.simplifyNutritionalInfo(bitmaps)
                                    nutritionalInfoSimplified = simplified
                                } else {
                                    nutritionalInfoSimplified = "Error: No valid images found."
                                }
                            } else {
                                // Fallback if it's just text (legacy)
                                nutritionalInfoSimplified = "Error: Expected image but got text."
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            nutritionalInfoSimplified = "Error processing: ${e.message}"
                        }
                        isSimplifying = false
                    }
                }
            }

            // Reuse scan screen for text? Or add a specific callback?
            // For simplicity, let's assume we use the same onScanDate callback but with a special index or flag?
            // Actually, AddProductScreen signature doesn't support generic text scan callback.
            // We need to update the signature or handle it locally if possible.
            // But ScanScreen is a separate screen.
            // Let's add a new callback `onScanNutrition`.
            
            if (nutritionalInfoSimplified.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Simplified Info (AI):", style = MaterialTheme.typography.labelLarge)
                        Text(nutritionalInfoSimplified)
                    }
                }
            } else if (isSimplifying) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = { onScanNutrition() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Scan Nutrition Label")
            }
            
            // Hidden field for raw info if needed, or just store it
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (isFormValid) {
                            val products = expirationDates.map { date ->
                                Product(
                                    id = if (productId != null && expirationDates.size == 1) productId else UUID.randomUUID().toString(),
                                    name = name,
                                    barcode = initialBarcode,
                                    expirationDate = date,
                                    brand = brand,
                                    manufacturer = manufacturer,
                                    category = category,
                                    units = 1, // Each entry is 1 unit
                                    quantityValue = quantityValue.toDouble(),
                                    quantityUnit = quantityUnit,
                                    photoUri = photoUri?.toString(),
                                    nutritionalInfoRaw = nutritionalInfoRaw,
                                    nutritionalInfoSimplified = nutritionalInfoSimplified
                                )
                            }
                            onSave(products)
                        }
                    },
                    enabled = isFormValid
                ) {
                    Text("Save")
                }
            }
        }
    }
}

