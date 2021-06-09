package com.codebrew.clikat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.preferences.DataNames
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager @Inject
constructor(private val context: Context) {

    @Inject
    lateinit var mPref: PreferenceHelper

    fun setLocale(c: Context): Context {
        return updateResources(c, getLanguage())
    }


    fun getLanguage(): String? {
        return mPref.getKeyValue(DataNames.SELECTED_LANGUAGE, Locale.getDefault().language).toString()
    }


    private fun updateResources(context: Context, language: String?): Context {
        var context = context
        val locale = Locale(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = res.configuration
        if (Build.VERSION.SDK_INT >= 21) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    fun getLocale(res: Resources): Locale {
        val config = res.configuration
        return if (Build.VERSION.SDK_INT >= 24) config.locales[0] else config.locale
    }
}