package com.codebrew.clikat.module.agent_time_slot

import com.codebrew.clikat.app_utils.SingleLiveEvent
import com.codebrew.clikat.base.BaseViewModel
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.modal.DataCommon
import com.codebrew.clikat.modal.ExampleCommon
import com.codebrew.clikat.modal.other.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.LiveData
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.modal.agent.AgentSlotsModel
import com.codebrew.clikat.preferences.DataNames


class AgentViewModel(dataManager: DataManager) : BaseViewModel<BaseInterface>(dataManager) {


    val agentSlotsData by lazy { SingleLiveEvent<List<String>>() }

    fun getAgentSlotsList(param: HashMap<String, String>)
    {
        setIsLoading(true)

        val headerParam=HashMap<String,String>()
        headerParam["secret_key"]=dataManager.getKeyValue(DataNames.AGENT_DB_SECRET,PrefenceConstants.TYPE_STRING).toString()
        headerParam["api_key"]=dataManager.getKeyValue(DataNames.AGENT_API_KEY,PrefenceConstants.TYPE_STRING).toString()

        compositeDisposable.add(dataManager.getAvailabilitySlot(headerParam,param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ this.validateAgentSlots(it) }, { this.handleError(it) })
        )
    }



    private fun validateAgentSlots(it: AgentSlotsModel?) {

        setIsLoading(false)

        if (it?.statusCode == NetworkConstants.SUCCESS) {
            agentSlotsData.value = it.data
        } else {
            it?.message?.let { it1 -> navigator.onErrorOccur(it1) }
        }
    }

    private fun handleError(e: Throwable) {
        setIsLoading(false)
        handleErrorMsg(e).let {
            if (it == NetworkConstants.AUTH_MSG) {
                dataManager.setUserAsLoggedOut()
                navigator.onSessionExpire()
            } else {
                navigator.onErrorOccur(it)
            }
        }


    }

}
