package com.example.appshelfsmart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.utils.DateUtils
import com.example.appshelfsmart.viewmodel.ProductViewModel

enum class ExpirationFilter {
    ALL, EXPIRING_SOON, EXPIRED, VALID
}

enum class SortOption {
    NEWEST, OLDEST, EXPIRING_SOONEST
}

@Composable
fun InventoryScreen(
    onScanClick: () -> Unit,
    inventoryItems: List<Product>,
    totalProducts: Int,
    expiringSoonCount: Int,
    lowStockCount: Int,
    onDeleteClick: (Product) -> Unit,
    viewModel: ProductViewModel
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expirationFilter by remember { mutableStateOf(ExpirationFilter.ALL) }
    var sortOption by remember { mutableStateOf(SortOption.NEWEST) }

    val categories = listOf(
        "Todos",
        "Lácteos y derivados",
        "Carnes y pescados",
        "Frutas",
        "Verduras",
        "Granos y cereales",
        "Bebidas",
        "Condimentos y especias",
        "Snacks y dulces",
        "Productos de limpieza",
        "Otros"
    )

    // Apply filters and search
    var filteredItems = if (selectedCategory == null || selectedCategory == "Todos") {
        inventoryItems
    } else {
        inventoryItems.filter { it.category == selectedCategory }
    }

    // Apply search
    if (searchQuery.isNotBlank()) {
        val lowerQuery = searchQuery.lowercase()
        filteredItems = filteredItems.filter { product ->
            product.name.lowercase().contains(lowerQuery) ||
            product.brand.lowercase().contains(lowerQuery)
        }
    }

    // Apply expiration filter
    filteredItems = when (expirationFilter) {
        ExpirationFilter.EXPIRING_SOON -> filteredItems.filter { 
            DateUtils.isExpiringSoon(it.expirationDate, 7) 
        }
        ExpirationFilter.EXPIRED -> filteredItems.filter { 
            DateUtils.isExpired(it.expirationDate) 
        }
        ExpirationFilter.VALID -> filteredItems.filter { 
            !DateUtils.isExpired(it.expirationDate) && 
            !DateUtils.isExpiringSoon(it.expirationDate, 7) 
        }
        ExpirationFilter.ALL -> filteredItems
    }

    // Apply sorting
    filteredItems = when (sortOption) {
        SortOption.NEWEST -> filteredItems.sortedByDescending { it.purchaseDate }
        SortOption.OLDEST -> filteredItems.sortedBy { it.purchaseDate }
        SortOption.EXPIRING_SOONEST -> filteredItems.sortedBy { 
            DateUtils.parseDate(it.expirationDate)?.time ?: Long.MAX_VALUE 
        }
    }

    if (selectedProduct != null) {
        ProductDetailDialog(
            product = selectedProduct!!,
            onDismiss = { selectedProduct = null },
            onConsumed = { 
                viewModel.markAsConsumed(it)
                selectedProduct = null
            },
            onWasted = { 
                viewModel.markAsWasted(it)
                selectedProduct = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dashboard at the top
                InventoryDashboard(
                    totalProducts = totalProducts,
                    expiringSoonCount = expiringSoonCount,
                    lowStockCount = lowStockCount
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar por nombre o marca...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )

                // Expiration Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = expirationFilter == ExpirationFilter.ALL,
                            onClick = { expirationFilter = ExpirationFilter.ALL },
                            label = { Text("Todos") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = expirationFilter == ExpirationFilter.EXPIRING_SOON,
                            onClick = { expirationFilter = ExpirationFilter.EXPIRING_SOON },
                            label = { Text("Por vencer") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = expirationFilter == ExpirationFilter.EXPIRED,
                            onClick = { expirationFilter = ExpirationFilter.EXPIRED },
                            label = { Text("Vencidos") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = expirationFilter == ExpirationFilter.VALID,
                            onClick = { expirationFilter = ExpirationFilter.VALID },
                            label = { Text("Vigentes") }
                        )
                    }
                }

                // Sort Options
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = sortOption == SortOption.NEWEST,
                            onClick = { sortOption = SortOption.NEWEST },
                            label = { Text("Más reciente") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = sortOption == SortOption.OLDEST,
                            onClick = { sortOption = SortOption.OLDEST },
                            label = { Text("Más antiguo") }
                        )
                    }
                    item {
                        androidx.compose.material3.FilterChip(
                            selected = sortOption == SortOption.EXPIRING_SOONEST,
                            onClick = { sortOption = SortOption.EXPIRING_SOONEST },
                            label = { Text("Próximo a vencer") }
                        )
                    }
                }

                // Category Filter
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        androidx.compose.material3.FilterChip(
                            selected = (category == "Todos" && selectedCategory == null) || category == selectedCategory,
                            onClick = {
                                selectedCategory = if (category == "Todos") null else category
                            },
                            label = { Text(category) }
                        )
                    }
                }

                if (filteredItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (inventoryItems.isEmpty()) "No items in inventory" else "No items match filters",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (inventoryItems.isEmpty()) {
                            Text(
                                text = "Tap + to add a product",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems) { item ->
                            ProductCard(
                                product = item,
                                onView = { selectedProduct = item },
                                onDelete = { onDeleteClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val daysUntilExpiration = DateUtils.daysBetween(product.expirationDate)
    val isExpired = DateUtils.isExpired(product.expirationDate)
    val isExpiringSoon = DateUtils.isExpiringSoon(product.expirationDate, 7)
    
    val expirationColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        isExpiringSoon -> Color(0xFFFF9800) // Orange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onView() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    androidx.compose.material3.IconButton(onClick = onView) {
                        Icon(androidx.compose.material.icons.Icons.Default.Visibility, contentDescription = "View")
                    }
                    androidx.compose.material3.IconButton(onClick = onDelete) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (product.brand.isNotBlank()) {
                Text(text = "Brand: ${product.brand}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "Units: ${product.units}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateUtils.formatDaysUntil(daysUntilExpiration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = expirationColor
                )
                Text(
                    text = product.barcode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConsumed: (Product) -> Unit,
    onWasted: (Product) -> Unit
) {
    val daysSincePurchase = ((System.currentTimeMillis() - product.purchaseDate) / (1000 * 60 * 60 * 24)).toInt()
    val daysUntilExpiration = DateUtils.daysBetween(product.expirationDate)
    val scrollState = androidx.compose.foundation.rememberScrollState()
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        title = { Text(text = product.name) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.photoUri != null) {
                    coil.compose.AsyncImage(
                        model = product.photoUri,
                        contentDescription = "Product Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                
                // Purchase date info
                Text(
                    text = "📅 Fecha de compra: ${DateUtils.formatDate(product.purchaseDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "⏱️ ${DateUtils.formatDaysAgo(daysSincePurchase)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Expiration date info
                Text(
                    text = "⚠️ Fecha de caducidad: ${product.expirationDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = DateUtils.formatDaysUntil(daysUntilExpiration),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (DateUtils.isExpired(product.expirationDate)) 
                        MaterialTheme.colorScheme.error 
                    else if (DateUtils.isExpiringSoon(product.expirationDate, 7))
                        Color(0xFFFF9800)
                    else 
                        Color(0xFF4CAF50)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Product details
                Text(
                    text = "📦 Cantidad por unidad: ${product.quantityValue ?: "N/A"} ${product.quantityUnit ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "🔢 Unidades disponibles: ${product.units}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.brand.isNotBlank()) {
                    Text("🏷️ Marca: ${product.brand}")
                }
                if (product.category.isNotBlank()) {
                    Text("📂 Categoría: ${product.category}")
                }
                Text("🔖 Código de barras: ${product.barcode}")
                
                if (!product.nutritionalInfoSimplified.isNullOrBlank()) {
                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("🥗 Info Nutricional:", style = MaterialTheme.typography.labelLarge)
                    Text(product.nutritionalInfoSimplified, style = MaterialTheme.typography.bodySmall)
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConsumed(product) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("✓ Consumido")
                    }
                    Button(
                        onClick = { onWasted(product) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("✗ Desperdiciado")
                    }
                }
            }
        }
    )
}
