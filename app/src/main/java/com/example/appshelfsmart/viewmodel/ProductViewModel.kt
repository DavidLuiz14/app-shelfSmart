package com.example.appshelfsmart.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.utils.DateUtils

    class ProductViewModel : ViewModel() {
        private val _inventoryItems = mutableStateListOf<Product>()
        val inventoryItems: List<Product> get() = _inventoryItems

        // Statistics
        val totalProductsCount: Int
            get() = _inventoryItems.sumOf { it.units }

        fun getExpiringSoonProducts(days: Int = 7): List<Product> {
            return _inventoryItems.filter { product ->
                DateUtils.isExpiringSoon(product.expirationDate, days)
            }
        }

        fun getLowStockProducts(threshold: Int = 2): List<Product> {
            return _inventoryItems.filter { it.units <= threshold }
        }

        // Search functionality
        fun searchProducts(query: String): List<Product> {
            if (query.isBlank()) return _inventoryItems
            val lowerQuery = query.lowercase()
            return _inventoryItems.filter { product ->
                product.name.lowercase().contains(lowerQuery) ||
                product.brand.lowercase().contains(lowerQuery) ||
                product.category.lowercase().contains(lowerQuery)
            }
        }

        // Filter by expiration status
        fun getExpiredProducts(): List<Product> {
            return _inventoryItems.filter { product ->
                DateUtils.isExpired(product.expirationDate)
            }
        }

        fun getValidProducts(days: Int = 7): List<Product> {
            return _inventoryItems.filter { product ->
                !DateUtils.isExpired(product.expirationDate) &&
                !DateUtils.isExpiringSoon(product.expirationDate, days)
            }
        }

        // Alert system functions
        fun getUrgentExpirationAlerts(): List<Product> {
            return _inventoryItems.filter { product ->
                DateUtils.isExpiringToday(product.expirationDate)
            }
        }

        fun getWarningExpirationAlerts(): List<Product> {
            return _inventoryItems.filter { product ->
                DateUtils.isExpiringIn1to3Days(product.expirationDate)
            }
        }

        fun getCautionExpirationAlerts(): List<Product> {
            return _inventoryItems.filter { product ->
                DateUtils.isExpiringIn4to7Days(product.expirationDate)
            }
        }

        fun getTotalAlertsCount(): Int {
            val expirationAlerts = getUrgentExpirationAlerts().size +
                    getWarningExpirationAlerts().size +
                    getCautionExpirationAlerts().size
            val lowStockAlerts = getLowStockProducts().size
            return expirationAlerts + lowStockAlerts
        }

        // Sort functions
        fun sortByPurchaseDate(products: List<Product>, ascending: Boolean = false): List<Product> {
            return if (ascending) {
                products.sortedBy { it.purchaseDate }
            } else {
                products.sortedByDescending { it.purchaseDate }
            }
        }

        fun sortByExpirationDate(products: List<Product>): List<Product> {
            return products.sortedBy { product ->
                DateUtils.parseDate(product.expirationDate)?.time ?: Long.MAX_VALUE
            }
        }

        // Consumption tracking
        fun markAsConsumed(product: Product) {
            if (product.units > 1) {
                val updatedProduct = product.copy(units = product.units - 1)
                updateProduct(updatedProduct)
            } else {
                removeProduct(product)
            }
        }

        fun markAsWasted(product: Product) {
            if (product.units > 1) {
                val updatedProduct = product.copy(units = product.units - 1)
                updateProduct(updatedProduct)
            } else {
                removeProduct(product)
            }
        }

        fun addProduct(product: Product) {
            // Check if product with same barcode and expiration date exists
            val existingProduct = _inventoryItems.find {
                it.barcode == product.barcode && it.expirationDate == product.expirationDate
            }

            if (existingProduct != null) {
                // Update units if exists
                val updatedProduct =
                    existingProduct.copy(units = existingProduct.units + product.units)
                updateProduct(updatedProduct)
            } else {
                _inventoryItems.add(product)
            }
        }

        fun removeProduct(product: Product) {
            _inventoryItems.remove(product)
        }

        fun updateProduct(product: Product) {
            val index = _inventoryItems.indexOfFirst { it.id == product.id }
            if (index != -1) {
                _inventoryItems[index] = product
            }
        }
    }

