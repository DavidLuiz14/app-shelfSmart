package com.example.appshelfsmart.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    urgentAlerts: List<Product>,
    warningAlerts: List<Product>,
    cautionAlerts: List<Product>,
    lowStockAlerts: List<Product>,
    onProductClick: (Product) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas Inteligentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Warning, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (urgentAlerts.isEmpty() && warningAlerts.isEmpty() && 
            cautionAlerts.isEmpty() && lowStockAlerts.isEmpty()) {
            // No alerts
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No hay alertas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Todo está bajo control",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Urgent alerts (Red)
                if (urgentAlerts.isNotEmpty()) {
                    item {
                        AlertSection(
                            title = "🔴 Urgente - Caducan HOY",
                            description = "${urgentAlerts.size} producto(s)",
                            color = Color(0xFFD32F2F),
                            products = urgentAlerts,
                            onProductClick = onProductClick
                        )
                    }
                }

                // Warning alerts (Orange)
                if (warningAlerts.isNotEmpty()) {
                    item {
                        AlertSection(
                            title = "🟠 Próxima - Caducan en 1-3 días",
                            description = "${warningAlerts.size} producto(s)",
                            color = Color(0xFFFF9800),
                            products = warningAlerts,
                            onProductClick = onProductClick
                        )
                    }
                }

                // Caution alerts (Yellow)
                if (cautionAlerts.isNotEmpty()) {
                    item {
                        AlertSection(
                            title = "🟡 Preventiva - Caducan en 4-7 días",
                            description = "${cautionAlerts.size} producto(s)",
                            color = Color(0xFFFFC107),
                            products = cautionAlerts,
                            onProductClick = onProductClick
                        )
                    }
                }

                // Low stock alerts
                if (lowStockAlerts.isNotEmpty()) {
                    item {
                        AlertSection(
                            title = "📦 Stock Bajo",
                            description = "${lowStockAlerts.size} producto(s) con pocas unidades",
                            color = Color(0xFF2196F3),
                            products = lowStockAlerts,
                            onProductClick = onProductClick,
                            isStockAlert = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertSection(
    title: String,
    description: String,
    color: Color,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    isStockAlert: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Section header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }

        // Product cards
        products.forEach { product ->
            AlertProductCard(
                product = product,
                color = color,
                onClick = { onProductClick(product) },
                isStockAlert = isStockAlert
            )
        }
    }
}

@Composable
fun AlertProductCard(
    product: Product,
    color: Color,
    onClick: () -> Unit,
    isStockAlert: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall
                )
                if (product.brand.isNotBlank()) {
                    Text(
                        text = product.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isStockAlert) {
                    Text(
                        text = "Quedan ${product.units} unidad(es)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                } else {
                    val days = DateUtils.daysBetween(product.expirationDate)
                    Text(
                        text = when {
                            days == 0 -> "¡Caduca HOY!"
                            days == 1 -> "Caduca mañana"
                            else -> "Caduca en $days días"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
            }

            // Icon indicator
            Icon(
                imageVector = if (isStockAlert) Icons.Default.Inventory else Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
