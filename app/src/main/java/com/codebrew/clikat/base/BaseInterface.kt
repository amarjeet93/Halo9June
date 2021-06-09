package com.codebrew.clikat.base

interface BaseInterface {

    fun onErrorOccur(message: String)

    fun onSessionExpire()
}