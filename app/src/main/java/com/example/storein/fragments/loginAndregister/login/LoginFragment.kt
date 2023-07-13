package com.example.storein.fragments.loginAndregister.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.storein.R
import com.example.storein.activites.ShoppingActivity
import com.example.storein.databinding.FragmentLoginBinding
import com.example.storein.dialog.setUpBottomSheetDialog
import com.example.storein.utils.NetworkResult
import com.example.storein.utils.getGoogleSignInClient
import com.example.storein.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.apply {
            buttonLoginLogin.setOnClickListener {
                val email = binding.edEmailLogin.text.toString().trim()
                val password = binding.edPasswordLogin.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Snackbar.make(requireView(), "Please enter the email and password!!", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.tvForgotPasswordLogin.setOnClickListener {
            setUpBottomSheetDialog { email ->
              viewModel.resetPassword(email)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.resetPassword.collect() {
                when (it) {
                    is NetworkResult.Loading -> {}

                    is NetworkResult.Success -> {
                       Snackbar.make(requireView(), "Reset password link has been sent to your email", Snackbar.LENGTH_LONG).show()
                    }

                    is NetworkResult.Error -> {
                        Snackbar.make(requireView(), "Error ${it.message}", Snackbar.LENGTH_LONG).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.login.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        binding.buttonLoginLogin.startAnimation()
                    }

                    is NetworkResult.Success -> {
                        binding.buttonLoginLogin.revertAnimation()
                        Intent(requireActivity(), ShoppingActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }

                    is NetworkResult.Error -> {
                        binding.buttonLoginLogin.revertAnimation()
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }

                    else -> Unit
                }
            }
        }

        binding.googleLogin.setOnClickListener {
            val signInClient = getGoogleSignInClient(requireContext())
            googleSignInLauncher.launch(signInClient.signInIntent)
        }

    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    viewModel.signInWithGoogle(it.idToken!!)
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign-in failed", Toast.LENGTH_LONG).show()
            }
        }
}