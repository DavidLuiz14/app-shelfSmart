package com.example.appshelfsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

import com.example.appshelfsmart.ui.InventoryScreen
import com.example.appshelfsmart.ui.ScanScreen
import com.example.appshelfsmart.ui.MainMenuScreen
import com.example.appshelfsmart.ui.theme.AppShelfSmartTheme
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.ui.AddProductScreen
import com.example.appshelfsmart.ui.ScanMode
import com.example.appshelfsmart.viewmodel.ProductViewModel
import com.example.appshelfsmart.data.repository.ProductRepository
import com.example.appshelfsmart.ui.AlertsScreen
import com.example.appshelfsmart.data.database.AppDatabase
import com.example.appshelfsmart.viewmodel.ProductViewModelFactory
import com.example.appshelfsmart.workers.AlertWorker
import com.example.appshelfsmart.data.repository.UserPreferencesRepository
import com.example.appshelfsmart.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // User Preferences (Dark Mode)
            val userPreferencesRepository = remember { UserPreferencesRepository(applicationContext) }
            val isDarkMode by userPreferencesRepository.isDarkMode.collectAsState(initial = false)





            AppShelfSmartTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // WorkManager
                    androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                        "ShelfSmartAlertWork",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        androidx.work.PeriodicWorkRequestBuilder<AlertWorker>(
                            12, java.util.concurrent.TimeUnit.HOURS
                        ).build()
                    )
                    
                    // Request Notification Permission (Android 13+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val permissionState = androidx.core.content.ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val launcher = rememberLauncherForActivityResult(
                                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                            ) { isGranted: Boolean ->
                                // Handle permission
                            }
                            LaunchedEffect(Unit) {
                                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val database = remember { AppDatabase.getDatabase(applicationContext) }
                    val viewModelFactory = remember { ProductViewModelFactory(database.productDao()) }
                    val viewModel: ProductViewModel = viewModel(factory = viewModelFactory)
                    val recipeViewModel: com.example.appshelfsmart.viewmodel.RecipeViewModel = viewModel()
                    val repository = remember { ProductRepository() }
                    
                    // Check for deep link from notification
                    val initialScreen = if (intent.getStringExtra("navigate_to") == "alerts") "alerts" else "main_menu"
                    var currentScreen by remember { mutableStateOf(initialScreen) }
                    
                    var selectedRecipe by remember { mutableStateOf<com.example.appshelfsmart.data.Recipe?>(null) }
                    
                    // Temporary state to hold scanned data before adding/editing product
                    var tempId by remember { mutableStateOf<String?>(null) }
                    var tempBarcode by remember { mutableStateOf("") }
                    var tempDate by remember { mutableStateOf("") }
                    var tempName by remember { mutableStateOf("") }
                    var tempBrand by remember { mutableStateOf("") }
                    var tempManufacturer by remember { mutableStateOf("") }
                    var tempUnits by remember { mutableStateOf("1") }
                    var tempCategory by remember { mutableStateOf("") }
                    var tempQuantityUnit by remember { mutableStateOf("") }
                    var tempQuantityValue by remember { mutableStateOf(0.0) }
                    var tempNutritionalInfoRaw by remember { mutableStateOf("") }
                    var tempNutritionalInfoSimplified by remember { mutableStateOf("") }
                    var tempExpirationDates by remember { mutableStateOf(listOf<String>()) }
                    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
                    
                    // App State
                    var selectedProductFromAlert by remember { mutableStateOf<Product?>(null) }
                    
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
                                tempUnits = "1"
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
                                tempUnits = "1"
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
                                val ingredients = viewModel.inventoryItems.value.map { it.name }
                                recipeViewModel.searchRecipes(ingredients)
                            },
                            onNavigateToSettings = { currentScreen = "settings" },
                            alertsCount = viewModel.getTotalAlertsCount()
                        )
                        "settings" -> SettingsScreen(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { enabled ->
                                lifecycleScope.launch {
                                    userPreferencesRepository.setDarkMode(enabled)
                                }
                            },
                            onBack = { currentScreen = "main_menu" }
                        )
                        "inventory" -> {
                            val inventoryItems by viewModel.inventoryItems.collectAsState()
                            
                            InventoryScreen(
                            onScanClick = { 
                                tempId = null
                                tempBarcode = ""
                                tempDate = ""
                                tempName = ""
                                tempBrand = ""
                                tempManufacturer = ""
                                tempCategory = ""
                                tempUnits = "1"
                                tempQuantityUnit = ""
                                tempQuantityValue = 0.0
                                tempNutritionalInfoRaw = ""
                                tempNutritionalInfoSimplified = ""
                                currentScreen = "scan_barcode" 
                            },
                            inventoryItems = inventoryItems,
                            totalProducts = viewModel.totalProductsCount,
                            expiringSoonCount = viewModel.getExpiringSoonProducts().size,
                            lowStockCount = viewModel.getLowStockProducts().size,
                            onDeleteClick = { product ->
                                viewModel.deleteProduct(product)
                            },
                            viewModel = viewModel,
                            initialSelectedProduct = selectedProductFromAlert,
                            onInitialProductHandled = { selectedProductFromAlert = null }
                        )
                        }
                        "scan_barcode" -> ScanScreen(
                            onProductScanned = { barcode ->
                                tempBarcode = barcode
                                // Check local inventory first to preserve details like Photo
                                val existingProduct = viewModel.getProductByBarcode(barcode)
                                
                                lifecycleScope.launch {
                                    if (existingProduct != null) {
                                         // ADD new unit to existing product
                                         tempName = existingProduct.name
                                         tempBrand = existingProduct.brand
                                         tempManufacturer = existingProduct.manufacturer
                                         tempCategory = existingProduct.category
                                         tempQuantityUnit = existingProduct.quantityUnit ?: ""
                                         tempQuantityValue = existingProduct.quantityValue ?: 0.0
                                         if (existingProduct.photoUri != null) {
                                             tempPhotoUri = android.net.Uri.parse(existingProduct.photoUri)
                                         }
                                    } else {
                                        val product = repository.getProductDetails(barcode)
                                        if (product != null) {
                                            if (product.name.isNotBlank()) tempName = product.name
                                            if (product.brand.isNotBlank()) tempBrand = product.brand
                                            if (product.manufacturer.isNotBlank()) tempManufacturer = product.manufacturer
                                            if (product.category.isNotBlank()) tempCategory = product.category
                                            if (!product.quantityUnit.isNullOrBlank()) tempQuantityUnit = product.quantityUnit
                                            if (product.quantityValue != null && product.quantityValue > 0) tempQuantityValue = product.quantityValue
                                        }
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
                            onProductScanned = { _ -> },
                            onDateScanned = { date, _, _, _ ->
                                tempDate = date
                                currentScreen = "add_product" 
                            },
                            onManualEntry = { currentScreen = "add_product" },
                            onFinish = { currentScreen = "inventory" },
                            initialMode = ScanMode.TEXT
                        )
                        "scan_nutrition" -> ScanScreen(
                            onProductScanned = { _ -> },
                            onDateScanned = { text, _, _, _ ->
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
                            initialUnits = tempUnits,
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
                                    tempUnits = "1"
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
                            onStateChanged = { name, brand, manufacturer, category, qtyVal, qtyUnit, dates, units, photo ->
                                tempName = name
                                tempBrand = brand
                                tempManufacturer = manufacturer
                                tempCategory = category
                                tempQuantityValue = qtyVal
                                tempQuantityUnit = qtyUnit
                                tempExpirationDates = dates
                                tempUnits = units
                                tempPhotoUri = photo
                            }
                        )
                        "alerts" -> AlertsScreen(
                            urgentAlerts = viewModel.getUrgentExpirationAlerts(),
                            warningAlerts = viewModel.getWarningExpirationAlerts(),
                            cautionAlerts = viewModel.getCautionExpirationAlerts(),
                            lowStockAlerts = viewModel.getLowStockProducts(),
                            onProductClick = { product ->
                                selectedProductFromAlert = product
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



