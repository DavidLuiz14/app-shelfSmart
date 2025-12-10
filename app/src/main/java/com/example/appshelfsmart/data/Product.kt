package com.example.appshelfsmart.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String,
    val expirationDate: String,
    val brand: String = "",
    val manufacturer: String = "",
    val category: String = "",
    val units: Int = 1,
    val quantityValue: Double? = null,
    val quantityUnit: String? = null, // e.g., "ml", "g", "kg"
    val photoUri: String? = null,
    val purchaseDate: Long = System.currentTimeMillis(),
    val nutritionalInfoRaw: String? = null,
    val nutritionalInfoSimplified: String? = null
)
