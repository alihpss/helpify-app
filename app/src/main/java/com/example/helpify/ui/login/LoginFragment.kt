package com.example.helpify.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.helpify.R
import com.example.helpify.databinding.FragmentLoginBinding
import com.example.helpify.network.LoginRequest
import com.example.helpify.network.LoginResponse
import com.example.helpify.network.RetrofitClient
import com.example.helpify.utils.AuthUtils
import com.example.helpify.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ao clicar no botão de login, capturamos o e-mail e senha e enviamos a requisição
        binding.btnLogin.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            // Valida se os campos não estão vazios
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LOGIN", "ENTRANDO")
                if (response.isSuccessful) {
                    Log.d("LOGIN", "ENTROU NO SUCESSO")
                    Log.d("RESPOSTA", response.toString() ?: "")
                    val token = response.body()?.accessToken
                    Log.d("LOGIN", token.toString())

                    if (token != null) {
                        SharedPreferencesManager.saveToken(requireContext(), token)

                        // Decodifica o token e salva os dados do usuário
                        val userData = AuthUtils.decodeJWT(token)
                        userData?.let {
                            SharedPreferencesManager.saveUserData(requireContext(), it)
                        }

                        Toast.makeText(requireContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_home)
                    }
                } else {
                    Toast.makeText(requireContext(), "Falha no login. Verifique suas credenciais.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
