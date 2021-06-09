package com.codebrew.clikat.module.bottom_navigation

import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.module.rate_order.GetRateExample
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainScreenViewModel(dataManager: DataManager) : BaseViewModel<BaseInterface>(dataManager) {

    val getrateLiveData by lazy { MutableLiveData<MutableList<GetRateExample>>() }


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
            NetworkConstants.SUCCESS,4 -> {

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
                navigator.onErrorOccur(it)
            }
        }
    }


}
