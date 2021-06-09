package com.codebrew.clikat.module.product.product_listing

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.model.api.ProductData
import com.codebrew.clikat.data.model.others.FilterInputModel
import com.codebrew.clikat.data.model.api.ProductListModel
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.module.product.ProductNavigator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProductTabViewModel(dataManager: DataManager) : BaseViewModel<ProductNavigator>(dataManager) {

    var isViewType = ObservableBoolean(true)


    val productLiveData: MutableLiveData<ProductData> by lazy {
        MutableLiveData<ProductData>()
    }

    fun getProductList(param: FilterInputModel,viewType:Boolean) {
        setIsLoading(true)


        compositeDisposable.add(dataManager.getProductFilter(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
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

        //setIsLoading(false)

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onFavStatus()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun validateResponse(it: ProductListModel?) {

        setIsLoading(false)

        if (it?.status == NetworkConstants.SUCCESS) {
            productLiveData.value=it.data
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            navigator.onSessionExpire()
        } else {
            navigator.onErrorOccur(it?.message?:"")
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

     fun setViewType(viewType:Boolean)
    {
       this.isViewType.set(viewType)
    }
}