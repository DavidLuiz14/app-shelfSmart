package com.example.appshelfsmart.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.appshelfsmart.data.Product

class ProductViewModel : ViewModel() {
    private val _inventoryItems = mutableStateListOf<Product>()
    val inventoryItems: List<Product> get() = _inventoryItems

    fun addProduct(product: Product) {
        _inventoryItems.add(product)
    }

    fun removeProduct(product: Product) {
        _inventoryItems.remove(product)
    }

    fun updateProduct(updatedProduct: Product) {
        val index = _inventoryItems.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            _inventoryItems[index] = updatedProduct
        }
    }
}
