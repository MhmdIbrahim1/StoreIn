package com.example.storein.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    fun isUserLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null
    }

    fun saveUserLoggedInState() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("USER_LOGGED_IN", true)
        editor.apply()
    }

    fun hasUserFinishedIntroduction(): Boolean {
        val hasFinished = sharedPreferences.getBoolean("USER_LOGGED_IN", false)
        return hasFinished
    }
}