package com.example.appshelfsmart.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appshelfsmart.data.Product
import com.example.appshelfsmart.data.dao.ProductDao

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shelf_smart_database"
                )
                .fallbackToDestructiveMigration() // For development, recreate DB on schema changes
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
