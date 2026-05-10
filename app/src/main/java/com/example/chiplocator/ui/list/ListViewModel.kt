package com.example.chiplocator.ui.list

import android.location.Location
import androidx.lifecycle.*
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    val shops: StateFlow<List<Shop>> = shopRepository.getAllShops()
        .combine(_userLocation) { shops, location ->
            if (location == null) shops
            else shops.map { shop ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.first, location.second,
                    shop.latitude, shop.longitude,
                    results
                )
                shop.copy(distance = results[0])
            }.sortedBy { it.distance }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(app.shopRepository) as T
        }
    }
}