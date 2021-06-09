package com.codebrew.clikat.app_utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.codebrew.clikat.R
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.CartInfo
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.facebook.FacebookSdk.getApplicationContext
import javax.inject.Inject


class CartUtils @Inject constructor(private val mContext: Context) {

    @Inject
    lateinit var mPreferenceHelper: PreferenceHelper

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    @Inject
    lateinit var appUtils: AppUtils


    var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    fun addItemToCart(mProduct: CartInfo?): CartInfo? {


        startClearCartService()

        if (screenFlowBean == null) {
            screenFlowBean = mPreferenceHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        }

        if (bookingFlowBean == null) {
            bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        }

        var quantity = mProduct?.quantity ?: 0

        quantity++
        if (mProduct?.priceType == 1) {

            var minHour: Int
            var maxHour = 0
            val totalDuration = mProduct.serviceDuration.times(quantity)

            mProduct.hourlyPrice.forEachIndexed { index, hourlyPrice ->

                minHour = hourlyPrice.min_hour ?: 0
                maxHour = hourlyPrice.max_hour ?: 0

                if (totalDuration in minHour.rangeTo(maxHour)) {
                    mProduct.price = hourlyPrice.price_per_hour ?: 0f
                }
            }

            if (totalDuration < maxHour) {
                mProduct.quantity = quantity
                mProduct.serviceDurationSum = totalDuration
                StaticFunction.updateCart(mContext, mProduct.productId, quantity, mProduct.price)
            } else {
                Toast.makeText(mContext, "Max Limit Reached", Toast.LENGTH_SHORT).show()
                return null
            }

        } else {

            // fixed price 1 for product 0 for service

            if (mProduct?.serviceType == 0) {
                mProduct.serviceDurationSum = bookingFlowBean?.interval?.times(quantity) ?: 0

                mProduct.quantity = quantity
                StaticFunction.updateCart(mContext, mProduct.productId, quantity, mProduct.price)
            } else {

                val actualQuantity = if (mProduct?.productAddonId ?: 0 > 0) {
                    appUtils.getCartList().cartInfos?.filter { it.productId == mProduct?.productId }?.sumBy { it.quantity }?.plus(1)
                            ?: 0
                } else {
                    quantity
                }

                val remaingProd = mProduct?.prodQuant?.minus(mProduct.purchasedQuant ?: 0) ?: 0

                if (actualQuantity <= remaingProd) {
                    mProduct?.quantity = quantity
                    StaticFunction.updateCart(mContext, mProduct?.productId, quantity, mProduct?.price
                            ?: 0.0f)
                } else {
                    Toast.makeText(mContext, R.string.maximum_limit_cart, Toast.LENGTH_SHORT).show()
                    return null
                }
            }
        }


        return mProduct
    }

    private fun startClearCartService() {

        cancelClearCartService()

        val myIntent = Intent(getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)


        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager[AlarmManager.RTC, System.currentTimeMillis() + 7200000] = pendingIntent

    }

    private fun cancelClearCartService() {

        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager!!.cancel(pendingIntent)

    }


    fun removeItemToCart(mProduct: CartInfo?): CartInfo {

        startClearCartService()

        if (bookingFlowBean == null) {
            bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        }


        if (mProduct?.quantity != 0) {
            var quantity = mProduct?.quantity ?: 0

            if (quantity > 0) {

                quantity--

                if (mProduct?.priceType == 1) {

                    var minHour: Int
                    var maxHour: Int
                    val totalDuration = mProduct.serviceDuration.times(quantity)

                    mProduct.hourlyPrice.forEachIndexed { index, hourlyPrice ->

                        minHour = hourlyPrice.min_hour ?: 0
                        maxHour = hourlyPrice.max_hour ?: 0

                        if ((totalDuration in minHour.rangeTo(maxHour))) {
                            mProduct.price = hourlyPrice.price_per_hour ?: 0f
                        }
                    }
                    mProduct.serviceDurationSum = totalDuration

                } else {
                    if (mProduct?.serviceType == 0) {
                        mProduct.serviceDurationSum = bookingFlowBean?.interval?.times(quantity)
                                ?: 0
                    }
                }

                mProduct?.quantity = quantity


                if (quantity == 0) {
                    StaticFunction.removeFromCart(mContext, mProduct?.productId, 0)
                } else {
                    StaticFunction.updateCart(mContext, mProduct?.productId, quantity, mProduct?.price
                            ?: 0.0f)
                }

            }
        }

        return mProduct!!
    }

}