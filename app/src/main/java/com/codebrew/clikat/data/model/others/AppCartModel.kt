package com.codebrew.clikat.data.model.others

data class AppCartModel(var supplierName: String,var totalCount:Int,
                        var totalPrice:String,var cartAvail:Boolean){
    constructor() : this("",0,"",false)
}