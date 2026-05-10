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

class ProductGridAdapter : ListAdapter<Product, ProductGridAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
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
        }
    }

    class Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(o: Product, n: Product) = o.id == n.id
        override fun areContentsTheSame(o: Product, n: Product) = o == n
    }
}