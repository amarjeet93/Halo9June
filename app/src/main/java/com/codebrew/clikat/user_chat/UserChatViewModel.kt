package com.codebrew.clikat.user_chat


import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.ChatMessageListing
import com.codebrew.clikat.data.network.ApiResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class UserChatViewModel(
        dataManager: DataManager
) : BaseViewModel<UserChatNavigator>(dataManager) {


    val isListCount = ObservableInt(0)

    val userMessagelist: MutableLiveData<List<ChatMessageListing>> by lazy {
        MutableLiveData<List<ChatMessageListing>>()
    }



    fun fetchAllChat(orderId: String, isFirst: Boolean, chatBetween: String) {
        setIsLoading(true)

        val hashMap = hashMapOf(

            "accessToken" to dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING)?.toString(),
            "receiver_created_id" to dataManager.getKeyValue(PrefenceConstants.USER_CHAT_ID,PrefenceConstants.TYPE_STRING)?.toString(), // user_id
            "limit" to if(isFirst) "500" else userMessagelist.value?.count()?.plus(50)?.toString(),
            "skip" to if(isFirst) "0" else userMessagelist.value?.count().toString(),
            "userType" to "2",//  1 for agent , 2 for user
            "order_id" to orderId,
            "chat_between" to chatBetween
        )

        compositeDisposable.add(
            dataManager.getChatMessages(hashMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.handleChatResponse(it) }, { this.handleError(it) })
        )
    }


    private fun handleChatResponse(it: ApiResponse<ArrayList<ChatMessageListing>>?) {
        setIsLoading(false)

        setListCount(it?.data?.count()?:0)

        when (it?.status) {
            NetworkConstants.SUCCESS -> {
                if (it.data?.isNotEmpty() == true) {
                    userMessagelist.value = it.data
                }
            }
            else -> navigator.onErrorOccur(it?.msg ?: "")
        }
    }


    fun setListCount(count: Int) {
        this.isListCount.set(count)
    }


    private fun handleError(e: Throwable) {
        setIsLoading(false)
        setListCount(0)
        handleErrorMsg(e).let {
            when (it) {
                NetworkConstants.AUTH_MSG -> {
                    navigator.onErrorOccur(NetworkConstants.AUTH_MSG)
                    dataManager.setUserAsLoggedOut()
                    navigator.onSessionExpire()
                }
                else -> navigator.onErrorOccur(it)
            }
        }
    }
}
