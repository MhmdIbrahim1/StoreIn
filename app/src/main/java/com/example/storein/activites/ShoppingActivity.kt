package com.example.storein.activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.storein.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.storein.databinding.ActivityShoppingBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.CartViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {
    val binding by lazy { ActivityShoppingBinding.inflate(layoutInflater) }

    val viewModel by viewModels<CartViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // set up the navigation controller
        val navController = findNavController(R.id.navHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

        lifecycleScope.launchWhenStarted {
            viewModel.cartProduct.collectLatest {
                when (it) {
                    is NetworkResult.Success -> {
                        val count = it.data?.size ?: 0
                        val bottomNav = binding.bottomNavigation
                        bottomNav.getOrCreateBadge(R.id.cartFragment).apply {
                            number = count
                            backgroundColor = resources.getColor(R.color.g_blue )
                        }
                    }
                    else -> Unit
                }
            }
        }

    }
}