package com.example.chiplocator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chiplocator.data.local.dao.ProductDao
import com.example.chiplocator.data.local.dao.ShopDao
import com.example.chiplocator.data.local.entity.ProductEntity
import com.example.chiplocator.data.local.entity.ShopEntity

@Database(
    entities = [ShopEntity::class, ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shopDao(): ShopDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chiplocator.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}