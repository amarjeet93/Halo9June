package com.codebrew.clikat.module.cart.model

data class TaxModel(val categoryId: Int,
                    val subCategoryName: String,
                    var price: Double,
                    val handleAdminCharges :Float,
                    val promoAmount :Float
)