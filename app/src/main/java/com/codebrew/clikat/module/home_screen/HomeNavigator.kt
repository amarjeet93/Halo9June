package com.codebrew.clikat.module.home_screen

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.modal.other.SupplierInArabicBean

interface HomeNavigator: BaseInterface {

    fun onFavStatus()
    fun unFavSupplierResponse(data: SupplierInArabicBean?)
    fun favSupplierResponse(supplierId: SupplierInArabicBean?)
}
