package com.example.chiplocator.data.remote

import com.example.chiplocator.data.remote.dto.ProductDto
import com.example.chiplocator.data.remote.dto.ShopDto

class FirebaseDataSource {

    suspend fun getShops(): List<ShopDto> = listOf(
        ShopDto(
            id = "shop_1",
            name = "Пятёрочка на Тверской",
            address = "Тверская, 10, Москва",
            latitude = 55.762046,
            longitude = 37.605685,
            phone = "+7 495 123-45-67",
            workingHours = "08:00 – 23:00",
            hasPromo = true,
            hasNewProducts = false,
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Pyaterochka_logo.svg/1280px-Pyaterochka_logo.svg.png"
        ),
        ShopDto(
            id = "shop_2",
            name = "Магнит на Арбате",
            address = "Старый Арбат, 25, Москва",
            latitude = 55.751999,
            longitude = 37.594545,
            phone = "+7 495 234-56-78",
            workingHours = "09:00 – 22:00",
            hasPromo = false,
            hasNewProducts = true,
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Magnit_logo.svg/1280px-Magnit_logo.svg.png"
        ),
        ShopDto(
            id = "shop_3",
            name = "Перекрёсток на Покровке",
            address = "Покровка, 17, Москва",
            latitude = 55.758995,
            longitude = 37.640438,
            phone = "+7 495 345-67-89",
            workingHours = "круглосуточно",
            hasPromo = true,
            hasNewProducts = true,
            photoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0a/Perekrestok_logo.svg/1280px-Perekrestok_logo.svg.png"
        )
    )

    suspend fun getProducts(): List<ProductDto> = listOf(
        ProductDto(
            id = "p1",
            name = "Lays Краб",
            category = "Чипсы",
            price = 89.0,
            imageUrl = "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=300",
            isNew = true,
            isPromo = false,
            description = "Хрустящие чипсы со вкусом краба",
            availableInShops = listOf("shop_2", "shop_3")
        ),
        ProductDto(
            id = "p2",
            name = "Lays Сметана и лук",
            category = "Чипсы",
            price = 79.0,
            imageUrl = "https://images.unsplash.com/photo-1613919113640-25732ec5e61f?w=300",
            isNew = false,
            isPromo = true,
            description = "Классика жанра",
            availableInShops = listOf("shop_1", "shop_3")
        ),
        ProductDto(
            id = "p3",
            name = "Cheetos Сыр",
            category = "Кукурузные снеки",
            price = 65.0,
            imageUrl = "https://images.unsplash.com/photo-1599629954294-14df9ec8bc03?w=300",
            isNew = false,
            isPromo = false,
            description = "Сырные кукурузные палочки",
            availableInShops = listOf("shop_1", "shop_2")
        ),
        ProductDto(
            id = "p4",
            name = "Pringles Original",
            category = "Чипсы",
            price = 159.0,
            imageUrl = "https://images.unsplash.com/photo-1621447504864-d8686e12698c?w=300",
            isNew = true,
            isPromo = true,
            description = "Чипсы в тубе",
            availableInShops = listOf("shop_1", "shop_2", "shop_3")
        )
    )
}