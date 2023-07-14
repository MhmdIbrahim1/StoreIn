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

    private val pagingInfo = PagingInfo()

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
            .whereEqualTo("category", "Special Products")
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
            .whereEqualTo("category", "Best Deals")
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
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _bestProducts.emit(NetworkResult.Loading())
            }
            //we can use query to get the data like this
            //firebaseFirestore.collection("products").whereEqualTo("category", "Chair").orderBy("id")
            firebaseFirestore.collection("products")
                .limit(pagingInfo.bestProductPage * 10)
                .get()
                .addOnSuccessListener { result ->
                    val bestProductsList = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = bestProductsList == pagingInfo.oldBestProduct
                    pagingInfo.oldBestProduct = bestProductsList
                    viewModelScope.launch {
                        _bestProducts.emit(NetworkResult.Success(bestProductsList))
                    }
                    pagingInfo.bestProductPage++
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _bestProducts.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        }
    }
}

internal data class PagingInfo(
    var bestProductPage: Long = 1,
    var oldBestProduct: List<Product> = emptyList(),
    var isPagingEnd: Boolean = false
)