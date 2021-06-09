package com.codebrew.clikat.module.login

import co.paystack.android.api.model.ApiResponse
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.UserExistModel
import com.codebrew.clikat.preferences.DataNames
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginViewModel(dataManager: DataManager) : BaseViewModel<LoginNavigator>(dataManager) {

    fun validateLogin(hashMap: HashMap<String, String>) {
        setIsLoading(true)
        dataManager.login(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.codeResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }


    fun validateUserExists(hashMap: HashMap<String, String?>) {
        setIsLoading(true)
        dataManager.isUserExist(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.isUserExistResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }


    fun validateForgotPswr(emailId: String) {
        setIsLoading(true)

        dataManager.forgotPassword(emailId)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.forgotResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }


    fun validateFb(hashMap: HashMap<String, String?>) {
        setIsLoading(true)

        dataManager.fbLogin(hashMap)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.fbResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun fbResponse(it: PojoSignUp?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {

                // dataManager.addGsonValue(DataNames.USER_DATA,Gson().toJson(it))

                // dataManager.setkeyValue(PrefenceConstants.USER_LOGGED_IN,true)
                //  dataManager.setkeyValue(PrefenceConstants.ACCESS_TOKEN,it.data.access_token)
                //dataManager.setkeyValue(PrefenceConstants.USER_ID,it.data.id.toString())
                navigator.onFbLogin(it)
            }
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
        }
    }


    private fun forgotResponse(it: ExampleCommon?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onForgotPswr(it.message)
            }
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
        }
    }

    private fun isUserExistResponse(it: com.codebrew.clikat.data.network.ApiResponse<UserExistModel>?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                navigator.onUserExist(it.data?.is_exists == "1")
            }
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.msg ?: "")
        }
    }


    private fun codeResponse(it: PojoSignUp?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {

                // val signupDta=it.data


                val isVerified = it.data.otp_verified

                dataManager.setkeyValue(PrefenceConstants.IsVerified, it.data.otp_verified)

                dataManager.addGsonValue(DataNames.USER_DATA, Gson().toJson(it))

                dataManager.setkeyValue(PrefenceConstants.USER_LOGGED_IN, true)

                it.data.user_created_id?.let {
                    dataManager.setkeyValue(PrefenceConstants.USER_CHAT_ID, it)
                }

                it.data.referral_id?.let {
                    dataManager.setkeyValue(PrefenceConstants.USER_REFERRAL_ID, it)
                }

                it.data.customer_payment_id?.let {
                    dataManager.setkeyValue(PrefenceConstants.CUSTOMER_PAYMENT_ID, it)
                }

                dataManager.setkeyValue(PrefenceConstants.ACCESS_TOKEN, it.data.access_token)
                dataManager.setkeyValue(PrefenceConstants.USER_ID, it.data.id.toString())

                if (isVerified == 0) {
                    navigator.userNotVerified(it.data.access_token)

                } else {

                    navigator.onLogin()

                }


            }
            NetworkConstants.AUTHFAILED -> navigator.onSessionExpire()
            else -> navigator.onErrorOccur(it?.message ?: "")
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
