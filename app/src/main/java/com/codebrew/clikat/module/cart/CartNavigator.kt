package com.codebrew.clikat.module.cart

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.model.SupplierLocationBean
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.CartData
import com.codebrew.clikat.data.model.api.DataBean
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.PromoCodeModel
import java.util.ArrayList

interface CartNavigator : BaseInterface {

    fun onUpdateCart()

    fun onRefreshCartError()

    fun onCartAdded(cartdata: AddtoCartModel.CartdataBean?)

    fun onOrderPlaced(data: ArrayList<Int>)
fun onOutOfStock(product_name:String)
    fun onValidatePromo(data: PromoCodeModel.DataBean)

    fun onRefreshCart(mCartData: CartData?)

    fun onCalculateDistance(value: Int?)

    fun onAddress(data: DataBean?)

    fun onSupplierLocations(data: SupplierLocationBean?)

    fun onReferralAmt(value: Float?)
    fun getSaddedPaymentSuccess(data: AddCardResponseData?)
    fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?)
    fun onAddAddress(data: AddressBean?)
    fun onGroupResponse(data: GroupData?)
}
