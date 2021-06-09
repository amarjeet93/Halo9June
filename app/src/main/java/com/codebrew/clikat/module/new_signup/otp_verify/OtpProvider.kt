package com.codebrew.clikat.module.new_signup.otp_verify

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OtpProvider {

    @ContributesAndroidInjector
    abstract fun provideOtpFactory(): OtpVerifyFragment
}
