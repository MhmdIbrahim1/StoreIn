package com.example.storein.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.bottomnavigation.BottomNavigationView
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


        binding.tvSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        binding.imgScan.setOnClickListener {
            Toast.makeText(requireContext(), (R.string.ComingsSoon), Toast.LENGTH_SHORT).show()
        }
        binding.imgMic.setOnClickListener {
            Toast.makeText(requireContext(), (R.string.ComingsSoon), Toast.LENGTH_SHORT).show()
        }

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
                0 -> tab.text = resources.getString(R.string.HOME)
                1 -> tab.text = resources.getString(R.string.SHOES)
                2 -> tab.text = resources.getString(R.string.CHAIR)
                3 -> tab.text = resources.getString(R.string.CUPBOARD)
                4 -> tab.text = resources.getString(R.string.TABLE)
                5 -> tab.text = resources.getString(R.string.ACCESSORY)
                6 -> tab.text = resources.getString(R.string.FURNITURE)
            }
        }.attach()
    }
    override fun onResume() {
        super.onResume()
        val bottomNavigation =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}