package com.codebrew.clikat.module.order_detail

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.api.GeofenceItem
import com.codebrew.clikat.modal.other.AddtoCartModel

interface OrderDetailNavigator:BaseInterface {

    fun onCartAdded(cartdata: AddtoCartModel.CartdataBean?)
    fun onCancelOrder()

    fun onCompletePayment()
    fun onGeofencePayment(data: List<String>?)
    fun getSaddedPaymentSuccess(data: AddCardResponseData?)
    fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?)
}