package com.codebrew.clikat.utils.configurations

import android.util.Log
import com.codebrew.clikat.R
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.SettingModel.DataBean.AppTerminology
import com.codebrew.clikat.modal.other.SettingModel.DataBean.Terminology
import com.codebrew.clikat.utils.ClikatConstants
import com.google.gson.JsonObject
import org.json.JSONObject

class TextConfig(private val type: Int, terminology: Terminology?, languageId: String) {
    @kotlin.jvm.JvmField
    var recomended = R.string.recomended.toString()
    @kotlin.jvm.JvmField
    var promotions: String? = null

    @kotlin.jvm.JvmField
    var my_favorites: String? = null
    @kotlin.jvm.JvmField
    var order_history: String? = null
    @kotlin.jvm.JvmField
    var track_my_order: String? = null
    @kotlin.jvm.JvmField
    var rate_my_order: String? = null
    @kotlin.jvm.JvmField
    var upcoming_order: String? = null
    @kotlin.jvm.JvmField
    var loyality_points: String? = null
    @kotlin.jvm.JvmField
    var share_app: String? = null
    @kotlin.jvm.JvmField
    var settings: String? = null
    @kotlin.jvm.JvmField
    var login: String? = null
    @kotlin.jvm.JvmField
    var forgot_password: String? = null
    @kotlin.jvm.JvmField
    var _continue: String? = null
    @kotlin.jvm.JvmField
    var signup_otp_msg: String? = null
    @kotlin.jvm.JvmField
    var send_otp: String? = null
    @kotlin.jvm.JvmField
    var tap_to_add_profile_pic: String? = null
    @kotlin.jvm.JvmField
    var finish: String? = null
    @kotlin.jvm.JvmField
    var oreder_scheduler: String? = null
    @kotlin.jvm.JvmField
    var delivery_address: String? = null
    @kotlin.jvm.JvmField
    var hint_email: String? = null
    @kotlin.jvm.JvmField
    var hint_password: String? = null
    @kotlin.jvm.JvmField
    var payment_method: String? = null
    @kotlin.jvm.JvmField
    var set_schedule: String? = null
    @kotlin.jvm.JvmField
    var net_total: String? = null
    @kotlin.jvm.JvmField
    var delivery_speed: String? = null
    @kotlin.jvm.JvmField
    var delivery_charges: String? = null
    @kotlin.jvm.JvmField
    var empty: String? = null
    @kotlin.jvm.JvmField
    var reviews: String? = null
    @kotlin.jvm.JvmField
    var NoCart: String? = null
    @kotlin.jvm.JvmField
    var save: String? = null
    @kotlin.jvm.JvmField
    var language: String? = null
    @kotlin.jvm.JvmField
    var address: String? = null
    @kotlin.jvm.JvmField
    var pickkup: String? = null
    @kotlin.jvm.JvmField
    var items: String? = null
    @kotlin.jvm.JvmField
    var net_payable: String? = null
    @kotlin.jvm.JvmField
    var additional_remarks: String? = null
    @kotlin.jvm.JvmField
    var update: String? = null
    @kotlin.jvm.JvmField
    var cancel: String? = null
    @kotlin.jvm.JvmField
    var open: String? = null
    @kotlin.jvm.JvmField
    var filter: String? = null
    @kotlin.jvm.JvmField
    var clearCart: String? = null
    @kotlin.jvm.JvmField
    var apply: String? = null
    @kotlin.jvm.JvmField
    var clear: String? = null
    @kotlin.jvm.JvmField
    var close: String? = null
    @kotlin.jvm.JvmField
    var both: String? = null
    @kotlin.jvm.JvmField
    var card: String? = null
    @kotlin.jvm.JvmField
    var cash: String? = null
    @kotlin.jvm.JvmField
    var rating: String? = null
    @kotlin.jvm.JvmField
    var busy: String? = null
    @kotlin.jvm.JvmField
    var logout: String? = null

    @kotlin.jvm.JvmField
    var points: String? = null
    @kotlin.jvm.JvmField
    var no_internet_connection_found: String? = null
    @kotlin.jvm.JvmField
    var retry: String? = null


