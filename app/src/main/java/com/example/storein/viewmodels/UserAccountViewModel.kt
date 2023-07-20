package com.example.storein.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.MyApplication
import com.example.storein.data.User
import com.example.storein.utils.NetworkResult
import com.example.storein.utils.RegisterValidation
import com.example.storein.utils.validateEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
) : AndroidViewModel(app) {


    private val _user = MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val user = _user.asStateFlow()

    private val _updateInfo = MutableStateFlow<NetworkResult<User>>(NetworkResult.UnSpecified())
    val updateInfo = _updateInfo.asStateFlow()


    init {
        getUser()
    }
    fun getUser() {
        viewModelScope.launch {
            _user.emit(NetworkResult.Loading())
        }

        firestore.collection("users").document(auth.uid!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    // Log the User object received from Firestore
                    Log.d("UserAccountViewModel", "User from Firestore: $it")
                    if (it.firstName != null && it.lastName != null && it.email != null) {
                        viewModelScope.launch {
                            _user.emit(NetworkResult.Success(it))
                        }
                    } else {
                        viewModelScope.launch {
                            _user.emit(NetworkResult.Error("User data is incomplete"))
                        }
                    }
                }
            }.addOnFailureListener {
                viewModelScope.launch {
                    _user.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun updateUser(user: User, imageUri: Uri?) {
        // Log the User object before updating
        Log.d("UserAccountViewModel", "User before updating: $user")
        val areInputsValid = user.email?.let { validateEmail(it) } is RegisterValidation.Success
                && user.firstName?.trim()?.isNotEmpty() ?: false
                && user.lastName?.trim()?.isNotEmpty()?: false

        if (!areInputsValid) {
            viewModelScope.launch {
                _user.emit(NetworkResult.Error("Check your inputs"))
            }
            return
        }

        viewModelScope.launch {
            _updateInfo.emit(NetworkResult.Loading())
        }

        if (imageUri == null) {
            saveUserInformation(user, true)
        } else {
            saveUserInformationWithNewImage(user, imageUri)
        }

    }

    private fun saveUserInformationWithNewImage(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<MyApplication>().contentResolver,
                    imageUri
                )
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory =
                    storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")
                val result = imageDirectory.putBytes(imageByteArray).await()
                val imageUrl = result.storage.downloadUrl.await().toString()
                saveUserInformation(user.copy(imagePath = imageUrl), false)
            } catch (e: Exception) {
                viewModelScope.launch {
                    _user.emit(NetworkResult.Error(e.message.toString()))
                }
            }
        }
    }

    private fun saveUserInformation(user: User, shouldRetrievedOldImage: Boolean) {
        firestore.runTransaction { transaction ->
            val documentRef = firestore.collection("users").document(auth.uid!!)
            if (shouldRetrievedOldImage) {
                val currentUser = transaction.get(documentRef).toObject(User::class.java)
                // Log the User object retrieved in the transaction
                Log.d("UserAccountViewModel", "User in transaction: $currentUser")
                val newUser = user.copy(imagePath = currentUser?.imagePath ?: "")
                transaction.set(documentRef, newUser)
            } else {
                transaction.set(documentRef, user)
            }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Success(user))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Error(it.message.toString()))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _updateInfo.emit(NetworkResult.Loading())
        }

        if (validateEmail(email) is RegisterValidation.Failed) {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Error("Invalid email"))
            }
            return
        }

        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Success(User()))
            }
        }.addOnFailureListener {
            viewModelScope.launch {
                _updateInfo.emit(NetworkResult.Error(it.message.toString()))
            }
        }
    }
}