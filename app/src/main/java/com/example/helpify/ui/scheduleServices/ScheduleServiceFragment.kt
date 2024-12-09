package com.example.helpify.ui.scheduleServices

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.helpify.R
import com.example.helpify.classes.ScheduleServiceRequest
import com.example.helpify.classes.ScheduledService
import com.example.helpify.classes.Service
import com.example.helpify.databinding.FragmentScheduleServiceProviderBinding
import com.example.helpify.databinding.FragmentScheduleServiceUserBinding
import com.example.helpify.network.RetrofitClient
import com.example.helpify.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ScheduleServiceFragment : Fragment() {

    private var _bindingUser: FragmentScheduleServiceUserBinding? = null
    private var _bindingProvider: FragmentScheduleServiceProviderBinding? = null
    private val bindingUser get() = _bindingUser!!
    private val bindingProvider get() = _bindingProvider!!

    private var selectedService: Service? = null
    private val selectedAttributes = mutableMapOf<String, Float>()
    private var selectedDate: String? = null
    private var price: Float = 0.0F

    private lateinit var binding: FragmentScheduleServiceProviderBinding
    private val services = mutableListOf<Service>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userData = SharedPreferencesManager.getUserData(requireContext())
        val userRole = userData?.role



        return if (userRole == "PROVIDER") {
            // Inflar layout para o provider
//            _bindingProvider = FragmentScheduleServiceProviderBinding.inflate(inflater, container, false)
//            bindingProvider.root

            _bindingProvider = FragmentScheduleServiceProviderBinding.inflate(inflater, container, false)

            fun setupDatePicker() {
                val today = Calendar.getInstance()

                // Inicializa o DatePicker com a data atual
                bindingProvider.datePicker.init(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ) { _, year, month, dayOfMonth ->
                    // Atualiza os serviços sempre que a data for alterada
                    val selectedDate = "${month + 1}/$dayOfMonth/$year"
                    fetchProviderServices(selectedDate)
                }
            }
            fetchProviderServices(getCurrentDate())
            setupDatePicker()


            return bindingProvider.root

        } else {
            // Inflar layout para o usuário



            fetchServices()
            _bindingUser = FragmentScheduleServiceUserBinding.inflate(inflater, container, false)

            bindingUser.inputDate.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) { // Quando o campo perde o foco, obtemos o valor inserido
                    selectedDate = bindingUser.inputDate.text.toString()
                    Log.d("ScheduleServiceFragment", "Data inserida: $selectedDate")
                }
            }

            bindingUser.registerScheduledService.setOnClickListener {
                val serviceDate = bindingUser.inputDate.text.toString()
                val serviceAddress = bindingUser.addressInput.text.toString()
                val totalPrice = price // Função que calcula o preço total
                val contractingUserId = SharedPreferencesManager.getUserData(requireContext())?.id
                val selectedServiceId = this.selectedService?.id

                if (serviceDate.isEmpty() || serviceAddress.isEmpty() || selectedServiceId.isNullOrEmpty() || contractingUserId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Faz a requisição para agendar o serviço
                scheduleService(serviceDate, serviceAddress, totalPrice, selectedServiceId, contractingUserId)
            }

            bindingUser.root
        }
    }



    private fun getCurrentDate(): String {
        val today = Calendar.getInstance()
        val month = today.get(Calendar.MONTH) + 1
        val day = today.get(Calendar.DAY_OF_MONTH)
        val year = today.get(Calendar.YEAR)
        return "$month/$day/$year"
    }

    private fun fetchProviderServices(date: String) {
        bindingProvider.noServicesText.visibility = View.GONE
        bindingProvider.servicesContainer.removeAllViews()

        RetrofitClient.apiService.getScheduledServices(date).enqueue(object : Callback<List<ScheduledService>> {
            override fun onResponse(call: Call<List<ScheduledService>>, response: Response<List<ScheduledService>>) {
                if (response.isSuccessful && response.body() != null) {
                    val fetchedServices = response.body()!!
                    Log.d("AQUIIIIIIIIII", response.body()!!.toString())
                    if (fetchedServices.isEmpty()) {
                        bindingProvider.noServicesText.visibility = View.VISIBLE
                    } else {
                        populateServices(fetchedServices)
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ScheduledService>>, t: Throwable) {
                Toast.makeText(requireContext(), "Erro ao conectar", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateServices(services: List<ScheduledService>) {
        // Limpa os serviços antigos antes de popular novos
        bindingProvider.servicesContainer.removeAllViews()

        services.forEach { service ->
            // Inflar o layout de cada serviço
            val serviceView = layoutInflater.inflate(R.layout.service_result_layout, bindingProvider.servicesContainer, false)

            // Encontrar as Views no layout
            val serviceName = serviceView.findViewById<TextView>(R.id.serviceName)
            val userName = serviceView.findViewById<TextView>(R.id.userName)
            val serviceDetails = serviceView.findViewById<TextView>(R.id.serviceDetails)
            val servicePrice = serviceView.findViewById<TextView>(R.id.servicePrice)
            val logButton = serviceView.findViewById<Button>(R.id.logServiceButton)

            // Configurar os dados do serviço
            serviceName.text = service.service.name
            userName.text = "Usuário: ${service.contracting_user.name}" // Nome do usuário
            serviceDetails.text = "Detalhes: ${service.status}" // Exemplo: Tamanho da casa, número de hóspedes
            servicePrice.text = "Preço: R$ ${"%.2f".format(service.service_price)}"

            // Configurar ação do botão de log
            logButton.setOnClickListener {
                Log.d("ServiceResult", "Service ID: ${service.id}")
                Toast.makeText(requireContext(), "Service ID: ${service.id}", Toast.LENGTH_SHORT).show()
            }

            // Adicionar o layout ao container principal
            bindingProvider.servicesContainer.addView(serviceView)
        }
    }

    private fun showError() {
        Toast.makeText(requireContext(), "Erro ao buscar serviços", Toast.LENGTH_SHORT).show()
    }


    private fun fetchServices() {
        RetrofitClient.apiService.getAvailableServices().enqueue(object : Callback<List<Service>> {
            override fun onResponse(call: Call<List<Service>>, response: Response<List<Service>>) {
                if (response.isSuccessful && response.body() != null) {
                    val services = response.body()!!
                    setupSpinner(services)
                } else {
                    Toast.makeText(requireContext(), "Erro ao buscar serviços", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Service>>, t: Throwable) {
                Log.e("ScheduleServiceFragment", "Erro na API: ${t.message}")
                Toast.makeText(requireContext(), "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinner(services: List<Service>) {
        val serviceNames = services.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, serviceNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bindingUser.spinnerServices.adapter = adapter

        bindingUser.spinnerServices.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedServiceId = services[position].id
                selectedService = services[position]
                Log.d("ScheduleServiceFragment", "Serviço selecionado: $selectedServiceId")
                fetchAdditionalDetails(selectedServiceId) // Disparar nova requisição com base no ID
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Não faz nada quando nenhum item é selecionado
            }
        }
    }

    private fun fetchAdditionalDetails(serviceId: String) {
        RetrofitClient.apiService.getServiceDetails(serviceId).enqueue(object : Callback<Service> {
            override fun onResponse(call: Call<Service>, response: Response<Service>) {
                if (response.isSuccessful && response.body() != null) {
                    val serviceDetails = response.body()!!
                    setupAttributes(serviceDetails)
                    Log.d("ScheduleServiceFragment", "Detalhes do serviço: $serviceDetails")
                    // Atualize a UI com os detalhes do serviço, se necessário
                } else {
                    Toast.makeText(requireContext(), "Erro ao buscar detalhes do serviço", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Service>, t: Throwable) {
                Log.e("ScheduleServiceFragment", "Erro ao buscar detalhes: ${t.message}")
                Toast.makeText(requireContext(), "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAttributes(service: Service) {
        bindingUser.textViewOptions.visibility = View.VISIBLE
        bindingUser.attributesContainer.removeAllViews()
        selectedAttributes.clear()

        service.options.forEach { attribute ->
            val textView = TextView(requireContext()).apply {
                text = attribute.name
                textSize = 16f
                setPadding(0, 24, 0, 8)
            }

            val spinner = Spinner(requireContext()).apply {
                val optionNames = attribute.options.map { it.field_name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                this.adapter = adapter

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedOption = attribute.options[position]
                        selectedAttributes[attribute.id] = selectedOption.additional_cost
                        updateTotalCost(service.default_cost)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            bindingUser.attributesContainer.addView(textView)
            bindingUser.attributesContainer.addView(spinner)
        }
    }

    private fun updateTotalCost(defaultCost: Float) {
        val totalCost = defaultCost + selectedAttributes.values.sum()
        price = totalCost
        bindingUser.textTotalCost.text = "Total: R$ %.2f".format(totalCost)
    }

    private fun scheduleService(serviceDate: String, serviceAddress: String, servicePrice: Float, serviceId: String, contractingUserId: String) {
        val request = ScheduleServiceRequest(
            service_date = serviceDate,
            service_address = serviceAddress,
            service_price = servicePrice,
            service_id = serviceId,
            contracting_user_id = contractingUserId
        )

        try {
            // Faz a chamada à API usando Retrofit
            RetrofitClient.apiService.scheduleService(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Serviço agendado com sucesso!", Toast.LENGTH_SHORT).show()
                        // Aqui você pode navegar para outra tela ou reiniciar o formulário
                        Toast.makeText(requireContext(), "Agendamento criado", Toast.LENGTH_SHORT).show()
//                        findNavController().navigate(R.id.action_scheduleServiceFragment_to_homeFragment)
                    } else {
                        Toast.makeText(requireContext(), "Erro ao agendar o serviço.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Erro de rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            // Mostra um Toast em caso de erro inesperado sem fechar o app
            Toast.makeText(requireContext(), "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            Log.d("ScheduleServiceError", e.message.toString())
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _bindingUser = null
        _bindingProvider = null
    }
}