    @kotlin.jvm.JvmField
    var nothing_found: String? = null
    @kotlin.jvm.JvmField
    var skip: String? = null
    @kotlin.jvm.JvmField
    var success: String? = null
    @kotlin.jvm.JvmField
    var status: String? = null
    @kotlin.jvm.JvmField
    var minimum_order: String? = null
    @kotlin.jvm.JvmField
    var delivery_time: String? = null
    @kotlin.jvm.JvmField
    var payment_options: String? = null
    @kotlin.jvm.JvmField
    var total: String? = null
    @kotlin.jvm.JvmField
    var others: String? = null
    @kotlin.jvm.JvmField
    var view_all = R.string.view_all.toString()
    @kotlin.jvm.JvmField
    var supplier_name: String? = null
    @kotlin.jvm.JvmField
    var terms: String? = null
    @kotlin.jvm.JvmField
    var hour: String? = null
    @kotlin.jvm.JvmField
    var day: String? = null
    @kotlin.jvm.JvmField
    var about_us: String? = null
    @kotlin.jvm.JvmField
    var add: String? = null
    @kotlin.jvm.JvmField
    var scheduled_order: String? = null
    @kotlin.jvm.JvmField
    var yes: String? = null
    @kotlin.jvm.JvmField
    var loyality_orders: String? = null
    @kotlin.jvm.JvmField
    var search: String? = null
    @kotlin.jvm.JvmField
    var discount: String? = null
    @kotlin.jvm.JvmField
    var error: String? = null


    //Constants
    @kotlin.jvm.JvmField
    var viewtype: String? = null
    @kotlin.jvm.JvmField
    var supplier: String? = null
    @kotlin.jvm.JvmField
    var product: String? = null
    @kotlin.jvm.JvmField
    var products: String? = null
    @kotlin.jvm.JvmField
    var order: String? = null
    @kotlin.jvm.JvmField
    var orders: String? = null
    @kotlin.jvm.JvmField
    var agent: String? = null
    @kotlin.jvm.JvmField
    var agents: String? = null
    @kotlin.jvm.JvmField
    var suppliers: String? = null
    @kotlin.jvm.JvmField
    var brand: String? = null
    @kotlin.jvm.JvmField
    var brands: String? = null
    @kotlin.jvm.JvmField
    var category: String? = null
    @kotlin.jvm.JvmField
    var categories: String? = null
    @kotlin.jvm.JvmField
    var catalogue: String? = null
    @kotlin.jvm.JvmField
    var otherTab: String? = null
    @kotlin.jvm.JvmField
    var wishlist: String? = null

