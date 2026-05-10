package com.example.chiplocator.ui.catalog

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.databinding.FragmentCatalogBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by viewModels {
        CatalogViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private lateinit var adapter: CatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CatalogAdapter { product ->
            // Переход на карту с фильтром по товару можно реализовать
            // через SharedViewModel или аргументы навигации
        }

        binding.rvCatalog.apply {
            adapter = this@CatalogFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        viewModel.syncProducts()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { products ->
                adapter.submitList(products)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}