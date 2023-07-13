package com.example.storein.fragments.loginAndregister.intorduction

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.storein.R
import com.example.storein.activites.ShoppingActivity
import com.example.storein.databinding.FragmentIntroductionBinding
import com.example.storein.viewmodels.IntroductionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class IntroductionFragment : Fragment(R.layout.fragment_introduction) {
    private lateinit var binding: FragmentIntroductionBinding
    private val viewModel: IntroductionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false)

        if (viewModel.hasUserFinishedIntroduction()) {
            val intent = Intent(requireActivity(), ShoppingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            viewModel.saveUserLoggedInState()
            findNavController().navigate(R.id.action_introductionFragment_to_accountOptionsFragment)
        }
    }
}