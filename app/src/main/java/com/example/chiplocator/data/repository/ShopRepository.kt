package com.example.chiplocator.data.repository

import com.example.chiplocator.data.local.dao.ShopDao
import com.example.chiplocator.data.local.entity.ShopEntity
import com.example.chiplocator.data.remote.FirebaseDataSource
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShopRepository(
    private val shopDao: ShopDao,
    private val remoteDataSource: FirebaseDataSource
) {

    fun getAllShops(): Flow<List<Shop>> =
        shopDao.getAllShops().map { list -> list.map { it.toDomain() } }

    fun getShopsWithPromo(): Flow<List<Shop>> =
        shopDao.getShopsWithPromo().map { list -> list.map { it.toDomain() } }

    fun getShopsWithNewProducts(): Flow<List<Shop>> =
        shopDao.getShopsWithNewProducts().map { list -> list.map { it.toDomain() } }

    suspend fun getShopById(shopId: String): Shop? =
        shopDao.getShopById(shopId)?.toDomain()

    // Загрузить с сервера и сохранить в кэш
    suspend fun syncShops() {
        val remote = remoteDataSource.getShops()
        if (remote.isNotEmpty()) {
            val entities = remote.map { ShopEntity.fromDomain(it.toDomain()) }
            shopDao.deleteAll()
            shopDao.upsertAll(entities)
        }
    }
}