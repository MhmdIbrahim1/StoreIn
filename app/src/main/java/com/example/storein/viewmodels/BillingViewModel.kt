package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.data.Address
import com.example.storein.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _address =
        MutableStateFlow<NetworkResult<List<Address>>>(NetworkResult.UnSpecified())
    val address = _address.asStateFlow()

    init {
        getUserAddress()
    }


    fun getUserAddress() {
        viewModelScope.launch { _address.emit(NetworkResult.Loading()) }

        firestore.collection("users").document(auth.uid!!).collection("address")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _address.emit(NetworkResult.Error(error.message.toString())) }
                    return@addSnapshotListener
                }
                val addressList = value?.toObjects(Address::class.java)
                viewModelScope.launch { _address.emit(NetworkResult.Success(addressList!!)) }
            }

    }

}