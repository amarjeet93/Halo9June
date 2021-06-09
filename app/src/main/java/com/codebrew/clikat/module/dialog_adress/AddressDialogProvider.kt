package com.codebrew.clikat.module.dialog_adress

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class AddressDialogProvider {

    @ContributesAndroidInjector
     abstract fun provideAddressDialogFactory(): AddressDialogFragment
}
