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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storein.R
import com.example.storein.adapters.SpecialProductAdapter
import com.example.storein.databinding.FragmentMainCategoryBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var specialProductAdapter: SpecialProductAdapter
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