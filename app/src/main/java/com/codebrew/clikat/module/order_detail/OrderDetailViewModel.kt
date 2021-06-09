package com.codebrew.clikat.module.order_detail

import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.api.orderDetail.OrderDetailModel
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.model.others.ReturnProductModel
import com.codebrew.clikat.data.network.ApiResponse
import com.codebrew.clikat.modal.CartInfoServerArray
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.OrderDetailParam
import com.codebrew.clikat.modal.other.ProductDataBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OrderDetailViewModel(dataManager: DataManager) : BaseViewModel<OrderDetailNavigator>(dataManager) {

    val orderDetailLiveData by lazy { MutableLiveData<List<OrderHistory>>() }
    val returnLiveData by lazy { MutableLiveData<ProductDataBean>() }

    fun getOrderDetail(orderIds: List<Int>) {
        setIsLoading(true)

        val orderInput = OrderDetailParam(
                dataManager.getLangCode(),
                dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString(), orderIds)

        dataManager.orderDetails(orderInput)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    fun makePayment(param: MakePaymentInput)
    {
        setIsLoading(true)
        dataManager.makePayment(param)
                .observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.makePaymentResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    fun getPaymentGeofence(latitude:Double,longitude:Double)
    {

        val param = hashMapOf("lat" to latitude.toString(),
        "long" to longitude.toString())

       // setIsLoading(true)
        dataManager.geofenceGateway(param)
                .observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.geofencePayment(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun geofencePayment(it: ApiResponse<GeofenceData>?) {

        //setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onGeofencePayment(it.data?.gateways)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.msg.let { it1 -> navigator.onErrorOccur(it1?:"") }
            }
        }
    }


    fun remainingPayment(param: MakePaymentInput)
    {
        setIsLoading(true)
        dataManager.remainingPayment(param)
                .observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.remainingResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    fun addCart(cartInfo: CartInfoServerArray) {
        setIsLoading(true)

        cartInfo.accessToken = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        cartInfo.cartId="0"
        cartInfo.remarks = "0"

        dataManager.getAddToCart(cartInfo)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.addCartInfo(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }

    fun cancelOrder(orderId: String) {
        setIsLoading(true)

        val hashMap=hashMapOf("orderId" to orderId,
                "languageId" to dataManager.getLangCode(),
                "accessToken" to dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString())

        dataManager.cancelOrder(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.cancelResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    fun apiReturnProduct(item: ProductDataBean?, reason: String) {

        val body = ReturnProductModel(order_price_id = item?.order_price_id.toString(),
                product_id = item?.product_id.toString(),
                reason = reason)

        setIsLoading(true)
        dataManager.returnProduct(body)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateReturnResponse(it, item) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }


    private fun makePaymentResponse(it: SuccessModel) {
        setIsLoading(false)
        when (it.statusCode) {
            NetworkConstants.SUCCESS -> {
                navigator.onCompletePayment()
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it.message.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }

    fun getSaddedPaymentUrl(email:String,name:String,amount:String) {
        setIsLoading(true)
        dataManager.getSaddedPaymentUrl(email,name,amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.paymentSadedResponse(it) }, { this.handleError(it) })?.let {
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

    fun getMyFatoorahPaymentUrl(currency:String,amount:String) {
        setIsLoading(true)
        dataManager.getMyFatoorahPaymentUrl(currency,amount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.paymentMyFatoorahResponse(it) },
                        { this.handleError(it) })?.let {
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

    private fun remainingResponse(it: SuccessModel) {
        setIsLoading(false)
        when (it.statusCode) {
            NetworkConstants.SUCCESS -> {
                navigator.onCompletePayment()
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it.message.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    private fun cancelResponse(it: ExampleCommon?) {

        setIsLoading(false)

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onCancelOrder()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun addCartInfo(it: AddtoCartModel?) {

        setIsLoading(false)

        if (it?.status == NetworkConstants.SUCCESS) {
        //    dataManager.setkeyValue(DataNames.CART_ID,it.cartdata.cartId)
            navigator.onCartAdded(it.data)
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }

    }


    private fun validateResponse(it: OrderDetailModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            if (it.data?.orderHistory?.isNotEmpty()==true) {
                orderDetailLiveData.value = it.data.orderHistory
            }
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    private fun validateReturnResponse(it: OrderDetailModel?, item: ProductDataBean?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            returnLiveData.value = item
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    private fun handleError(e: Throwable) {
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
