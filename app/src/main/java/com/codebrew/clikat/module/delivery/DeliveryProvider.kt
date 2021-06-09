package com.codebrew.clikat.module.delivery

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class DeliveryProvider {

    @ContributesAndroidInjector
     abstract fun provideDeliveryFactory(): DeliveryFragment
}
