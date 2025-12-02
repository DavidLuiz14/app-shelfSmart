package com.example.appshelfsmart.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse
}

data class OpenFoodFactsResponse(
    @SerializedName("product") val product: OpenFoodFactsProduct?,
    @SerializedName("status") val status: Int
)

data class OpenFoodFactsProduct(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("quantity") val quantity: String?, // Often contains weight/volume
    @SerializedName("origins") val origins: String?
)
