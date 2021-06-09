package com.codebrew.clikat.module.new_signup.login

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LoginProvider {

    @ContributesAndroidInjector
    abstract fun provideLoginFactory(): LoginFragment
}
