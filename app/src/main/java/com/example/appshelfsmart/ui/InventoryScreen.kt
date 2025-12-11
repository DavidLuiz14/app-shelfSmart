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
import androidx.compose.runtime.LaunchedEffect
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
    viewModel: ProductViewModel,
    initialSelectedProduct: Product? = null,
    onInitialProductHandled: () -> Unit = {}
) {
    var selectedGroup by remember { mutableStateOf<List<Product>?>(null) }
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
    
    // Group items for display
    val groupedItems = filteredItems
        .groupBy { it.barcode.ifBlank { "${it.name}|${it.brand}" } }
        .map { (_, group) -> group }

    // Auto-select group from alert deep link
    LaunchedEffect(initialSelectedProduct) {
        if (initialSelectedProduct != null) {
            val targetGroup = groupedItems.find { group -> 
                group.any { it.barcode == initialSelectedProduct.barcode && it.name == initialSelectedProduct.name }
            }
            if (targetGroup != null) {
                selectedGroup = targetGroup
                onInitialProductHandled()
            }
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

    // Group items by Barcode (primary) or Name+Brand (fallback)
    val groupedInventory = filteredItems.groupBy { 
        if (it.barcode.isNotBlank()) it.barcode else "${it.name}|${it.brand}"
    }.values.toList()

    // Apply sorting to groups based on their "representative" item (e.g. earliest expiring)
    val sortedGroups = when (sortOption) {
        SortOption.NEWEST -> groupedInventory.sortedByDescending { group -> 
            group.maxOfOrNull { it.purchaseDate } ?: 0L 
        }
        SortOption.OLDEST -> groupedInventory.sortedBy { group -> 
            group.minOfOrNull { it.purchaseDate } ?: Long.MAX_VALUE 
        }
        SortOption.EXPIRING_SOONEST -> groupedInventory.sortedBy { group ->
            group.minOfOrNull { DateUtils.parseDate(it.expirationDate)?.time ?: Long.MAX_VALUE } ?: Long.MAX_VALUE
        }
    }

    if (selectedGroup != null) {
        // Refresh the selected group from inventoryItems to capture updates (e.g., consumption)
        val refreshedGroup = inventoryItems.filter { 
            val representative = selectedGroup!!.first()
            if (representative.barcode.isNotBlank()) {
                it.barcode == representative.barcode
            } else {
                it.name == representative.name && it.brand == representative.brand
            }
        }
        
        if (refreshedGroup.isNotEmpty()) {
            ProductDetailDialog(
                productGroup = refreshedGroup,
                onDismiss = { selectedGroup = null },
                onConsumed = { viewModel.markAsConsumed(it) },
                onWasted = { viewModel.markAsWasted(it) }
            )
        } else {
            selectedGroup = null // Group empty/deleted
        }
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

                if (sortedGroups.isEmpty()) {
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
                        items(sortedGroups) { group ->
                            ProductGroupCard(
                                productGroup = group,
                                onView = { selectedGroup = group }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGroupCard(
    productGroup: List<Product>,
    onView: () -> Unit
) {
    val representative = productGroup.first()
    val totalUnits = productGroup.sumOf { it.units }
    
    // Calculate aggregate expiration status
    // Find earliest expiration date
    val sortedByExpiration = productGroup.sortedBy { DateUtils.parseDate(it.expirationDate)?.time ?: Long.MAX_VALUE }
    val earliestExpiration = sortedByExpiration.first().expirationDate
    val daysUntilExpr = DateUtils.daysBetween(earliestExpiration)
    
    val anyExpired = productGroup.any { DateUtils.isExpired(it.expirationDate) }
    val anyExpiringSoon = productGroup.any { DateUtils.isExpiringSoon(it.expirationDate, 7) }
    
    val statusColor = when {
        anyExpired -> MaterialTheme.colorScheme.error
        anyExpiringSoon -> Color(0xFFFF9800) // Orange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusText = when {
        anyExpired -> "At least 1 Expired"
        anyExpiringSoon -> "Expiring Soon: $earliestExpiration"
        else -> "Expires: $earliestExpiration"
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
                    text = representative.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onView) {
                    Icon(Icons.Default.Visibility, contentDescription = "View Details")
                }
            }
            if (representative.brand.isNotBlank()) {
                Text(text = "Brand: ${representative.brand}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "Total Units: $totalUnits", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor
                )
                Text(
                    text = representative.barcode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    productGroup: List<Product>,
    onDismiss: () -> Unit,
    onConsumed: (Product) -> Unit,
    onWasted: (Product) -> Unit
) {
    val representative = productGroup.first()
    val scrollState = androidx.compose.foundation.rememberScrollState()
    val totalUnits = productGroup.sumOf { it.units }
    val daysSincePurchase = ((System.currentTimeMillis() - representative.purchaseDate) / (1000 * 60 * 60 * 24)).toInt()
    
    // Sort items by expiration date for the list
    val sortedItems = productGroup.sortedBy { DateUtils.parseDate(it.expirationDate)?.time ?: Long.MAX_VALUE }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        title = { Text(text = representative.name) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (representative.photoUri != null) {
                    coil.compose.AsyncImage(
                        model = representative.photoUri,
                        contentDescription = "Product Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                
                // General Info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "📅 Fecha de compra: ${DateUtils.formatDate(representative.purchaseDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "⏱️ ${DateUtils.formatDaysAgo(daysSincePurchase)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (representative.brand.isNotBlank()) Text("🏷️ Brand: ${representative.brand}")
                    if (representative.category.isNotBlank()) Text("📂 Category: ${representative.category}")
                    Text("📦 Quantity per Unit: ${representative.quantityValue ?: "N/A"} ${representative.quantityUnit ?: ""}")
                    Text(
                         text = "🔢 Total Units in Stock: $totalUnits",
                         style = MaterialTheme.typography.titleSmall,
                         color = MaterialTheme.colorScheme.primary
                    )
                    Text("🔖 Barcode: ${representative.barcode}")
                }
                
                HorizontalDivider()
                
                Text("Individual Items:", style = MaterialTheme.typography.titleMedium)
                
                // List of items
                sortedItems.forEach { product ->
                    ProductUnitRow(
                        product = product,
                        onConsumed = onConsumed,
                        onWasted = onWasted
                    )
                }
                
                if (!representative.nutritionalInfoSimplified.isNullOrBlank()) {
                     HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                     Text("🥗 Info Nutricional (General):", style = MaterialTheme.typography.labelLarge)
                     Text(representative.nutritionalInfoSimplified, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    )
}

@Composable
fun ProductUnitRow(
    product: Product,
    onConsumed: (Product) -> Unit,
    onWasted: (Product) -> Unit
) {
    val daysUntilExpiration = DateUtils.daysBetween(product.expirationDate)
    val isExpired = DateUtils.isExpired(product.expirationDate)
    val isExpiringSoon = DateUtils.isExpiringSoon(product.expirationDate, 7)
    
    val dateColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        isExpiringSoon -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exp: ${product.expirationDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = dateColor
                )
                Text(
                    text = "Units: ${product.units}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.TextButton(
                    onClick = { onConsumed(product) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    Text("Check")
                }
                androidx.compose.material3.TextButton(
                    onClick = { onWasted(product) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Trash")
                }
            }
        }
    }
}
