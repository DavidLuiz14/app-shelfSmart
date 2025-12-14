package com.example.appshelfsmart.data.repository

import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.data.api.OpenFoodFactsApi
import com.example.appshelfsmart.utils.CategoryMapper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductRepository {
    private val api: OpenFoodFactsApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenFoodFactsApi::class.java)
    }

    suspend fun getProductDetails(barcode: String): Product? {
        return try {
            val response = api.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                val p = response.product
                val name = p.productName ?: ""
                Product(
                    name = name,
                    barcode = barcode,
                    expirationDate = "", // API doesn't usually give expiration for specific item
                    brand = p.brands ?: "",
                    manufacturer = p.manufacturingPlaces ?: "",
                    category = CategoryMapper.mapCategory(p.categories, name),

                    quantityUnit = p.quantity?.replace(Regex("[0-9.]"), "")?.trim() ?: "",
                    quantityValue = p.quantity?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
