package com.codebrew.clikat.app_utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.codebrew.clikat.R
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.facebook.FacebookSdk
import javax.inject.Inject

class ProdUtils @Inject constructor(private val mContext: Context) {

    @Inject
    lateinit var mPreferenceHelper: PreferenceHelper

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    @Inject
    lateinit var appUtils: AppUtils


    var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    fun addItemToCart(mProduct: ProductDataBean?): ProductDataBean? {

        startClearCartService()

        if (screenFlowBean == null) {
            screenFlowBean = mPreferenceHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        }

        if (bookingFlowBean == null) {
            bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        }

        if (mProduct?.selectQuestAns?.isNotEmpty() == true) {
            //   mProduct.prod_quantity = 0

            mProduct.selectQuestAns?.map { list1 ->
                list1.optionsList = list1.optionsList.filter { it.isChecked }
                list1.optionsList.map {
                    it.productPrice = mProduct.netPrice ?: 0.0f
                }
            }

            // mProduct.selectQuestAns=mQuestionList
        }


        if (mProduct?.prod_quantity == 0) {

            mProduct.prod_quantity = 1
            // mProduct?.fixed_price=mProduct?.netPrice.toString()
            mProduct.serviceDuration = if (mProduct.price_type == 1) mProduct.duration else bookingFlowBean?.interval
            appUtils.addProductDb(mContext, screenFlowBean?.app_type
                    ?: 0, mProduct)

        } else {
            var quantity = mProduct?.prod_quantity ?: 0

            quantity++
            if (mProduct?.price_type == 1) {

                var minHour: Int
                var maxHour = 0
                val totalDuration = mProduct.duration?.times(quantity)

                mProduct.hourly_price?.forEachIndexed { index, hourlyPrice ->

                    minHour = hourlyPrice.min_hour ?: 0
                    maxHour = hourlyPrice.max_hour ?: 0

                    if (totalDuration in minHour.rangeTo(maxHour)) {
                        if (mProduct.hourly_price?.get(0)?.discount_price?.isNaN() == false) {
                            mProduct.netPrice = mProduct.hourly_price?.get(0)?.discount_price
                            mProduct.netDiscount = mProduct.hourly_price?.get(0)?.price_per_hour
                        } else {
                            mProduct.netPrice = mProduct.hourly_price?.get(0)?.price_per_hour
                            mProduct.netDiscount = 0.0f
                        }
                    }
                }

                if (totalDuration ?: 0 < maxHour) {
                    mProduct.prod_quantity = quantity
                    // mProduct.serviceDuration = totalDuration
                    StaticFunction.updateCart(mContext, mProduct.product_id, quantity, mProduct.netPrice
                            ?: 0.0f)
                } else {
                    Toast.makeText(mContext, "Max Limit Reached", Toast.LENGTH_SHORT).show()
                    return null
                }

            } else {

                // fixed price 1 for product 0 for service
                if (mProduct?.is_product == 0) {
                    mProduct.prod_quantity = quantity
                    //mProduct.serviceDuration = bookingFlowBean?.interval?.times(quantity)
                    StaticFunction.updateCart(mContext, mProduct.product_id, quantity, mProduct.netPrice
                            ?: 0.0f)
                } else {
                    val remaingProd = mProduct?.quantity?.minus(mProduct.purchased_quantity ?: 0)
                            ?: 0

                    if (quantity <= remaingProd) {
                        mProduct?.prod_quantity = quantity
                        StaticFunction.updateCart(mContext, mProduct?.product_id, quantity, mProduct?.netPrice
                                ?: 0.0f)
                    } else {
                        Toast.makeText(mContext, R.string.maximum_limit_cart, Toast.LENGTH_SHORT).show()
                        return null
                    }
                }
            }
        }

        return mProduct
    }

    private fun startClearCartService() {

        cancelClearCartService()

        val myIntent = Intent(FacebookSdk.getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                FacebookSdk.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)


        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager[AlarmManager.RTC, System.currentTimeMillis() + 7200000] = pendingIntent

    }

