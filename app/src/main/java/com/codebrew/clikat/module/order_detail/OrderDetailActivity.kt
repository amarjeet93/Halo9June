package com.codebrew.clikat.module.order_detail

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.OrderPayment
import com.codebrew.clikat.data.OrderStatus
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddCardResponseData
import com.codebrew.clikat.data.model.api.AddsOn
import com.codebrew.clikat.data.model.api.MakePaymentInput
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.OrderEvent
import com.codebrew.clikat.data.model.others.RateProductListModel
import com.codebrew.clikat.data.model.others.SaveCardInputModel
import com.codebrew.clikat.databinding.ActivityOrderDetailsBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.RatingBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.SettingModel.DataBean.BookingFlowBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.cart.MY_FATOORAH_PAYMENT_REQUEST
import com.codebrew.clikat.module.cart.SADDED_PAYMENT_REQUEST
import com.codebrew.clikat.module.order_detail.adapter.OrderDetailProductAdapter
import com.codebrew.clikat.module.order_detail.adapter.OrderPagerAdapter
import com.codebrew.clikat.module.order_detail.rate_product.RateProductActivity
import com.codebrew.clikat.module.payment_gateway.PaymentListActivity
import com.codebrew.clikat.module.payment_gateway.PaymentWebViewActivity
import com.codebrew.clikat.module.payment_gateway.dialog_card.CardDialogFrag
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.user_chat.RestaurantChatActivity
import com.codebrew.clikat.user_chat.UserChatActivity
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.setStatusBarColor
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.activity_order_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/*
 * Created by cbl80 on 26/4/16.
 */
