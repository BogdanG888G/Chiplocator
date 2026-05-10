package com.example.chiplocator.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.chiplocator.ChipLocatorApp
import com.example.chiplocator.R
import com.example.chiplocator.databinding.FragmentMapBinding
import com.example.chiplocator.domain.model.Shop
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels {
        MapViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<String, Marker>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            enableMyLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fabMyLocation.setOnClickListener { centerOnMyLocation() }
        binding.fabFilter.setOnClickListener { showFilterBottomSheet() }

        viewModel.syncData()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        checkLocationPermission()
        observeShops()

        map.setOnInfoWindowClickListener { marker ->
            val shopId = marker.tag as? String ?: return@setOnInfoWindowClickListener
            val bundle = Bundle().apply { putString("shopId", shopId) }
            findNavController().navigate(R.id.shopDetailFragment, bundle)
        }
    }

    private fun observeShops() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shops.collectLatest { shops -> updateMarkers(shops) }
        }
    }

    private fun updateMarkers(shops: List<Shop>) {
        markers.values.forEach { it.remove() }
        markers.clear()
        shops.forEach { shop ->
            val icon = when {
                shop.hasPromo -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                shop.hasNewProducts -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            }
            val marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(shop.latitude, shop.longitude))
                    .title(shop.name)
                    .snippet(shop.address)
                    .icon(icon)
            )
            marker?.tag = shop.id
            if (marker != null) markers[shop.id] = marker
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        dialog.setContentView(sheet)

        sheet.findViewById<Chip>(R.id.chipAll).setOnClickListener {
            viewModel.setFilter(ShopFilter.ALL); dialog.dismiss()
        }
        sheet.findViewById<Chip>(R.id.chipPromo).setOnClickListener {
            viewModel.setFilter(ShopFilter.PROMO); dialog.dismiss()
        }
        sheet.findViewById<Chip>(R.id.chipNew).setOnClickListener {
            viewModel.setFilter(ShopFilter.NEW_PRODUCTS); dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            centerOnMyLocation()
        }
    }

    private fun centerOnMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}