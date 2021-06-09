package com.codebrew.clikat.module.rate_order

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

class RatingViewModel(dataManager: DataManager):BaseViewModel<BaseInterface>(dataManager) {

    val rateLiveData by lazy { MutableLiveData<String>() }


    fun add_rating(rating:String,rating_type:String,order_id:String) {


        val hashMap= hashMapOf("order_id" to order_id,
                "rating_value" to rating,
                "rating_types" to rating_type)


        setIsLoading(true)
        dataManager.add_Rating(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.codeResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    private fun codeResponse(it: RateExample?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS-> {
rateLiveData.value=it?.message
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
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