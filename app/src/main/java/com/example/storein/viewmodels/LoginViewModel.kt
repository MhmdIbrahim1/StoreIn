package com.example.storein.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.utils.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _login = MutableSharedFlow<NetworkResult<String>>()
    val login = _login.asSharedFlow()

    private val _resetPassword = MutableSharedFlow<NetworkResult<String>>()
    val resetPassword = _resetPassword.asSharedFlow()

    private val firestore = FirebaseFirestore.getInstance()


    fun login(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()) {
            viewModelScope.launch {
                _login.emit(NetworkResult.Error("Email or password cannot be empty"))
            }
            return
        }
        viewModelScope.launch { _login.emit(NetworkResult.Loading()) }
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.user?.let {
                        _login.emit(NetworkResult.Success("Login Success"))
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch { _resetPassword.emit(NetworkResult.Loading()) }

        firebaseAuth
            .sendPasswordResetEmail(email)
            .addOnSuccessListener {
                viewModelScope.launch {
                    _resetPassword.emit(NetworkResult.Success("Reset Password Success"))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _resetPassword.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                viewModelScope.launch {
                    authResult.user?.let { user ->
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "displayName" to user.displayName,
                            "photoUrl" to user.photoUrl.toString()
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _login.emit(NetworkResult.Success("Login Success"))
                                }
                            }
                            .addOnFailureListener {
                                viewModelScope.launch {
                                    _login.emit(NetworkResult.Error("Failed to add user data"))
                                }
                            }
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _login.emit(NetworkResult.Error(it.message.toString()))
                }
            }
    }

}