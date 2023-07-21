package com.example.storein.fragments.shopping

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storein.R
import com.example.storein.adapters.CartProductAdapter
import com.example.storein.databinding.FragmentCartBinding
import com.example.storein.firebase.FirebaseCommon
import com.example.storein.helper.formatPrice
import com.example.storein.utils.NetworkResult
import com.example.storein.utils.VerticalItemDecoration
import com.example.storein.viewmodels.CartViewModel
import kotlinx.coroutines.flow.collectLatest

class CartFragment : Fragment(R.layout.fragment_cart) {
    private lateinit var bindind: FragmentCartBinding
    private val cartProductAdapter by lazy { CartProductAdapter() }
    private val viewModel by activityViewModels<CartViewModel>()

    var totalPrice = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindind = FragmentCartBinding.inflate(inflater)
        return bindind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCartRV()
        observeDeleteDialog()
        observeProductPrice()
        observeCartProduct()


        bindind.buttonCheckout.setOnClickListener {
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(
                totalPrice,
                cartProductAdapter.differ.currentList.toTypedArray(),
                true
            )
            findNavController().navigate(action)
        }

        cartProductAdapter.onProductClick = {
            val b = Bundle().apply { putParcelable("product", it.product) }
            findNavController().navigate(R.id.action_cartFragment_to_productDetailsFragment, b)
        }

        cartProductAdapter.onPlusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartProductAdapter.onMinusClick = {
            viewModel.changeQuantity(it, FirebaseCommon.QuantityChanging.DECREASE)
        }

        bindind.imageCloseCart.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeDeleteDialog(){
        lifecycleScope.launchWhenStarted {
            viewModel.deleteDialog.collectLatest {
                val alertDialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle(resources.getString(R.string.DeleteItemFromCart))
                    setMessage(resources.getString(R.string.AreYouSureYouWantToDeleteThisItemFromCart))
                    setNegativeButton(resources.getString(R.string.Cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    setPositiveButton(resources.getString(R.string.Delete)) { dialog, _ ->
                        viewModel.deleteCartProduct(it)
                        dialog.dismiss()
                    }
                }
                alertDialog.create()
                alertDialog.show()
            }
        }
    }

    private fun observeProductPrice(){
        lifecycleScope.launchWhenStarted {
            viewModel.productsPrice.collectLatest { price ->
                price?.let {
                    totalPrice = it
                    bindind.tvTotalPrice.text = "E£ ${price.formatPrice()}"
                }

            }
        }
    }

    private fun observeCartProduct(){
        lifecycleScope.launchWhenStarted {
            viewModel.cartProduct.collect() {
                when (it) {

                    is NetworkResult.Loading -> {
                        bindind.progressbarCart.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        bindind.progressbarCart.visibility = View.INVISIBLE
                        if (it.data!!.isEmpty()) {
                            showEmptyCart()
                            hideOtherViews()
                        } else {
                            hideEmptyCart()
                            showOtherViews()
                            cartProductAdapter.differ.submitList(it.data)
                        }
                    }

                    is NetworkResult.Error -> {
                        bindind.progressbarCart.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit

                }
            }
        }
    }

    private fun showOtherViews() {
        bindind.apply {
            rvCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
            buttonCheckout.visibility = View.VISIBLE
        }
    }

    private fun hideOtherViews() {
        bindind.apply {
            rvCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
            buttonCheckout.visibility = View.GONE
        }
    }

    private fun hideEmptyCart() {
        bindind.apply {
            layoutCartEmpty.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        bindind.apply {
            layoutCartEmpty.visibility = View.VISIBLE
        }
    }

    private fun setUpCartRV() {
        bindind.rvCart.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = cartProductAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}