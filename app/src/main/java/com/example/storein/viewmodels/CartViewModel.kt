package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.data.CartProduct
import com.example.storein.firebase.FirebaseCommon
import com.example.storein.helper.getProductPrice
import com.example.storein.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {
    private val _cartProducts =
        MutableStateFlow<NetworkResult<List<CartProduct>>>(NetworkResult.UnSpecified())
    val cartProduct = _cartProducts.asStateFlow()

    private val _deleteDialog = Channel <CartProduct>(Channel.CONFLATED)
    val deleteDialog = _deleteDialog.receiveAsFlow()

    private var cartProductDocumented = emptyList<DocumentSnapshot>()
    fun deleteCartProduct(cartProduct: CartProduct) {
        val index = _cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {

            val documentId = cartProductDocumented[index].id
            firestore.collection("users").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }

    val productsPrice = cartProduct.map {
        when (it) {
            is NetworkResult.Success -> calculateProductsPrice(it.data!!)
            else -> null
        }
    }

    private fun calculateProductsPrice(data: List<CartProduct>): Float {
        return data.sumByDouble { cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
    }


    init {
        getCartProducts()
    }


    private fun getCartProducts() {
        viewModelScope.launch {
            _cartProducts.emit(NetworkResult.Loading())
        }
        firestore.collection("users").document(auth.uid!!).collection("cart")
            // to update the cart when the cart collection is changed
            .addSnapshotListener { value, error ->
                if (error != null || value == null) {
                    viewModelScope.launch {
                        _cartProducts.emit(NetworkResult.Error(error?.message.toString()))
                    }
                } else {
                    cartProductDocumented = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch {
                        _cartProducts.emit(NetworkResult.Success(cartProducts))
                    }
                }

            }
    }


    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChanging: FirebaseCommon.QuantityChanging
    ) {

        val index = _cartProducts.value.data?.indexOf(cartProduct)

        /**
         * index could be equal to -1 if the fun [getCartProducts] delays which will also delay the result we expect to be inside the [_cartProducts]
         * and to prevent the app from crashing we check if the index is not null and not equal to -1
         */
        if (index != null && index != -1) {
            val documentId = cartProductDocumented[index].id
            when (quantityChanging) {
                FirebaseCommon.QuantityChanging.INCREASE -> {
                    viewModelScope.launch { _cartProducts.emit(NetworkResult.Loading()) }
                    increaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChanging.DECREASE -> {
                    if (cartProduct.quantity == 1) {
                        viewModelScope.launch {
                            _deleteDialog.send(cartProduct)
                        }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(NetworkResult.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }

    }

    private fun decreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId) { result, e ->
            if (e != null)
                viewModelScope.launch {
                    _cartProducts.emit(NetworkResult.Error(e.message.toString()))
                }
        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId) { result, e ->
            if (e != null)
                viewModelScope.launch {
                    _cartProducts.emit(NetworkResult.Error(e.message.toString()))
                }
        }
    }
}