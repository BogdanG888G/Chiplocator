package com.example.chiplocator.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
                showRouteDialog(shop.latitude, shop.longitude, shop.name)
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

    /**
     * Показывает диалог с выбором способа маршрута.
     */
    private fun showRouteDialog(lat: Double, lon: Double, name: String) {
        val options = arrayOf("Пешком 🚶", "На машине 🚗", "Общественный транспорт 🚌")
        AlertDialog.Builder(requireContext())
            .setTitle("Как добираемся?")
            .setItems(options) { _, which ->
                val mode = when (which) {
                    0 -> RouteMode.PEDESTRIAN
                    1 -> RouteMode.AUTO
                    else -> RouteMode.TRANSIT
                }
                openRoute(lat, lon, name, mode)
            }
            .show()
    }

    /**
     * Открывает маршрут в Яндекс.Картах.
     * Сначала пытается открыть приложение, иначе — веб-версию.
     */
    private fun openRoute(lat: Double, lon: Double, name: String, mode: RouteMode) {
        val yandexMode = when (mode) {
            RouteMode.PEDESTRIAN -> "pd"
            RouteMode.AUTO -> "auto"
            RouteMode.TRANSIT -> "mt"
        }

        // 1. Пробуем приложение Яндекс.Карты
        val yandexAppUri = Uri.parse(
            "yandexmaps://maps.yandex.ru/?rtext=~$lat,$lon&rtt=$yandexMode"
        )
        val yandexAppIntent = Intent(Intent.ACTION_VIEW, yandexAppUri).apply {
            setPackage("ru.yandex.yandexmaps")
        }
        if (yandexAppIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(yandexAppIntent)
            return
        }

        // 2. Пробуем Яндекс Навигатор (для авто)
        if (mode == RouteMode.AUTO) {
            val naviUri = Uri.parse("yandexnavi://build_route_on_map?lat_to=$lat&lon_to=$lon")
            val naviIntent = Intent(Intent.ACTION_VIEW, naviUri).apply {
                setPackage("ru.yandex.yandexnavi")
            }
            if (naviIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(naviIntent)
                return
            }
        }

        // 3. Открываем Яндекс.Карты в браузере
        val webUri = Uri.parse(
            "https://yandex.ru/maps/?rtext=~$lat,$lon&rtt=$yandexMode"
        )
        try {
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (e: Exception) {
            val geoUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($name)")
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class RouteMode { PEDESTRIAN, AUTO, TRANSIT }
}