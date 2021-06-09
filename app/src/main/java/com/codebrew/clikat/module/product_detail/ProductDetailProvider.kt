package com.codebrew.clikat.module.product_detail

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ProductDetailProvider {

    @ContributesAndroidInjector
     abstract fun provideProdDetailFactory(): ProductDetails
}
