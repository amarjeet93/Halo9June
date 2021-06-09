package com.codebrew.clikat.module.supplier_all

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class SupplierProvider {

    @ContributesAndroidInjector
     abstract fun provideSupplierFactory(): SupplierAll
}
