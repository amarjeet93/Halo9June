package com.codebrew.clikat.module.cart

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class CartProvider {

    @ContributesAndroidInjector
     abstract fun provideCartFactory(): Cart
}
