package com.codebrew.clikat.module.restaurant_detail

import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.network.ApiResponse
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.other.DataBean
import com.codebrew.clikat.modal.other.SuplierProdListModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class RestDetailViewModel(dataManager: DataManager) : BaseViewModel<RestDetailNavigator>(dataManager) {

    val supplierLiveData by lazy { SingleLiveEvent<DataBean>() }
    val prescLiveData by lazy { SingleLiveEvent<String>() }


    fun getProductList(supplierId:String) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()
        param["supplier_id"] = supplierId
        compositeDisposable.add(dataManager.getProductLst(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    fun uploadPresImage(image: String, supplierId: String, adrsId: Int?, appType: String)
    {
       // setImageLoading(true)

        val file= File(image)
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val partImage = MultipartBody.Part.createFormData("file", file.name, requestBody)

        dataManager.uploadPres(CommonUtils.convrtReqBdy(""),CommonUtils.convrtReqBdy(supplierId),CommonUtils.convrtReqBdy(adrsId.toString()),
                CommonUtils.convrtReqBdy(appType),partImage)
                .observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validatePresc(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }

    }

    private fun validatePresc(it: ApiResponse<Any>?) {
       // setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                prescLiveData.value = it.msg
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



    fun markFavSupplier(supplierId:String) {


        val param = dataManager.updateUserInf()
        param["supplierId"] = supplierId
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN,PrefenceConstants.TYPE_STRING).toString()
        compositeDisposable.add(dataManager.favouriteSupplier(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.favResponse(it) }, { this.handleError(it) })
        )
    }

    fun unFavSupplier(supplierId:String) {


        val param = dataManager.updateUserInf()
        param["supplierId"] = supplierId
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN,PrefenceConstants.TYPE_STRING).toString()
        compositeDisposable.add(dataManager.unfavSupplier(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.unfavResponse(it) }, { this.handleError(it) })
        )
    }

    private fun unfavResponse(it: ExampleCommon?) {
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.unFavResponse()
        }else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        }
        else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun favResponse(it: ExampleCommon?) {

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.favResponse()
        }else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        }
        else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun validateResponse(it: SuplierProdListModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            supplierLiveData.value = it.data
        }else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        }
        else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
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
