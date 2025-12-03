package com.example.appshelfsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import com.example.appshelfsmart.ui.MainMenuScreen
import com.example.appshelfsmart.ui.theme.AppShelfSmartTheme

import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.ui.AddProductScreen
import com.example.appshelfsmart.ui.ScanMode
import com.example.appshelfsmart.viewmodel.ProductViewModel

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
                    var currentScreen by remember { mutableStateOf("main_menu") }
                    
                    // Temporary state to hold scanned data before adding/editing product
                    var tempId by remember { mutableStateOf<String?>(null) }
                    var tempBarcode by remember { mutableStateOf("") }
                    var tempDate by remember { mutableStateOf("") }
                    var tempName by remember { mutableStateOf("") }
                    var tempBrand by remember { mutableStateOf("") }
                    var tempManufacturer by remember { mutableStateOf("") }
                    var tempCategory by remember { mutableStateOf("") }
                    var tempQuantityUnit by remember { mutableStateOf("") }
                    var tempQuantityValue by remember { mutableStateOf(0.0) }
                    
                    // Navigation Logic
                    BackHandler(enabled = currentScreen != "main_menu") {
                        when (currentScreen) {
                            "inventory", "scan_barcode", "recipes", "settings" -> currentScreen = "main_menu"
                            "scan_date" -> currentScreen = "add_product" // Return to add product
                            "add_product" -> {
                                if (tempId != null) {
                                    currentScreen = "inventory"
                                    tempId = null
                                } else {
                                    currentScreen = "scan_barcode"
                                }
                                // Reset temp fields
                                tempBarcode = ""
                                tempDate = ""
                            }
                            else -> currentScreen = "main_menu"
                        }
                    }

                    when (currentScreen) {
                        "main_menu" -> MainMenuScreen(
                            onNavigateToRegistration = { 
                                tempId = null
                                tempBarcode = ""
                                tempDate = ""
                                tempName = ""
                                tempBrand = ""
                                tempManufacturer = ""
                                tempCategory = ""
                                tempQuantityUnit = ""
                                tempQuantityValue = 0.0
                                currentScreen = "scan_barcode" 
                            },
                            onNavigateToInventory = { currentScreen = "inventory" },
                            onNavigateToRecipes = { /* TODO */ },
                            onNavigateToSettings = { /* TODO */ }
                        )
                        "inventory" -> InventoryScreen(
                            onScanClick = { 
                                tempId = null
                                tempBarcode = ""
                                tempDate = ""
                                tempName = ""
                                tempBrand = ""
                                tempManufacturer = ""
                                tempCategory = ""
                                tempQuantityUnit = ""
                                tempQuantityValue = 0.0
                                currentScreen = "scan_barcode" 
                            },
                            inventoryItems = viewModel.inventoryItems,
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
                                        if (product.brand.isNotBlank()) tempBrand = product.brand
                                        if (product.manufacturer.isNotBlank()) tempManufacturer = product.manufacturer
                                        if (product.category.isNotBlank()) tempCategory = product.category
                                        if (!product.quantityUnit.isNullOrBlank()) tempQuantityUnit = product.quantityUnit
                                        if (product.quantityValue != null && product.quantityValue > 0) tempQuantityValue = product.quantityValue
                                    }
                                    currentScreen = "add_product"
                                }
                            },
                            onDateScanned = { date, _, _, _ ->
                                tempDate = date
                            },
                            onManualEntry = {
                                currentScreen = "add_product"
                            },
                            onFinish = {
                                currentScreen = "inventory"
                            },
                            initialMode = ScanMode.BARCODE
                        )
                        "scan_date" -> ScanScreen(
                            onProductScanned = { },
                            onDateScanned = { date, _, _, _ ->
                                tempDate = date
                                currentScreen = "add_product" 
                            },
                            onManualEntry = { currentScreen = "add_product" },
                            onFinish = { currentScreen = "inventory" },
                            initialMode = ScanMode.TEXT
                        )
                        "add_product" -> AddProductScreen(
                            productId = tempId,
                            initialBarcode = tempBarcode,
                            initialDate = tempDate,
                            initialName = tempName,
                            initialBrand = tempBrand,
                            initialManufacturer = tempManufacturer,
                            initialCategory = tempCategory,
                            initialQuantityValue = tempQuantityValue,
                            initialQuantityUnit = tempQuantityUnit,
                            onSave = { productList ->
                                productList.forEach { product ->
                                    if (tempId != null) {
                                        viewModel.updateProduct(product)
                                    } else {
                                        viewModel.addProduct(product)
                                    }
                                }
                                
                                if (tempId != null) {
                                    currentScreen = "inventory"
                                } else {
                                    // Reset temp fields for next scan
                                    tempId = null
                                    tempBarcode = ""
                                    tempDate = ""
                                    tempName = ""
                                    tempBrand = ""
                                    tempManufacturer = ""
                                    tempCategory = ""
                                    tempQuantityUnit = ""
                                    tempQuantityValue = 0.0
                                    currentScreen = "scan_barcode"
                                }
                            },
                            onCancel = { 
                                if (tempId != null) currentScreen = "inventory" else currentScreen = "main_menu"
                            }, 
                            onScanDate = { 
                                currentScreen = "scan_date" 
                            }
                        )
                    }
                }
            }
        }
    }
}
