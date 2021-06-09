package com.codebrew.clikat.module.supplier_detail

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class SupplierDetailProvider {

    @ContributesAndroidInjector
     abstract fun provideSupplierFactory(): SupplierDetailFragment
}
