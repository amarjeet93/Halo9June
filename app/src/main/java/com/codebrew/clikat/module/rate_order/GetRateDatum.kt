package com.codebrew.clikat.module.rate_order

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class GetRateDatum {
    @SerializedName("is_rated")
    @Expose
    private var isRated: Int? = null

    @SerializedName("order_id")
    @Expose
    private var orderId: Int? = null

    fun getIsRated(): Int? {
        return isRated
    }

    fun setIsRated(isRated: Int?) {
        this.isRated = isRated
    }

    fun getOrderId(): Int? {
        return orderId
    }

    fun setOrderId(orderId: Int?) {
        this.orderId = orderId
    }

}