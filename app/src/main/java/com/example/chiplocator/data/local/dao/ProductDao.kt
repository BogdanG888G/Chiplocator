package com.example.chiplocator.data.local.dao

import androidx.room.*
import com.example.chiplocator.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE isNew = 1")
    fun getNewProducts(): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE ',' || availableInShops || ',' LIKE '%,' || :shopId || ',%'
    """)
    fun getProductsForShop(shopId: String): Flow<List<ProductEntity>>

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}