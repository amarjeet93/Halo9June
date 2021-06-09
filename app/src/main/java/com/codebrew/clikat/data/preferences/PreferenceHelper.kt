package com.codebrew.clikat.data.preferences

interface PreferenceHelper {

    fun setkeyValue(key: String, value: Any)

    fun getKeyValue(key: String, type: String): Any?

    fun addGsonValue(key: String,value: String)

    fun <T>getGsonValue(key: String, type: Class<T>): T?

    fun logout()

    fun onClear()

    fun removeValue(key:String)

    fun getCurrentUserLoggedIn(): Boolean

    fun getLangCode():String

}