package com.example.chiplocator.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chiplocator.databinding.ItemProductBinding
import com.example.chiplocator.domain.model.Product

class ProductGridAdapter : ListAdapter<Product, ProductGridAdapter.ProductViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "%.0f ₽".format(product.price)
            binding.badgeNew.visibility =
                if (product.isNew) android.view.View.VISIBLE else android.view.View.GONE
            binding.badgePromo.visibility =
                if (product.isPromo) android.view.View.VISIBLE else android.view.View.GONE

            if (product.imageUrl.isNotBlank()) {
                Glide.with(binding.root)
                    .load(product.imageUrl)
                    .centerCrop()
                    .into(binding.ivProduct)
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}