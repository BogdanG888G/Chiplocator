package com.example.chiplocator.data.remote.dto

import com.example.chiplocator.domain.model.Shop

data class ShopDto(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",
    val workingHours: String = "",
    val hasPromo: Boolean = false,
    val hasNewProducts: Boolean = false,
    val photoUrl: String = ""
) {
    fun toDomain(): Shop = Shop(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        phone = phone,
        workingHours = workingHours,
        hasPromo = hasPromo,
        hasNewProducts = hasNewProducts,
        photoUrl = photoUrl
    )
}