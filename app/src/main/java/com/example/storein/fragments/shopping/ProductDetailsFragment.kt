package com.example.storein.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storein.R
import com.example.storein.adapters.ColorsAdapter
import com.example.storein.adapters.SizesAdapter
import com.example.storein.adapters.ViewPager2Images
import com.example.storein.data.CartProduct
import com.example.storein.databinding.FragmentProductDetailsBinding
import com.example.storein.helper.formatPrice
import com.example.storein.utils.HideBottomNavigation
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.DetailsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    // Get the product details from the navigation arguments
    private val args by navArgs<ProductDetailsFragmentArgs>()

    // Initialize view binding and lazy adapters
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }

    // Selected color and size variables to store user choices
    private var selectedColor: Int? = null
    private var selectedSize: String? = null

    // View model to handle business logic
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Hide the bottom navigation bar for this fragment
        HideBottomNavigation()

        // Inflate the layout using view binding
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the product from the navigation arguments
        val product = args.product

        // Set up RecyclerViews and ViewPager2
        setUpSizesRV()
        setUpColorsRV()
        setUpViewPager2()
        observeAddToCart()


        // Set up click listeners
        binding.imgClose.setOnClickListener {
            // Navigate back when the close button is clicked
            findNavController().navigateUp()
        }

        // Set up item click listeners for sizes and colors
        sizesAdapter.onItemClick = { size ->
            // Update the selected size when the user selects one from the Sizes RecyclerView
            selectedSize = size
        }

        colorsAdapter.onItemClick = { color ->
            // Update the selected color when the user selects one from the Colors RecyclerView
            selectedColor = color
        }

        // Handle "Add to Cart" button click
        binding.btnAddToCart.setOnClickListener {
            // Check if there are available colors and sizes for the product
            val availableColors = product.colors
            val availableSizes = product.sizes

            // Ask the user to select a color and size before adding to cart
            if (availableColors.isNullOrEmpty() && availableSizes.isNullOrEmpty()) {
                // If both colors and sizes are empty or null, the product is out of stock
                Toast.makeText(requireContext(), (R.string.OutOfStock), Toast.LENGTH_SHORT).show()
            } else if (availableColors.isNullOrEmpty()) {
                // If only colors are empty or null, prompt the user to select a size
                if (selectedSize == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectSize), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected size
                viewModel.addUpdateProductInCart(CartProduct(product, 1, null, selectedSize))
            } else if (availableSizes.isNullOrEmpty()) {
                // If only sizes are empty or null, prompt the user to select a color
                if (selectedColor == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectColor), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected color
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, null))
            } else {
                // Both colors and sizes are available, ask the user to select one
                if (selectedColor == null || selectedSize == null) {
                    Toast.makeText(requireContext(), (R.string.PleaseSelectColorAndSize), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Add the product to the cart with the selected color and size
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
            }
        }



        // Populate the UI with product details
        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "E£ ${(product.price.formatPrice())}"
            tvProductDescription.text = product.description

            // Hide the labels for color and size if they are not available
            if (product.colors.isNullOrEmpty()) {
                tvProductColor.visibility = View.INVISIBLE
            }
            if (product.sizes.isNullOrEmpty()) {
                tvProductSize.visibility = View.INVISIBLE
            }
        }

        // Update the RecyclerViews and ViewPager2 with data from the product object
        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorsAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }

        // Set up TabLayout with the ViewPager2
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
            TabLayoutMediator(tabLayoutImageIndicator, viewPagerProductImages) { _, _ -> }.attach()
        }
    }


    private fun observeAddToCart(){
        // Observe the "addToCart"  for changes and react accordingly
        lifecycleScope.launchWhenStarted {
            viewModel.addToCart.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        // Show loading animation when adding to cart
                        binding.btnAddToCart.startAnimation()
                    }
                    is NetworkResult.Success -> {
                        // Show success animation and a toast message when the product is added to cart successfully
                        binding.btnAddToCart.revertAnimation()
                        binding.btnAddToCart.setBackgroundColor(resources.getColor(R.color.black))
                        Toast.makeText(requireContext(), (R.string.AddToCart), Toast.LENGTH_SHORT).show()
                    }
                    is NetworkResult.Error -> {
                        // Show error message in a toast if there's an issue adding the product to cart
                        binding.btnAddToCart.revertAnimation()
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }


    private fun setUpSizesRV() {
        binding.rvSizes.apply {
            adapter = sizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setUpColorsRV() {
        binding.rvColors.apply {
            adapter = colorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setUpViewPager2() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

}