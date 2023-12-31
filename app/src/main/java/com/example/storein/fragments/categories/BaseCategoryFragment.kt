package com.example.storein.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storein.R
import com.example.storein.adapters.BestProductAdapter
import com.example.storein.databinding.FragmentBaseCategoryBinding
import com.example.storein.fragments.shopping.HomeFragmentDirections
import com.example.storein.helper.getProductPrice
import com.example.storein.utils.ShowBottomNavigation

open class BaseCategoryFragment : Fragment(R.layout.fragment_base_category) {
    lateinit var binding: FragmentBaseCategoryBinding
    protected val offerAdapter: BestProductAdapter by lazy { BestProductAdapter() }
    protected val bestProductAdapter: BestProductAdapter by lazy { BestProductAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpOfferRV()
        setUpBestProductRv()

        bestProductAdapter.onClick = {

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

        offerAdapter.onClick = {
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

        binding.rvOffer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollHorizontally(1) && dx != 0) {
                    onOfferPagingRequest()
                }
            }
        })

        binding.nestedScrollBaseCategory.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val reachEnd =
                scrollY >= (binding.nestedScrollBaseCategory.getChildAt(0).measuredHeight - binding.nestedScrollBaseCategory.measuredHeight)
            if (reachEnd) {
                onBestProductPagingRequest()
            }
        }
    }

    open fun onOfferPagingRequest() {

    }

    open fun onBestProductPagingRequest() {

    }


    private fun setUpOfferRV() {
        binding.rvOffer.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = offerAdapter
        }
    }

    private fun setUpBestProductRv() {
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

    override fun onResume() {
        super.onResume()
        ShowBottomNavigation()
    }
}