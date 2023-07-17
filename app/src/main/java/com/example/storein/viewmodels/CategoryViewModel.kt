package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.data.Category
import com.example.storein.data.Product
import com.example.storein.utils.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel constructor(
    private val firestore: FirebaseFirestore,
    private val category: Category
) : ViewModel() {

    private val _offerProducts =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val offerProducts = _offerProducts.asStateFlow()

    private val _bestProducts =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val bestProducts = _bestProducts.asStateFlow()


    init {
        fetchOfferProducts()
        fetchBestProducts()
    }


    fun fetchOfferProducts() {
        viewModelScope.launch {
            _offerProducts.emit(NetworkResult.Loading())
        }
        firestore.collection("products")
            .whereEqualTo("category", category.category)
            .whereNotEqualTo("offerPercentage", null)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.toObjects(Product::class.java)
                viewModelScope.launch {
                    _offerProducts.emit(NetworkResult.Success(products))
                }
            }
            .addOnFailureListener { exception ->
                _offerProducts.value = NetworkResult.Error(exception.message.toString())
            }
    }

    fun fetchBestProducts() {
        viewModelScope.launch {
            _bestProducts.emit(NetworkResult.Loading())
        }
        firestore.collection("products")
            .whereEqualTo("category", category.category)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestProducts.emit(NetworkResult.Success(products))
                }
            }
            .addOnFailureListener { exception ->
                _bestProducts.value = NetworkResult.Error(exception.message.toString())
            }
    }
}