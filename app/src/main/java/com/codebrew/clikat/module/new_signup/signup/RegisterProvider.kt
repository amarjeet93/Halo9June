package com.codebrew.clikat.module.new_signup.signup

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RegisterProvider {

    @ContributesAndroidInjector
    abstract fun provideRegisterFactory(): RegisterFragment
}
