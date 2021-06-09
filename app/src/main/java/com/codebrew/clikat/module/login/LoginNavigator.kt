package com.codebrew.clikat.module.login

import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.modal.PojoSignUp

interface LoginNavigator: BaseInterface {
    fun onForgotPswr(message: String)
    fun onLogin()
    fun onFbLogin(signup: PojoSignUp?)
    fun userNotVerified(accessToken: String)
    fun onUserExist(existed :Boolean)
}