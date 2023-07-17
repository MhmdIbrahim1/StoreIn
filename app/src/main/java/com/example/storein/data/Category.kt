package com.example.storein.data

sealed class Category(val category: String) {

    object Shoes : Category("Shoes")
    object Chair : Category("Chair")
    object Cupboard : Category("Cupboard")
    object Table : Category("Table")
    object Accessories : Category("Accessory")
    object Furniture : Category("Furniture")
}