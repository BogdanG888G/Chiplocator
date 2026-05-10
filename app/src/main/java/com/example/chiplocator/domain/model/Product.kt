package com.example.chiplocator.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val isNew: Boolean = false,
    val isPromo: Boolean = false,
    val description: String = "",
    // id магазинов, где товар есть в наличии
    val availableInShops: List<String> = emptyList()
)