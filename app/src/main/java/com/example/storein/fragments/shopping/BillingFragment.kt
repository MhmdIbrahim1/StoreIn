package com.example.storein.fragments.shopping

import android.app.AlertDialog
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
import androidx.recyclerview.widget.RecyclerView
import com.example.storein.R
import com.example.storein.adapters.AddressAdapter
import com.example.storein.adapters.BillingProductAdapter
import com.example.storein.data.Address
import com.example.storein.data.CartProduct
import com.example.storein.data.order.Order
import com.example.storein.data.order.OrderStatus
import com.example.storein.databinding.FragmentBillingBinding
import com.example.storein.utils.HorizontalItemDecoration
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.BillingViewModel
import com.example.storein.viewmodels.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private val billingViewModel by viewModels<BillingViewModel>()
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductAdapter by lazy { BillingProductAdapter() }
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f

    private var selectedAddress: Address? = null
    private val orderViewModel by viewModels<OrderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpBillingProductRv()
        setUpAddressRv()

        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        lifecycleScope.launchWhenStarted {
            orderViewModel.orders.collectLatest {
                when (it) {
                    is NetworkResult.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }

                    is NetworkResult.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(
                            requireView(),
                            "Order Placed Successfully",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is NetworkResult.Error -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            billingViewModel.address.collectLatest {
                when (it) {
                    is NetworkResult.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                    }

                    is NetworkResult.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
        billingProductAdapter.differ.submitList(products)
        binding.tvTotalPrice.text = "E£ $totalPrice"

        binding.imageCloseBilling.setOnClickListener {
            findNavController().popBackStack()
        }

        addressAdapter.onClick = {
            selectedAddress = it
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Please select an address", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                showOrderConfirmationDialog()
            }
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Order Items")
            setMessage("Are you sure you want to place this order?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { dialog, _ ->
                val order = Order(
                    OrderStatus.Ordered.status,
                    totalPrice,
                    products,
                    selectedAddress!!
                )
                orderViewModel.placeOrder(order)
                dialog.dismiss()
            }
        }
        alertDialog.create()
        alertDialog.show()

    }

    private fun setUpAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }

    private fun setUpBillingProductRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductAdapter
            addItemDecoration(HorizontalItemDecoration())
        }
    }
}