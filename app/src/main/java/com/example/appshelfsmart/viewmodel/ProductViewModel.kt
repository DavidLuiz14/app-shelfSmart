package com.example.appshelfsmart.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.appshelfsmart.data.Product

    class ProductViewModel : ViewModel() {
        private val _inventoryItems = mutableStateListOf<Product>()
        val inventoryItems: List<Product> get() = _inventoryItems

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

