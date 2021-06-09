package com.codebrew.clikat.module.restaurant_detail

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class RestDetailNewProvider {

    @ContributesAndroidInjector
     abstract fun provideRestDetailNewFactory(): RestaurantDetailNewFragment

    @ContributesAndroidInjector
    abstract fun provideRestSearchFactory(): RestaurantSearchDialogFragment
}
