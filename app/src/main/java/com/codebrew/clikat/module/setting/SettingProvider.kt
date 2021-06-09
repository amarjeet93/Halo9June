package com.codebrew.clikat.module.setting

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class SettingProvider {

    @ContributesAndroidInjector
     abstract fun provideSettingFactory(): SettingFragment
}