class OrderDetailActivity : BaseActivity<ActivityOrderDetailsBinding, OrderDetailViewModel>(), OrderDetailNavigator,
        OrderDetailProductAdapter.OnReturnClicked, CardDialogFrag.onPaymentListener, HasAndroidInjector, EasyPermissions.PermissionCallbacks, PaymentResultWithDataListener {


    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>


    private var pagerAdapter: OrderPagerAdapter? = null
    private val appBackground by lazy { Color.parseColor(Configurations.colors.appBackground) }
    private var bookingFlowBean: BookingFlowBean? = null
    var settingData: SettingModel.DataBean.SettingData? = null
    private val textConfig by lazy { appUtil.loadAppConfig(0).strings }
    private var screenFlowBean: ScreenFlowBean? = null


    private val orderHistoryBeans by lazy { mutableListOf<OrderHistory>() }

    lateinit var orderDetail: OrderHistory

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtil: AppUtils

    @Inject
    lateinit var orderUtils: OrderUtils

    @Inject
    lateinit var permissionUtil: PermissionFile

    var phoneNum: String? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private var mViewModel: OrderDetailViewModel? = null

    private lateinit var mBinding: ActivityOrderDetailsBinding

    private var mDeliveryType = 0

    private val orderId = ArrayList<Int>()

    lateinit var mSelectedPayment: CustomPayModel

    lateinit var orderData: OrderHistory

    var paymentSelec = 0

    var netAmt = 0.0f

    var mGson = Gson()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_order_details
    }

    override fun getViewModel(): OrderDetailViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(OrderDetailViewModel::class.java)
        return mViewModel as OrderDetailViewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = viewDataBinding

        mViewModel?.navigator = this

        mBinding.color = Configurations.colors
        // mBinding.strings = appUtil.loadAppConfig(0).strings


        setStatusBarColor(this, appBackground)
        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        bookingFlowBean = Prefs.with(this).getObject(DataNames.BOOKING_FLOW, BookingFlowBean::class.java)
        screenFlowBean = Prefs.with(this).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        Prefs.with(this).save(DataNames.DB_AUTH, true)

        setlanguage()
        settoolbar()
        settypeface()
        clickListner()

        EventBus.getDefault().register(this)


        pagerAdapter = OrderPagerAdapter(this, orderHistoryBeans, appUtil, this, orderUtils)

        pagerAdapter?.setUserId(dataManager.getKeyValue(PrefenceConstants.USER_ID, PrefenceConstants.TYPE_STRING).toString(), bookingFlowBean, screenFlowBean, settingData)

        pagerAdapter?.settingCallback(OrderPagerAdapter.OrderListener({

            if (permissionUtil.hasCallPermissions(this)) {

                this.phoneNum = it.phone_number
                callPhone(it.phone_number ?: "")

            } else {
                permissionUtil.phoneCallTask(this)
            }

        }, {
            if (orderHistoryBeans.isEmpty() && orderId.isEmpty()) return@OrderListener

            if (orderHistoryBeans[vp_product_item.currentItem].agent?.isEmpty() == true) return@OrderListener

            val currentAgent = orderHistoryBeans[vp_product_item.currentItem].agent?.first()
                    ?: return@OrderListener

            launchActivity<UserChatActivity> {
                putExtra("orderId", orderId[vp_product_item.currentItem].toString())
                putExtra("supplier_id", orderHistoryBeans[0].supplier_id.toString())
                putExtra("userData", currentAgent)
            }
        }, {
            //rate supplier
            launchActivity<RateProductActivity> {
                putParcelableArrayListExtra("rateProducts", rateAgentSup(it, "Supplier"))
                putExtra("type", "Supplier")
            }
        }, {
            //rate agent
            launchActivity<RateProductActivity> {
                putParcelableArrayListExtra("rateProducts", rateAgentSup(it, "Agent"))
                putExtra("type", "Agent")
            }
        },
                {
                    launchActivity<RestaurantChatActivity> {
                        putExtra("orderId", orderId[vp_product_item.currentItem].toString())
                        putExtra("supplier_id", orderHistoryBeans[0].supplier_id.toString())
                        putExtra("isRestaurant", true)
                    }
                }))

        vp_product_item.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        vp_product_item.adapter = pagerAdapter

        /*      vp_product_item.offscreenPageLimit = 3

              val pageMargin: Float = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
              val pageOffset: Float = resources.getDimensionPixelOffset(R.dimen.offset).toFloat()*/


        /*    vp_product_item.setPageTransformer { page, position ->
                val myOffset: Float = position * -(2 * pageOffset + pageMargin)

                when {
                    position < -1 -> {
                        page.translationX = -myOffset
                    }
                    position <= 1 -> {
                        val scaleFactor = 0.7f.coerceAtLeast(1 - abs(position - 0.14285715f))
                        page.translationX = myOffset
                        page.scaleY = scaleFactor
                        page.alpha = scaleFactor
                    }
                    else -> {
                        page.alpha = 0.0f
                        page.translationX = myOffset
                    }
                }
            }*/



        vp_product_item.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                //  AppConstants.APP_SUB_TYPE= orderHistoryBeans[position].type?:0


                if (orderHistoryBeans[position].type == 0) {
                    orderHistoryBeans[position].type = screenFlowBean?.app_type
                }

                mBinding.strings = appUtil.loadAppConfig(orderHistoryBeans[position].type
                        ?: 0).strings

                if (orderHistoryBeans[position].status == 11.0) {
                    orderHistoryBeans[position].status = OrderStatus.In_Kitchen.orderStatus
                }

                setAppTerminology(orderHistoryBeans[position])

                statusOrder(orderHistoryBeans[position].status ?: 0.0, orderHistoryBeans[position])
                orderDetail = orderHistoryBeans[position]


                mDeliveryType = orderDetail.self_pickup ?: 0
            }

        })


        if (intent.hasExtra("orderId")) {
            orderId.addAll(intent.getIntegerArrayListExtra("orderId")!!)
        } /* else {
            orderId.addAll(getIntent().getIntegerArrayListExtra("orderId"));
        }*/

        orderDetailObserver()

        orderReturnProduct()
    }


    private fun rateAgentSup(orderHistory: OrderHistory?, type: String): ArrayList<RateProductListModel> {

        val name = if (type == "Agent") orderHistory?.agent?.firstOrNull()?.name else orderHistory?.supplier_name
        val image = if (type == "Agent") orderHistory?.agent?.firstOrNull()?.image else orderHistory?.logo

        val rateProductListModels = ArrayList<RateProductListModel>()
        rateProductListModels.add(RateProductListModel(name, orderHistory?.supplier_name, image,
                supplier_id = orderHistory?.supplier_id.toString(), order_id = orderDetail.order_id.toString()))
        return rateProductListModels
    }

    //*********************Location Request Or Permission Enable*************************
    private fun checkingLocationPermission() {
        if (permissionUtil.hasLocation(this)) {
            createLocationRequest()
        } else {
            permissionUtil.locationTask(this)
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnCompleteListener { task1 ->
            try {
                val response = task1.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                getCurrentLocation()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    this,
                                    AppConstants.RC_LOCATION_PERM
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.e("Data", "SETTINGS_CHANGE_UNAVAILABLE")
                }
            }
        }
    }

    private fun getCurrentLocation() {

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    showLoading()
                    mViewModel?.getPaymentGeofence(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    break
                }
            }
        }


        startLocationUpdates()
    }


    private fun startLocationUpdates() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return
            }
        }


        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }

    //***********************************************************************************


    private fun setAppTerminology(orderHistory: OrderHistory) {

        if (pagerAdapter == null && orderHistory.terminology.isNullOrEmpty()) return

        val terminologyBean = mGson.fromJson(orderHistory.terminology, SettingModel.DataBean.Terminology::class.java)
        val languageId = dataManager.getLangCode()

        var appTerminology: SettingModel.DataBean.AppTerminology? = null
        if (orderHistory.terminology != null) {
            appTerminology = if (languageId == ClikatConstants.ENGLISH_SHORT || languageId == ClikatConstants.ENGLISH_FULL) {
                terminologyBean.english
            } else {
                terminologyBean.other
            }
        }


        pagerAdapter?.settingTerminology(appTerminology)
    }


    override fun onResume() {
        super.onResume()

        if (isNetworkConnected) {
            viewModel.getOrderDetail(orderId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OrderEvent) {
        if (event.type == AppConstants.NOTIFICATION_EVENT) {
            if (isNetworkConnected) {
                //  viewModel.orderDetailLiveData.value=null
                viewModel.getOrderDetail(orderId)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun orderDetailObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<List<OrderHistory>> { resource ->
            initailize(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.orderDetailLiveData.observe(this, catObserver)
    }


    private fun clickListner() {
        btnCancel.setOnClickListener {
            if (!::orderDetail.isInitialized) return@setOnClickListener
            if (orderDetail.status == OrderStatus.Scheduled.orderStatus) sweetDialog(1) else sweetDialog(0)
        }


        btnReorder.setOnClickListener {
            if (!::orderDetail.isInitialized) return@setOnClickListener
            if (isInternetConnected(this@OrderDetailActivity)) {
                if (isNetworkConnected) {
                    if (dataManager.getCurrentUserLoggedIn()) {
                        val productList = covertCartToArray(orderDetail.product)


                        val cartInfoServerArray = CartInfoServerArray()

                        val calendar = Calendar.getInstance()
                        cartInfoServerArray.order_day = appUtil.getDayId(calendar.get(Calendar.DAY_OF_WEEK))
                                ?: ""
                        cartInfoServerArray.order_time = appUtil.convertDateOneToAnother(calendar.time.toString(), "EEE MMM d HH:mm:ss Z yyyy", "HH:mm:ss")
                                ?: ""

                        cartInfoServerArray.addons = orderDetail.product?.map {
                            it?.adds_on?.associateBy { it?.insertData() }
                        }?.flatMap { it?.keys ?: emptyList<ProductAddon>() }

                        cartInfoServerArray.productList = productList

                        cartInfoServerArray.variants = productList.flatMap {
                            it.variants ?: mutableListOf()
                        }

                        cartInfoServerArray.supplierBranchId = orderDetail.supplier_branch_id ?: 0

                        viewModel.addCart(cartInfoServerArray)
                    }
                }
            }
        }
    }

    fun AddsOn.insertData() = ProductAddon(
            id = adds_on_id.toString(),
            name = adds_on_name,
            price = price,
            type_id = adds_on_type_jd.toString(),
            type_name = adds_on_type_name,
            quantity = quantity,
            serial_number = serial_number
    )


    private fun settypeface() {
        btnCancel!!.typeface = AppGlobal.regular
        btnReorder!!.typeface = AppGlobal.regular
        tvTitle!!.typeface = AppGlobal.regular
    }

    private fun initailize(orderHistoryBeanList: List<OrderHistory>) {

        if (orderHistoryBeanList[0].type == 0) {
            orderHistoryBeanList[0].type = screenFlowBean?.app_type
        }

        mBinding.strings = appUtil.loadAppConfig(orderHistoryBeans.firstOrNull()?.type ?: 0).strings


        if (orderHistoryBeanList[0].status == 11.0) {
            orderHistoryBeanList[0].status = OrderStatus.In_Kitchen.orderStatus
        }

        setAppTerminology(orderHistoryBeanList[0])

        orderHistoryBeanList[0].status?.let {
            statusOrder(it, orderHistoryBeanList[0])
        }

        //AppConstants.APP_SUB_TYPE= orderHistoryBeanList[0].type?:0


        orderHistoryBeans.clear()
        orderHistoryBeans.addAll(orderHistoryBeanList)
        pagerAdapter?.notifyDataSetChanged()
    }

    private fun statusOrder(status: Double, orderHist: OrderHistory) {

        btnPayNow.visibility = View.GONE

        when (status) {
            OrderStatus.Pending.orderStatus -> {
                lyt_action_btn.visibility = View.VISIBLE
                btnCancel.visibility = View.VISIBLE
                btnReorder.visibility = View.GONE
            }

            OrderStatus.Confirmed.orderStatus, OrderStatus.On_The_Way.orderStatus,
            OrderStatus.Near_You.orderStatus,
            OrderStatus.Ready_to_be_picked.orderStatus, OrderStatus.Reached.orderStatus,
            OrderStatus.In_Kitchen.orderStatus -> {
                btnReorder.visibility = View.GONE
                btnCancel.visibility = View.GONE

                lyt_action_btn.visibility = View.GONE
            }


            OrderStatus.Delivered.orderStatus, OrderStatus.Rejected.orderStatus,
            OrderStatus.Customer_Canceled.orderStatus, OrderStatus.Rating_Given.orderStatus -> {

                btnReorder.visibility = if (orderHist.type ?: 0 != AppDataType.HomeServ.type) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                btnCancel.visibility = View.GONE

                lyt_action_btn.visibility = View.VISIBLE
            }
        }


        if (orderHist.type ?: 0 == AppDataType.HomeServ.type && status == OrderStatus.Customer_Canceled.orderStatus &&
                status == OrderStatus.Delivered.orderStatus) {
            btnReorder.visibility = View.GONE
            btnCancel.visibility = View.GONE
        }

        settingData?.disable_order_cancel?.let {
            if (it == "1") {
                btnCancel.visibility = View.GONE
            }
        }

        val paymentFlow = orderUtils.checkPaymtFlow(orderHist)
        if (paymentFlow > 0)
            enablePayNow(orderHist, paymentFlow)
    }

    private fun enablePayNow(orderHist: OrderHistory, paymentFlow: Int) {

        with(orderHist)
        {

            when (paymentFlow) {
                OrderPayment.ReceiptOrder.payment, OrderPayment.PaymentAfterConfirm.payment -> {
                    orderData = this
                    paymentSelec = OrderPayment.ReceiptOrder.payment
                    netAmt = net_amount ?: 0.0f

                    lyt_action_btn.visibility = View.VISIBLE
                    btnPayNow.visibility = View.VISIBLE
                    btnCancel.visibility = View.VISIBLE
                    btnPayNow.text = getString(R.string.pay_now, AppConstants.CURRENCY_SYMBOL, net_amount)

                }
                OrderPayment.EditOrder.payment -> {
                    orderData = this
                    paymentSelec = OrderPayment.EditOrder.payment

                    netAmt = remaining_amount ?: 0.0f
                    lyt_action_btn.visibility = View.VISIBLE
                    btnPayNow.visibility = View.VISIBLE
                    btnPayNow.text = getString(R.string.pay_now, AppConstants.CURRENCY_SYMBOL, netAmt)
                }
            }

            btnPayNow.setOnClickListener {
                if (::orderData.isInitialized) {
                    checkingLocationPermission()
                }
            }
        }


    }

    private fun settoolbar() {
        setSupportActionBar(toolbar)
        val icon = resources.getDrawable(R.drawable.ic_back)
        icon.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_IN)
        tvTitle!!.text = getString(R.string.order_details, appUtil.loadAppConfig(orderHistoryBeans.firstOrNull()?.type
                ?: 0).strings?.order)
        supportActionBar!!.setHomeAsUpIndicator(icon)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun setlanguage() {
        val selectedLang = dataManager.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()

        if (selectedLang == "arabic" || selectedLang == "ar") {
            GeneralFunctions.force_layout_to_RTL(this)
        } else {
            GeneralFunctions.force_layout_to_LTR(this)
        }
    }


    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    private fun sweetDialog(isScheduled: Int) {

        val sweetAlertDialog = AlertDialog.Builder(this@OrderDetailActivity)
        sweetAlertDialog.setTitle(getString(R.string.cancel_order, textConfig?.order))
        sweetAlertDialog.setMessage(getString(R.string.doYouCancel, textConfig?.order))

        sweetAlertDialog.setPositiveButton(getString(R.string.yes)) { dialog, which ->

            if (isNetworkConnected) {
                viewModel.cancelOrder(orderDetail.order_id.toString())
            }
            dialog.dismiss()
        }
        sweetAlertDialog.setNegativeButton(getString(R.string.no)) { dialog, which ->
            dialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    private fun sweetDialogSu() {
        val sweetAlertDialog = AlertDialog.Builder(this@OrderDetailActivity)
        sweetAlertDialog.setTitle(getString(R.string.success))
        sweetAlertDialog.setMessage(getString(R.string.cancel_msg, textConfig?.order))
        sweetAlertDialog.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            dialog.dismiss()
            setResult(Activity.RESULT_OK)
            finish()
        }
        sweetAlertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun covertCartToArray(product: List<ProductDataBean?>?): MutableList<CartInfoServer> {
        val listCartInfoServers: MutableList<CartInfoServer> = ArrayList()
        if (product != null) {
            for (i in product.indices) {
                val cartInfoServer = CartInfoServer()
                cartInfoServer.quantity = product[i]?.quantity
                cartInfoServer.productId = product[i]?.product_id.toString()
                cartInfoServer.handlingAdmin = product[i]?.handling_admin
                cartInfoServer.supplier_branch_id = product[i]?.supplier_branch_id
                cartInfoServer.handlingSupplier = product[i]?.handling_supplier
                cartInfoServer.supplier_id = product[i]?.supplier_id
                cartInfoServer.deliveryCharges = product[i]?.delivery_charges
                cartInfoServer.pricetype = 0
                cartInfoServer.name = product[i]?.name
                cartInfoServer.fixed_price = product[i]?.fixed_price?.toFloatOrNull()
                cartInfoServer.variants = product[i]?.prod_variants
                cartInfoServer.price = product[i]?.price?.toFloatOrNull()
                cartInfoServer.supplier_id = product[i]?.supplier_id
                cartInfoServer.category_id = product[i]?.category_id
                cartInfoServer.supplier_branch_id = product[i]?.supplier_branch_id
                cartInfoServer.handlingSupplier = product[i]?.handling_supplier
                listCartInfoServers.add(cartInfoServer)
            }
        }
        return listCartInfoServers
    }

    private fun addtoCartLocal(product: ProductDataBean?) {

        product?.delivery_charges = orderDetail.delivery_charges

        product?.discount = if (product?.fixed_price == product?.display_price) 1 else 0

        appUtil.addProductDb(this, orderHistoryBeans[vp_product_item.currentItem].type
                ?: 0, product)
    }


    override fun onCartAdded(cartdata: AddtoCartModel.CartdataBean?) {

        if (!::orderDetail.isInitialized) return

        dataManager.setkeyValue(DataNames.CART_ID, cartdata?.cartId ?: "")

        /*      val bundle = bundleOf(DataNames.SUPPLIER_BRANCH_ID to supplierBranchId)
              val deliveryFragment = DeliveryFragment()
              deliveryFragment.arguments = bundle*/

        addCartLocal2(orderDetail)

        Toast.makeText(this@OrderDetailActivity, getString(R.string.cart_added), Toast.LENGTH_SHORT).show()
        /*  val intent = Intent()
          intent.putExtra(DataNames.SUPPLIER_BRANCH_ID, supplierBranchId)
          setResult(Activity.RESULT_OK, intent)*/
        finish()
    }

    private fun addCartLocal2(orderDetail: OrderHistory?) {

        val prodList = arrayListOf<ProductDataBean?>()

        var cartinfo: CartInfo

        orderDetail?.product?.mapIndexed { index, product ->

            if (product?.adds_on.isNullOrEmpty()) {
                product?.prod_quantity = product?.quantity
                product?.netPrice = product?.fixed_price?.toFloatOrNull()
                product?.supplier_name = orderDetail.supplier_name
                prodList += product?.copy()
            } else {
                product?.adds_on?.groupBy {
                    it?.serial_number
                }?.mapValues {
                    product.adds_on = it.value
                    product.add_on_name = it.value.map { it?.adds_on_type_name }.joinToString()
                    product.prod_quantity = it.value[0]?.quantity ?: 0
                    product.supplier_name = orderDetail.supplier_name

                    //product.supplier_image=orderDetail.upp
                    // product.netPrice= product.fixed_price
                    prodList += product.copy()
                }
            }

        }


        prodList.mapIndexed { index, productDataBean ->

            if (productDataBean?.adds_on?.isEmpty() == true) {
                addtoCartLocal(productDataBean)
            } else {
                cartinfo = appUtil.checkProductAddon(productDataBean?.adds_on?.map {
                    it?.insertData()
                }?.toMutableList() ?: mutableListOf())

                if (cartinfo.productId != 0) {
                    productDataBean?.productAddonId = cartinfo.productAddonId
                    updateCartDB(productDataBean, cartinfo)
                } else {
                    addCartDB(productDataBean, productDataBean?.adds_on?.asSequence()?.map { it?.insertData() }?.toMutableList()
                            ?: mutableListOf())
                }
            }
        }

    }

    private fun updateCartDB(productModel: ProductDataBean?, cartinfo: CartInfo) {
        var quantity = cartinfo.quantity
        quantity++
        productModel?.prod_quantity = quantity
        productModel?.fixed_quantity = quantity

        appUtil.updateItem(productModel)
    }


    private fun addCartDB(productModel: ProductDataBean?, productAddon: MutableList<ProductAddon?>) {
        val cartInfo = CartInfo()
        cartInfo.quantity = 1
        cartInfo.productName = productModel?.name
        //  cartInfo.supplierAddress=productModel.suppl
        cartInfo.productId = productModel?.product_id ?: 0
        cartInfo.imagePath = productModel?.image_path.toString()
        cartInfo.fixed_price = productModel?.fixed_price?.toFloatOrNull()
        cartInfo.supplierName = productModel?.supplier_name
        cartInfo.suplierBranchId = productModel?.supplier_branch_id ?: 0
        cartInfo.measuringUnit = productModel?.measuring_unit
        cartInfo.deliveryCharges = productModel?.delivery_charges ?: 0.0f
        cartInfo.supplierId = productModel?.supplier_id ?: 0
        cartInfo.urgent_type = productModel?.urgent_type ?: 0
        cartInfo.isUrgent = productModel?.can_urgent ?: 0
        cartInfo.latitude = productModel?.latitude
        cartInfo.longitude = productModel?.longitude
        // cartInfo.setUrgentValue(productModel.getUrgent_value());
        cartInfo.categoryId = productModel?.category_id ?: 0

        cartInfo.add_ons?.addAll(productAddon)

        cartInfo.add_on_name = productAddon.joinToString { it?.type_name ?: "" }

        cartInfo.price = if (productAddon.isNotEmpty()) {
            productAddon.sumBy {
                it?.price?.toInt() ?: 0
            }.toFloat().plus(productModel?.price?.toFloatOrNull() ?: 0.0f)
        } else {
            productModel?.netPrice ?: 0.0f
        }

        cartInfo.deliveryType = mDeliveryType
        cartInfo.productAddonId = Calendar.getInstance().timeInMillis

        cartInfo.handlingAdmin = productModel?.handling_admin ?: 0.0f
        cartInfo.handlingSupplier = productModel?.handling_supplier ?: 0.0f


        // cartInfo.handlingCharges = productModel?.handling_admin ?.plus(productModel.handling_supplier?: 0.0f)?: 0.0f
        appUtil.addItem(cartInfo)

        // productModel?.prod_quantity = productModel?.prod_quantity
    }


    override fun onCancelOrder() {
        sweetDialogSu()
    }

    override fun onCompletePayment() {

        mBinding.root.onSnackbar("Payment done successfully.")

        if (isNetworkConnected) {
            viewModel.getOrderDetail(orderId)
        }
    }

    override fun onGeofencePayment(data: List<String>?) {
        hideLoading()
        if (::orderData.isInitialized) {
            paymentDetail(orderData, data as ArrayList<String>?)
        }
    }

    override fun onErrorOccur(message: String) {
        mBinding.root.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    private fun callPhone(number: String) {

        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Manifest.permission.CALL_PHONE
            startActivity(intent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        }
    }

    override fun onReturnProductClicked(item: ProductDataBean?) {
        showReturnDialog(item)
    }

    override fun onRateProd(item: ProductDataBean?) {

        launchActivity<RateProductActivity> {
            putParcelableArrayListExtra("rateProducts", calculateRateProduct(item))
            putExtra("type", "Prod")
        }
    }


    private fun calculateRateProduct(product: ProductDataBean?): ArrayList<RateProductListModel> {
        val rateProductListModels = ArrayList<RateProductListModel>()
        rateProductListModels.add(RateProductListModel(product?.name, product?.supplier_name, product?.image_path.toString(),
                product?.product_id.toString(), supplier_id = product?.supplier_id.toString(), order_id = orderDetail.order_id.toString()))
        return rateProductListModels
    }


    private fun showReturnDialog(item: ProductDataBean?) {
        val returnDialog = Dialog(this)
        returnDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        returnDialog.setContentView(R.layout.layout_return_product)
        returnDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        returnDialog.setCancelable(false)

        val ivCross = returnDialog.findViewById(R.id.ivCross) as ImageView
        val etReason = returnDialog.findViewById(R.id.etReason) as EditText
        val btnReturnProd = returnDialog.findViewById(R.id.btnReturnProd) as Button

        ivCross.setOnClickListener {
            returnDialog.dismiss()
        }

        btnReturnProd.setOnClickListener {
            val reason = etReason.text.toString().trim()
            if (reason.isEmpty()) {
                AppToasty.error(this, getString(R.string.enter_reason_to_return_product))
            } else {
                if (isNetworkConnected) {
                    returnDialog.dismiss()
                    viewModel.apiReturnProduct(item, reason)
                }
            }
        }
        returnDialog.show()
    }

    private fun orderReturnProduct() {
        // Create the observer which updates the UI.
        val returnProduct = Observer<ProductDataBean> { resource ->
            val returnData = RatingBean()
            returnData.status = 0
            resource.return_data?.add(returnData)
            pagerAdapter?.notifyDataSetChanged()

            mBinding.root.onSnackbar(getString(R.string.return_request_msg))

//            if(resource.orderPos!=null)
//            {
//                val pos=orderHistoryBeans[resource.orderPos?:0].product?.indexOfFirst { it?.product_id==resource?.product_id }
//                if(pos!=null && pos!=-1)
//                {
//                    orderHistoryBeans[resource.orderPos?:0].product?.get(pos)?.return_data= arrayListOf(returnData)
//                }
//                pagerAdapter?.notifyItemChanged(resource?.orderPos?:0)
//            }

        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.returnLiveData.observe(this, returnProduct)
    }

    private fun paymentDetail(orderHist: OrderHistory, payment_gateways: ArrayList<String>?) {

        dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
            val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
            val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)

            launchActivity<PaymentListActivity>(AppConstants.REQUEST_PAYMENT_OPTION) {
                putParcelableArrayListExtra("feature_data", featureList)
                putExtra("orderData", orderHist)
                putExtra("mSelectPayment", payment_gateways)
                putExtra("mTotalAmt", netAmt)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_PAYMENT_OPTION && resultCode == Activity.RESULT_OK) {

            mSelectedPayment = data?.getParcelableExtra("payItem") ?: CustomPayModel()

            when {
                mSelectedPayment.keyId == DataNames.PAYMENT_CASH.toString() -> {
                    callApi(mSelectedPayment)
                }
                mSelectedPayment.payName == getString(R.string.razor_pay) -> {
                    initRazorPay(mSelectedPayment)
                }
                mSelectedPayment.payName == getString(R.string.zelle) -> {
                    pagerAdapter?.setZelleDoc(mSelectedPayment)
                    callApi(mSelectedPayment)
                }

                mSelectedPayment.payName == getString(R.string.saded) -> {
                    getSadedPayment()
                }
                mSelectedPayment.payName == getString(R.string.myFatoora) -> {
                    initMyFatooraGateway()
                }
                else -> {
                    if (mSelectedPayment.addCard == true) {
                        callApi(mSelectedPayment)
                    } else {
                        CardDialogFrag.newInstance(mSelectedPayment, netAmt
                                ?: 0.0f).show(supportFragmentManager, "paymentDialog")
                    }
                }
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == AppConstants.RC_LOCATION_PERM) {
            getCurrentLocation()
        } else if (requestCode == SADDED_PAYMENT_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    mBinding.root.onSnackbar(getString(R.string.payment_done_successful))
                    callApi(mSelectedPayment)
                    // use the result to update your UI and send the payment method nonce to your server
                }
                else -> mBinding.root.onSnackbar(getString(R.string.payment_unsuccessful))

            }
        } else if (requestCode == MY_FATOORAH_PAYMENT_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    mBinding.root.onSnackbar(getString(R.string.payment_done_successful))
                    if (data != null && data.hasExtra("paymentId"))
                        mSelectedPayment.keyId = data.getStringExtra("paymentId")

                    callApi(mSelectedPayment)
                    // use the result to update your UI and send the payment method nonce to your server
                }
                else -> mBinding.root.onSnackbar(getString(R.string.payment_unsuccessful))

            }

        }
    }

    private fun callApi(mSelectedPayment: CustomPayModel) {


        //  val currency = dataManager.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)
        val currency = dataManager.getKeyValue(PrefenceConstants.CURRENCY_NAME, PrefenceConstants.TYPE_STRING).toString()

        when (paymentSelec) {
            OrderPayment.ReceiptOrder.payment, OrderPayment.PaymentAfterConfirm.payment -> {
                with(this.mSelectedPayment)
                {

                    val param: MakePaymentInput? = when (mSelectedPayment.payName) {
                        getString(R.string.cash_on) ->
                            MakePaymentInput(currency = currency.toString() ?: "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString(), payment_token = "", payment_type = DataNames.PAYMENT_CASH)

                        getString(R.string.zelle), getString(R.string.razor_pay),
                        getString(R.string.myFatoora), getString(R.string.saded) ->
                            MakePaymentInput(card_id = "", currency = currency.toString()
                                    ?: "", customer_payment_id = "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString(), payment_token = keyId
                                    ?: "", payment_type = DataNames.PAYMENT_CARD)


                        "paystack" ->
                            MakePaymentInput(card_id = ""
                                    ?: "", currency = currency
                                    ?: "", customer_payment_id = "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString()
                                            ?: "", payment_token = keyId
                                    ?: "", payment_type = DataNames.PAYMENT_CARD)

                        else ->
                            MakePaymentInput(card_id = cardId
                                    ?: "", currency = currency
                                    ?: "", customer_payment_id = customerId ?: "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString(), payment_token = keyId
                                    ?: "", payment_type = DataNames.PAYMENT_CARD)

                    }


                    param?.let { mViewModel?.makePayment(it) }
                }
            }

            OrderPayment.EditOrder.payment -> {
                with(this.mSelectedPayment)
                {
                    val param: MakePaymentInput = when (mSelectedPayment.payName) {
                        getString(R.string.myFatoora), getString(R.string.saded) -> {
                            MakePaymentInput(card_id = ""
                                    ?: "", currency = currency
                                    ?: "", customer_payment_id = "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString(), payment_token = keyId
                                    ?: "")
                        }
                        else -> {
                            MakePaymentInput(card_id = cardId
                                    ?: "", currency = currency
                                    ?: "", customer_payment_id = customerId ?: "",
                                    gateway_unique_id = payment_token
                                            ?: "", languageId = dataManager.getLangCode().toInt(),
                                    order_id = orderData.order_id.toString(), payment_token = keyId
                                    ?: "")
                        }
                    }
                    mViewModel?.remainingPayment(param)
                }
            }

        }
    }

    private fun getSadedPayment() {
        val userInfo = dataManager.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)
        if (isNetworkConnected) {
            mViewModel?.getSaddedPaymentUrl(userInfo?.data?.email ?: "", userInfo?.data?.firstname
                    ?: "", netAmt.toString())
        }
    }

    private fun initMyFatooraGateway() {
        // val currency = dataManager.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)
        val currency = dataManager.getKeyValue(PrefenceConstants.CURRENCY_NAME, PrefenceConstants.TYPE_STRING).toString()
        if (isNetworkConnected) {
            mViewModel?.getMyFatoorahPaymentUrl(currency ?: "", netAmt.toString())
        }
    }

    override fun getSaddedPaymentSuccess(data: AddCardResponseData?) {
        mSelectedPayment.keyId = data?.transaction_reference
        AppToasty.success(this, "success")
        val intent = Intent(this, PaymentWebViewActivity::class.java).putExtra("paymentData", data)
        startActivityForResult(intent, SADDED_PAYMENT_REQUEST)
    }

    override fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?) {
        AppToasty.success(this, "success")
        val intent = Intent(this, PaymentWebViewActivity::class.java).putExtra("paymentData", data)
                .putExtra("payment_gateway", getString(R.string.myFatoora))
        startActivityForResult(intent, MY_FATOORAH_PAYMENT_REQUEST)
    }

    private fun initRazorPay(mSelectedPayment: CustomPayModel?) {

        val userInfo = dataManager.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)

        Checkout.preload(this)
        val co = Checkout()

        co.setKeyID(mSelectedPayment?.keyId)
        co.setImage(R.mipmap.ic_launcher)

        try {
            val options = JSONObject()
            // options.put("name", "JnJ's Cafe")
            //  options.put("description", "Food Order")
            //You can omit the image option to fetch the image from dashboard
            // options.put("image", "https://cafejj-api.royoapps.com/clikat-buckettest/jnj.png")
            options.put("currency", "INR")
            options.put("amount", netAmt.times(100))
            options.put("payment_capture", true)
            // options.put("order_id", orderId)

            val preFill = JSONObject()
            preFill.put("email", userInfo?.data?.email)
            preFill.put("contact", userInfo?.data?.mobile_no)

            options.put("prefill", preFill)

            co.open(this, options)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun paymentToken(token: String, paymentMethod: String, savedCard: SaveCardInputModel?) {

        if (isNetworkConnected) {
            if (mSelectedPayment.payment_token == "paystack") {
                mSelectedPayment.keyId = token
            }

            callApi(mSelectedPayment)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == AppConstants.REQUEST_CODE_LOCATION) {
            if (CommonUtils.isNetworkConnected(applicationContext)) {
                createLocationRequest()
            }
        }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        mBinding.root.onSnackbar(p1 ?: "")
    }

    override fun onPaymentSuccess(p0: String?, paymentData: PaymentData?) {

        mSelectedPayment.keyId = paymentData?.paymentId
        mSelectedPayment.payment_token = "razorpay"
        callApi(mSelectedPayment)
    }
}
