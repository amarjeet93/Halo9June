package com.codebrew.clikat.data.model.others

import android.os.Parcelable
import com.codebrew.clikat.modal.other.SettingModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomPayModel(val payName: String? = null, val image: Int? = null, var keyId: String? = null,
                          var payment_token: String? = null, val payement_front: List<SettingModel.DataBean.KeyValueFront?>? = null,
                          val addCard: Boolean? = false,
                          var cardId: String? = null,
                          var customerId: String? = null) : Parcelable