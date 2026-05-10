package com.example.chiplocator.ui.list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.R
import com.example.chiplocator.databinding.FragmentListBinding
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListViewModel by viewModels {
        ListViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private lateinit var adapter: ShopAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShopAdapter { shop ->
            val bundle = Bundle().apply { putString("shopId", shop.id) }
            findNavController().navigate(R.id.shopDetailFragment, bundle)
        }
        binding.recyclerView.adapter = adapter

        tryGetLocation()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shops.collectLatest { adapter.submitList(it) }
        }
    }

    private fun tryGetLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(requireActivity())
                .lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let { viewModel.setUserLocation(it.latitude, it.longitude) }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}