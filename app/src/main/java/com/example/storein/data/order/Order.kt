package com.example.storein.data.order

import android.os.Parcelable
import com.example.storein.data.Address
import com.example.storein.data.CartProduct
import com.example.storein.data.Product
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random.Default.nextLong

@Parcelize
data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    var adddress: Address,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
    val orderId: Long = nextLong(0, 100_000_000_000_000_000) + totalPrice.toLong()

):Parcelable{
    constructor() : this("", 0f, emptyList(), Address())
}