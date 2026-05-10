package com.example.chiplocator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chiplocator.domain.model.Shop

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val workingHours: String,
    val hasPromo: Boolean,
    val hasNewProducts: Boolean,
    val photoUrl: String
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

    companion object {
        fun fromDomain(shop: Shop): ShopEntity = ShopEntity(
            id = shop.id,
            name = shop.name,
            address = shop.address,
            latitude = shop.latitude,
            longitude = shop.longitude,
            phone = shop.phone,
            workingHours = shop.workingHours,
            hasPromo = shop.hasPromo,
            hasNewProducts = shop.hasNewProducts,
            photoUrl = shop.photoUrl
        )
    }
}