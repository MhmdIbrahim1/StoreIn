package com.example.storein.fragments.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storein.R
import com.example.storein.adapters.BestDealsAdapter
import com.example.storein.adapters.BestProductAdapter
import com.example.storein.adapters.SpecialProductAdapter
import com.example.storein.databinding.FragmentMainCategoryBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var specialProductAdapter: SpecialProductAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var bestProductAdapter: BestProductAdapter


    private lateinit var binding: FragmentMainCategoryBinding

    private val viewModel by viewModels<MainCategoryViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSpecialProductRv()
        setUpBestDealsRv()
        setUpBestProductRv()


        lifecycleScope.launchWhenCreated {
            viewModel.specialProducts.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        showDialog()
                    }

                    is NetworkResult.Success -> {
                        specialProductAdapter.differ.submitList(it.data)
                        hideLoadingDialog()
                    }

                    is NetworkResult.Error -> {
                        hideLoadingDialog()
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(
                            requireContext(),
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.bestDeals.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        showDialog()
                    }

                    is NetworkResult.Success -> {
                        bestDealsAdapter.differ.submitList(it.data)
                        hideLoadingDialog()
                    }

                    is NetworkResult.Error -> {
                        hideLoadingDialog()
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(
                            requireContext(),
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.bestProducts.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        showDialog()
                    }

                    is NetworkResult.Success -> {
                        bestProductAdapter.differ.submitList(it.data)
                        hideLoadingDialog()
                    }

                    is NetworkResult.Error -> {
                        hideLoadingDialog()
                        Log.e("MainCategoryFragment", it.message.toString())
                        Toast.makeText(
                            requireContext(),
                            it.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setUpBestProductRv() {
           bestProductAdapter = BestProductAdapter()
            binding.rvBestProducts.apply {
                layoutManager = GridLayoutManager(
                    requireContext(),
                    2,
                    GridLayoutManager.VERTICAL,
                    false
                )
                adapter = bestProductAdapter
            }
    }

    private fun setUpBestDealsRv() {
        bestDealsAdapter = BestDealsAdapter()
        binding.rvBestDealsProducts.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = bestDealsAdapter
        }
    }

    private fun hideLoadingDialog() {
        binding.mainCategoryProgressbar.visibility = View.GONE
    }

    private fun showDialog() {
        binding.mainCategoryProgressbar.visibility = View.VISIBLE
    }

    private fun setSpecialProductRv() {
        specialProductAdapter = SpecialProductAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = specialProductAdapter
        }
    }


}