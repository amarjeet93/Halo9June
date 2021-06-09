package com.codebrew.clikat.modal

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.modal.other.VariantValuesBean
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/*
 * Created by cbl45 on 7/5/16.
 */
@Parcelize
class CartInfoServer (

    @SerializedName("productId")
    @Expose
    var productId: String? = null,

    @SerializedName("quantity")
    @Expose
    var quantity: Int? = null,
    @SerializedName("handling_admin")
    @Expose
    var handlingAdmin: Float? = null,
    @SerializedName("delivery_charge")
    var deliveryCharges:Float?=null,
    var name: String? = null,
    var price:Float?=null,
    var fixed_price:Float?=null,
    var subCatName: String? = null,
    @SerializedName("handling_supplier")
    @Expose
    var handlingSupplier: Float? = null,
    @SerializedName("price_type")
    @Expose
    var pricetype :Int?=null,
    @SerializedName("supplier_branch_id")
    @Expose
    var supplier_branch_id :Int?=null,
    @SerializedName("supplier_id")
    @Expose
    var supplier_id:Int?=0,
    var agent_type :Int?=null,
    var agent_list:Int?=null,
    var category_id:Int?=null,
    var deliveryType:Int?=null,
    @SerializedName("add_ons")
    @Expose
    var add_ons: List<ProductAddon?>? = null,
    var variants: List<VariantValuesBean?>? = null,
    var question_list: List<QuestionList?>? = null,
    var appType:Int?=null,
    var isPaymentConfirm:Int?=null

):Parcelable