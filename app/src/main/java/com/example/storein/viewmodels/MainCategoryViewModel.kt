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

    private val pagingInfoBestProduct = PagingInfo()
    private val pagingInfoBestDeals = PagingInfo()
    private val pagingInfoSpecialProduct = PagingInfo()

    init {
        fetchSpecialProducts()
        fetchBestDeals()
        fetchBestProducts()
    }

    fun fetchSpecialProducts() {
        fetchProductsByCategory("Special Products", pagingInfoSpecialProduct, _specialProducts)
    }

    fun fetchBestDeals() {
        fetchProductsByCategory("Best Deals", pagingInfoBestDeals, _bestDeals)
    }

    fun fetchBestProducts() {
        fetchProductsByCategory(null, pagingInfoBestProduct, _bestProducts)
    }

    private fun fetchProductsByCategory(
        category: String?,
        pagingInfo: PagingInfo,
        resultFlow: MutableStateFlow<NetworkResult<List<Product>>>
    ) {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                resultFlow.emit(NetworkResult.Loading())
            }
            val query = if (category != null) {
                firebaseFirestore.collection("products")
                    .whereEqualTo("category", category)
            } else {
                firebaseFirestore.collection("products")
            }
            query.limit(pagingInfo.page * 10)
                .get()
                .addOnSuccessListener { result ->
                    val productList = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = productList == pagingInfo.oldData
                    pagingInfo.oldData = productList
                    viewModelScope.launch {
                        resultFlow.emit(NetworkResult.Success(productList))
                    }
                    pagingInfo.page++
                }
                .addOnFailureListener { exception ->
                    viewModelScope.launch {
                        resultFlow.emit(NetworkResult.Error(exception.message.toString()))
                    }
                }
        }
    }

    internal data class PagingInfo(
        var page: Long = 1,
        var oldData: List<Product> = emptyList(),
        var isPagingEnd: Boolean = false
    )
}
