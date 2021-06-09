package com.codebrew.clikat.modal

import android.os.Parcelable
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.modal.other.VariantValuesBean
import kotlinx.android.parcel.Parcelize


@Parcelize
data class CartInfo(

        var productName: String? = null,
        val currency: String? = null,
        var measuringUnit: String? = null,
        var imagePath: String? = null,
        var price: Float = 0.toFloat(),
        var supplierName: String? = null,
        var supplierAddress: String? = null,
        var quantity: Int = 0,
        var supplierId: Int = 0,
        var categoryId: Int = 0,
        var handlingSupplier: Float = 0.toFloat(),
        var handlingCharges: Float = 0.toFloat(),
        var suplierBranchId: Int = 0,
        var productId: Int = 0,
        var handlingAdmin: Float = 0.toFloat(),
        var isUrgent: Int = 0,
        var urgentValue: Float = 0f,
        var priceType: Int = 0,
        var urgent_type: Int = 0,
        var packageType: Int = 1,

        var agentType: Int = 0,

        var agentList: Int = 0,

        // 0 for delivery 1 for pickup
        var deliveryType: Int = 0,

        // 0 for serviceType 1 for productType
        var serviceType: Int = 0,

        var serviceDuration: Int = 0,

        var serviceDurationSum: Int = 0,

        var deliveryCharges: Float = 0.toFloat(),

        var subCategoryName: String = "",

        var isQuant: Int = 1,
        var hourlyPrice: List<HourlyPrice> = emptyList(),
        var purchasedQuant: Int? = null,
        var prodQuant: Int? = null,


        var avgRating: Float? = 0f,
        var isQuantity: Int? = null,
        var isDiscount: Int? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        var radius_price: Float? = null,
        var distance_value: Float? = null,
        var appType: Int? = null,
        //question list
        var question_list: MutableList<QuestionList>? = mutableListOf(),
        //addon data price
        var add_ons: MutableList<ProductAddon?>? = mutableListOf(),
        var add_on_name: String? = null,
        var fixed_price: Float? = null,
        var productAddonId: Long = 0,
        var isStock:Boolean?=true,
        var deliveryMax:Int=0,
        var isPaymentConfirm:Int=0,
        // varient list
        var varients:MutableList<VariantValuesBean?>? = mutableListOf()

) : Parcelable


@Parcelize
data class ProductAddon(var id: String? = null, var name: String? = null, var price: Float? = null,
                        var type_id: String? = null, var type_name: String? = null, var quantity: Int? = null,
                        var serial_number: Int? = null) : Parcelable
