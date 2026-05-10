package com.example.chiplocator.ui.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.R
import com.example.chiplocator.databinding.FragmentCatalogBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by viewModels {
        CatalogViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private lateinit var catalogAdapter: CatalogAdapter

    // Текущий список категорий, чтобы понимать, что лежит во вкладке по индексу
    private var currentCategories: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTabs()
        observeCategories()
        observeProducts()
    }

    private fun setupRecyclerView() {
        catalogAdapter = CatalogAdapter { product ->
            val shopsCount = product.availableInShops.size
            Toast.makeText(
                requireContext(),
                "Товар \"${product.name}\" есть в $shopsCount магазинах",
                Toast.LENGTH_SHORT
            ).show()

            val bundle = Bundle().apply {
                putString("filterProductId", product.id)
            }
            findNavController().navigate(R.id.mapFragment, bundle)
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = catalogAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (position == 0) {
                    // Первая вкладка — "Все"
                    viewModel.setCategory(null)
                } else {
                    val categoryIndex = position - 1
                    val category = currentCategories.getOrNull(categoryIndex)
                    viewModel.setCategory(category)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /**
     * Наполняем вкладки реальными категориями из товаров.
     */
    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                if (categories == currentCategories) return@collectLatest
                currentCategories = categories

                binding.tabLayout.removeAllTabs()
                binding.tabLayout.addTab(
                    binding.tabLayout.newTab().setText("Все")
                )
                categories.forEach { category ->
                    binding.tabLayout.addTab(
                        binding.tabLayout.newTab().setText(category)
                    )
                }
            }
        }
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { catalogAdapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}