package com.codebrew.clikat.modal.other

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class SettingModel {

    var status = 0
    var message: String? = null
    var data: DataBean? = null

    class DataBean {
        var dialog_token: String? = null
        val supplier_branch_id = 0
        val supplier_id = 0
        val latitude: Double? = null
        val longitude: Double? = null
        val supplierName: String? = null
        val self_pickup: String? = null
        val key_value: SettingData? = null
        var screenFlow: List<ScreenFlowBean>? = null
        var bookingFlow: List<BookingFlowBean>? = null
        var termsAndConditions: List<TermCondition>? = null
        var featureData: List<FeatureData>? = null
        var addRestaurantFloor: String? = null

        class ScreenFlowBean {
            var is_multiple_branch = 0
            var is_single_vendor = 0
            var app_type = 0
            var sub_app_type = 0
        }

        class BookingFlowBean {
            var vendor_status = 0
            var cart_flow = 0
            var is_scheduled = 0
            var schedule_time = 0
            var admin_order_priority = 0
            var is_pickup_order = 0
            var interval = 0
            var booking_track_status = 0
        }

        class TermCondition(
                val termsAndConditions: Int?,
                val privacyPolicy: Int?,
                val language_id: Int)

        @Parcelize
        class SettingData(
                val agent_android_app_url: String? = null,
                val agent_ios_app_url: String? = null,
                val android_app_url: String? = null,
                val app_color: String? = null,
                val banner_four: String? = null,
                val banner_four_thumb: String? = null,
                val banner_one: String? = null,
                val banner_one_thumb: String? = null,
                val banner_three: String? = null,
                val banner_three_thumb: String? = null,
                val banner_thumb_url: String? = null,
                val banner_two: String? = null,
                val banner_two_thumb: String? = null,
                val login_template: String? = null,
                val banner_url: String? = null,
                val domain_name: String? = null,
                val element_color: String? = null,
                val font_family: String? = null,
                val ios_app_url: String? = null,
                val logo_background: String? = null,
                val logo_thumb_url: String? = null,
                val logo_url: String? = null,
                val theme_color: String? = null,
                val header_color: String? = null,
                val header_text_color: String? = null,
                var terminology: String? = null,
                var payment_after_confirmation: String? = null,
                val payment_method: String? = null,
                val referral_feature: String? = null,
                val chat_enable: String? = null,
                val hide_pickup_banner: String? = null,
                val hide_ratings: String? = null,
                val hide_restaurant_image: String? = null,
                val show_app_logo: String? = null,
                val showRestaurantName: String? = null,
                val isTedTheme: String? = null,
                val showTipViewWithPaymentAfterConfirm: String? = null,
                val showTrackOrder: String? = null,
                val removeDeliveryCharges: String? = null,
                val showChoosePaymentOption: String? = null,
                val showCategoryList: String? = null,
                val showServiceChargeEvenIfZero: String? = null,
                val showSpaceWithCurrencyInTip: String? = null,

                val referral_receive_price: String? = null,
                val referral_given_price: String? = null,
                val stripe_secret_key: String? = null,
                val stripe_publish_key: String? = null,
                val conekta_api_key: String? = null,
                val conekta_publish_key: String? = null,
                val razorpay_key_id: String? = null,
                val razorpay_key_secret: String? = null,
                val email: String? = null,
                val app_banner_width: String? = null,
                val pickup_url_one: String? = null,
                val pickup_url_two: String? = null,
                val pickup_url_three: String? = null,
                val login_icon_url: String? = null,

                //order_instructions
                //0: disabled
                //1: enabled
                //
                //cart_image_upload
                //0: disabled
                //1: enabled
                var cart_image_upload: String? = null,
                var order_instructions: String? = null,
                var disable_tax: String? = null,
                val cutom_country_code: String? = null,
                val waiting_charges: String? = null,
                val delivery_charge_type: String? = null,
                val user_service_fee: String? = null,
                var secondary_language: String? = null,
                var app_selected_template: String? = null,
                @SerializedName("user_register_flow", alternate = ["user_register_flow "])
                var user_register_flow: String? = null,

                val app_sharing_message: String? = null,
                val brandImage_url: String? = null,
                val bypass_otp: String? = null,
                val card_gateway: String? = null,
                val cybersource_merchant_id: String? = null,
                val cybersource_merchant_key_id: String? = null,
                val cybersource_merchant_secret_key: String? = null,
                val delivery_url_one: String? = null,
                val delivery_url_three: String? = null,
                val delivery_url_two: String? = null,
                val description_sections: String? = null,
                val empty_cart: String? = null,
                val favicon_url: String? = null,
                val gateway_name: String? = null,
                val gift_card: String? = null,
                val google_map_key: String? = null,
                val is_return_request: String? = null,
                val is_supplier_detail: String? = null,
                val is_user_type: String? = null,
                val language_type: String? = null,
                val order_loader: String? = null,
                val order_request: String? = null,
                val paypal_client_key: String? = null,
                val paypal_secret_key: String? = null,
                val phone_number: String? = null,
                val pickup_url: String? = null,
                val product_pdf_upload: String,
                val ride_base_url: String? = null,
                val ride_registeration: String? = null,
                val selected_template: String? = null,
                val square_publish_key: String? = null,
                val square_token: String? = null,
                val user_location: String? = null,
                val user_type_price: String? = null,
                val venmo_braintree_merchant_id: String? = null,
                val venmo_braintree_private_key: String? = null,
                val venmo_braintree_public_key: String? = null,
                //tidycoop
                //1 to show
                //0 to hide
                val extra_instructions: String? = null,
                //cannadash
                //1 to show
                //0 to hide
                val extra_functionality: String? = null,
                val things_to_remember: String? = null,
                val dynamic_home_section: String? = null,
                var min_order: Double? = null,
                val disable_order_cancel: String? = null,
                var base_delivery_charges: Double? = null,
                val search_by: String? = null,
                val is_product_wishlist: String? = null,
                val is_supplier_wishlist: String? = null,
                val show_prescription_requests: String? = null,
                val show_donate_popup: String? = null,
                val commission_delivery_wise: String? = null,
                val product_detail: String? = null,
                val is_tax_geofence: String? = null,
                val show_home_screen_theme: String? = null,
                val can_agent_edit: String? = null,
                val paystack_secret_key: String? = null,
                val paystack_publish_key: String? = null,
                val user_type_check: String? = null,
                val is_agent_rating: String? = null,
                val is_supplier_rating: String? = null,
                val is_product_rating: String? = null,
                val sadded_vendor_id: String? = null,
                val sadded_branch_id: String? = null,
                val sadded_termianl_id: String? = null,
                val sadded_api_key: String? = null,
                val addRestaurantFloor: String? = null
        ) : Parcelable


        @Keep
        data class Terminology(
                val english: AppTerminology?,
                val other: AppTerminology?
        )

        @Keep
        data class AppTerminology(
                val agent: String?,
                val agents: String?,
                val brand: String?,
                val brands: String?,
                val categories: String?,
                val category: String?,
                val order: String?,
                val tipText: String?,
                val orders: String?,
                val product: String?,
                val products: String?,
                val status: Status,
                val supplier: String?,
                val suppliers: String?,
                val catalogue: String?,
                val others_tab: String?,
                val choose_payment: String,
                val order_now: String,
                val wishlist: String,
                val subCategories: String,
                val payment: String,
                val cash: String,
                val total_revenue: String,
                val prescription_value: String,
                val instruction: String,
                val promotions: String,
                val promo_code: String,
                val product_file_upload: String,
                val delivery_timing: String
        )


        @Keep
        data class AppIcon(
                val app: String?,
                val web: String?
        )


        @Keep
        data class Status(
                @SerializedName("0")
                val PENDING: String?,
                @SerializedName("1")
                val ACCEPTED: String?,
                @SerializedName("2")
                val REJECTED: String?,
                @SerializedName("3")
                val ON_THE_WAY: String?,
                @SerializedName("4")
                val NEAR_YOU: String?,
                @SerializedName("5")
                val DELIVERED: String?,
                @SerializedName("6")
                val RATE_GIVEN: String?,
                @SerializedName("7")
                val TRACK_ORDER: String?,
                @SerializedName("8")
                val CUSTOMER_CANCEL: String?,
                @SerializedName("9")
                val SCHEDULED: String?,
                @SerializedName("10")
                val SHIPPED: String?,
                @SerializedName("11", alternate = ["2.5"])
                var PACKED: String?
        )


        @Keep
        @Parcelize
        data class FeatureData(
                val customer_feature_id: Int? = null,
                val id: Int? = null,
                val is_active: Int? = null,
                val key_value: List<KeyValue?>? = null,
                val key_value_front: List<KeyValueFront?>? = null,
                val name: String? = null,
                val type_id: Int? = null,
                val type_name: String? = null
        ) : Parcelable

        @Keep
        @Parcelize
        data class KeyValue(
                val for_front: Int? = null,
                val key: String? = null,
                val value: String? = null
        ) : Parcelable

        @Keep
        @Parcelize
        data class KeyValueFront(
                val created_at: String? = null,
                val customer_feature_id: Int? = null,
                val for_front: Int? = null,
                val id: Int? = null,
                val key: String? = null,
                val updated_at: String? = null,
                val value: String? = null
        ) : Parcelable

    }
}