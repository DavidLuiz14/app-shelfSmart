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
import androidx.compose.runtime.collectAsState
import com.example.appshelfsmart.data.repository.ProductRepository
import com.example.appshelfsmart.ui.AlertsScreen
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
                    val recipeViewModel: com.example.appshelfsmart.viewmodel.RecipeViewModel = viewModel()
                    val repository = remember { ProductRepository() }
                    var currentScreen by remember { mutableStateOf("main_menu") }
                    var selectedRecipe by remember { mutableStateOf<com.example.appshelfsmart.data.Recipe?>(null) }
                    
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
                    var tempNutritionalInfoRaw by remember { mutableStateOf("") }
                    var tempNutritionalInfoSimplified by remember { mutableStateOf("") }
                    var tempExpirationDates by remember { mutableStateOf(listOf<String>()) }
                    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
                    
                    // Navigation Logic
                    BackHandler(enabled = currentScreen != "main_menu") {
                        when (currentScreen) {
                            "inventory", "scan_barcode", "recipes", "settings", "alerts" -> currentScreen = "main_menu"
                            "recipe_detail" -> currentScreen = "recipes"
                            "scan_date", "scan_nutrition" -> currentScreen = "add_product" // Return to add product
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
                                tempNutritionalInfoRaw = ""
                                tempNutritionalInfoSimplified = ""
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
                                tempNutritionalInfoRaw = ""
                                tempNutritionalInfoSimplified = ""
                                currentScreen = "scan_barcode" 
                            },
                            onNavigateToInventory = { currentScreen = "inventory" },
                            onNavigateToAlerts = { currentScreen = "alerts" },
                            onNavigateToRecipes = { 
                                currentScreen = "recipes"
                                // Trigger recipe search with available ingredients
                                val ingredients = viewModel.inventoryItems.map { it.name }
                                recipeViewModel.searchRecipes(ingredients)
                            },
                            onNavigateToSettings = { /* TODO */ },
                            alertsCount = viewModel.getTotalAlertsCount()
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
                                tempNutritionalInfoRaw = ""
                                tempNutritionalInfoSimplified = ""
                                currentScreen = "scan_barcode" 
                            },
                            inventoryItems = viewModel.inventoryItems,
                            totalProducts = viewModel.totalProductsCount,
                            expiringSoonCount = viewModel.getExpiringSoonProducts().size,
                            lowStockCount = viewModel.getLowStockProducts().size,
                            onDeleteClick = { product ->
                                viewModel.removeProduct(product)
                            },
                            viewModel = viewModel
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
                        "scan_nutrition" -> ScanScreen(
                            onProductScanned = { },
                            onDateScanned = { text, _, _, _ ->
                                // We use onDateScanned callback but it returns text. 
                                // Ideally we should have a generic onTextScanned. 
                                // But for now, let's use the first parameter as the raw text.
                                // Wait, ScanScreen's TextRecognitionAnalyzer filters for dates.
                                // We need to update ScanScreen to support generic text scanning or just use what we have.
                                // The TextRecognitionAnalyzer logic is specific to dates.
                                // I need to update ScanScreen to allow raw text return.
                                tempNutritionalInfoRaw = text 
                                currentScreen = "add_product" 
                            },
                            onManualEntry = { currentScreen = "add_product" },
                            onFinish = { currentScreen = "inventory" },
                            initialMode = ScanMode.NUTRITION
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
                            initialNutritionalInfoRaw = tempNutritionalInfoRaw,
                            initialNutritionalInfoSimplified = tempNutritionalInfoSimplified,
                            initialExpirationDates = tempExpirationDates,
                            initialPhotoUri = tempPhotoUri,
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
                                    tempNutritionalInfoRaw = ""
                                    tempNutritionalInfoSimplified = ""
                                    tempExpirationDates = emptyList()
                                    tempPhotoUri = null
                                    currentScreen = "scan_barcode"
                                }
                            },
                            onCancel = { 
                                if (tempId != null) currentScreen = "inventory" else currentScreen = "main_menu"
                            }, 
                            onScanDate = { 
                                currentScreen = "scan_date" 
                            },
                            onScanNutrition = {
                                currentScreen = "scan_nutrition"
                            },
                            onStateChanged = { name, brand, manufacturer, category, qtyVal, qtyUnit, dates, photo ->
                                tempName = name
                                tempBrand = brand
                                tempManufacturer = manufacturer
                                tempCategory = category
                                tempQuantityValue = qtyVal
                                tempQuantityUnit = qtyUnit
                                tempExpirationDates = dates
                                tempPhotoUri = photo
                            }
                        )
                        "alerts" -> AlertsScreen(
                            urgentAlerts = viewModel.getUrgentExpirationAlerts(),
                            warningAlerts = viewModel.getWarningExpirationAlerts(),
                            cautionAlerts = viewModel.getCautionExpirationAlerts(),
                            lowStockAlerts = viewModel.getLowStockProducts(),
                            onProductClick = { product ->
                                // Navigate to product details or inventory
                                currentScreen = "inventory"
                            },
                            onBack = { currentScreen = "main_menu" }
                        )
                        "recipes" -> {
                            val completeRecipes by recipeViewModel.completeRecipes.collectAsState()
                            val almostCompleteRecipes by recipeViewModel.almostCompleteRecipes.collectAsState()
                            val isLoading by recipeViewModel.isLoading.collectAsState()
                            val errorMessage by recipeViewModel.errorMessage.collectAsState()
                            
                            com.example.appshelfsmart.ui.RecipesScreen(
                                completeRecipes = completeRecipes,
                                almostCompleteRecipes = almostCompleteRecipes,
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                onRecipeClick = { recipe ->
                                    selectedRecipe = recipe
                                    currentScreen = "recipe_detail"
                                },
                                onBack = { currentScreen = "main_menu" }
                            )
                        }
                        "recipe_detail" -> {
                            selectedRecipe?.let { recipe ->
                                com.example.appshelfsmart.ui.RecipeDetailScreen(
                                    recipe = recipe,
                                    onBack = { currentScreen = "recipes" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
