package com.example.storein.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storein.data.CartProduct
import com.example.storein.data.Product
import com.example.storein.databinding.CartProductItemBinding
import com.example.storein.databinding.SpecialRvItemBinding
import com.example.storein.helper.getProductPrice

class CartProductAdapter :
    RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder>() {

    inner class CartProductViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartProduct: CartProduct) {
            binding.apply {
                imageCartProduct.load(cartProduct.product.images.getOrNull(0)) {
                    crossfade(600)
                    error(com.example.storein.R.drawable.error_placeholder)
                }
                tvProductCartName.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                tvProductCartPrice.text = "E£ ${String.format("%.2f", cartProduct.product.price)}"

                imageCartProductColor.setImageDrawable(
                    ColorDrawable(
                        cartProduct.selectedColor ?: Color.TRANSPARENT
                    )
                )
                tvCartProductSize.text = cartProduct.selectedSize
                    ?: "N/A".also { imageCartProductSize.setImageDrawable(ColorDrawable(Color.TRANSPARENT)) }
            }
        }
    }


    private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        return CartProductViewHolder(
            CartProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartProduct = differ.currentList[position]
        holder.bind(cartProduct)

        holder.itemView.setOnClickListener {
            onProductClick?.invoke(cartProduct)
        }
        holder.binding.imagePlus.setOnClickListener {
            onPlusClick?.invoke(cartProduct)
        }
        holder.binding.imageMinus.setOnClickListener {
            onMinusClick?.invoke(cartProduct)
        }
    }


    var onProductClick: ((CartProduct) -> Unit)? = null
    var onPlusClick: ((CartProduct) -> Unit)? = null
    var onMinusClick: ((CartProduct) -> Unit)? = null
}