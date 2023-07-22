package com.example.storein.activites

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.storein.R
import com.example.storein.databinding.FragmentProfileBinding
import com.example.storein.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {
    private lateinit var binding: FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the language setting from SharedPreferences
        val sharedPref = getSharedPreferences("Language", Context.MODE_PRIVATE)
        val language = sharedPref.getString("language", "en") // default English if no setting is stored
        // Set the language based on the stored setting
        LocaleHelper.setLocale(this, language!!)

        setContentView(R.layout.activity_login_register)

    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAffinity()"))
    override fun onBackPressed() {
        // This will close the app when back button is pressed from LoginRegisterActivity
        finishAffinity()
    }
}