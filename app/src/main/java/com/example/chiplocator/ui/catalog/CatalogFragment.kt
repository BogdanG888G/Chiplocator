package com.example.chiplocator.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.R
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
            // При нажатии "Где купить?" — переход на карту с фильтром по товару
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}