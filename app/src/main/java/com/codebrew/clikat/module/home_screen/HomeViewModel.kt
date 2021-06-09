package com.codebrew.clikat.module.home_screen

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.ProductListModel
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.other.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class HomeViewModel(dataManager: DataManager) : BaseViewModel<HomeNavigator>(dataManager) {

    val homeDataLiveData by lazy { SingleLiveEvent<Data>() }
    val supplierLiveData by lazy { SingleLiveEvent<List<SupplierDataBean>>() }
    val offersLiveData by lazy { SingleLiveEvent<OfferDataBean>() }

    val productLiveData by lazy { SingleLiveEvent<DataBean>() }
    val popularLiveData by lazy { SingleLiveEvent<List<ProductDataBean>>() }


    fun getCategories() {
        setIsLoading(true)

        val param = dataManager.updateUserInf()

    /*    if (dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_INT) != null) {
            param["categoryId"] = dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_INT).toString()
        }*/

        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
        compositeDisposable.add(dataManager.getAllCategoryNew(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateResponse(it) }, { this.handleError(it) })
        )
    }

    fun getProductList(supplierId: String) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()

        if (dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            param["categoryId"] = dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString()
        }
        param["supplier_id"] = supplierId
        compositeDisposable.add(dataManager.getProductLst(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleProductList(it) }, { this.handleError(it) })
        )
    }


    fun getPopularProduct(catId: Int) {
        //  setIsLoading(true)

        val param = dataManager.updateUserInf()
        param["offset"] = "0"
        param["limit"] = "10"

        if (catId > 0) {
            param["categoryId"] = catId.toString()
        }

        dataManager.getPopularProd(param)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.handlePopularList(it) }, { this.handleError(it) })?.let { compositeDisposable.add(it) }
    }


    fun getSupplierList(selfPick: String?) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()

        param["self_pickup"] = selfPick ?: "0"

        if (dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING) .toString().isNotEmpty()) {
            param["categoryId"] = dataManager.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString()
        }

        compositeDisposable.add(dataManager.getSupplierList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleSuppliers(it) }, { this.handleError(it) })
        )
    }

    fun getOfferList(catId: Int) {
        setIsLoading(true)
        getOfferList1(catId)
        val param = dataManager.updateUserInf()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()

        if (catId > 0) {
            param["categoryId"] = catId.toString()
        }

        compositeDisposable.add(dataManager.getOfferList(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleOffers(it) }, { this.handleOfferError(it) })
        )
    }

    fun getOfferList1(catId: Int) {
        setIsLoading(true)

        val param = dataManager.updateUserInf()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()

        if (catId > 0) {
            param["categoryId"] = catId.toString()
        }

        compositeDisposable.add(dataManager.getOfferList1(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleOffers1(it) }, { this.handleError(it) })
        )
    }

    fun markFavProduct(productId: Int?, favStatus: Int?) {
        //setIsLoading(true)

        val mParam = HashMap<String?, String?>()
        mParam["product_id"] = productId.toString()
        mParam["status"] = favStatus.toString()
        dataManager.markWishList(mParam)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe({ this.validateFavResponse(it) }, { this.handleError(it) })?.let {
                    compositeDisposable.add(it)
                }
    }

    private fun handlePopularList(it: ProductListModel?) {
        //  setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            popularLiveData.value = it.data?.product
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    fun markFavSupplier(supplierId: SupplierInArabicBean?) {
        val param = dataManager.updateUserInf()
        param["supplierId"] = supplierId?.id.toString()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN,PrefenceConstants.TYPE_STRING).toString()
        compositeDisposable.add(dataManager.favouriteSupplier(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.favResponse(it,supplierId) }, { this.handleError(it) })
        )
    }

    fun unFavSupplier(supplierId: SupplierInArabicBean?) {
        val param = dataManager.updateUserInf()
        param["supplierId"] = supplierId?.id.toString()
        param["accessToken"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN,PrefenceConstants.TYPE_STRING).toString()
        compositeDisposable.add(dataManager.unfavSupplier(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.unfavResponse(it,supplierId) }, { this.handleError(it) })
        )
    }

    private fun unfavResponse(it: ExampleCommon?, supplierId: SupplierInArabicBean?) {
        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.unFavSupplierResponse(supplierId)
        }else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        }
        else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun favResponse(it: ExampleCommon?, supplierId: SupplierInArabicBean?) {

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.favSupplierResponse(supplierId)
        }else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        }
        else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun handleProductList(it: SuplierProdListModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            productLiveData.value = it.data
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun handleOffers(it: OfferListModel?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> offersLiveData.value = it.data
            NetworkConstants.AUTHFAILED -> {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            }
            else -> it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }
    private fun handleOffers1(it: Any?) {
        setIsLoading(false)
    }

    private fun handleSuppliers(it: HomeSupplierModel?) {
        setIsLoading(false)
        if (it?.status == NetworkConstants.SUCCESS) {
            supplierLiveData.value = it.data
        } else if (it?.status == NetworkConstants.AUTHFAILED) {
            dataManager.setUserAsLoggedOut()
            navigator.onSessionExpire()
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }


    private fun validateResponse(it: CategoryListModel?) {
        setIsLoading(false)
        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                homeDataLiveData.value = it.data
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

    private fun validateFavResponse(it: ExampleCommon?) {

        //setIsLoading(false)

        if (it?.status == NetworkConstants.SUCCESS) {
            navigator.onFavStatus()
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

    private fun handleOfferError(e: Throwable) {
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


