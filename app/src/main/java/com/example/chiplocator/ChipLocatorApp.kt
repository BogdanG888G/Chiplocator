package com.example.chiplocator

import android.app.Application
import android.util.Log
import com.example.chiplocator.data.local.AppDatabase
import com.example.chiplocator.data.remote.FirebaseDataSource
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.data.repository.ShopRepository
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChipLocatorApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val firebaseDataSource by lazy { FirebaseDataSource() }

    val shopRepository by lazy {
        ShopRepository(database.shopDao(), firebaseDataSource)
    }
    val productRepository by lazy {
        ProductRepository(database.productDao(), firebaseDataSource)
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 1. Инициализация Яндекс MapKit
        MapKitFactory.setApiKey(YANDEX_MAPKIT_API_KEY)

        // 2. Синхронизация данных при старте приложения
        appScope.launch {
            try {
                shopRepository.syncShops()
                productRepository.syncProducts()
                Log.d("ChipLocatorApp", "Sync completed successfully")
            } catch (e: Exception) {
                Log.e("ChipLocatorApp", "Sync failed", e)
            }
        }
    }

    companion object {
        const val YANDEX_MAPKIT_API_KEY = "7ae019f6-a31d-4a93-8f5c-76a04c844582"
    }
}