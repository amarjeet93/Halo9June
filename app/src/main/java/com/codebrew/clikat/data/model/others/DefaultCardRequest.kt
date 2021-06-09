package com.codebrew.clikat.data.model.others


data class DefaultCardRequest(
        val card_id: String,
        val gateway_unique_id: String,
        val customer_payment_id: String
)