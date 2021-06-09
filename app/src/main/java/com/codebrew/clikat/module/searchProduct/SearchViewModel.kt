package com.codebrew.clikat.module.searchProduct

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.ProductData
import com.codebrew.clikat.data.model.api.ProductListModel
import com.codebrew.clikat.data.model.others.FilterInputModel
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.other.HomeSupplierModel
import com.codebrew.clikat.modal.other.SupplierDataBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SearchViewModel(dataManager: DataManager) : BaseViewModel<SearchNavigator>(dataManager) {

    val isSearchHist = ObservableBoolean(false)

    //true for rest, false for prod

    val supplierLiveData by lazy { SingleLiveEvent<List<SupplierDataBean>>() }

    val productLiveData: MutableLiveData<ProductData> by lazy {
        MutableLiveData<ProductData>()
    }

    fun setIsSearchHist(param: Boolean) {
        this.isSearchHist.set(param)
    }



    fun getProductList(filterInput: FilterInputModel) {
        setIsLoading(true)

        compositeDisposable.add(dataManager.getProductFilter(filterInput)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    fun getSupplierList(resturant:String?) {
        setIsLoading(true)

        val param=dataManager.updateUserInf()
        param["search"]=resturant?:""

        compositeDisposable.add(dataManager.getSupplierList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleSuppliers(it) }, { this.handleError(it) })
        )
    }


    private fun handleSuppliers(it: HomeSupplierModel?) {
        setIsLoading(false)
        setIsList(it?.data?.size?:0)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                supplierLiveData.value = it.data
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




    fun markFavProduct(productId: Int?, favStatus: Int?) {
        //setIsLoading(true)

        val mParam = HashMap<String?,String?>()
        mParam["product_id"]=productId.toString()
        mParam["status"]=favStatus.toString()
        dataManager.markWishList(mParam)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateFavResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it) }
    }


    private fun validateFavResponse(it: ExampleCommon?) {

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onFavStatus()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun validateResponse(it: ProductListModel?) {
        setIsLoading(false)
        setIsList(0)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                setIsList(it.data?.product?.size?:0)
                productLiveData.value=it.data
            }
            NetworkConstants.AUTHFAILED -> {
                navigator.onSessionExpire()
            }
            else -> {
                navigator.onErrorOccur(it?.message?:"")
            }
        }
    }



    private fun handleError(e: Throwable) {
        setIsLoading(false)
        setIsList(0)
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
