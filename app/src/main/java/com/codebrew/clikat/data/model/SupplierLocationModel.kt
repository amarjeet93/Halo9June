package com.codebrew.clikat.data.model


data class SupplierLocationModel(
    val `data`: SupplierLocationBean,
    val message: String,
    val status: Int
)

data class SupplierLocationBean(
    val address: List<Addres>,
    val base_delivery_charges: Float?,
    val delivery_max_time: Int,
    val is_postpone: Int,
    val min_order: Float?,
    val notification_language: Int,
    val notification_status: Int,
    val payment_method: Int,
    val preparation_time: String,
    val standard: String,
    val supplier_locations: List<SupplierLocation>,
    val user_id: Int,
    val user_service_charge: Double?
)

data class Addres(
    val address_line_1: String,
    val address_line_2: String,
    val address_link: String,
    val area_id: Int,
    val city: String,
    val collectNumber: Any,
    val country_code: Any,
    val customer_address: String,
    val directions_for_delivery: String,
    val id: Int,
    val is_default: Int,
    val is_deleted: Int,
    val iso: Any,
    val landmark: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val phone_number: String,
    val pincode: String,
    val user_id: Int
)

data class SupplierLocation(
    val location: String? = null,
    var user_service_charge: Double? = null,
    var preparation_time: String? = null,
    var min_order: Float? = null,
    var base_delivery_charges: Float? = null
)
