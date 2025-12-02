package com.example.appshelfsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.appshelfsmart.ui.InventoryScreen
import com.example.appshelfsmart.ui.ScanScreen
import com.example.appshelfsmart.ui.theme.AppShelfSmartTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.ui.AddProductScreen
import com.example.appshelfsmart.ui.ScanMode
import com.example.appshelfsmart.viewmodel.ProductViewModel

import androidx.lifecycle.lifecycleScope
import com.example.appshelfsmart.data.repository.ProductRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppShelfSmartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ProductViewModel = viewModel()
                    val repository = remember { ProductRepository() }
                    var currentScreen by remember { mutableStateOf("inventory") }
                    
                    // Temporary state to hold scanned data before adding/editing product
                    var tempId by remember { mutableStateOf<String?>(null) }
                    var tempBarcode by remember { mutableStateOf("") }
                    var tempDate by remember { mutableStateOf("") }
                    var tempName by remember { mutableStateOf("") }
                    var tempWeight by remember { mutableStateOf("") }
                    var tempLot by remember { mutableStateOf("") }
                    var tempOrigin by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "inventory" -> InventoryScreen(
                            onScanClick = { 
                                tempId = null
                                tempBarcode = ""
                                tempDate = ""
                                tempName = ""
                                tempWeight = ""
                                tempLot = ""
                                tempOrigin = ""
                                currentScreen = "scan_barcode" 
                            },
                            inventoryItems = viewModel.inventoryItems,
                            onEditClick = { product ->
                                tempId = product.id
                                tempBarcode = product.barcode
                                tempName = product.name
                                tempDate = product.expirationDate
                                tempWeight = product.weight
                                tempLot = product.lot
                                tempOrigin = product.origin
                                currentScreen = "add_product"
                            },
                            onDeleteClick = { product ->
                                viewModel.removeProduct(product)
                            }
                        )
                        "scan_barcode" -> ScanScreen(
                            onProductScanned = { barcode ->
                                tempBarcode = barcode
                                // Fetch product details
                                lifecycleScope.launch {
                                    val product = repository.getProductDetails(barcode)
                                    if (product != null) {
                                        if (product.name.isNotBlank()) tempName = product.name
                                        if (product.weight.isNotBlank()) tempWeight = product.weight
                                        if (product.origin.isNotBlank()) tempOrigin = product.origin
                                    }
                                    currentScreen = "add_product"
                                }
                            },
                            onDateScanned = { date, weight, lot, origin ->
                                tempDate = date
                                if (weight != null) tempWeight = weight
                                if (lot != null) tempLot = lot
                                if (origin != null) tempOrigin = origin
                            },
                            initialMode = ScanMode.BARCODE
                        )
                        "scan_date" -> ScanScreen(
                            onProductScanned = { },
                            onDateScanned = { date, weight, lot, origin ->
                                tempDate = date
                                if (weight != null) tempWeight = weight
                                if (lot != null) tempLot = lot
                                if (origin != null) tempOrigin = origin
                                currentScreen = "add_product"
                            },
                            initialMode = ScanMode.TEXT
                        )
                        "add_product" -> AddProductScreen(
                            productId = tempId,
                            initialBarcode = tempBarcode,
                            initialDate = tempDate,
                            initialName = tempName,
                            initialWeight = tempWeight,
                            initialLot = tempLot,
                            initialOrigin = tempOrigin,
                            onSave = { product ->
                                if (tempId != null) {
                                    viewModel.updateProduct(product)
                                } else {
                                    viewModel.addProduct(product)
                                }
                                currentScreen = "inventory"
                            },
                            onCancel = { currentScreen = "inventory" },
                            onScanDate = { currentName -> 
                                tempName = currentName
                                currentScreen = "scan_date" 
                            }
                        )
                    }
                }
            }
        }
    }
}
