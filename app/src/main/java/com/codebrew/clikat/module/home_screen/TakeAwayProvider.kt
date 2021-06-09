package com.codebrew.clikat.module.home_screen

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class TakeAwayProvider {

    @ContributesAndroidInjector
     abstract fun provideTakeAwayFactory(): TakeAwayFragment
}
