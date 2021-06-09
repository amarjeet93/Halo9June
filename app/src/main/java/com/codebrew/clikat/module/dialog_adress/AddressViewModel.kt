package com.codebrew.clikat.module.dialog_adress

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocationBean
import com.codebrew.clikat.data.model.SupplierLocationModel
import com.codebrew.clikat.data.model.api.AddAddressModel
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.CustomerAddressModel
import com.codebrew.clikat.data.model.api.DataBean
import com.codebrew.clikat.modal.DataCommon
import com.codebrew.clikat.modal.ExampleCommon
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class AddressViewModel(dataManager: DataManager) : BaseViewModel<AddressNavigator>(dataManager) {


    val addressLiveData by lazy { SingleLiveEvent<DataBean>() }

    val     supplierLocationsLiveData by lazy { SingleLiveEvent<SupplierLocationBean>() }

    val addAdrsLiveData by lazy { SingleLiveEvent<AddressBean>() }
    val updateAdrsLiveData by lazy { SingleLiveEvent<AddressBean>() }
    val deleteAdrsData by lazy { SingleLiveEvent<DataCommon>() }


    fun getAddressList(supplierBranch:Int) {
        //  setIsLoading(true)

        val param = dataManager.updateUserInf()
        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        param["supplierBranchId"]=supplierBranch.toString()

        compositeDisposable.add(dataManager.getAllAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    fun getSupplierLocations(supplierBranch:Int) {
        //  setIsLoading(true)

        val param = dataManager.updateUserInf()
        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        param["supplierBranchId"]=supplierBranch.toString()

        compositeDisposable.add(dataManager.getAllSupplierLocations(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateLocationResponse(it) }, { this.handleError(it) })
        )
    }

    fun addAddress(param: HashMap<String, String>) {

        param["name"]="supplierBranchId"

        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        compositeDisposable.add(dataManager.addAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateAddAdrs(it) }, { this.handleError(it) })
        )
    }

    fun editAddress(param: HashMap<String, String>) {

        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        compositeDisposable.add(dataManager.updateAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.updateAdrs(it) }, { this.handleError(it) })
        )
    }

    fun deleteAddress(adressId: String) {
        val param = HashMap<String, String>()
        param["addressId"] = adressId
        if (dataManager.getCurrentUserLoggedIn()) {
            param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        }
        compositeDisposable.add(dataManager.deleteAddress(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.deleteAdrs(it) }, { this.handleError(it) })
        )
    }

    private fun deleteAdrs(it: ExampleCommon?) {
        if (it?.status == NetworkConstants.SUCCESS) {
            deleteAdrsData.value = it.data
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun updateAdrs(it: AddAddressModel?) {
        if (it?.status == NetworkConstants.SUCCESS) {
            updateAdrsLiveData.value = it.data
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun validateAddAdrs(it: AddAddressModel?) {
        if (it?.status == NetworkConstants.SUCCESS) {
            addAdrsLiveData.value = it.data
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }



    private fun validateResponse(it: CustomerAddressModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            addressLiveData.value = it.data
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    private fun validateLocationResponse(it: SupplierLocationModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            supplierLocationsLiveData.value = it.data
        } else {
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
