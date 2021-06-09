package com.codebrew.clikat.module.setting

import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.preferences.DataNames
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SettingViewModel(dataManager: DataManager) : BaseViewModel<SettingNavigator>(dataManager) {


    fun changeNotification(status: Int) {
        setIsLoading(true)


        val params = hashMapOf<String?, String?>(
                "accessToken" to dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString(),
                "status" to status.toString(),
                "languageId" to dataManager.getLangCode())


        dataManager.changeNotifStatus(params)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    fun changeNotiLang(languageId: Int) {
        setIsLoading(true)
        val param = hashMapOf<String?, String?>(
                "accessToken" to dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString(),
                "languageId" to languageId.toString())

        dataManager.notiLanguage(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.notiLangResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    fun uploadProfImage(image:String)
    {
        setImageLoading(true)

        val file=File(image)
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val partImage = MultipartBody.Part.createFormData("profilePic", file.name, requestBody)

        dataManager.uploadSingleImage(CommonUtils.convrtReqBdy(dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString())
        ,partImage)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.changePicResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }

    }

    private fun changePicResponse(it: ExampleCommon?) {
        setImageLoading(false)



        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onUploadPic(it.message,it.data.image)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    private fun notiLangResponse(it: ExampleCommon?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onNotiLangChange(it.message)
            }
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> {
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
            }
        }
    }


    private fun validateResponse(it: ExampleCommon?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onNotifiChange(it.message)
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
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

}
