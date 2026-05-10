package com.example.chiplocator.data.local.dao

import androidx.room.*
import com.example.chiplocator.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    @Query("SELECT * FROM shops")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE id = :shopId")
    suspend fun getShopById(shopId: String): ShopEntity?

    @Query("SELECT * FROM shops WHERE hasPromo = 1")
    fun getShopsWithPromo(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE hasNewProducts = 1")
    fun getShopsWithNewProducts(): Flow<List<ShopEntity>>

    @Upsert
    suspend fun upsertAll(shops: List<ShopEntity>)

    @Query("DELETE FROM shops")
    suspend fun deleteAll()
}