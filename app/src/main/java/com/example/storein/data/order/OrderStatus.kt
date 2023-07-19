package com.example.storein.data.order

sealed class OrderStatus(
    val status: String
) {
    object Ordered : OrderStatus("Ordered")
    object Cancelled : OrderStatus("Cancelled")
    object Confirmed : OrderStatus("Confirmed")
    object Shipped : OrderStatus("Shipped")
    object Delivered : OrderStatus("Delivered")
    object Returned : OrderStatus("Returned")
}