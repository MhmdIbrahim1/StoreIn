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
import com.example.storein.helper.formatPrice
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
        observeOrders()
        observeAddress()


        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
            }
        }


        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }

        billingProductAdapter.differ.submitList(products)
        binding.tvTotalPrice.text = "E£ ${totalPrice.formatPrice()}"

        binding.imageCloseBilling.setOnClickListener {
            findNavController().popBackStack()
        }

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val action = BillingFragmentDirections.actionBillingFragmentToAddressFragment(it)
                findNavController().navigate(action)
            }
        }

        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), resources.getString(R.string.PleaseSelectAddress), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            } else {
                showOrderConfirmationDialog()
            }
        }
    }

    private fun observeOrders() {
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
                            R.string.OrderPlacedSuccessfully,
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

    }

    private fun observeAddress() {
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
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.OrderItems)
            setMessage(R.string.AreYouSureYouWantToPlaceOrder)
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(R.string.Yes) { dialog, _ ->
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