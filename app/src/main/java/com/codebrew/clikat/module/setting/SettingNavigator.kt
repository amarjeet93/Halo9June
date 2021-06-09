package com.codebrew.clikat.module.setting

import com.codebrew.clikat.base.BaseInterface

interface SettingNavigator:BaseInterface {

    fun onNotifiChange(message: String)

    fun onNotiLangChange(message: String)

    fun onUploadPic(message: String,image:String)
}