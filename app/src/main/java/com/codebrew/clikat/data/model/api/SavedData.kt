package com.codebrew.clikat.data.model.api

import com.google.gson.annotations.SerializedName

data class SavedData(
        val `data`: ArrayList<SavedCardList>,
        val has_more: Boolean,
        val url: String,
        val defaultCard: DefaultCard?
)

data class SavedObj(
        val `data`: SavedData,
        val message: String,
        val status: Int
)

data class DefaultCard(
        val card_id: String,
        val card_number: String,
        val card_payment_id: Int,
        val card_source: String,
        val card_type: String,
        val created_at: String,
        val customer_payment_id: String,
        val exp_month: Int,
        val exp_year: Int,
        val id: Int,
        val is_default: Int,
        val is_deleted: Int,
        val updated_at: String,
        val user_id: Int
)

data class SavedCardList(
        val address_city: Any,
        val address_country: Any,
        val address_line1: Any,
        val address_line1_check: Any,
        val address_line2: Any,
        val address_state: Any,
        val address_zip: Any,
        val address_zip_check: Any,
        val brand: String,
        val country: String,
        val customer: String,
        val cvc_check: String,
        val dynamic_last4: Any,
        val exp_month: Int,
        val exp_year: Int,
        val fingerprint: String,
        val funding: String,
        val id: String,
        @SerializedName("last4", alternate = ["last_4"])
        val last4: String,
        val name: String,
        val tokenization_method: Any,
        var isDefault: Boolean = false
)