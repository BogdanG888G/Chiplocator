package com.example.chiplocator.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels {
        MapViewModel.Factory(requireActivity().application as ChipLocatorApp)
    }

    private lateinit var mapView: MapView
    private val placemarks = mutableMapOf<String, PlacemarkMapObject>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            centerOnMyLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Важно: инициализировать MapKit до создания MapView
        MapKitFactory.initialize(requireContext())
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

        mapView = binding.mapView

        mapView.map.move(
            CameraPosition(Point(55.751244, 37.618423), 11.0f, 0.0f, 0.0f)
        )

        binding.fabMyLocation.setOnClickListener { checkLocationPermission() }
        binding.fabFilter.setOnClickListener { showFilterBottomSheet() }

        // Если пришёл фильтр по товару из каталога
        val productId = arguments?.getString("filterProductId")
        if (!productId.isNullOrBlank()) {
            viewModel.setProductFilter(productId)
        }

        viewModel.syncData()
        observeShops()
    }

    private fun observeShops() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shops.collectLatest { shops -> updateMarkers(shops) }
        }
    }

    private fun updateMarkers(shops: List<Shop>) {
        // Очистка старых маркеров
        placemarks.values.forEach { mapView.map.mapObjects.remove(it) }
        placemarks.clear()

        shops.forEach { shop ->
            val color = when {
                shop.hasPromo -> Color.RED
                shop.hasNewProducts -> Color.BLUE
                else -> Color.rgb(0, 150, 0)
            }

            val placemark = mapView.map.mapObjects.addPlacemark(
                Point(shop.latitude, shop.longitude),
                ImageProvider.fromBitmap(createMarkerBitmap(color))
            )

            placemark.userData = shop
            placemark.addTapListener { mapObject, _ ->
                val tappedShop = mapObject.userData as? Shop
                if (tappedShop != null) {
                    val bundle = Bundle().apply { putString("shopId", tappedShop.id) }
                    findNavController().navigate(R.id.shopDetailFragment, bundle)
                }
                true
            }

            placemarks[shop.id] = placemark
        }
    }

    /**
     * Простой круглый маркер заданного цвета.
     */
    private fun createMarkerBitmap(color: Int): android.graphics.Bitmap {
        val size = 60
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = color
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

        // Белая обводка
        paint.color = Color.WHITE
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

        return bitmap
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
            centerOnMyLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun centerOnMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                mapView.map.move(
                    CameraPosition(Point(it.latitude, it.longitude), 14.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}