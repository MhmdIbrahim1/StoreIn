package com.example.storein.fragments.shopping

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storein.R
import com.example.storein.activites.ShoppingActivity
import com.example.storein.adapters.BestProductAdapter
import com.example.storein.adapters.SearchRecyclerAdapter
import com.example.storein.databinding.FragmentSearchBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.MainCategoryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val TAG = "SearchFragment"
    private lateinit var binding: FragmentSearchBinding
    private lateinit var inputMethodManger: InputMethodManager
    private val viewModel by viewModels<MainCategoryViewModel>()
    private lateinit var searchAdapter: SearchRecyclerAdapter
    private var isTypingOrSearching = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchRecyclerView()
        showKeyboardAutomatically()
        onHomeClick()

        searchProducts()
        observeSearch()

        onSearchTextClick()

        onCancelTvClick()

        binding.frameScan.setOnClickListener {
            val snackBar =
                requireActivity().findViewById<CoordinatorLayout>(R.id.snackBar_coordinator)
            Snackbar.make(
                snackBar,
                resources.getText(R.string.g_coming_soon),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        binding.fragmeMicrohpone.setOnClickListener {
            val snackBar =
                requireActivity().findViewById<CoordinatorLayout>(R.id.snackBar_coordinator)
            Snackbar.make(
                snackBar,
                resources.getText(R.string.g_coming_soon),
                Snackbar.LENGTH_SHORT
            ).show()
        }

    }


    private fun onCancelTvClick() {
        binding.tvCancel.setOnClickListener {
            searchAdapter.differ.submitList(emptyList())
            binding.edSearch.setText("")
            hideCancelTv()

            // User has canceled the search, show the bottom navigation view
            isTypingOrSearching = false
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.visibility = View.VISIBLE
        }
    }

    private fun onSearchTextClick() {
        searchAdapter.onItemClick = { product ->
            val bundle = Bundle()
            bundle.putParcelable("product", product)

            /**
             * Hide the keyboard
             */

            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)

            findNavController().navigate(
                R.id.action_searchFragment_to_productDetailsFragment,
                bundle
            )

        }
    }

    private fun setupSearchRecyclerView() {
        searchAdapter = SearchRecyclerAdapter()
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }


//    private fun hideCategoriesLoading() {
//        binding.progressbarCategories.visibility = View.GONE
//
//    }
//
//    private fun showCategoriesLoading() {
//        binding.progressbarCategories.visibility = View.VISIBLE
//
//    }


    private fun observeSearch() {
        viewModel.search.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Loading -> {
                    Log.d("testSearch", "Loading")
                }

                is NetworkResult.Success -> {
                    val products = response.data
                    searchAdapter.differ.submitList(products)
                    showChancelTv()
                    Log.d("testSearch", "Success - Product count: ${products?.size}")
                }

                is NetworkResult.Error -> {
                    Log.e(TAG, response.message.toString())
                    showChancelTv()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("testSearch", "Error: ${response.message}")

                }

                else -> Unit
            }
        }
    }


    private fun searchProducts() {
        var job: Job? = null
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                job?.cancel()
                job = MainScope().launch {
                    delay(500L)
                    s?.let {
                        if (s.toString().isNotEmpty()) {
                            viewModel.searchProducts(s.toString())
                        } else {
                            // If the search query is empty, clear the adapter
                            searchAdapter.differ.submitList(emptyList())
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun showChancelTv() {
        binding.tvCancel.visibility = View.VISIBLE
        binding.imgMic.visibility = View.GONE
        binding.imgScan.visibility = View.GONE
        binding.fragmeMicrohpone.visibility = View.GONE
        binding.frameScan.visibility = View.GONE

    }

    private fun hideCancelTv() {
        binding.tvCancel.visibility = View.GONE
        binding.imgMic.visibility = View.VISIBLE
        binding.imgScan.visibility = View.VISIBLE
        binding.fragmeMicrohpone.visibility = View.VISIBLE
        binding.frameScan.visibility = View.VISIBLE
    }

    private fun onHomeClick() {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemReselectedListener {
            if (isTypingOrSearching) {
                // User is typing or searching, hide the bottom navigation view
                isTypingOrSearching = false
                bottomNav.visibility = View.VISIBLE
            }
        }
        bottomNav?.menu?.getItem(0)?.setOnMenuItemClickListener {
            activity?.onBackPressed()
            true
        }
    }


    private fun showKeyboardAutomatically() {
        binding.edSearch.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.edSearch, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun onResume() {
        super.onResume()

        // Show the keyboard automatically when the fragment is resumed
        showKeyboardAutomatically()

        // Add a TextWatcher to detect when the user starts typing or searching
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // User is typing or searching, hide the bottom navigation view
                isTypingOrSearching = true
                val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }



    override fun onDestroy() {
        super.onDestroy()
        binding.edSearch.clearFocus()
    }
}