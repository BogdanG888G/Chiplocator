package com.example.chiplocator.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chiplocator.R
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

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(p: Product) {
            binding.tvProductName.text = p.name
            binding.tvProductPrice.text = "%.0f ₽".format(p.price)
            binding.badgeNew.visibility = if (p.isNew) View.VISIBLE else View.GONE
            binding.badgePromo.visibility = if (p.isPromo) View.VISIBLE else View.GONE

            Glide.with(binding.root)
                .load(p.imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(binding.ivProduct)

            // В ассортименте магазина кнопка "Где купить?" не нужна
            binding.btnWhereToBuy.visibility = View.GONE
        }
    }

    class Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
        override fun areContentsTheSame(old: Product, new: Product) = old == new
    }
}