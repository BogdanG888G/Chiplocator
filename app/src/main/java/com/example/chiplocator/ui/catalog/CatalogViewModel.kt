package com.example.chiplocator.ui.catalog

import androidx.lifecycle.*
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.domain.model.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .combine(_searchQuery) { products, query ->
            if (query.isBlank()) products
            else products.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncProducts() {
        viewModelScope.launch {
            productRepository.syncProducts()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CatalogViewModel(app.productRepository) as T
        }
    }
}