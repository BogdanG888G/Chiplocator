package com.example.chiplocator.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.databinding.FragmentShopDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopDetailFragment : Fragment() {

    private var _binding: FragmentShopDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShopDetailViewModel by viewModels {
        ShopDetailViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private lateinit var productAdapter: ProductGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductGridAdapter()
        binding.rvProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val shopId = arguments?.getString("shopId").orEmpty()
        viewModel.loadShop(shopId)

        viewModel.shop.observe(viewLifecycleOwner) { shop ->
            shop ?: return@observe
            binding.tvShopName.text = shop.name
            binding.tvAddress.text = shop.address
            binding.tvWorkingHours.text = shop.workingHours
            binding.tvPhone.text = shop.phone

            binding.tvPhone.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}")))
            }
            binding.btnRoute.setOnClickListener {
                val uri = Uri.parse("geo:${shop.latitude},${shop.longitude}?q=${shop.latitude},${shop.longitude}(${shop.name})")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            binding.btnShare.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${shop.name}\n${shop.address}")
                }
                startActivity(Intent.createChooser(intent, "Поделиться"))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.products.collectLatest { productAdapter.submitList(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}