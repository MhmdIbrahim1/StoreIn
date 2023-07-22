package com.example.storein.fragments.categories

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.example.storein.R
import com.example.storein.adapters.BestDealsAdapter
import com.example.storein.adapters.BestProductAdapter
import com.example.storein.adapters.SpecialProductAdapter
import com.example.storein.data.CartProduct
import com.example.storein.data.Product
import com.example.storein.databinding.FragmentMainCategoryBinding
import com.example.storein.fragments.shopping.HomeFragmentDirections
import com.example.storein.helper.getProductPrice
import com.example.storein.utils.HorizontalItemDecoration
import com.example.storein.utils.NetworkResult
import com.example.storein.utils.ShowBottomNavigation
import com.example.storein.viewmodels.DetailsViewModel
import com.example.storein.viewmodels.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var specialProductAdapter: SpecialProductAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var bestProductAdapter: BestProductAdapter
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var binding: FragmentMainCategoryBinding
    private val viewModel by viewModels<MainCategoryViewModel>()

    private val detailsViewModel by viewModels<DetailsViewModel>()

    private var isFirstTime = true

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

        specialProductAdapter.onClick = {
            if (isAdded) {
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                val action =
                    HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(updatedProduct)
                findNavController().navigate(action)
            }
        }

        bestDealsAdapter.onClick = {
            if (isAdded) {
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer = it.offerPercentage?.getProductPrice(it.price) ?: it.price

                // Create a new product object with the updated price
                val updatedProduct = it.copy(price = priceAfterOffer)

                val action =
                    HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(updatedProduct)
                findNavController().navigate(action)
            }
        }

        bestProductAdapter.onClick = { product ->
            if (isAdded) {
                // Calculate the price after applying the offer percentage (if available)
                val priceAfterOffer =
                    product.offerPercentage?.getProductPrice(product.price) ?: product.price

                // Create a new product object with the updated price
                val updatedProduct = product.copy(price = priceAfterOffer)

                val action =
                    HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(updatedProduct)
                findNavController().navigate(action)
            }
        }


        nestedScrollView = binding.nestedScrollMainCategory
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val reachEnd =
                scrollY >= (nestedScrollView.getChildAt(0).measuredHeight - nestedScrollView.measuredHeight)
            if (reachEnd) {
                fetchNewData()
            }
        })

        binding.rvBestDealsProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val reachEnd = !recyclerView.canScrollHorizontally(1)
                if (reachEnd) {
                    fetchNewDataForBestDealsRV()
                }
            }
        })

        binding.rvSpecialProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val reachEnd = !recyclerView.canScrollHorizontally(1)
                if (reachEnd) {
                    fetchNewDataForSpecialProductsRV()
                }
            }
        })


        observeSpecialProducts()
        observeBestDeals()
        observeBestProducts()


        specialProductAdapter.onAddToCartClickListener = object : SpecialProductAdapter.OnAddToCartClickListener {
            override fun onAddToCartClicked(product: Product, button: CircularProgressButton) {
                // Start the animation
                button.startAnimation()

                // Simulate a network operation
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    // Stop the animation after the operation completes
                    button.revertAnimation()

                    // Get the first available color and size from the product
                    val availableColors = product.colors?.get(0)
                    val availableSizes = product.sizes?.get(0)

                    // Add the product to the cart with the selected color and size
                    detailsViewModel.addUpdateProductInCart(CartProduct(product, 1, availableColors, availableSizes))
                    Toast.makeText(requireContext(),resources.getString(R.string.AddedToCart),Toast.LENGTH_SHORT).show()
                }, 2000) // Delay of 2 seconds
            }
        }
    }

    private fun fetchNewData() {
        viewModel.fetchBestProducts()
    }

    private fun fetchNewDataForBestDealsRV() {
        viewModel.fetchBestDeals()
    }

    private fun fetchNewDataForSpecialProductsRV() {
        viewModel.fetchSpecialProducts()
    }

    private fun observeSpecialProducts() {
        lifecycleScope.launchWhenCreated {
            viewModel.specialProducts.collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        // Check if it's the first time to show the shimmer effect
                        if (isFirstTime) {
                            showShimmerEffect()
                            isFirstTime = false
                        }
                    }

                    is NetworkResult.Success -> {
                        specialProductAdapter.differ.submitList(it.data)
                        hideShimmerEffect()
                    }

                    is NetworkResult.Error -> {
                        hideShimmerEffect()
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

    private fun observeBestDeals() {
        lifecycleScope.launchWhenCreated {
            viewModel.bestDeals.collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        // Check if it's the first time to show the shimmer effect
                        if (isFirstTime) {
                            showShimmerEffect()
                            isFirstTime = false
                        }
                    }

                    is NetworkResult.Success -> {
                        bestDealsAdapter.differ.submitList(it.data)
                        hideShimmerEffect()
                    }

                    is NetworkResult.Error -> {
                        hideShimmerEffect()
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

    private fun observeBestProducts() {
        lifecycleScope.launchWhenCreated {
            viewModel.bestProducts.collect {
                when (it) {
                    is NetworkResult.Loading -> {
                        // Check if it's the first time to show the shimmer effect
                        if (isFirstTime) {
                            showShimmerEffect()
                            isFirstTime = false
                        }
                    }

                    is NetworkResult.Success -> {
                        bestProductAdapter.differ.submitList(it.data)
                        hideShimmerEffect()
                    }

                    is NetworkResult.Error -> {
                        hideShimmerEffect()
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
            val layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL,
            )
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            binding.rvBestProducts.layoutManager = layoutManager
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
            binding.rvBestDealsProducts.addItemDecoration(HorizontalItemDecoration())
            adapter = bestDealsAdapter
        }
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

    private fun showShimmerEffect() {
        binding.shimmerSpecialProducts.startShimmer()
        binding.shimmerSpecialProducts.visibility = View.VISIBLE
        binding.rvSpecialProducts.visibility = View.GONE
        binding.rvBestProducts.visibility = View.GONE
        binding.rvBestDealsProducts.visibility = View.GONE
        binding.tvBestProducts.visibility = View.GONE
        binding.tvBestDeals.visibility = View.GONE
    }

    private fun hideShimmerEffect() {
        binding.shimmerSpecialProducts.stopShimmer()
        binding.shimmerSpecialProducts.visibility = View.GONE
        binding.rvSpecialProducts.visibility = View.VISIBLE
        binding.rvBestProducts.visibility = View.VISIBLE
        binding.rvBestDealsProducts.visibility = View.VISIBLE
        binding.tvBestProducts.visibility = View.VISIBLE
        binding.tvBestDeals.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        ShowBottomNavigation()
    }


}
