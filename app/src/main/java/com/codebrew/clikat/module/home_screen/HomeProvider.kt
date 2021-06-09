package com.codebrew.clikat.module.home_screen

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class HomeProvider {

    @ContributesAndroidInjector
     abstract fun provideHomeFactory(): HomeFragment
}
