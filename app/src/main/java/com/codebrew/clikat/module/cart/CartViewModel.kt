package com.codebrew.clikat.module.cart

import android.util.Log
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocationModel
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.api.distance_matrix.DistanceMatrix
import com.codebrew.clikat.data.model.others.AgentCustomParam
import com.codebrew.clikat.data.model.others.CartReviewParam
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.ImageListModel
import com.codebrew.clikat.data.network.ApiMsgResponse
import com.codebrew.clikat.data.network.ApiResponse
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.CheckPromoCodeParam
import com.codebrew.clikat.modal.other.PlaceOrderInput
import com.codebrew.clikat.modal.other.PromoCodeModel
import com.codebrew.clikat.module.cart.model.Data
import com.codebrew.clikat.preferences.DataNames
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
//import com.sun.org.apache.xalan.internal.lib.ExsltCommon.objectType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CartViewModel(dataManager: DataManager) : BaseViewModel<CartNavigator>(dataManager) {


    val imageLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val isCartList = ObservableInt(0)

    fun refreshCart(param: CartReviewParam?) {
        setIsLoading(true)
        dataManager.checkProductList(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.refreshCartResponse(it) }, { this.handleError(it, "1") })?.let {
                    compositeDisposable.add(it)
                }
    }

    fun createGroup(hashMap: HashMap<String, String>) {
        setIsLoading(true)
        dataManager.createGroup(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.createGroupResponse(it) }, { this.handleError(it, "1") })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun createGroupResponse(it: GroupResponseData?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onGroupResponse(it.data)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }

    fun uploadImage(image: String) {
        setIsLoading(true)

        val file = File(image)
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val partImage = MultipartBody.Part.createFormData("file", file.name, requestBody)

        compositeDisposable.add(dataManager.uploadFile(partImage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.imageResponse(it) }, { this.handleError(it, "2") })
        )
    }


    private fun refreshCartResponse(it: ApiMsgResponse<CartData>?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {

                if (it.data is CartData) {
                    Log.e("table_data", it.data.is_table_enabled.toString())
                    navigator.onRefreshCart(it.data)
                }
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                if (it?.msg is String) {

                    it.msg.let { it1 ->
                        navigator.onErrorOccur(it1)
                    }

                    navigator.onRefreshCartError()

                } else {
                    navigator.onRefreshCartError()
                }
            }
        }
    }

    fun getSaddedPaymentUrl(email: String, name: String, amount: String) {
        setIsLoading(true)
        dataManager.getSaddedPaymentUrl(email, name, amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.paymentSadedResponse(it) },
                        { this.handleError(it, "3") })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun paymentSadedResponse(it: AddCardResponse?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.getSaddedPaymentSuccess(it.data)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }

    fun getMyFatoorahPaymentUrl(currency: String, amount: String) {
        setIsLoading(true)
        dataManager.getMyFatoorahPaymentUrl(currency, amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.paymentMyFatoorahResponse(it) },
                        { this.handleError(it, "4") })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun paymentMyFatoorahResponse(it: AddCardResponse?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.getMyFatoorahPaymentSuccess(it.data)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }

    fun referralAmount() {
        setIsLoading(true)
        dataManager.getReferralAmount()
                .observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.referralResponse(it) }, { this.handleError(it, "5") })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun referralResponse(it: ApiResponse<ReferalAmt>?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onReferralAmt(it.data?.referalAmount ?: 0.0f)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.msg?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    fun validatePromo(param: CheckPromoCodeParam?) {
        setIsLoading(true)
        dataManager.checkPromo(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.promoResponse(it) }, { this.handleError(it, "6") })?.let {
                    compositeDisposable.add(it)
                }
    }

    fun getDistance(key: String, source: LatLng, dest: LatLng) {
        // setIsLoading(true)

        val sourcelatlng = "${source.latitude},${source.longitude}"
        val destlatlng = "${dest.latitude},${dest.longitude}"

        CommonUtils.changebaseUrl(dataManager.getRetrofitUtl(), BuildConfig.DISTANCE_URL)

        compositeDisposable.add(dataManager.getDistance(key, sourcelatlng, destlatlng)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.distanceResponse(it) }, { this.handleError(it, "7") })
        )
    }

    private fun distanceResponse(it: DistanceMatrix?) {
        //  setIsLoading(false)

        CommonUtils.changebaseUrl(dataManager.getRetrofitUtl(), BuildConfig.BASE_URL)

        if (it?.status == "OK") {
            if (it.rows?.isNotEmpty() == true) {
                navigator.onCalculateDistance(it.rows[0]?.elements?.get(0)?.distance?.value)
            }
        } else {
            navigator.onCalculateDistance(0)
        }
    }

    private fun imageResponse(it: ApiResponse<Any>?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                imageLiveData.value = it.data.toString()
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.msg?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    private fun promoResponse(it: PromoCodeModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onValidatePromo(it.data)
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    fun generateOrder(vat_price: String?=null,vat_percantage:String?=null,appUtils: AppUtils, table_name: String, mDeliveryType: Int?, mPaymentOption: Int, mAgentParam: AgentCustomParam?,
                      payToken: String, uniqueId: String, redeemedAmt: Double, imageList: MutableList<ImageListModel>, instruction: String, mTipCharges: Float,
                      restServiceTax: Double, mQuestionList: List<QuestionList>, questionAddonPrice: Float, appType: Int, mSelectedPayment: CustomPayModel?, have_pet: Int, cleaner_in: Int,
                      parking_instructions: String, area_to_focus: String, paymentConfirm: Boolean, isDonate: Boolean,
                      ShowRestaurantPersonalAddress: String,
                      location: String?,group_id:String?) {


        val placeOrderInput = PlaceOrderInput()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        placeOrderInput.accessToken = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()

        if (dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java) != null) {
            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
            placeOrderInput.promoCode = promoData?.promoCode
            placeOrderInput.promoId = promoData?.id ?: 0
            placeOrderInput.discountAmount = promoData?.discountPrice
                    ?: 0.0f
        }


        placeOrderInput.have_pet = have_pet
        placeOrderInput.cleaner_in = cleaner_in
        placeOrderInput.parking_instructions = parking_instructions
        placeOrderInput.area_to_focus = area_to_focus

        placeOrderInput.self_pickup = mDeliveryType!!//if (mDeliveryType == 0 || mDeliveryType == 2) 0 else 2

        placeOrderInput.offset = SimpleDateFormat("ZZZZZ", Locale.getDefault()).format(System.currentTimeMillis())
        placeOrderInput.cartId = dataManager.getKeyValue(DataNames.CART_ID, PrefenceConstants.TYPE_STRING).toString()

        if (paymentConfirm) {
            placeOrderInput.payment_after_confirmation = 1
        }

        placeOrderInput.donate_to_someone = if (isDonate) 1 else 0
if(vat_percantage!=null)
{
    placeOrderInput.vat_percentage = listOf(vat_percantage ?: "0")
}
        if(vat_price!=null)
        {
            placeOrderInput.vat_value = listOf(vat_price ?: "0")
        }

        if (mPaymentOption == DataNames.PAYMENT_CARD && uniqueId.isNotEmpty()) {

            /* if(mSelectedPayment?.customerId?.isNotEmpty()==true && mSelectedPayment.cardId?.isNotEmpty()==true)
             {*/
            placeOrderInput.customer_payment_id = mSelectedPayment?.customerId
            placeOrderInput.card_id = mSelectedPayment?.cardId
            /*   }else
               {*/
            placeOrderInput.gateway_unique_id = uniqueId
            placeOrderInput.payment_token = payToken
            // }
        }

        placeOrderInput.pres_description = instruction

        if (redeemedAmt > 0) {
            placeOrderInput.use_refferal = 1
        }

        if (mQuestionList.isNotEmpty()) {
            placeOrderInput.questions = mQuestionList
            placeOrderInput.addOn = questionAddonPrice
        }
placeOrderInput.table_name=table_name
        placeOrderInput.type = appType
        placeOrderInput.paymentType = mPaymentOption
        placeOrderInput.languageId = dataManager.getLangCode().toInt()
placeOrderInput.group_id=group_id
        val calendar = Calendar.getInstance()
        placeOrderInput.order_day = appUtils.getDayId(calendar.get(Calendar.DAY_OF_WEEK)) ?: ""
        placeOrderInput.order_time = appUtils.convertDateOneToAnother(calendar.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "HH:mm:ss")
                ?: ""

        if (mAgentParam?.agentData != null) {

            placeOrderInput.agentIds = listOf(mAgentParam.agentData?.id ?: 0)
            placeOrderInput.date_time = mAgentParam.serviceDate + " " + mAgentParam.serviceTime
            placeOrderInput.duration = mAgentParam.duration ?: 0
        } else {
            placeOrderInput.booking_date_time = dateFormat.format(calendar.time)
        }


        if (mTipCharges > 0) {
            placeOrderInput.tip_agent = mTipCharges
        }

        if (restServiceTax > 0) {
            placeOrderInput.user_service_charge = restServiceTax
        }

        if (ShowRestaurantPersonalAddress == "1")
            placeOrderInput.restaurantFloor = location


        if (!imageList.isNullOrEmpty()) {

            imageList.forEachIndexed { index, imageListModel ->

                when (index) {
                    0 -> placeOrderInput.pres_image1 = imageListModel.image ?: ""
                    1 -> placeOrderInput.pres_image2 = imageListModel.image ?: ""
                    2 -> placeOrderInput.pres_image3 = imageListModel.image ?: ""
                    3 -> placeOrderInput.pres_image4 = imageListModel.image ?: ""
                    4 -> placeOrderInput.pres_image5 = imageListModel.image ?: ""
                }
            }
        }

        setIsLoading(true)
        dataManager.generateOrder(placeOrderInput)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateResponse(it) }, { this.handleError(it, "8") })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun validateResponse(it: ApiResponse<Any>) {
        setIsLoading(false)
        if (it.status == NetworkConstants.SUCCESS) {

            if (it.data is ArrayList<*>) {
                navigator.onOrderPlaced(it.data as ArrayList<Int>)
            }else{
                val responseData = Gson().fromJson<Data>(
                        Gson().toJsonTree(it.data),
                        Data::class.java)

                // it.data2!!.product_name
               navigator.onOutOfStock(responseData.product_name.toString())
                Log.e("product", responseData.product_name.toString())
            }
        } else if (it.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it.msg?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    fun addAddress(param: HashMap<String, String>) {

        param["name"] = "supplierBranchId"

        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        compositeDisposable.add(dataManager.addAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateAddAdrs(it) }, { this.handleError(it, "9") })
        )
    }

    private fun validateAddAdrs(it: AddAddressModel?) {
        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onAddAddress(it.data)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }

    fun updateCartInfo(cartList: MutableList<CartInfo>?, appUtils: AppUtils, mDeliveryType: Int?, mDeliveryCharge: Float, netTotal: Double,
                       adminCharges: Float,
                       deliveryMax: Int?,
                       mQuestionList: List<QuestionList>,
                       questionAddonPrice: Float,
                       ShowRestaurantPersonalAddress: String,
                       addressId: Int) {


        val calendar = Calendar.getInstance()

        val hashMap = hashMapOf("accessToken" to dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString(),
                "cartId" to dataManager.getKeyValue(DataNames.CART_ID, PrefenceConstants.TYPE_STRING).toString(),
                "deliveryType" to DataNames.DELIVERY_TYPE_STANDARD.toString(),
                "netAmount" to netTotal.toString(),
                "currencyId" to "1",
                "languageId" to dataManager.getLangCode(),
                "handlingAdmin" to adminCharges.toString(),
                "handlingSupplier" to cartList?.maxBy {
                    it.handlingSupplier
                }?.handlingSupplier.toString(),
                "order_day" to appUtils.getDayId(calendar.get(Calendar.DAY_OF_WEEK)),
                "order_time" to appUtils.convertDateOneToAnother(calendar.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "HH:mm:ss"))
        if (mDeliveryType == FoodAppType.Pickup.foodType) {
            hashMap["deliveryCharges"] = "0.0"
            hashMap["deliveryId"] = "0"
        } else {

            if (ShowRestaurantPersonalAddress != "1") {
                hashMap["deliveryId"] = dataManager.getKeyValue(DataNames.PICKUP_ID, PrefenceConstants.TYPE_STRING).toString()
            } else {
                hashMap["deliveryId"] = addressId.toString()
            }


            hashMap["deliveryCharges"] = mDeliveryCharge.toString()
        }

        if (mQuestionList.isNotEmpty()) {
            hashMap["addOn"] = questionAddonPrice.toString()
            hashMap["questions"] = Gson().toJson(mQuestionList)
        }

        calendar.add(Calendar.MINUTE, deliveryMax ?: 0)
        hashMap["deliveryDate"] = appUtils.convertDateOneToAnother(calendar.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "yyyy-MM-dd")
                ?: ""
        hashMap["deliveryTime"] = appUtils.convertDateOneToAnother(calendar.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "HH:mm:ss")
                ?: ""
        setIsLoading(true)

        compositeDisposable.add(dataManager.updateCartInfo(hashMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.updateCart(it) }, { this.handleError(it, "10") })
        )
    }


    private fun updateCart(it: ExampleCommon?) {

        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onUpdateCart()
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    fun addCart(cartList: MutableList<CartInfoServer>, appUtils: AppUtils,
                questionAddonPrice: Float, mQuestionList: List<QuestionList>) {
        val cartInfoServerArray = CartInfoServerArray()
        val addonList = mutableListOf<ProductAddon?>()
        cartList.filter { !it.add_ons.isNullOrEmpty() }.map { it.add_ons }.mapIndexed { pos, list ->

            list?.map {
                it?.serial_number = pos.inc()
            }
            list?.let { addonList.addAll(it) }
        }
        cartInfoServerArray.addons = addonList
        cartInfoServerArray.variants = cartList.flatMap { it.variants ?: mutableListOf() }
        //remove duplicate value & sum count if contains addons of same product
        cartList.mapIndexed { index, cartInfoServer ->

            cartInfoServer.price = cartInfoServer.fixed_price
            cartInfoServer.variants = null
        }
        val product = cartList.distinctBy {
            it.productId
        }
        cartInfoServerArray.productList = product
        //------------------------------------------------------------------//
        cartInfoServerArray.accessToken = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        cartInfoServerArray.remarks = "0"
        cartInfoServerArray.cartId = "0"
        cartInfoServerArray.supplierBranchId = cartList.get(0).supplier_branch_id ?: 0

        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault()
        cartInfoServerArray.order_day = appUtils.getDayId(calendar.get(Calendar.DAY_OF_WEEK)) ?: ""
        cartInfoServerArray.order_time = android.text.format.DateFormat.format("HH:mm:ss", calendar.time).toString()

        if (mQuestionList.isNotEmpty()) {
            cartInfoServerArray.questions = mQuestionList
            cartInfoServerArray.questionAddonPrice = questionAddonPrice
        }


        setIsLoading(true)
        CommonUtils.changebaseUrl(dataManager.getRetrofitUtl(), BuildConfig.BASE_URL)

        dataManager.getAddToCart(cartInfoServerArray)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.addCartInfo(it) }, { this.handleError(it, "11") })?.let {
                    compositeDisposable.add(it)
                }
    }


    private fun addCartInfo(it: AddtoCartModel?) {

        setIsLoading(false)

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                dataManager.setkeyValue(DataNames.CART_ID, it.data?.cartId ?: "")
                navigator.onCartAdded(it.data)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }

    }


    fun getAddressList(supplierBranch: Int) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()
        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        param["supplierBranchId"] = supplierBranch.toString()

        compositeDisposable.add(dataManager.getAllAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.addressResponse(it) }, { this.handleError(it, "12") })
        )
    }

    fun getLocationsList(supplierBranch: Int) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()
        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        param["supplierBranchId"] = supplierBranch.toString()

        compositeDisposable.add(dataManager.getAllSupplierLocations(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.allLocationsResponse(it) }, { this.handleError(it, "13") })
        )
    }

    private fun addressResponse(it: CustomerAddressModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onAddress(it.data)
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    private fun allLocationsResponse(it: SupplierLocationModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onSupplierLocations(it.data)
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    fun setIsCartList(listCount: Int) {
        this.isCartList.set(listCount)
    }


    private fun handleError(e: Throwable, s: String) {
        setIsLoading(false)
        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
                navigator.onErrorOccur(it)
            }
        }
    }
}
