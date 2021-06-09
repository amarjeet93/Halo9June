package com.codebrew.clikat.modal.other

data class HomeItemModel(

        var specialOffers: MutableList<ProductDataBean>? = null,
        var sponserList: List<SupplierInArabicBean>? = null,
        var categoryList: List<English>? = null,
        var brandsList: List<Brand>? = null,
        var vendorProdList:ProductBean?=null,
        var popularProdList:MutableList<ProductDataBean>? = null,
        var bannerList: List<TopBanner>? = null,
        var mSpecialType:Int?=null,
        var mSpecialOfferName:String?=null,
        var screenType: Int = 0,
        var isSingleVendor:Int=0,
        var bannerWidth:Int=0,
        var supplierCount: Int = 0)

