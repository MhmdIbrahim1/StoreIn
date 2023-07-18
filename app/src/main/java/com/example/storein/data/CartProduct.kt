package com.example.storein.data

data class CartProduct(
    val product: Product,
    var quantity: Int,
    val selectedColor: Int? = null,
    val selectedSize: String? = null

){
    constructor(): this(Product(), 1, null, null)
}