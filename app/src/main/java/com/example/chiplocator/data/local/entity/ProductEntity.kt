package com.example.chiplocator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chiplocator.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val isNew: Boolean,
    val isPromo: Boolean,
    val description: String,
    // храним как строку через запятую
    val availableInShops: String
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        category = category,
        price = price,
        imageUrl = imageUrl,
        isNew = isNew,
        isPromo = isPromo,
        description = description,
        availableInShops = if (availableInShops.isBlank()) emptyList()
        else availableInShops.split(",")
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            name = product.name,
            category = product.category,
            price = product.price,
            imageUrl = product.imageUrl,
            isNew = product.isNew,
            isPromo = product.isPromo,
            description = product.description,
            availableInShops = product.availableInShops.joinToString(",")
        )
    }
}