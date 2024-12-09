package com.example.helpify.classes

data class ScheduledService(
    val id: String,
    val service_date: String,  // Utilize String para simplificar o exemplo
    val service_address: String,
    val service_address_complement: String?,
    val service_price: Float,
    val status: String,
    val latitude: Float,
    val longitude: Float,
    val service: Service,
    val contracting_user: User,
    val contracted_user: User?
)

data class AcceptScheduledService (
    val contracted_user_id: String
)

data class ScheduleServiceRequest(
    val service_date: String,
    val service_address: String,
    val service_price: Float,
    val service_id: String,
    val contracting_user_id: String
)

data class ScheduleServiceResponse(
    val success: Boolean,
    val message: String
)

data class Service(
    val id: String,
    val name: String,
    val image_path: String,
    val description: String,
    val default_cost: Float,
    val options: List<ServiceCostAttribute>
)

data class ServiceCostAttribute(
    val id: String,
    val name:String,
    val options: List<ServiceCostOptions>
)

data class ServiceCostOptions(
    val id:String,
    val field_name: String,
    val additional_cost: Float
)

data class User(
    val id: String,
    val name: String
)

