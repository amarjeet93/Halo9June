package com.codebrew.clikat.module.rate_order

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.data.model.api.QuestionResponse
import com.codebrew.clikat.preferences.DataNames
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GetRateViewModel(dataManager: DataManager):BaseViewModel<BaseInterface>(dataManager) {

    val getrateLiveData by lazy { MutableLiveData<GetRateDatum>() }


    fun get_rating() {

        setIsLoading(true)
        dataManager.check_last_Rating()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.codeResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    private fun codeResponse(it: GetRateExample?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
Log.e("check_rate","check_rate")
                getrateLiveData.value=it?.getData()
            }

        }

    }

    private fun handleError(e: Throwable) {
        setIsLoading(false)

        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
               // navigator.onErrorOccur(it)
            }
        }
    }

}