package com.example.appshelfsmart.data.dao

import androidx.room.*
import com.example.appshelfsmart.data.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products ORDER BY purchaseDate DESC")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): Product?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteById(productId: String)
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
