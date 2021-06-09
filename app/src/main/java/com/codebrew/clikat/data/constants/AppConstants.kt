package com.codebrew.clikat.data.constants

import com.codebrew.clikat.data.DeliveryType
import com.codebrew.clikat.data.SearchType

interface AppConstants {
    companion object {
        const val LIMIT=5
        const val CameraGalleryPicker = 123
        const val CameraPicker = 124
        const val GalleyPicker = 125
        const val REQUEST_CODE_LOCATION = 1002
        const val REQUEST_CALL = 323
        const val CANCELREQUEST = 5
        const val MULTI_CAM_STOR = 1254
        const val PIC_CROP = 457
        const val PLACE_PICKER_REQUEST = 482
        const val PERMISSION_REQUEST=786
        const val RC_LOCATION_PERM=787
        const val REQUEST_PRODUCT_FAV = 789
        const val REQUEST_ADDRESS_ADD = 790
        const val REQUEST_USER_PROFILE = 791
        const val REQUEST_WISH_PROD = 792
        const val REQUEST_WISH_LIST = 793
        const val REQUEST_AGENT_DETAIL = 794
        const val REQUEST_CART_LOGIN_BOOKING = 795
        const val REQUEST_PRODUCT_FAVOURITE = 796
        const val REQUEST_PAYMENT_DEBIT_CARD = 798
        const val REQUEST_PAYMENT_OPTION = 797
        const val REQUEST_CARD_ADD = 799
        const val REQUEST_SQUARE_PAY = 800
        const val REQUEST_SQUARE_LOGIN = 801
        const val REQUEST_PRES_UPLOAD = 802

        var isChatOpen=false
        var currentOrderId="0"

        //Filter Event constant
        const val CATEGORY_SELECT="category_select"
        const val SUBCATEGORY_ADD="subcategory_add"
        const val SUBCATEGORY_REMOVE="subcategory_remove"
        const val SUBCATEGORY_DETAIL="subcategory_detail"
        const val SUBCATEGORY_BACKPRESS="backpressed"
        const val SUBCATEGORY_CATEGORY="subcat_cat"
        const val BANNER_PROMO_BEAN ="bannerPromoBean"


        const val CANCEL_EVENT="cancel"
        const val NOTIFICATION_EVENT="orderType"

        var DELIVERY_OPTIONS= DeliveryType.DeliveryOrder.type
        var SEARCH_OPTION= SearchType.TYPE_PROD.type
        var CURRENCY_SYMBOL="kr"
        var APP_SUB_TYPE=-1
        var APP_SAVED_SUB_TYPE=-1

    }
}