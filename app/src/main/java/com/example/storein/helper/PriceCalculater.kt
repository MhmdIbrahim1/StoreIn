package com.example.storein.helper

fun Float?.getProductPrice(price: Float): Float{
    //this --> Percentage
    if (this == null)
        return price
    val remainingPricePercentage = 1f - this
    val priceAfterOffer = remainingPricePercentage * price

    return priceAfterOffer
}

fun Float?.formatPrice(): String{
    if (this == null)
        return "0.0"
    return String.format("%.2f", this)
}