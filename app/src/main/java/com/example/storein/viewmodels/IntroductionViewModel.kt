package com.example.storein.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storein.R
import com.example.storein.utils.Constants.INTRODUCTION_VALUE
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _navigateState = MutableStateFlow(0)
    val navigateState: StateFlow<Int> = _navigateState

    companion object{
        const val SHOPPING_ACTIVITY = 23
        val ACCOUNT_OPTION_FRAGMENT get() = R.id.action_introductionFragment_to_accountOptionsFragment
    }

        init {
            val isButtonClicked = sharedPreferences.getBoolean(INTRODUCTION_VALUE, false)
            val user = firebaseAuth.currentUser

            if (user != null) {
                viewModelScope.launch {
                    _navigateState.emit(SHOPPING_ACTIVITY)
                }
            } else if (isButtonClicked) {
                viewModelScope.launch {
                    _navigateState.emit(ACCOUNT_OPTION_FRAGMENT)
                }
            } else {
                Unit
                }
            }


    fun startButtonClicked() {
        sharedPreferences.edit().putBoolean(INTRODUCTION_VALUE, true).apply()
    }

}

