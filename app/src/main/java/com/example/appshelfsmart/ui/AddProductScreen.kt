package com.example.appshelfsmart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appshelfsmart.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    productId: String? = null,
    initialBarcode: String = "",
    initialName: String = "",
    initialDate: String = "",
    initialWeight: String = "",
    initialLot: String = "",
    initialOrigin: String = "",
    onSave: (Product) -> Unit,
    onCancel: () -> Unit,
    onScanDate: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var expirationDate by remember { mutableStateOf(initialDate) }
    var weight by remember { mutableStateOf(initialWeight) }
    var lot by remember { mutableStateOf(initialLot) }
    var origin by remember { mutableStateOf(initialOrigin) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Add Product" else "Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = expirationDate,
                onValueChange = { expirationDate = it },
                label = { Text("Expiration Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { onScanDate(name) }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Scan Date")
                    }
                }
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight/Volume") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lot,
                onValueChange = { lot = it },
                label = { Text("Lot Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origin") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val product = if (productId != null) {
                            Product(
                                id = productId,
                                name = name,
                                barcode = barcode,
                                expirationDate = expirationDate,
                                weight = weight,
                                lot = lot,
                                origin = origin
                            )
                        } else {
                            Product(
                                name = name,
                                barcode = barcode,
                                expirationDate = expirationDate,
                                weight = weight,
                                lot = lot,
                                origin = origin
                            )
                        }
                        onSave(product)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save to Inventory")
            }
        }
    }
}
