package com.example.chiplocator.ui.catalog

import androidx.lifecycle.*
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.data.repository.ProductRepository
import com.example.chiplocator.domain.model.Product
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null) // null = "Все"

    private val allProducts: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Список доступных категорий (для табов).
     */
    val categories: StateFlow<List<String>> = allProducts
        .map { products ->
            products.map { it.category }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products
            .filter { product ->
                category == null || product.category.equals(category, ignoreCase = true)
            }
            .filter { product ->
                query.isBlank() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncProducts()
    }

    fun syncProducts() {
        viewModelScope.launch {
            productRepository.syncProducts()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * @param category null — показать все категории, иначе — фильтровать по конкретной.
     */
    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    class Factory(private val app: ChipLocatorApp) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CatalogViewModel(app.productRepository) as T
        }
    }
}