    private fun cancelClearCartService() {

        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(FacebookSdk.getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                FacebookSdk.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager!!.cancel(pendingIntent)

    }

    fun removeItemToCart(mProduct: ProductDataBean?): ProductDataBean {

        startClearCartService()

        if (bookingFlowBean == null) {
            bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        }


        if (mProduct?.prod_quantity != 0) {
            var quantity = mProduct?.prod_quantity ?: 0

            if (quantity > 0) {

                quantity--

                if (mProduct?.price_type == 1) {

                    var minHour: Int
                    var maxHour: Int
                    val totalDuration = mProduct.duration?.times(quantity)

                    mProduct.hourly_price?.forEachIndexed { index, hourlyPrice ->

                        minHour = hourlyPrice.min_hour ?: 0
                        maxHour = hourlyPrice.max_hour ?: 0

                        if ((totalDuration in minHour.rangeTo(maxHour))) {
                            if (mProduct.hourly_price?.get(0)?.discount_price?.isNaN() == false) {
                                mProduct.netPrice = mProduct.hourly_price?.get(0)?.discount_price
                                mProduct.netDiscount = mProduct.hourly_price?.get(0)?.price_per_hour
                            } else {
                                mProduct.netPrice = mProduct.hourly_price?.get(0)?.price_per_hour
                                mProduct.netDiscount = 0.0f
                            }
                        }
                    }
                    // mProduct.serviceDuration = totalDuration

                } else {
                    if (mProduct?.is_product == 0) {
                        //   mProduct.serviceDuration = bookingFlowBean?.interval?.times(quantity)
                    }
                }

                mProduct?.prod_quantity = quantity


                if (quantity == 0) {
                    StaticFunction.removeFromCart(mContext, mProduct?.product_id, 0)
                } else {
                    StaticFunction.updateCart(mContext, mProduct?.product_id, quantity, mProduct?.netPrice
                            ?: 0.0f)
                }

            }
        }

        return mProduct!!
    }

    fun changeProductList(mProduct: ProductDataBean?): ProductDataBean {

        startClearCartService()

        if (bookingFlowBean == null) {
            bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        }

        mProduct?.prod_quantity = StaticFunction.getCartQuantity(mContext, mProduct?.product_id)

        if (mProduct?.price_type == 0) {
            mProduct.netPrice = if (mProduct.fixed_price?.toFloatOrNull() ?: 0.0f > 0) mProduct.fixed_price?.toFloatOrNull()
                    ?: 0.0f else 0f

            mProduct.netDiscount = mProduct.display_price?.toFloatOrNull() ?: 0.0f

            if (mProduct.is_product == 0) {
                mProduct.serviceDuration = bookingFlowBean?.interval
            }

        } else {
            val quant = mProduct?.prod_quantity
            mProduct?.serviceDuration = mProduct?.duration
            if (quant == 0) {

                if (mProduct.hourly_price != null && mProduct.hourly_price?.isNotEmpty() == true) {
                    if (mProduct.hourly_price?.get(0)?.discount_price?.isNaN() == false) {
                        mProduct.netPrice = mProduct.hourly_price?.get(0)?.discount_price
                        mProduct.netDiscount = mProduct.hourly_price?.get(0)?.price_per_hour
                    } else {
                        mProduct.netPrice = mProduct.hourly_price?.get(0)?.price_per_hour
                        mProduct.netDiscount = 0.0f
                    }
                }

            } else {
                if (mProduct?.price_type == 1) {

                    var minHour: Int
                    var maxHour = 0
                    val totalDuration = mProduct.duration?.times(quant ?: 0)

                    mProduct.hourly_price?.forEachIndexed { index, hourlyPrice ->

                        minHour = hourlyPrice.min_hour ?: 0
                        maxHour = hourlyPrice.max_hour ?: 0

                        if (totalDuration in minHour.rangeTo(maxHour)) {
                            if (mProduct.hourly_price?.get(0)?.discount_price?.isNaN() == false) {
                                mProduct.netPrice = mProduct.hourly_price?.get(0)?.discount_price
                                mProduct.netDiscount = mProduct.hourly_price?.get(0)?.price_per_hour
                            } else {
                                mProduct.netPrice = mProduct.hourly_price?.get(0)?.price_per_hour
                                mProduct.netDiscount = 0.0f
                            }
                        }
                    }

                    if (totalDuration ?: 0 < maxHour) {
                        mProduct.prod_quantity = quant
                        // mProduct.serviceDuration = totalDuration
                    }
                } else {
                    mProduct?.prod_quantity = quant

                    mProduct?.netPrice = if (mProduct?.fixed_price?.toFloatOrNull() ?: 0.0f > 0) mProduct?.fixed_price?.toFloatOrNull()
                            ?: 0.0f else 0f

                    mProduct?.netDiscount = mProduct?.display_price?.toFloatOrNull() ?: 0.0f
                    //  mProduct?.serviceDuration = bookingFlowBean?.interval?.times(quant?:0)
                }
            }
        }

        return mProduct!!
    }


}