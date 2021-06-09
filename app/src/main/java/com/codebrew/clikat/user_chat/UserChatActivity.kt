package com.codebrew.clikat.user_chat

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.DateTimeUtils
import com.codebrew.clikat.app_utils.PaginationScrollListener
import com.codebrew.clikat.app_utils.SocketManager
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.NetworkConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.ChatMessageListing
import com.codebrew.clikat.data.model.api.SuccessModel
import com.codebrew.clikat.data.model.api.orderDetail.Agent
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivityUserChatBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.user_chat.adapter.ChatAdapter
import com.codebrew.clikat.user_chat.adapter.ChatListener
import com.codebrew.clikat.utils.configurations.Configurations
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Ack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_user_chat.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UserChatActivity : BaseActivity<ActivityUserChatBinding, UserChatViewModel>(),
        UserChatNavigator, SwipeRefreshLayout.OnRefreshListener {


    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var isFirst: Boolean = true

    private var mUserChatModel: UserChatViewModel? = null
    private lateinit var mActivityUserChatBinding: ActivityUserChatBinding

    private val msglist = mutableListOf<ChatMessageListing>()
    private var orderId = ""
    private var supplierId = ""
    private var userDetail: Agent? = null

    private lateinit var adapter: ChatAdapter

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var mDataManager: PreferenceHelper

    @Inject
    lateinit var mDateTimeUtils: DateTimeUtils

    var mLinearLayoutManager: LinearLayoutManager? = null

    var currentDate = ""

    private val socketManager
            by lazy { SocketManager.getInstance(mDataManager.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString(),
                    mDataManager.getKeyValue(PrefenceConstants.DB_SECRET, PrefenceConstants.TYPE_STRING).toString(),"") }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_user_chat
    }

    override fun getViewModel(): UserChatViewModel {
        mUserChatModel = ViewModelProviders.of(this, factory).get(UserChatViewModel::class.java)
        return mUserChatModel as UserChatViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityUserChatBinding = viewDataBinding
        mActivityUserChatBinding.color= Configurations.colors
        mActivityUserChatBinding.drawables=Configurations.drawables
        mActivityUserChatBinding.strings=Configurations.strings
        mUserChatModel?.navigator = this


        intialize()

        chatObserver()

        if (intent != null) {

            if (intent.hasExtra("orderId")) {
                orderId = intent.getStringExtra("orderId") ?: ""
            }

            if (intent.hasExtra("supplier_id")) {
                supplierId = intent.getStringExtra("supplier_id") ?: ""
            }

            if (intent.hasExtra("userData")) {
                userDetail = intent.getParcelableExtra("userData")
            }
        }

        settingToolbar()

        AppConstants.isChatOpen = true
        AppConstants.currentOrderId = orderId


        if (isNetworkConnected) {
            socketManager.on(SocketManager.ON_RECEIVE_MESSAGE, recieveChatListener)
            socketManager.connect(socketListener)
        }

        callApi(isFirst)


        fabSend.setOnClickListener {
            if (isNetworkConnected) {
                sendTextMessage()
            }
        }


        rv_user_chat?.addOnScrollListener(object : PaginationScrollListener(mLinearLayoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                //you have to call loadmore items to get more data
                getMoreItems()
            }
        })

    }

    override fun onPause() {
        super.onPause()

        orderId = ""
        AppConstants.currentOrderId = orderId
        AppConstants.isChatOpen = false
    }

    fun getMoreItems() {
        //after fetching your data assuming you have fetched list in your
        // recyclerview adapter assuming your recyclerview adapter is
        //rvAdapter
        //  after getting your data you have to assign false to isLoading
        isLoading = false

     //   viewModel.fetchAllChat(orderId, isFirst)
    }


    private fun settingToolbar() {

        userDetail?.image?.let { ivProfilePic.loadImage(it) }
        tvTitle.text=userDetail?.name

        iconBack.setOnClickListener {
            finish()
        }
    }


    private fun callApi(isFirst: Boolean) {
        if (isNetworkConnected) {
            viewModel.fetchAllChat(orderId, isFirst,"1")
        }
    }


    private fun chatObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<List<ChatMessageListing>> { resource ->

            resource.map { messageResult ->

                if (messageResult.send_by == mDataManager.getKeyValue(PrefenceConstants.USER_CHAT_ID, PrefenceConstants.TYPE_STRING)) {
                    messageResult.ownMessage = true
                }
                messageResult.sent_at = mDateTimeUtils.convertDateOneToAnother(messageResult.sent_at
                        ?: "", "yyyy-MM-dd HH:mm:ss", "hh:mm aaa")

            }
            resource?.let { msglist.addAll(it) }

            msglist.reverse()
            if (isFirst) {
                adapter.addItmSubmitList(msglist)
                rv_user_chat.smoothScrollToPosition(msglist.count().minus(1))
                isFirst = false
            }
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.userMessagelist.observe(this, catObserver)
    }

    private fun formattedDate(resource: List<ChatMessageListing?>?) {

        resource?.forEachIndexed { index, chatMessageListing ->

            currentDate = getCurrentDate(chatMessageListing) ?: ""

            if (currentDate != getCurrentDate(chatMessageListing) ?: "") {
                msglist.add(ChatMessageListing(sent_at = currentDate, chat_type = "time"))
                chatMessageListing?.let { msglist.add(it) }
            } else {
                chatMessageListing?.let { msglist.add(it) }
            }
        }
    }


    private fun updateMessage(resource: ChatMessageListing?) {

        if (resource?.send_by == mDataManager.getKeyValue(PrefenceConstants.USER_CHAT_ID, PrefenceConstants.TYPE_STRING)) {
            resource?.ownMessage = true
        }
        resource?.sent_at = mDateTimeUtils.convertDateOneToAnother(resource?.sent_at
                ?: "", "yyyy-MM-dd HH:mm:ss", "hh:mm aaa")

        resource?.let { msglist.add(it) }

        viewModel.setListCount(msglist.count())


     /*   currentDate = getCurrentDate(resource) ?: ""

        if (currentDate != getCurrentDate(resource) ?: "") {
            msglist.add(ChatMessageListing(sent_at = currentDate, chat_type = "time"))
            resource?.let { msglist.add(it) }
        } else {
            resource?.let { msglist.add(it) }
        }*/


      //  msglist.reverse()
        adapter.addItmSubmitList(msglist)
       // adapter.notifyItemInserted(msglist.count())
        rv_user_chat.smoothScrollToPosition(msglist.size-1)
    }


    private fun sendTextMessage() {
        val message = etMessage.text.toString().trim()
        if (message.isNotBlank() && isNetworkConnected) {
            etMessage.setText("")

            val messageModel = ChatMessageListing("",
                    "",
                    orderId,
                    userDetail?.agent_created_id,
                    mDataManager.getKeyValue(PrefenceConstants.USER_CHAT_ID, PrefenceConstants.TYPE_STRING).toString(),
                    message,
                    "",
                    mDateTimeUtils.currentDate,
                    "text",
                    false,
                    "",
                    "",
                    true,
                    "",
                    "")


            sendMesage(message, orderId, mDateTimeUtils, userDetail?.agent_created_id ?: "")

            updateMessage(messageModel)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        socketManager.disconnect()
        socketManager.off(SocketManager.ON_RECEIVE_MESSAGE, recieveChatListener)
    }

    private fun intialize() {
        mLinearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mLinearLayoutManager?.stackFromEnd = true
        rv_user_chat.layoutManager = mLinearLayoutManager
        adapter = ChatAdapter(ChatListener {})
        rv_user_chat.adapter = adapter

        // swipeRefresh.setOnRefreshListener(this)
    }


    override fun onRefresh() {
        //swipeRefresh.isRefreshing = false
        isFirst = false
        callApi(isFirst)
    }


    override fun onErrorOccur(message: String) {

        window.decorView.rootView.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    override fun onMessageRecieved(newMessage: ChatMessageListing) {


    }


    fun getCurrentDate(chatMessage: ChatMessageListing?): String? {

        try {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("MMM,dd yyyy", Locale.ENGLISH)

            val formattedTimeZone = mDateTimeUtils.getCalendarFormat(chatMessage?.sent_at
                    ?: "", "hh:mm aaa")

            val previousDate = Calendar.getInstance()
            previousDate.add(Calendar.DATE, -1)


            return when {
                calendar.time == formattedTimeZone?.time -> {
                    getString(R.string.today)
                }
                formattedTimeZone?.time == previousDate.time -> {
                    getString(R.string.yesterday)
                }
                else -> {

                    sdf.format(formattedTimeZone?.time)
                }
            }
        } catch (ignored: Exception) {
            return ""
        }

    }

    fun sendMesage(message: String, mOrderId:String, mDateTime: DateTimeUtils, userId:String) {
        // setIsLoading(true)

        val chatMessage = ChatMessageListing()
        chatMessage.text=message
        chatMessage.receiver_created_id=userId
        chatMessage.chat_type="text"
        chatMessage.order_id=mOrderId
        chatMessage.sent_at=mDateTime.currentDate
       // chatMessage.receiver_created_id=supplierId

        socketManager.emit(
                SocketManager.EMIT_SEND_MESSAGE,
                sendMsgJsonObject(chatMessage),
                Ack {
                    val acknowledgement = it.firstOrNull()
                    if (acknowledgement != null && acknowledgement is JSONObject) {

                        val response = Gson().fromJson<ChatMessageListing>(
                                acknowledgement.toString(),
                                object : TypeToken<ChatMessageListing>() {}.type
                        )

                        //sendMessage.value = response
                        //  navigator.onSendMessage(response)
                    }

                })

        socketManager.onErrorEvent()
    }

    private fun sendMsgJsonObject(message: ChatMessageListing): JSONObject {

        val jsonObject = JSONObject()
        try {
            val obj = JSONObject()
            //obj.put("to", message.getSend_to());
            obj.put("text", message.text)
            obj.put("type", "1")
            obj.put("receiver_created_id", message.receiver_created_id)
            obj.put("sent_at", message.sent_at)
            obj.put("chat_type", message.chat_type)
            obj.put("order_id", message.order_id)
            obj.put("chat_between", 1)
            jsonObject.put("detail", obj)
            println("sentMessage -> $jsonObject")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonObject
    }


    private val recieveChatListener = Emitter.Listener { args ->

        if (args.isNotEmpty() &&  args[0] is JSONObject) {
            val response = Gson().fromJson<ChatMessageListing>(
                    (args[0] as JSONObject).getJSONObject("detail").toString(),
                    object : TypeToken<ChatMessageListing>() {}.type
            )
                runOnUiThread {
                    updateMessage(response)
                }
        }
    }


    private val socketListener = Emitter.Listener { args ->
        if (args.isNotEmpty() &&  args[0] is JSONObject) {
            val response = Gson().fromJson<SuccessModel>(
                    args[0].toString(),
                    object : TypeToken<SuccessModel>() {}.type
            )

            if (response?.success == NetworkConstants.AUTHFAILED) {
                mDataManager.logout()
               onSessionExpire()
            } else {
                Timber.e(response.message)
            }
        }
    }



}
