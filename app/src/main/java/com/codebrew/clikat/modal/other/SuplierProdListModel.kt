package com.codebrew.clikat.modal.other

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class SuplierProdListModel(var status: Int = 0,
                                var message: String? = null,
                                var data: DataBean? = null)

@Parcelize
data class DataBean(var supplier_detail: SupplierDetailBean? = null,
                    var product: List<ProductBean>? = null) : Parcelable

@Parcelize
data class SupplierDetailBean(var id: Int? = null,
                              var logo: String? = null,
                              var email: String? = null,
                              var trade_license_no: String? = null,
                              var total_reviews: Double? = null,
                              var rating: Float? = null,
                              var name: String? = null,
                              var user_request_flag: Int? = null,
                              var supplier_branch_id: Int? = null,
                              var timing: List<TimeDataBean>? = null,
                              var payment_method: Int? = null,
                              var description: String? = null,
                              var about: String? = null,
                              var terms_and_conditions: String? = null,
                              var address: String? = null,
                              var delivery_min_time: Int? = null,
                              var delivery_max_time: Int? = null,
                              var min_order: Int? = null,
                              var min_order_delivery: Int? = null,
                              var business_start_date: String? = null,
                              var total_order: Int? = null,
                              var delivery_prior_time: String? = null,
                              var urgent_delivery_time: String? = null,
                              var delivery_charges: Int? = null,
                              var onOffComm: Int? = null,
                              var Favourite: Int? = null,
                              var speciality: String? = null,
                              var nationality: String? = null,
                              var facebook_link: String? = null,
                              var linkedin_link: String? = null,
                              var brand: String? = null,
                              var supplier_image: List<String>? = null,
                              var out_of_stock: Int? = null,
                              var currency_name: String? = null,
                              var currency_symbol: String? = null,
                              var is_vat_included: Int?=0) : Parcelable

@Parcelize
data class ProductBean(var sub_cat_name: String? = null,
                       var detailed_sub_category: String? = null,
                       var is_SubCat_visible: Boolean? = null,
                       val detailed_category_name: List<DetailedCategoryName>? = null,
                       var value: MutableList<ProductDataBean>? = null) : Parcelable

@Parcelize
data class TimeDataBean(var week_id: Int? = null,
                        var start_time: String? = null,
                        var end_time: String? = null,
                        var is_open: Int? = null) : Parcelable

@Parcelize
data class DetailedCategoryName(
        val detailed_sub_category_id: Int? = null,
        val name: String? = null
) : Parcelable

