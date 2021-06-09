package com.codebrew.clikat.module.compare_product

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class CompareProductProvider {

    @ContributesAndroidInjector
     abstract fun provideCompareProdFactory(): CompareProductsResultFragment
}
