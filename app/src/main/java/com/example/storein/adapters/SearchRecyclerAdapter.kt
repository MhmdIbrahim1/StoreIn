package com.example.storein.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storein.R
import com.example.storein.data.Product
import com.example.storein.databinding.RecyclerviewSearchItemBinding

class SearchRecyclerAdapter : RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewHolder>() {
    var onItemClick: ((Product) -> Unit)? = null


    inner class SearchViewHolder(val binding: RecyclerviewSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind(product: Product) {
            binding.apply {
                imgProduct.load(product.images.getOrNull(0)) {
                    crossfade(600)
                    error(R.drawable.error_placeholder)
                }
                product.offerPercentage?.let {
                    val remainingPercentage = 1f - it
                    val priceAfterDiscount = product.price * remainingPercentage
                    tvNewPrice.text = "E£ ${String.format("%.2f", priceAfterDiscount)}"
                    tvPrice.paintFlags = tvNewPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                if (product.offerPercentage == null){
                    tvNewPrice.visibility = View.INVISIBLE
                }
                tvPrice.text = "E£ ${product.price}"
                tvName.text = product.name
            }

        }
        }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            RecyclerviewSearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null
}