package com.codebrew.clikat.module.new_signup.enter_phone

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class EnterPhoneProvider {

    @ContributesAndroidInjector
    abstract fun providePhoneFactory(): EnterPhoneFrag
}
