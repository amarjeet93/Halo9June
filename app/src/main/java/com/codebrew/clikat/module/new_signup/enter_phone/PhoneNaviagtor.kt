package com.codebrew.clikat.module.new_signup.enter_phone

import com.codebrew.clikat.base.BaseInterface

interface PhoneNaviagtor : BaseInterface {

    fun onPhoneVerify(accessToken: String)
}