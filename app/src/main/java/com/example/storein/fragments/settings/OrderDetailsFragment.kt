package com.example.storein.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storein.adapters.BillingProductAdapter
import com.example.storein.data.order.OrderStatus
import com.example.storein.data.order.getOrderStatus
import com.example.storein.databinding.FragmentOrderDetailBinding
import com.example.storein.databinding.FragmentOrdersBinding
import com.example.storein.utils.VerticalItemDecoration

class OrderDetailsFragment: Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding
    private val billingProductAdapter by lazy {BillingProductAdapter()}
    private val args by navArgs<OrderDetailsFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpOrderDetailRv()

        val order = args.order
        binding.apply {
            tvOrderId.text = "Order #${order.orderId}"
            stepView.setSteps(
                mutableListOf(
                    OrderStatus.Ordered.status,
                    OrderStatus.Confirmed.status,
                    OrderStatus.Shipped.status,
                    OrderStatus.Delivered.status,
                    )
            )
            val currentOrderStep = when(getOrderStatus(order.orderStatus)){
                OrderStatus.Ordered -> 0
                OrderStatus.Confirmed -> 1
                OrderStatus.Shipped -> 2
                OrderStatus.Delivered -> 3
                else -> 0
            }
            stepView.go(currentOrderStep, true)
            if (currentOrderStep == 3) {
                stepView.done(true)
            }


            tvFullName.text = order.adddress.fullName
            tvAddress.text = "${order.adddress.street} ${order.adddress.city} ${order.adddress.state}"
            tvPhoneNumber.text = order.adddress.phone
            tvTotalPrice.text = "E£ ${order.totalPrice}"

        }

        billingProductAdapter.differ.submitList(order.products)

        binding.imageCloseOrder.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setUpOrderDetailRv() {
        binding.rvProducts.apply {
            adapter = billingProductAdapter
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false)
            addItemDecoration(VerticalItemDecoration())
        }
    }
}