    private fun changeNaming(type: Int, terminology: Terminology?, languageId: String) {

        var appTerminology: AppTerminology? = null
        if (terminology != null) {
            appTerminology = if (languageId == ClikatConstants.ENGLISH_FULL || languageId==ClikatConstants.ENGLISH_SHORT) {
                terminology.english
            } else {
                terminology.other
            }
        }

        when (type) {
            AppDataType.Food.type -> {
                supplier = if (checkTerminolgy(appTerminology, appTerminology?.supplier)) appTerminology?.supplier else "Restaurant"
                suppliers = if (checkTerminolgy(appTerminology, appTerminology?.suppliers)) appTerminology?.suppliers else "Restaurants"
                product = if (checkTerminolgy(appTerminology, appTerminology?.product)) appTerminology?.product else "Food Item"
                products = if (checkTerminolgy(appTerminology, appTerminology?.products)) appTerminology?.products else "Food Items"
                order = if (checkTerminolgy(appTerminology, appTerminology?.order)) appTerminology?.order else "Order"
                orders = if (checkTerminolgy(appTerminology, appTerminology?.orders)) appTerminology?.orders else "Orders"
                agent = if (checkTerminolgy(appTerminology, appTerminology?.agent)) appTerminology?.agent else "Agent"
                agents = if (checkTerminolgy(appTerminology, appTerminology?.agents)) appTerminology?.agents else "Agents"
                category = if (checkTerminolgy(appTerminology, appTerminology?.category)) appTerminology?.category else "Category"
                categories = if (checkTerminolgy(appTerminology, appTerminology?.categories)) appTerminology?.categories else "Categories"
                brand = if (checkTerminolgy(appTerminology, appTerminology?.brand)) appTerminology?.brand else "Brand"
                brands = if (checkTerminolgy(appTerminology, appTerminology?.brands)) appTerminology?.brands else "Brands"
                catalogue = if (checkTerminolgy(appTerminology, appTerminology?.catalogue)) appTerminology?.catalogue else "Menu"
                wishlist = if (checkTerminolgy(appTerminology, appTerminology?.wishlist)) appTerminology?.wishlist else "Wishlist"
                otherTab = if (checkTerminolgy(appTerminology, appTerminology?.others_tab)) appTerminology?.others_tab else AppGlobal.context?.getString(R.string.others)
            }

            AppDataType.HomeServ.type -> {
                supplier = if (checkTerminolgy(appTerminology, appTerminology?.supplier)) appTerminology?.supplier else "Service Provider"
                suppliers = if (checkTerminolgy(appTerminology, appTerminology?.suppliers)) appTerminology?.suppliers else "Service Providers"
                product = if (checkTerminolgy(appTerminology, appTerminology?.product)) appTerminology?.product else "Service"
                products = if (checkTerminolgy(appTerminology, appTerminology?.products)) appTerminology?.products else "Services"
                order = if (checkTerminolgy(appTerminology, appTerminology?.order)) appTerminology?.order else "Booking"
                orders = if (checkTerminolgy(appTerminology, appTerminology?.orders)) appTerminology?.orders else "Bookings"
                agent = if (checkTerminolgy(appTerminology, appTerminology?.agent)) appTerminology?.agent else "Agent"
                agents = if (checkTerminolgy(appTerminology, appTerminology?.agents)) appTerminology?.agents else "Agents"
                category = if (checkTerminolgy(appTerminology, appTerminology?.category)) appTerminology?.category else "Category"
                categories = if (checkTerminolgy(appTerminology, appTerminology?.categories)) appTerminology?.categories else "Categories"
                brand = if (checkTerminolgy(appTerminology, appTerminology?.brand)) appTerminology?.brand else "Brand"
                brands = if (checkTerminolgy(appTerminology, appTerminology?.brands)) appTerminology?.brands else "Brands"
                catalogue = if (checkTerminolgy(appTerminology, appTerminology?.catalogue)) appTerminology?.catalogue else "Services"
                otherTab = if (checkTerminolgy(appTerminology, appTerminology?.others_tab)) appTerminology?.others_tab else "Others"
                wishlist = if (checkTerminolgy(appTerminology, appTerminology?.wishlist)) appTerminology?.wishlist else "Wishlist"
            }

            else -> {
                supplier = if (checkTerminolgy(appTerminology, appTerminology?.supplier)) appTerminology?.supplier else "Supplier"
                suppliers = if (checkTerminolgy(appTerminology, appTerminology?.suppliers)) appTerminology?.suppliers else "Suppliers"
                product = if (checkTerminolgy(appTerminology, appTerminology?.product)) appTerminology?.product else "Product"
                products = if (checkTerminolgy(appTerminology, appTerminology?.products)) appTerminology?.products else "Products"
                order = if (checkTerminolgy(appTerminology, appTerminology?.order)) appTerminology?.order else "Order"
                orders = if (checkTerminolgy(appTerminology, appTerminology?.orders)) appTerminology?.orders else "Orders"
                agent = if (checkTerminolgy(appTerminology, appTerminology?.agent)) appTerminology?.agent else "Agent"
                agents = if (checkTerminolgy(appTerminology, appTerminology?.agents)) appTerminology?.agents else "Agents"
                category = if (checkTerminolgy(appTerminology, appTerminology?.category)) appTerminology?.category else "Category"
                categories = if (checkTerminolgy(appTerminology, appTerminology?.categories)) appTerminology?.categories else "Categories"
                brand = if (checkTerminolgy(appTerminology, appTerminology?.brand)) appTerminology?.brand else "Brand"
                brands = if (checkTerminolgy(appTerminology, appTerminology?.brands)) appTerminology?.brands else "Brands"
                catalogue = if (checkTerminolgy(appTerminology, appTerminology?.catalogue)) appTerminology?.catalogue else "Catalogue"
                otherTab = if (checkTerminolgy(appTerminology, appTerminology?.others_tab)) appTerminology?.others_tab else "Others"
                wishlist = if (checkTerminolgy(appTerminology, appTerminology?.wishlist)) appTerminology?.wishlist else "Wishlist"

            }
        }
    }

    private fun checkTerminolgy(appTerminology: AppTerminology?, naming: String?): Boolean {
        return appTerminology != null && naming?.isNotEmpty() == true
    }

    init {
        changeNaming(type, terminology, languageId)
    }
}