package com.codebrew.clikat.module.order_detail.rate_product

import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.SuccessModel
import com.codebrew.clikat.data.network.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class RateViewModel(dataManager: DataManager) : BaseViewModel<BaseInterface>(dataManager) {

    val rateLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }


    fun validateRateApi(param: HashMap<String,String>) {
        setIsLoading(true)
        dataManager.rateProduct(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.rateResponse(it) }, { this.handleError(it) })?.let { compositeDisposable.add(it) }
    }

    fun supplierRateApi(param: HashMap<String,String>) {
        setIsLoading(true)
        dataManager.supplierRating(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.rateResponse(it) }, { this.handleError(it) })?.let { compositeDisposable.add(it) }
    }


    fun agentRateApi(param: HashMap<String,String>) {
        setIsLoading(true)
        dataManager.agentRating(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.rateResponse(it) }, { this.handleError(it) })?.let { compositeDisposable.add(it) }
    }


    private fun handleError(e: Throwable) {
        setIsLoading(false)
        setImageLoading(false)
        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
                navigator.onErrorOccur(it)
            }
        }
    }

    private fun rateResponse(it: SuccessModel?) {
        setIsLoading(false)
        when (it?.statusCode) {
            NetworkConstants.SUCCESS -> {
                rateLiveData.value=it.message
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message.let { it1 -> navigator.onErrorOccur(it1?:"") }
            }
        }
    }
}
