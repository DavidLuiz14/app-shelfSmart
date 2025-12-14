package com.example.appshelfsmart.data.api

import com.example.appshelfsmart.data.MealDetailResponse
import com.example.appshelfsmart.data.MealListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDBApi {
    
    @GET("filter.php")
    suspend fun searchByIngredient(
        @Query("i") ingredient: String
    ): MealListResponse
    
    @GET("lookup.php")
    suspend fun getRecipeDetails(
        @Query("i") id: String
    ): MealDetailResponse
    
    companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}
