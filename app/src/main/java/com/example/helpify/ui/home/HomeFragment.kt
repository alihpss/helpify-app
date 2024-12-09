package com.example.helpify.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.helpify.R
import com.example.helpify.classes.ScheduledService
import com.example.helpify.classes.Service
import com.example.helpify.databinding.FragmentHomeBinding
import com.example.helpify.network.RetrofitClient
import com.example.helpify.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userData = SharedPreferencesManager.getUserData(requireContext())
        val userName = userData?.name ?: "Usuário"  // Define um valor padrão caso o nome seja nulo
        binding.textViewUserName.text = userName

        // Exibir a data de hoje formatada
        binding.textViewDate.text = getCurrentDateFormatted()

        Log.d("USUARIO", userData?.toString() ?: "")

        val userId = userData?.id  ?: ""// Substitua pelo ID do usuário atual
        fetchAvailableServices()
        fetchScheduledServices(userId)

    }

    private fun fetchAvailableServices() {
        RetrofitClient.apiService.getAvailableServices().enqueue(object : Callback<List<Service>> {
            override fun onResponse(call: Call<List<Service>>, response: Response<List<Service>>) {
                if (response.isSuccessful) {
                    val services = response.body() ?: return
                    populateServiceLayouts(services)
                } else {
                    Toast.makeText(requireContext(), "Erro ao buscar serviços", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                Toast.makeText(requireContext(), "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateServiceLayouts(services: List<Service>) {
        binding.servicesContainer.removeAllViews()

        services.forEach { service ->
            val serviceLayout = layoutInflater.inflate(R.layout.service_item_layout, binding.servicesContainer, false)

            val imageView = serviceLayout.findViewById<ImageView>(R.id.serviceImage)
            val textView = serviceLayout.findViewById<TextView>(R.id.serviceName)

            // Carregar imagem com Glide
            Glide.with(this).load(service.image_path).into(imageView)

            textView.text = service.name

            // Configurar clique para logar o ID do serviço
            serviceLayout.setOnClickListener {
                Log.d("ServiceClick", "Service ID: ${service.id}")
            }

            binding.servicesContainer.addView(serviceLayout)
        }
    }

    private fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
        return dateFormat.format(Date())
    }

    private fun fetchScheduledServices(userId: String) {
        RetrofitClient.apiService.getNextScheduledServices(userId)
            .enqueue(object : Callback<List<ScheduledService>> {
                override fun onResponse(
                    call: Call<List<ScheduledService>>,
                    response: Response<List<ScheduledService>>
                ) {
                    if (response.isSuccessful) {
                        val scheduledServices = response.body() ?: emptyList()

                        if (scheduledServices.isNotEmpty()) {
                            populateScheduledServices(scheduledServices)
                        } else {
                            displayNoServiceMessage()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao buscar serviços.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ScheduledService>>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Erro de rede: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun populateScheduledServices(scheduledServices: List<ScheduledService>) {
        // Limpa qualquer card existente
        binding.appointmentContainer.removeAllViews()

        // Popula o container horizontal com novos cards
        scheduledServices.forEach { service ->
            val appointmentCard = layoutInflater.inflate(R.layout.appointment_card, binding.appointmentContainer, false)

            appointmentCard.findViewById<TextView>(R.id.appointmentNumber).text = "Serviço #${service.id}"
            appointmentCard.findViewById<TextView>(R.id.appointmentDate).text = service.service_date
            appointmentCard.findViewById<TextView>(R.id.appointmentType).text = "Tipo: ${service.service.name}"
            appointmentCard.findViewById<TextView>(R.id.appointmentProvider).text = "Prestador: ${service.contracted_user?.name ?: "A definir"}"
            appointmentCard.findViewById<TextView>(R.id.appointmentStatus).text = "Status: ${service.status}"

            binding.appointmentContainer.addView(appointmentCard)
        }
    }

    // Caso o array retornado seja vazio, exibe a mensagem "Nenhum serviço agendado"
    private fun displayNoServiceMessage() {
        val appointmentCard = layoutInflater.inflate(R.layout.appointment_card, binding.appointmentContainer, false)
        // Altera o layoutParams para ocupar a largura total
        appointmentCard.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

//        val appointmentCard = layoutInflater.inflate(R.layout.appointment_card, binding.appointmentContainer, false)
        appointmentCard.findViewById<TextView>(R.id.appointmentNumber).text = "Nenhum serviço agendado"
        appointmentCard.findViewById<TextView>(R.id.appointmentDate).text = ""
        appointmentCard.findViewById<TextView>(R.id.appointmentType).text = ""
        appointmentCard.findViewById<TextView>(R.id.appointmentProvider).text = ""
        appointmentCard.findViewById<TextView>(R.id.appointmentStatus).text = ""

        binding.appointmentContainer.removeAllViews()
        binding.appointmentContainer.addView(appointmentCard)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
