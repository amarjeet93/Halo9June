package com.codebrew.clikat.app_utils

import com.codebrew.clikat.module.requestsLists.RequestListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ClearCartProvider {

    @ContributesAndroidInjector
    abstract fun providesClearCartProvider(): ClearCartBroadCastReceiver
}