package com.codebrew.clikat.data.model.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddCardResponseData(
        @field:SerializedName("transaction-reference")
        val transaction_reference: String? = null,

        @field:SerializedName("notification-mode")
        val notification_mode: String? = null,

        @field:SerializedName("PaymentURL")
        val PaymentURL: String? = null,

        @field:SerializedName("payment-url")
        val payment_url: String? = null,

        @field:SerializedName("status")
        val status: Int? = null,

        @field:SerializedName("error-code")
        val error_code: Int? = null,

        @field:SerializedName("error-message")
        val error_message: String? = null,

        @field:SerializedName("customer_payment_id")
        val customer_payment_id: String? = null,

        @field:SerializedName("InvoiceId")
        val InvoiceId: String? = null

):Parcelable

data class AddCardResponse(
        val data: AddCardResponseData? = null,
        val message: String? = null,
        val status: Int? = null
)