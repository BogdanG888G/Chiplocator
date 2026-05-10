package com.example.chiplocator.ui.map

import androidx.lifecycle.*
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ShopFilter { ALL, PROMO, NEW_PRODUCTS }

class MapViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _filter = MutableStateFlow(ShopFilter.ALL)
    val filter: StateFlow<ShopFilter> = _filter.asStateFlow()

    val shops: StateFlow<List<Shop>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                ShopFilter.ALL -> shopRepository.getAllShops()
                ShopFilter.PROMO -> shopRepository.getShopsWithPromo()
                ShopFilter.NEW_PRODUCTS -> shopRepository.getShopsWithNewProducts()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setFilter(filter: ShopFilter) {
        _filter.value = filter
    }

    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            shopRepository.syncShops()
            _isLoading.value = false
        }
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(app.shopRepository) as T
        }
    }
}