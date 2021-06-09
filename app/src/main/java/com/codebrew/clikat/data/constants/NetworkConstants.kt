package com.codebrew.clikat.data.constants


interface NetworkConstants {

    companion object {
        const val SUCCESS = 200
        const val AUTHFAILED = 401

        const val AUTH_MSG = "AuthError"


        const val VERIFY_USER_CODE="/v1/common/agent/boot"

        const val WISHLIST = "/favourite_product"
        const val SUPPLIERS_WISH_LIST = "/get_my_favourite"
        const val SUPPLIER_DETAIL = "/supplier_details"
        const val ADD_TO_FAVOURITE_SUPL = "/add_to_favourite"
        const val UN_FAVOURITE_SUPL = "/un_favourite"
        const val GET_SETTING = "/getSettings"
        const val GET_ALL_COUNTRY = "/get_all_country"
        const val GET_ALL_CITY = "/get_all_city"
        const val GET_ALL_AREA = "/get_all_area"
        const val GET_COMPLETE_ORDER_STATUS = "/get_total_pending_schedule"
        const val GET_ALL_CATEGORY_NEW = "/get_all_category_new"
        const val GET_SUPPLIER_LIST = "/home/supplier_list"
        const val GET_ALL_OFFER_LIST="/get_all_offer_list"
        const val GET_ALL_CUSTOMER_ADRS="/get_all_customer_address"
        const val GET_ALL_SUPPLIER_LOCATIONS="/get_supplier_location"
        const val ADD_CUSTOMER_ADRS="/add_new_address"
        const val EDIT_CUSTOMER_ADRS="/edit_address"
        const val DELETE_CUSTOMER_ADRS="/delete_customer_address"
        const val PRODUCT_FILTERATION="/v1/product_filteration"
        const val CHAT_LISTING = "/getChat"
        const val REFERRAL_AMT = "/user/referralAmount"
        const val REFERRAL_LIST = "/user/myReferral"
        const val UPLOAD_DOC = "/user/order/addReceipt"

    }
}