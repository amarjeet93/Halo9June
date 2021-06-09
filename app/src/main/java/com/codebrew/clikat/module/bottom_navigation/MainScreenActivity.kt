package com.codebrew.clikat.module.bottom_navigation

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.base.BaseInterface
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.model.others.PaymentEvent
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivityMainScreenBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.rate_order.GetRateDatum
import com.codebrew.clikat.module.rate_order.GetRateViewModel
import com.codebrew.clikat.module.rate_order.RatingActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction.setStatusBarColor
import com.codebrew.clikat.utils.configurations.Configurations
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_main_screen.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


class

MainScreenActivity : BaseActivity<ActivityMainScreenBinding, GetRateViewModel>(), BaseInterface,
        HasAndroidInjector, PaymentResultWithDataListener {

    var alertDialog: AlertDialog? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
   lateinit var vibrationEffect: VibrationEffect
    private var mHomeViewModel: GetRateViewModel? = null
lateinit var vibrator: Vibrator
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    lateinit var image1: ImageView
    lateinit var tv_order: TextView

    @Inject
    lateinit var appUtil: AppUtils

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main_screen
    }

    override fun getViewModel(): GetRateViewModel {
        mHomeViewModel = ViewModelProviders.of(this, factory).get(GetRateViewModel::class.java)
        return mHomeViewModel as GetRateViewModel
    }

    private val broadcastReceiverApi: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val intent1 = Intent(this@MainScreenActivity, RatingActivity::class.java)
            startActivity(intent1)
        }
    }
    private val broadcastReceiverApi_vibrate: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.EFFECT_HEAVY_CLICK));
                val mp: MediaPlayer = MediaPlayer.create(this@MainScreenActivity, R.raw.chime)
           mp.start()
            } else {
                //deprecated in API 26
                vibrator.vibrate(500);
            }
            val order_id = intent.getStringExtra("order_id")
            Show_Dialog_Seller(order_id!!)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiverApi)
        unregisterReceiver(broadcastReceiverApi_vibrate)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiverApi, IntentFilter("com.CUSTOM_INTENT"))
        registerReceiver(broadcastReceiverApi_vibrate, IntentFilter("com.CUSTOM_INTENT_vibrate"))
    }

    fun Show_Dialog_Seller(order_id: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        // ...Irrelevant code for customizing the buttons and title
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.layout_vibrate, null)
        dialogBuilder.setView(dialogView)
        image1 = dialogView.findViewById(R.id.image1)
        tv_order = dialogView.findViewById(R.id.tv_order_number)
        tv_order.text =  order_id
        image1.setOnClickListener { alertDialog!!.dismiss() }
        alertDialog = dialogBuilder.create()
        alertDialog?.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()

    }

    private fun questionsObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<GetRateDatum> { resource ->
            Log.e("dataaaaaaaaaaa", "data")
            if (resource.getIsRated() == 0) {
                var intent = Intent(this, RatingActivity::class.java)
                intent.putExtra("order_id", resource.getOrderId().toString())
                startActivity(intent)
            }
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.viewModel.questionsLiveData.observe(this, catObserver)
        mHomeViewModel!!.getrateLiveData.observe(this, catObserver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
       // Show_Dialog_Seller("110")
        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        viewDataBinding?.appType = screenFlowBean?.app_type
        viewDataBinding?.color = Configurations.colors

        val statusColor = Color.parseColor(Configurations.colors.appBackground)
        setStatusBarColor(this, statusColor)
        questionsObserver()
        mHomeViewModel!!.get_rating()
        val navController = findNavController(R.id.nav_host_container)
        if (screenFlowBean?.app_type ?: 0 > AppDataType.Custom.type) {
            nav_view.inflateMenu(R.menu.bottom_custom_menu)
            nav_view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        } else {
            nav_view.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            when (screenFlowBean?.app_type) {
                AppDataType.CarRental.type -> {
                    nav_view.inflateMenu(R.menu.bottom_rental_menu)
                }
                AppDataType.Ecom.type -> {
                    nav_view.inflateMenu(R.menu.bottom_ecom_menu)
                }
                else -> {
                    nav_view.inflateMenu(R.menu.bottom_nav_menu)
                }
            }
        }

        //  Log.e("data",""+intent.extras)

        nav_view.setupWithNavController(navController)


        /*     if(intent.extras!=null)
             {
                 navController.setGraph(navController.graph,intent.extras)
             }
     */


        if (screenFlowBean?.app_type == AppDataType.Ecom.type) {
            // nav_view.menu.getItem(3).title = appUtil.loadAppConfig(0).strings?.orders
            nav_view.menu.getItem(3).title = getString(R.string.take_away)
            //  nav_view.menu.getItem(4).title = appUtil.loadAppConfig(0).strings?.otherTab
        } else {
            //  nav_view.menu.getItem(2).title = appUtil.loadAppConfig(0).strings?.orders
            nav_view.menu.getItem(2).title = getString(R.string.take_away)
            //  nav_view.menu.getItem(3).title = appUtil.loadAppConfig(0).strings?.otherTab
        }

        // Whenever the selected controller changes, setup the action bar.

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.label) {
                "fragment_ecommerce" -> nav_view.visibility = View.VISIBLE
                "fragment_all_category" -> nav_view.visibility = View.VISIBLE
                "fragment_resturant_home" -> {
                    nav_view.visibility = View.VISIBLE
                }
                "fragment_cart" -> nav_view.visibility = View.VISIBLE
                "fragment_base_order" -> nav_view.visibility = View.VISIBLE
                "fragment_more" -> nav_view.visibility = View.VISIBLE
                "fragment_home_rental" -> nav_view.visibility = View.VISIBLE
                "CustomHomeFrag" -> nav_view.visibility = View.VISIBLE
                else -> {
                    nav_view.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container)
        navHostFragment?.childFragmentManager?.fragments?.get(0)?.onActivityResult(requestCode, resultCode, data)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onErrorOccur(message: String) {

    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onPaymentError(errorCode: Int, errorMsg: String?, paymentDta: PaymentData) {
        EventBus.getDefault().post(PaymentEvent(errorMsg ?: "", errorCode, paymentDta.paymentId
                ?: ""))
    }

    override fun onPaymentSuccess(successMsg: String?, paymentDta: PaymentData) {
        EventBus.getDefault().post(PaymentEvent(successMsg ?: "", 0, paymentDta.paymentId ?: ""))
    }

}
