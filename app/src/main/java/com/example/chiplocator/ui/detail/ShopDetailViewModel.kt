package com.example.chiplocator.ui.detail

import androidx.lifecycle.*
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Product
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopDetailViewModel(
    private val shopRepository: ShopRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _shop = MutableLiveData<Shop?>()
    val shop: LiveData<Shop?> = _shop

    private val _shopId = MutableStateFlow("")

    val products: StateFlow<List<Product>> = _shopId
        .filter { it.isNotBlank() }
        .flatMapLatest { shopId ->
            productRepository.getProductsForShop(shopId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadShop(shopId: String) {
        _shopId.value = shopId
        viewModelScope.launch {
            _shop.value = shopRepository.getShopById(shopId)
        }
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShopDetailViewModel(app.shopRepository, app.productRepository) as T
        }
    }
}