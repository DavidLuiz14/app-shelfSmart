package com.example.appshelfsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.data.dao.ProductDao
import com.example.appshelfsmart.utils.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val productDao: ProductDao) : ViewModel() {
    
    // Reactive data from database
    val inventoryItems: StateFlow<List<Product>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    // Statistics
    val totalProductsCount: Int
        get() = inventoryItems.value.sumOf { it.units }
    
    fun getExpiringSoonProducts(days: Int = 7): List<Product> {
        return inventoryItems.value.filter { product ->
            DateUtils.isExpiringSoon(product.expirationDate, days)
        }
    }
    
    fun getLowStockProducts(threshold: Int = 2): List<Product> {
        return inventoryItems.value
            .groupBy { it.barcode.ifBlank { "${it.name}|${it.brand}" } }
            .mapNotNull { (_, group) ->
                val totalUnits = group.sumOf { it.units }
                if (totalUnits <= threshold) {
                    val representative = group.maxByOrNull { it.purchaseDate }
                    representative?.copy(units = totalUnits)
                } else {
                    null
                }
            }
    }
    
    // Search functionality
    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return inventoryItems.value
        val lowerQuery = query.lowercase()
        return inventoryItems.value.filter { product ->
            product.name.lowercase().contains(lowerQuery) ||
            product.brand.lowercase().contains(lowerQuery) ||
            product.category.lowercase().contains(lowerQuery)
        }
    }
    
    // Filter by expiration status
    fun filterByExpirationStatus(status: String): List<Product> {
        return when (status) {
            "All" -> inventoryItems.value
            "Expiring Soon" -> inventoryItems.value.filter { 
                DateUtils.isExpiringSoon(it.expirationDate, 7) && !DateUtils.isExpired(it.expirationDate)
            }
            "Expired" -> inventoryItems.value.filter { DateUtils.isExpired(it.expirationDate) }
            "Valid" -> inventoryItems.value.filter { 
                !DateUtils.isExpiringSoon(it.expirationDate, 7) && !DateUtils.isExpired(it.expirationDate)
            }
            else -> inventoryItems.value
        }
    }
    
    // Filter by category
    fun filterByCategory(category: String): List<Product> {
        if (category == "All") return inventoryItems.value
        return inventoryItems.value.filter { it.category == category }
    }
    
    // Sort functionality
    fun sortProducts(products: List<Product>, sortBy: String): List<Product> {
        return when (sortBy) {
            "Newest" -> products.sortedByDescending { it.purchaseDate }
            "Oldest" -> products.sortedBy { it.purchaseDate }
            "Expiring Soonest" -> products.sortedBy { DateUtils.parseDate(it.expirationDate)?.time ?: Long.MAX_VALUE }
            else -> products
        }
    }
    
    // Alert functions
    fun getUrgentExpirationAlerts(): List<Product> {
        return inventoryItems.value.filter { DateUtils.isExpiringToday(it.expirationDate) }
    }
    
    fun getWarningExpirationAlerts(): List<Product> {
        return inventoryItems.value.filter { DateUtils.isExpiringIn1to3Days(it.expirationDate) }
    }
    
    fun getCautionExpirationAlerts(): List<Product> {
        return inventoryItems.value.filter { DateUtils.isExpiringIn4to7Days(it.expirationDate) }
    }
    
    fun getTotalAlertsCount(): Int {
        val urgentCount = getUrgentExpirationAlerts().size
        val warningCount = getWarningExpirationAlerts().size
        val cautionCount = getCautionExpirationAlerts().size
        val lowStockCount = getLowStockProducts().size
        return urgentCount + warningCount + cautionCount + lowStockCount
    }
    
    // CRUD operations
    fun addProduct(product: Product) {
        viewModelScope.launch {
            productDao.insertProduct(product)
        }
    }
    
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productDao.updateProduct(product)
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.deleteProduct(product)
        }
    }
    
    fun markAsConsumed(product: Product) {
        viewModelScope.launch {
            if (product.units > 1) {
                // Decrease units
                val updatedProduct = product.copy(units = product.units - 1)
                productDao.updateProduct(updatedProduct)
            } else {
                // Remove product
                productDao.deleteProduct(product)
            }
        }
    }
    
    fun markAsWasted(product: Product) {
        viewModelScope.launch {
            if (product.units > 1) {
                // Decrease units
                val updatedProduct = product.copy(units = product.units - 1)
                productDao.updateProduct(updatedProduct)
            } else {
                // Remove product
                productDao.deleteProduct(product)
            }
        }
    }
    
    fun getProductById(id: String): Product? {
        return inventoryItems.value.find { it.id == id }
    }

    fun getProductByBarcode(barcode: String): Product? {
        return inventoryItems.value
            .filter { it.barcode == barcode }
            .maxByOrNull { it.purchaseDate } // Get the most recent one to likely have the latest photo info
    }
}
