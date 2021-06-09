package com.codebrew.clikat.app_utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ClearCartBroadCastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appUtils: AppUtils

    override fun onReceive(context: Context?, intent: Intent?) {

        AndroidInjection.inject(this, context)

        context?.let {
            appUtils.clearCart()
        }
    }

}