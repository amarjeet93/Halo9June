package com.codebrew.clikat.modal.other

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class OfferListModel(
        var status: Int? = null,
        var message: String? = null,
        var data: OfferDataBean? = null)

data class OfferDataBean(
        var offerEnglish: List<ProductDataBean>? = null,
        @SerializedName("SupplierInArabic")
        var supplierInArabic: List<SupplierInArabicBean>? = null,
        val getOfferByCategory: List<GetOfferByCategory>?=null)


data class GetOfferByCategory(
        val name: String,
        val value: MutableList<ProductDataBean>
)

@Parcelize
data class SupplierInArabicBean(
        var id : Int? = null,
        var supplier_branch_id: Int? = null,
        var supplier_image: String? = null,
        var is_subcategory: Int? = null,
        var logo: String? = null,
        var status : Int? = null,
        var payment_method : Int? = null,
        var rating: Float? = null,
        var total_reviews : Float? = null,
        var name: String? = null,
        val delivery_radius: Float?=null,
        val distance: Double?=null,
        var description: String? = null,
        var uniqueness: String? = null,
        var Favourite:Int?= 0,
        var terms_and_conditions: String? = null,
        var category: MutableList<SubCategoryData>? = null,
        var address: String? = null,
        val type: Int,
        var parentPosition:Int?= null
      ):Parcelable
