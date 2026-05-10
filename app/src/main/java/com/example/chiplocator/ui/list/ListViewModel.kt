package com.example.chiplocator.ui.list

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    val shops: StateFlow<List<Shop>> = shopRepository.getAllShops()
        .combine(_userLocation) { shops, loc ->
            if (loc == null) shops
            else shops.map { shop ->
                val r = FloatArray(1)
                Location.distanceBetween(loc.first, loc.second, shop.latitude, shop.longitude, r)
                shop.copy(distance = r[0])
            }.sortedBy { it.distance }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            shopRepository.syncShops()
        }
    }

    fun setUserLocation(lat: Double, lng: Double) { _userLocation.value = Pair(lat, lng) }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ListViewModel(app.shopRepository) as T
        }
    }
}