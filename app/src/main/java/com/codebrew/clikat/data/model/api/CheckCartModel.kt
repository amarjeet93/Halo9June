package com.codebrew.clikat.data.model.api

import com.codebrew.clikat.modal.other.ProductDataBean

data class CheckCartModel(
        val data: CartData? = null,
        val message: String? = null,
        val status: Int? = null
)

data class CartData(
        val result: List<ProductDataBean>? = null,
        var tips: ArrayList<Int>? = null,
        var min_order: Float? = null,
        var base_delivery_charges: Float? = null,
        val not_available_ids: List<String>,
        val payment_gateways: ArrayList<String>,
        val referralAmount: Int,
        var self_pickup_request: Int? = null,
        val region_delivery_charge: Float,
        val defaultCard: DefaultCard?,
        var is_vat_included: Int? = 0,
        var address: String? = "",
        var latitude: Double? = 0.0,
        var longitude: Double? = 0.0,
        var takeoff_prep_time: String? = null,
        var is_table_enabled: Int
)
//"payment_gateways": "stripe#zelle"
