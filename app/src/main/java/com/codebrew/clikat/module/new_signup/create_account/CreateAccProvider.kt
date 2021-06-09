package com.codebrew.clikat.module.new_signup.create_account

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CreateAccProvider {

    @ContributesAndroidInjector
    abstract fun provideCreateAccFactory(): CreateAccFragment
}
