package com.codebrew.clikat.module.payment_gateway.dialog_card

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.data.network.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CardViewModel(dataManager: DataManager) : BaseViewModel<CardNavigator>(dataManager) {

}
