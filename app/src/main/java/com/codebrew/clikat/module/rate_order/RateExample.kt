package com.codebrew.clikat.module.rate_order

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class RateExample
{
    @SerializedName("status")
    @Expose
    internal var status: Int? = null

    @SerializedName("message")
    @Expose
    internal var message: String? = null


    fun getStatus(): Int? {
        return status
    }

    fun setStatus(status: Int?) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

}