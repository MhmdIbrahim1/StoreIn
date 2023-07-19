package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.data.order.Order
import com.example.storein.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
):ViewModel() {

    private var _orders = MutableStateFlow<NetworkResult<Order>>(NetworkResult.UnSpecified())
    val orders = _orders.asStateFlow()

    fun placeOrder(order: Order) {
        viewModelScope.launch { _orders.emit(NetworkResult.Loading()) }

        firestore.runBatch { batch ->
            //TODO: Add order to users collection
            //TODO: Add order into orders collection
            //TODO: Delete the products from user cart collection

            firestore.collection("users")
                .document(auth.uid!!).collection("orders")
                .document().set(order)

            firestore.collection("orders")
                .document().set(order)

            firestore.collection("users")
                .document(auth.uid!!).collection("cart")
                .get().addOnSuccessListener {
                    it.documents.forEach {
                        it.reference.delete()
                    }
                }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _orders.emit(NetworkResult.Success(order))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _orders.emit(NetworkResult.Error(it.message.toString()))
            }
        }
    }
}