package com.example.storein.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.storein.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        // Add log to check if the activity is created
        Log.d("LoginRegisterActivity", "onCreate() called")

    }


    override fun onBackPressed() {
        // Add log to check if back button press is detected
        Log.d("LoginRegisterActivity", "onBackPressed() called")

        // Rest of your code...
    }

}