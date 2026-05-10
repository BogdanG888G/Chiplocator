package com.example.chiplocator.ui.list

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chiplocator.domain.model.Shop
import com.example.chiplocator.databinding.ItemShopBinding

class ShopAdapter(
    private val onShopClick: (Shop) -> Unit
) : ListAdapter<Shop, ShopAdapter.ShopViewHolder>(ShopDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ShopViewHolder(
        private val binding: ItemShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
            binding.tvShopName.text = shop.name
            binding.tvShopAddress.text = shop.address
            binding.tvDistance.text = if (shop.distance > 0)
                "%.1f км".format(shop.distance / 1000)
            else ""

            binding.badgePromo.visibility =
                if (shop.hasPromo) android.view.View.VISIBLE else android.view.View.GONE
            binding.badgeNew.visibility =
                if (shop.hasNewProducts) android.view.View.VISIBLE else android.view.View.GONE

            if (shop.photoUrl.isNotBlank()) {
                Glide.with(binding.root)
                    .load(shop.photoUrl)
                    .centerCrop()
                    .into(binding.ivShopPhoto)
            }

            binding.root.setOnClickListener { onShopClick(shop) }

            binding.btnRoute.setOnClickListener {
                val uri = Uri.parse("geo:${shop.latitude},${shop.longitude}?q=${shop.latitude},${shop.longitude}(${shop.name})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                binding.root.context.startActivity(intent)
            }
        }
    }

    class ShopDiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(old: Shop, new: Shop) = old.id == new.id
        override fun areContentsTheSame(old: Shop, new: Shop) = old == new
    }
}