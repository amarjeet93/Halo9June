package com.codebrew.clikat.data.model.api.orderDetail

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Agent (
    var image: String?=null,
    var agent_created_id:String?=null,
    var country: String?=null,
    var occupation: String?=null,
    var city: String?=null,
    var name: String?=null,
    var phone_number: String?=null,
    var state: String?=null,
    var experience: Int?=null,
    var longitude: Double?=null,
    var latitude: Double?=null,
    var email: String?=null):Parcelable