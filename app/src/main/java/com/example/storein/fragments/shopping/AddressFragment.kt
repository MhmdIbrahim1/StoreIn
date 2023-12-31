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
import com.example.storein.data.Address
import com.example.storein.databinding.FragmentAddressBinding
import com.example.storein.utils.NetworkResult
import com.example.storein.viewmodels.AddressViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddressFragment : Fragment() {
    private lateinit var binding: FragmentAddressBinding
    val viewModel by viewModels<AddressViewModel>()
    val args by navArgs<AddressFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeAddAddress()
        observeError()
        observeDeleteAddress()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAddressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addressArgs = args.address
        if (addressArgs == null) {
            binding.buttonDelelte.visibility = View.GONE
        } else {
            binding.apply {
                edAddressTitle.setText(addressArgs.title)
                edFullName.setText(addressArgs.fullName)
                edStreet.setText(addressArgs.street)
                edPhone.setText(addressArgs.phone)
                edCity.setText(addressArgs.city)
                edState.setText(addressArgs.state)
            }
        }

        binding.apply {
            buttonSave.setOnClickListener {
                val addressTitle = edAddressTitle.text.toString()
                val fullName = edFullName.text.toString()
                val street = edStreet.text.toString()
                val phone = edPhone.text.toString()
                val city = edCity.text.toString()
                val state = edState.text.toString()
                val address = Address(addressTitle, fullName, street, phone, city, state)

                if (addressArgs != null){
                    val updatedAddress = addressArgs.copy(
                        title = addressTitle,
                        fullName = fullName,
                        street = street,
                        phone = phone,
                        city = city,
                        state = state
                    )
                    viewModel.updateAddress(updatedAddress)
                }else{
                    val address = Address(addressTitle, fullName, street, phone, city, state)
                    viewModel.addAddress(address)
                }
            }
        }

        binding.buttonDelelte.setOnClickListener {
            if (addressArgs != null) {
                viewModel.deleteAddress(addressArgs)
            }
        }
        binding.imageAddressClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }


   private fun observeError() {
        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddAddress() {
        lifecycleScope.launchWhenStarted {
            viewModel.address.collectLatest {
                when (it) {
                    is NetworkResult.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        findNavController().navigateUp()
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    fun observeUpdateAddress(){
        lifecycleScope.launchWhenStarted {
            viewModel.address.collectLatest {
                when(it) {
                    is NetworkResult.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        findNavController().navigateUp()
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun observeDeleteAddress(){
        lifecycleScope.launchWhenStarted {
            viewModel.deleteStatus.collectLatest {
                when(it) {
                    is NetworkResult.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is NetworkResult.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        findNavController().navigateUp()
                    }

                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }
}