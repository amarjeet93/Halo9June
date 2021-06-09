package com.codebrew.clikat.module.rate_order

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




 class GetRateExample
 {
     @SerializedName("status")
     @Expose
     internal var status: Int? = null

     @SerializedName("message")
     @Expose
     private var message: String? = null

     @SerializedName("data")
     @Expose
     private var data: GetRateDatum? = null

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

     fun getData(): GetRateDatum? {
         return data
     }

     fun setData(data: GetRateDatum?) {
         this.data = data
     }

 }
