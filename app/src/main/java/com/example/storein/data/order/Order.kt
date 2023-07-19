package com.example.storein.data.order

import com.example.storein.data.Address
import com.example.storein.data.CartProduct
import com.example.storein.data.Product

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    var adddress: Address,
 )