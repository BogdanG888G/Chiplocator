package com.example.chiplocator.data.remote.dto

import com.example.chiplocator.domain.model.Product

data class ProductDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val isNew: Boolean = false,
    val isPromo: Boolean = false,
    val description: String = "",
    val availableInShops: List<String> = emptyList()
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
        availableInShops = availableInShops
    )
}