package com.example.chiplocator.data.repository

import com.example.chiplocator.data.local.dao.ProductDao
import com.example.chiplocator.data.local.entity.ProductEntity
import com.example.chiplocator.data.remote.FirebaseDataSource
import com.example.chiplocator.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val productDao: ProductDao,
    private val remoteDataSource: FirebaseDataSource
) {

    fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getProductsByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getProductsForShop(shopId: String): Flow<List<Product>> =
        productDao.getProductsForShop(shopId).map { list -> list.map { it.toDomain() } }

    fun getNewProducts(): Flow<List<Product>> =
        productDao.getNewProducts().map { list -> list.map { it.toDomain() } }

    suspend fun getProductByIdSync(productId: String): Product? =
        productDao.getProductById(productId)?.toDomain()

    suspend fun syncProducts() {
        val remote = remoteDataSource.getProducts()
        if (remote.isNotEmpty()) {
            val entities = remote.map { ProductEntity.fromDomain(it.toDomain()) }
            productDao.deleteAll()
            productDao.upsertAll(entities)
        }
    }
}