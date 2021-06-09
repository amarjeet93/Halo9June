package com.codebrew.clikat.data.model.api

import com.google.gson.annotations.SerializedName

data class SuccessModel(
    val message: String,
    @SerializedName("status",alternate = ["statusCode"])
    val statusCode: Int,
    val success: Int
)

