package com.example.chiplocator

import android.app.Application
import com.example.chiplocator.data.local.AppDatabase
import com.example.chiplocator.data.remote.FirebaseDataSource
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.data.repository.ShopRepository

class ChipLocatorApp : Application() {

    // Простой DI вручную (без Hilt/Koin)
    val database by lazy { AppDatabase.getInstance(this) }
    val firebaseDataSource by lazy { FirebaseDataSource() }

    val shopRepository by lazy {
        ShopRepository(database.shopDao(), firebaseDataSource)
    }
    val productRepository by lazy {
        ProductRepository(database.productDao(), firebaseDataSource)
    }
}