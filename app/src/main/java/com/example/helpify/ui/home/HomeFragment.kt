package com.example.helpify.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.helpify.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Exemplo de uso de binding para acessar componentes da UI
        val welcomeTextView = binding.textViewWelcome
        val userNameTextView = binding.textViewUserName
        val nextAppointmentsTextView = binding.textViewNextAppointments

        // Aqui você pode popular os itens da Grid de serviços programaticamente se necessário

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
