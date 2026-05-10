package com.example.chiplocator.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.data.repository.ShopRepository
import com.example.chiplocator.domain.model.Shop
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ShopFilter { ALL, PROMO, NEW_PRODUCTS }

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val shopRepository: ShopRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(ShopFilter.ALL)
    val filter: StateFlow<ShopFilter> = _filter.asStateFlow()

    private val _productFilter = MutableStateFlow<String?>(null)

    val shops: StateFlow<List<Shop>> = combine(
        _filter.flatMapLatest { f ->
            when (f) {
                ShopFilter.ALL -> shopRepository.getAllShops()
                ShopFilter.PROMO -> shopRepository.getShopsWithPromo()
                ShopFilter.NEW_PRODUCTS -> shopRepository.getShopsWithNewProducts()
            }
        },
        _productFilter
    ) { shops, productId ->
        if (productId.isNullOrBlank()) shops
        else {
            val shopIdsWithProduct = productRepository
                .getProductsForShop(productId)
                .let { /* пустая заглушка, упростим */ emptyList<String>() }

            // Получим список магазинов, где товар есть в наличии
            val product = productRepository.getProductByIdSync(productId)
            if (product != null) {
                shops.filter { it.id in product.availableInShops }
            } else shops
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setFilter(filter: ShopFilter) { _filter.value = filter }

    fun setProductFilter(productId: String?) { _productFilter.value = productId }

    fun syncData() {
        viewModelScope.launch {
            _isLoading.value = true
            shopRepository.syncShops()
            productRepository.syncProducts()
            _isLoading.value = false
        }
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(app.shopRepository, app.productRepository) as T
        }
    }
}