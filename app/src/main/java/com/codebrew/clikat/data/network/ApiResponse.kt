package com.codebrew.clikat.data.network

import com.codebrew.clikat.module.cart.model.Data
import com.google.gson.annotations.SerializedName

class ApiResponse<T> {
    val success: Int? = null
    val status: Int? = null
    @SerializedName("message",alternate = ["msg"])
    val msg: String? = null
    val data: T? = null
    var data2:Data? = null
}