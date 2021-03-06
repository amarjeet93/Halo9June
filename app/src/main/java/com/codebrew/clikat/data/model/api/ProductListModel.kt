package com.codebrew.clikat.data.model.api

import android.os.Parcelable
import com.codebrew.clikat.modal.HourlyPrice
import com.codebrew.clikat.modal.other.ProductDataBean
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class ProductListModel(
        var status: Int = 0,
        var message: String? = null,
        var data: ProductData? = null)

data class ProductData(var product: ArrayList<ProductDataBean>? = null)




@Parcelize
data class AddsOn(
        val name: String? = null,
        val value: List<Value>? = null,
        val adds_on_id: Int?=null,
        val adds_on_name: String?=null,
        val adds_on_type_jd: Int?=null,
        val adds_on_type_name: String?=null,
        val cart_id: Int?=null,
        val created_at: String?=null,
        val id: Int?=null,
        val price: Float?=null,
        val quantity: Int?=null,
        val serial_number: Int?=null,
        val addon_limit: Int?=null,
        val updated_at: String?=null,
        val is_mandatory: String?=null
) : Parcelable

@Parcelize
data class Value(
        val id: String? = null,
        var status:Boolean?=null,
        val is_default: String? = null,
        val is_multiple: String? = null,
        val max_adds_on: String? = null,
        val min_adds_on: String? = null,
        val name: String? = null,
        val price: Float? = null,
        val type_id: String? = null,
        val addon_limit: Int?=null,
        val is_mandatory: String?=null,
        val serial_number: Int?=null,
        val type_name: String? = null
) : Parcelable






