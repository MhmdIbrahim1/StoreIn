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

    init {
        fetchSpecialProducts()
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


}