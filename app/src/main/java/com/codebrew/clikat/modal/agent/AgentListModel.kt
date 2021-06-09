package com.codebrew.clikat.modal.agent

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class AgentListModel(
        /**
         * statusCode : 200
         * success : 1
         * message : Success
         * data : [{"service_id":13,"cbl_user":{"email":"sahil@code-brew.com","id":10,"image":null,"city":"342342","phone_number":"2342342","experience":null,"occupation":null}}]
         */
        var statusCode: Int? = null,
        var success: Int? = null,
        var message: String? = null,
        var data: List<DataBean>? = null)

@Parcelize
class DataBean(
        /**
         * service_id : 13
         * cbl_user : {"email":"sahil@code-brew.com","id":10,"image":null,"city":"342342","phone_number":"2342342","experience":null,"occupation":null}
         */
        var service_id: Int? = null,
        var cbl_user: CblUserBean? = null) : Parcelable

@Parcelize
class CblUserBean(
        /**
         * email : sahil@code-brew.com
         * id : 10
         * image : null
         * city : 342342
         * phone_number : 2342342
         * experience : null
         * occupation : null
         */
        var email: String? = null,
        var id: Int? = null,
        var name: String? = null,
        var image: String? = null,
        var city: String? = null,
        var phone_number: String? = null,
        var experience: Int? = null,
        var occupation: String? = null) : Parcelable