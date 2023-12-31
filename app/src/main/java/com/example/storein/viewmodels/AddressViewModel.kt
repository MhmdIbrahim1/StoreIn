package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.storein.data.Address
import com.example.storein.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _address = MutableStateFlow<NetworkResult<Address>>(NetworkResult.UnSpecified())
    val address = _address.asStateFlow()

    private val _deleteStatus = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.UnSpecified())
    val deleteStatus = _deleteStatus.asStateFlow()



    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun addAddress(address: Address) {
        val validateInputs = validateInputs(address)
        if (validateInputs) {
            viewModelScope.launch { _address.emit(NetworkResult.Loading()) }
            val newDocRef = firestore.collection("users").document(auth.uid!!).collection("address").document()
               newDocRef.set(address.copy(documentId = newDocRef.id))
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _address.emit(NetworkResult.Success(address))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _address.emit(NetworkResult.Error(it.message.toString()))
                    }
                }

        } else {
            viewModelScope.launch {
                _error.emit("Please fill all the fields")
            }
        }
    }

    fun updateAddress(address: Address) {
        val validateInputs = validateInputs(address)
        if (validateInputs) {
            viewModelScope.launch { _address.emit(NetworkResult.Loading()) }
            firestore.collection("users").document(auth.uid!!)
                .collection("address").document(address.documentId)
                .set(address, SetOptions.merge()) /// use SetOptions.merge() to update the document
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _address.emit(NetworkResult.Success(address))
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _address.emit(NetworkResult.Error(it.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("Please fill all the fields")
            }
        }
    }

    fun deleteAddress(address: Address) {
        viewModelScope.launch { _deleteStatus.emit(NetworkResult.Loading()) }
        firestore.collection("users").document(auth.uid!!)
            .collection("address").document(address.documentId)
            .delete()
            .addOnSuccessListener {
                viewModelScope.launch {
                    _deleteStatus.emit(NetworkResult.Success(Unit))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _deleteStatus.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }


    private fun validateInputs(address: Address): Boolean {
        return address.title.trim().isNotEmpty() &&
                address.fullName.trim().isNotEmpty() &&
                address.phone.trim().isNotEmpty() &&
                address.city.trim().isNotEmpty() &&
                address.state.trim().isNotEmpty() &&
                address.street.trim().isNotEmpty()

    }
}

