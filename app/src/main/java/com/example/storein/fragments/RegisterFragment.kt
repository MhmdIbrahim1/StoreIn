package com.example.storein.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.storein.R
import com.example.storein.data.User
import com.example.storein.databinding.FragmentResgiesterBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


private const val TAG = "RegisterFragment"
@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentResgiesterBinding
    private val viewModel by viewModels<RegisterViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentResgiesterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonRegisterRegister.setOnClickListener{
                val user = User(
                    edFirstNameRegister.text.toString().trim(),
                    edLastNameRegister.text.toString().trim(),
                    edEmailRegister.text.toString().trim(),
                )
                val password = edPasswordRegister.text.toString()
                viewModel.createAccountWithEmailAndPassword(user, password)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.register.collect(){
                when(it){
                    is NetworkResult.Loading -> {
                        binding.buttonRegisterRegister.startAnimation()
                    }
                    is NetworkResult.Success -> {
                        Log.d("RegisterFragment", it.data.toString())
                        binding.buttonRegisterRegister.revertAnimation()
                        binding.apply {
                            edFirstNameRegister.setText("")
                            edLastNameRegister.setText("")
                            edEmailRegister.setText("")
                            edPasswordRegister.setText("")
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, it.message.toString())
                        binding.buttonRegisterRegister.revertAnimation()
                    }
                    else -> Unit
                }
            }
        }
    }
}