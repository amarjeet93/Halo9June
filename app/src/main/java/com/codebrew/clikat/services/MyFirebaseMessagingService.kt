package com.codebrew.clikat.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.codebrew.clikat.R
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.orderDetail.Agent
import com.codebrew.clikat.data.model.others.OrderEvent
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.order_detail.OrderDetailActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.user_chat.UserChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.inject.Inject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */

    @Inject
    lateinit var dataManager: DataManager

    var orderId:String=""

    @Override
    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

         //screenFlowBean=dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
    }

    override fun onNewToken(token: String) {
        dataManager.setkeyValue(DataNames.REGISTRATION_ID, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("notifi", remoteMessage.data.toString())
        //{device_token=fF20iu4QQAyue_hQGfmemr:APA91bHrYnNPie-9pL3FbPT6_FHsJ1p2Onnq85hbu_2KOWIcfcr7Zil_en4aV7CFDapGzaLoDFHwOiBnNODZo-SuwhWBBda6PMAmTrpVGvtqhvWc
        // _VdCfQhp5Nx2puFjTxnyPUIY7GQu, id=59, date=1584967088626, text=Xcf, type=chat, order_id=211, chat_type=text, user_image=}
        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            if (remoteMessage.data.containsKey("status")) {
if(remoteMessage.data["status"].equals("15")) {
//            if(activity_flag)
//            {
// app is in foreground, broadcast the push message
    val pushNotification = Intent("pushNotification")
    pushNotification.action = "com.CUSTOM_INTENT"
    sendBroadcast(pushNotification)
}else if(remoteMessage.data["status"].equals("16"))
{
    val pushNotification = Intent("pushNotification")
    pushNotification.action = "com.CUSTOM_INTENT_vibrate"
    pushNotification.putExtra("order_id", remoteMessage.data["orderId"].toString())
    sendBroadcast(pushNotification)
}else{
    sendNotification(remoteMessage.data, remoteMessage.notification)
}
            }else{
                sendNotification(remoteMessage.data, remoteMessage.notification)
            }

        }
    }

    private fun sendNotification(messageBody: Map<String, String?>, notification: RemoteMessage.Notification?) {
        val notificationItent: Intent
        var pendingIntent: PendingIntent?=null
        var requestID = System.currentTimeMillis().toInt()
        val homeIntent: Intent = Intent(this, MainScreenActivity::class.java)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)


        if (messageBody.containsKey("type")) {

            when (messageBody["type"]) {
                "chat" -> {
                    requestID = if (messageBody.containsKey("order_id")) {
                        messageBody["order_id"]?.toInt() ?: 0
                    } else {
                        0
                    }
                    orderId = requestID.toString()


                    val agentData = Agent(agent_created_id = messageBody["agent_created_id"]
                            ?: "0", image = messageBody["sender_image"], name = messageBody["sender_name"])

                    notificationItent = Intent(this, UserChatActivity::class.java)
                    notificationItent.putExtra("userData", agentData)
                    notificationItent.putExtra("orderId", messageBody["order_id"]
                            ?: "0")
                    notificationItent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    pendingIntent = PendingIntent.getActivity(
                            this,
                            requestID,
                            notificationItent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    )

                }
            }
        }else{
            if (messageBody.containsKey("orderId")) {

                EventBus.getDefault().post(OrderEvent(AppConstants.NOTIFICATION_EVENT))

                val orderId = messageBody["orderId"]
                notificationItent = Intent(this, OrderDetailActivity::class.java)
                val arrayList = ArrayList<Int>()
                arrayList.add(orderId!!.toInt())
                notificationItent.putIntegerArrayListExtra("orderId", arrayList)
                notificationItent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                pendingIntent = PendingIntent.getActivities(this, requestID, arrayOf(homeIntent, notificationItent), PendingIntent.FLAG_ONE_SHOT)
            } else {
                pendingIntent = PendingIntent.getActivity(this, requestID /* Request code */, homeIntent,
                        PendingIntent.FLAG_ONE_SHOT)
            }
        }



        //{orderId=203, message= Your Order Has been Confirmed}
        var image = ""
        var remote_picture: Bitmap? = null
        if (messageBody.containsKey("data")) {
            try {
                val `object` = JSONObject(messageBody["data"] ?: "")
                image = `object`.getString("images")
                val notiStyle = NotificationCompat.BigPictureStyle()
                try {
                    remote_picture = BitmapFactory.decodeStream(URL(image).content as InputStream)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                notiStyle.bigPicture(remote_picture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val intent = Intent(this, MainScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_new)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(notification!!.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)


        if (remote_picture != null) {
            notificationBuilder.setLargeIcon(remote_picture)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "clickat_channel",
                    NotificationManager.IMPORTANCE_HIGH)
            channel.lockscreenVisibility=Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        if(AppConstants.isChatOpen && AppConstants.currentOrderId==orderId)
        {

        }else
        {
            notificationManager.notify(requestID /* ID of notification */, notificationBuilder.build())
        }
    }


}
