package com.codebrew.clikat.module.subcategory

import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.supplier_detail.DataSupplierDetail
import com.codebrew.clikat.modal.ExampleAllSupplier
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.ExampleSupplierDetail
import com.codebrew.clikat.modal.other.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SubCategoryViewModel(dataManager: DataManager) : BaseViewModel<SubCategoryNavigator>(dataManager) {

    val subCatLiveData by lazy { SingleLiveEvent<SubCatData>() }
    val suppliersList by lazy { SingleLiveEvent<ExampleAllSupplier>() }

    val isSubCat= ObservableInt(0)

    val supllierLiveData: MutableLiveData<DataSupplierDetail> by lazy {
        MutableLiveData<DataSupplierDetail>()
    }

    fun getSubCategory(param:HashMap<String,String>) {
        setIsLoading(true)

        compositeDisposable.add(dataManager.getSubCategory(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    fun fetchSuplierDetail(branch_id:Int,catId:Int,supplier_id:Int) {
        setIsLoading(true)
        val hashMap = dataManager.updateUserInf()


        hashMap["supplierId"] = supplier_id.toString()
        hashMap["branchId"] = branch_id.toString()
        hashMap["categoryId"] = catId.toString()


        compositeDisposable.add(dataManager.getSupplierDetails(hashMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateSupplierResponse(it) }, { this.handleError(it) })
        )
    }

    fun getSuppliers(param:HashMap<String,String>) {
        setIsLoading(true)
        compositeDisposable.add(dataManager.getAllSuppliersNew(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponseSuppliersList(it) },
                        { this.handleError(it) })
        )
    }

    private fun validateResponse(it: SubCategoryListModel?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                subCatLiveData.value = it.data
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

    private fun validateSupplierResponse(it: ExampleSupplierDetail?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> supllierLiveData.setValue(it.data)

            else ->
                it?.message?.let { it1 -> navigator.onErrorOccur(it1) }

        }
    }
    private fun validateResponseSuppliersList(it: ExampleAllSupplier?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                suppliersList.value = it
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



    private fun handleError(e: Throwable) {
        setIsLoading(false)
        setSubCat(0)
        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
                navigator.onErrorOccur(it)
            }
        }
    }

    fun setSubCat(count: Int) {
        this.isSubCat.set(count)
    }

}
