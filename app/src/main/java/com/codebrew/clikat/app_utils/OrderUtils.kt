package com.codebrew.clikat.app_utils

import android.content.Context
import com.codebrew.clikat.data.OrderPayment
import com.codebrew.clikat.data.RequestsStatus
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.preferences.DataNames
import javax.inject.Inject

class OrderUtils @Inject constructor(private val mContext: Context) {

    @Inject
    lateinit var mPreferenceHelper: PreferenceHelper


    fun checkPaymtFlow(orderHistory: OrderHistory): Int {


        with(orderHistory)
        {


            ((payment_status == 0 && created_by ?: 0 > 0 && status == RequestsStatus.Approved.status) ||
                    (remaining_amount ?: 0.0f > 0.0f && payment_type == 1 && is_edit == 1) ||
                    (payment_status == 0 && payment_type == DataNames.PAYMENT_AFTER_CONFIRM && payment_after_confirmation == 1 && status == RequestsStatus.Approved.status))


            if (payment_status == 0 && payment_type == DataNames.PAYMENT_AFTER_CONFIRM && payment_after_confirmation == 1 && status == RequestsStatus.Approved.status) {
                return OrderPayment.PaymentAfterConfirm.payment

            }


            if (payment_status == 0 && created_by ?: 0 > 0 && status == RequestsStatus.Approved.status) {
                return OrderPayment.ReceiptOrder.payment
            }

            if (remaining_amount ?: 0.0f > 0.0f && payment_type == 1 && is_edit == 1) {
                return OrderPayment.EditOrder.payment
            }

        }

        return 0
    }


    fun checkOrderListFlow(orderHistory: OrderHistory): Boolean {

        with(orderHistory)
        {

            return  ((payment_status == 0 && created_by ?: 0 > 0 && status == RequestsStatus.Approved.status) ||
                    (remaining_amount ?: 0.0f > 0.0f && payment_type == 1 && is_edit == 1) ||
                    (payment_status == 0 && payment_type == DataNames.PAYMENT_AFTER_CONFIRM && payment_after_confirmation == 1 && status == RequestsStatus.Approved.status))
        }
    }


}