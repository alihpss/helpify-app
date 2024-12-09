package com.example.helpify.ui.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.helpify.R
import com.example.helpify.databinding.FragmentRegisterBinding
import com.example.helpify.network.LoginRequest
import com.example.helpify.network.RegisterRequest
import com.example.helpify.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userTypeOptions = listOf("Cliente", "Prestador de Serviços")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.userTypeSpinner.adapter = adapter


        // Ação ao clicar no botão de Registrar

        binding.btnGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextTextPassword2.text.toString()
            val phone = binding.editTextText3.text.toString()
            val name = binding.editTextText.text.toString()

            // Verifique se os campos estão preenchidos
            if (email.isNotEmpty() && password.isNotEmpty() && phone.isNotEmpty() && name.isNotEmpty()) {
                // Obtenha a seleção do tipo de usuário
                val role = if (binding.userTypeSpinner.selectedItem == "Cliente") "USER" else "PROVIDER"

                // Chame a função register com os dados e o tipo de usuário
                register(email, password, phone, name, role)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Por favor, preencha todos os campos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun register(email: String, password: String, phone: String, name: String, role: String) {
        val registerRequest = RegisterRequest(email, password, name, phone, role)

        try {
            // Faz a chamada à API usando Retrofit
            RetrofitClient.apiService.register(registerRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Usuário registrado!", Toast.LENGTH_SHORT).show()
                        // Navegue para a tela Home aqui, se necessário
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else {
                        Toast.makeText(requireContext(), "Falha no registro.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            // Mostra um Toast em caso de erro inesperado sem fechar o app
            Toast.makeText(requireContext(), "Erro ao tentar realizar login: ${e.message}", Toast.LENGTH_LONG).show()
            Log.d("e", e.message.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
