package com.example.storein.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.storein.R
import com.example.storein.adapters.HomeViewPagerAdapter
import com.example.storein.databinding.FragmentHomeBinding
import com.example.storein.fragments.categories.AccessoryFragment
import com.example.storein.fragments.categories.ChairFragment
import com.example.storein.fragments.categories.CupboardFragment
import com.example.storein.fragments.categories.FurnitureFragment
import com.example.storein.fragments.categories.MainCategoryFragment
import com.example.storein.fragments.categories.ShoesFragment
import com.example.storein.fragments.categories.TableFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ShoesFragment(),
            ChairFragment(),
            CupboardFragment(),
            TableFragment(),
            AccessoryFragment(),
            FurnitureFragment()
        )

        binding.viewpagerHome.isUserInputEnabled = false

        val viewPager2Adapter = HomeViewPagerAdapter(
            categoriesFragments,
            childFragmentManager,
            lifecycle
        )
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when(position) {
                0 -> tab.text = "Home"
                1 -> tab.text = "Shoes"
                2 -> tab.text = "Chair"
                3 -> tab.text = "Cupboard"
                4 -> tab.text = "Table"
                5 -> tab.text = "Accessory"
                6 -> tab.text = "Furniture"
            }
        }.attach()
    }
}