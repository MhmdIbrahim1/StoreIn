package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.data.Product
import com.example.storein.utils.NetworkResult
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : ViewModel() {

    private val _specialProducts =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val specialProducts = _specialProducts as StateFlow<NetworkResult<List<Product>>>

    private val _bestDeals =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val bestDeals = _bestDeals as StateFlow<NetworkResult<List<Product>>>

    private val _bestProducts =
        MutableStateFlow<NetworkResult<List<Product>>>(NetworkResult.UnSpecified())
    val bestProducts = _bestProducts as StateFlow<NetworkResult<List<Product>>>


    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
    }

    fun fetchSpecialProducts() {
        viewModelScope.launch {
            _specialProducts.emit(NetworkResult.Loading())
        }
        firebaseFirestore.collection("products")
            .whereEqualTo("category","Special Products")
            .get()
            .addOnSuccessListener { result ->
                val specialProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _specialProducts.emit(NetworkResult.Success(specialProductsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _specialProducts.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestDeals() {
        viewModelScope.launch {
            _bestDeals.emit(NetworkResult.Loading())
        }
        firebaseFirestore.collection("products")
            .whereEqualTo("category","Best Deals")
            .get()
            .addOnSuccessListener { result ->
                val bestDealsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestDeals.emit(NetworkResult.Success(bestDealsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _bestDeals.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun fetchBestProducts() {
        viewModelScope.launch {
            _bestProducts.emit(NetworkResult.Loading())
        }
        firebaseFirestore.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val bestProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestProducts.emit(NetworkResult.Success(bestProductsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _bestProducts.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }


}