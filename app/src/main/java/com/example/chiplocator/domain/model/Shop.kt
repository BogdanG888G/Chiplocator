package com.example.chiplocator.domain.model

data class Shop(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",
    val workingHours: String = "",
    val hasPromo: Boolean = false,
    val hasNewProducts: Boolean = false,
    val photoUrl: String = "",
    val distance: Float = 0f  // вычисляется на клиенте
)