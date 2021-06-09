package com.codebrew.clikat.module.payment_gateway

import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.data.network.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class PaymentListViewModel(dataManager: DataManager) : BaseViewModel<BaseInterface>(dataManager) {

    val settingLiveData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun validateZelleImage(image: String) {
        setIsLoading(true)

        val file= File(image)
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val partImage = MultipartBody.Part.createFormData("file", file.name, requestBody)

        compositeDisposable.add(dataManager.uploadFile(partImage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.imageResponse(it) }, { this.handleError(it) })
        )
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

    private fun imageResponse(it: ApiResponse<Any>?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                settingLiveData.value=it.data.toString()
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.msg?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }
}
