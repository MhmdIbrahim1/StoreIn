package com.example.storein.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.storein.data.Category
import com.example.storein.databinding.FragmentBaseCategoryBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.CategoryViewModel
import com.example.storein.viewmodels.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AccessoryFragment : BaseCategoryFragment() {
    private var isFirstTime = true
    @Inject
    lateinit var firestore: FirebaseFirestore


    val viewMode by viewModels<CategoryViewModel> {
        BaseCategoryViewModelFactory(firestore, Category.Accessories)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            viewMode.offerProducts.collectLatest {
                when (it) {
                    is NetworkResult.Loading -> {
                        if (isFirstTime) {
                            showShimmer()
                            isFirstTime = false
                        }
                    }

                    is NetworkResult.Success -> {
                        offerAdapter.differ.submitList(it.data)
                        hideShimmer()
                    }

                    is NetworkResult.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                        hideShimmer()
                    }

                    else -> Unit

                }

            }
        }

        lifecycleScope.launchWhenCreated {
            viewMode.bestProducts.collectLatest {
                when (it) {
                    is NetworkResult.Loading -> {
                        if (isFirstTime) {
                            showShimmer()
                            isFirstTime = false
                        }
                    }

                    is NetworkResult.Success -> {
                        bestProductAdapter.differ.submitList(it.data)
                        hideShimmer()
                    }

                    is NetworkResult.Error -> {
                        Snackbar.make(requireView(), it.message.toString(), Snackbar.LENGTH_SHORT)
                            .show()
                        hideShimmer()
                    }

                    else -> Unit
                }

            }
        }

    }

    private fun showShimmer() {
        binding.shimmerSpecialProducts.startShimmer()
        binding.shimmerSpecialProducts.visibility = View.VISIBLE
        binding.rvBestProducts.visibility = View.GONE
        binding.rvOffer.visibility = View.GONE
        binding.tvBestProducts.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerSpecialProducts.stopShimmer()
        binding.shimmerSpecialProducts.visibility = View.GONE
        binding.rvBestProducts.visibility = View.VISIBLE
        binding.rvOffer.visibility = View.VISIBLE
        binding.tvBestProducts.visibility = View.VISIBLE
    }

}