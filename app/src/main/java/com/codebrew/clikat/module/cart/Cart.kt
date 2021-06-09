package com.codebrew.clikat.module.cart

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.bumptech.glide.Glide
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.loadUserImage
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.*
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.constants.PrefenceConstants.Companion.IS_VAT_INCLUDED
import com.codebrew.clikat.data.constants.PrefenceConstants.Companion.TYPE_INT
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.SupplierLocationBean
import com.codebrew.clikat.data.model.api.*
import com.codebrew.clikat.data.model.others.*
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.DialogBothDeliveryBinding
import com.codebrew.clikat.databinding.DialogDonateBinding
import com.codebrew.clikat.databinding.FragmentCartBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.AddtoCartModel
import com.codebrew.clikat.modal.other.CheckPromoCodeParam
import com.codebrew.clikat.modal.other.PromoCodeModel
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.cart.adapter.*
import com.codebrew.clikat.module.cart.model.TaxModel
import com.codebrew.clikat.module.cart.model.TiPModel
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.order_detail.OrderDetailActivity
import com.codebrew.clikat.module.payment_gateway.PaymentListActivity
import com.codebrew.clikat.module.payment_gateway.PaymentWebViewActivity
import com.codebrew.clikat.module.payment_gateway.dialog_card.CardDialogFrag
import com.codebrew.clikat.module.payment_gateway.savedcards.SaveCardsActivity
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.service_selection.ServSelectionActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.DialogIntrface
import com.codebrew.clikat.utils.SingleShotLocationProvider
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quest.intrface.ImageCallback
import com.quest.utils.dialogintrface.ImageDialgFragment
import com.razorpay.Checkout
import kotlinx.android.synthetic.main.dialog_add_show_group.*
import kotlinx.android.synthetic.main.dialog_take_away.*
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.item_agent_list.*
import kotlinx.android.synthetic.main.layout_adrs_time.*
import kotlinx.android.synthetic.main.layout_instructions.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import sqip.CardDetails
import sqip.CardEntry
import sqip.handleActivityResult
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val PAYPALREQUEST = 587
const val SADDED_PAYMENT_REQUEST = 588
const val MY_FATOORAH_PAYMENT_REQUEST = 589

class Cart : BaseFragment<FragmentCartBinding, CartViewModel>(),
        CartAdaptor.CartCallback, CartNavigator, AddressDialogFragment.Listener,
        DialogIntrface, DialogListener,
        CardDialogFrag.onPaymentListener, ImageCallback,
        EasyPermissions.PermissionCallbacks, TipAdapter.TipCallback {

    var groupDialog: Dialog? = null
    private var terminologyBean: SettingModel.DataBean.Terminology? = null
    private var cartList: MutableList<CartInfo>? = null
    private var Check_cartList: MutableList<CheckCartModel>? = null
    private var cartAdapter: CartAdaptor? = null
    private var questAdapter: SelectedQuestAdapter? = null
    private var supplierBranchId: Int? = null
    private var supplierId: Int? = null
    private var addressId = 0
    private var supplierAddress = ""
    private var takeOffPrepTime = ""
    private var supplierLatitude: Double = 0.0
    private var supplierLongitude: Double = 0.0
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    val categories: ArrayList<TaxModel> = ArrayList()
    private var agentType: Int = 0
    private var mReferralAmt: Float = 0.0f
    private var redeemedAmt: Double = 0.0
    private var isReferrale = false
    private var is_vat_included = 0
    private var mAgentParam: AgentCustomParam? = null
    private var isTipClear = false
    private var isDecClick = false

    lateinit var tipList: ArrayList<TiPModel>

    private var tipAdapter: TipAdapter? = null

    private var tipPercentage = 0

    //Ready Chef
    private var havePets = 0
    private var cleaner_in = 0

    var location_data = ""

    // 0 for delivery
    private val deliveryType = 0
    private var table_name = ""

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var cartUtils: CartUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    @Inject
    lateinit var mDateTime: DateTimeUtils

    @Inject
    lateinit var retrofit: Retrofit


    @Inject
    lateinit var factory: ViewModelProviderFactory

    private val productList = mutableListOf<CartInfoServer>()

    @Inject
    lateinit var prefHelper: PreferenceHelper
    private val item_filter = StringBuilder()
    var item_concate: String? = ""

    private val vat_filter = StringBuilder()
    var vat_concate: String? = ""

    private var mViewModel: CartViewModel? = null
    private var mBinding: FragmentCartBinding? = null
    private var group_id = ""
    private lateinit var adrsData: AddressBean

    private lateinit var locationData: SupplierLocation

    private var totalAmt = 0.0

    private var subTotal = 0.0F

    private var mDeliveryCharge = 0.0f

    private var deliveryId: String = ""
    private var mTipCharges = 0.0f
    private var questionAddonPrice = 0.0f
    private var maxHandlingAdminCharges = 0.0f
    private var mQuestionList = listOf<QuestionList>()
var firstt=0

    var orderId = arrayListOf<Int>()

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    var productData: CartInfo? = null

    /*    Pickup(1),
        Delivery(0),
        Both(2)*/
    var mDeliveryType: Int? = null
    var mDeliveryType1: Int? = null
    var is_table_enabled: Int? = null
    var first = 0
    var first2 = 0
    var first1 = 0
    var first3 = 0
    var mSelectedPayment: CustomPayModel? = null

    //  var mTotalAmt: Float? = null

    var settingData: SettingModel.DataBean.SettingData? = null

    @Inject
    lateinit var imageUtils: ImageUtility

    @Inject
    lateinit var permissionFile: PermissionFile

    private var photoFile: File? = null
    private val imageDialog by lazy { ImageDialgFragment() }

    private var imageList: MutableList<ImageListModel>? = null


    private var mAdapter: ImageListAdapter? = null

    private var restServiceTax = 0.0
    private var minOrder: Float? = null
    private var baseDeliveryCharges = 0.0f
    private var regionDeliveryCharges = 0.0f

    private var isPaymentConfirm: Boolean = false

    private var payment_gateways: ArrayList<String>? = null

    private val decimalFormat: DecimalFormat = DecimalFormat("0.00")

    private var isDonate = false

    private var ShowRestaurantPersonalAddress: String = ""

    private var addTipInPercentage: String? = "1"
    private var mLocationManager: LocationManager? = null

    private val mLocationListener: LocationListener = LocationListener { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this

        imageObserver()

        SingleShotLocationProvider.requestSingleUpdate(context,
                object : LocationCallback(), SingleShotLocationProvider.LocationCallback {
                    override fun onNewLocationAvailable(location: SingleShotLocationProvider.GPSCoordinates?) {
                        if (location != null && location.latitude.toDouble() != 0.0 && location.longitude.toDouble() != 0.0) {
                            lat = location.latitude.toDouble()
                            lng = location.longitude.toDouble()
                        }
                    }

                })
        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        terminologyBean = prefHelper.getGsonValue(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)
        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        settingData?.addRestaurantFloor?.let {
            ShowRestaurantPersonalAddress = it
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: PaymentEvent?) {
        if (event?.resultCode == 0) {
            if (isNetworkConnected) {
                mSelectedPayment?.keyId = event.gateway_unique_id
                mSelectedPayment?.payment_token = "razorpay"
                Log.e("hello", "jklhjgkjhtt")
                onlinePayment(mSelectedPayment)
            }
        } else {
            mBinding?.root?.onSnackbar(event?.message ?: "")
        }
    }


    private fun imageObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<String> { resource ->

            imageList?.add(ImageListModel(is_imageLoad = true, image = resource, _id = ""))

            mAdapter?.submitMessageList(imageList, "cart")
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.imageLiveData.observe(this, catObserver)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding

        EventBus.getDefault().register(this)

        mBinding?.color = Configurations.colors
        mBinding?.strings = appUtils.loadAppConfig(0).strings
        mBinding?.currency = AppConstants.CURRENCY_SYMBOL

        imageList = mutableListOf()

        mBinding?.rvTax?.layoutManager = LinearLayoutManager(requireContext())


        if (settingData?.extra_instructions == "1") {
            settingInstructionLayout()
        }

        etPromoCode.afterTextChanged {
            if (it.trim().isNotEmpty()) {
                tvRedeem.isEnabled = true
                tvRedeem.setTextColor(Color.parseColor(Configurations.colors.primaryColor))
            } else {
                tvRedeem.isEnabled = false
                tvRedeem.setTextColor(Color.parseColor(Configurations.colors.textSubhead))
            }
        }

        lytGrp.setOnClickListener {
            openGroupDialog()
        }


        tvRedeem.setOnClickListener {
            if (tvRedeem.text.toString() == getString(R.string.remove)) {
                tvRedeem.isEnabled = true
                tvRedeem.text = getString(R.string.apply)
                etPromoCode.setText("")
                etPromoCode.isEnabled = true
                dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)
                mBinding?.root?.onSnackbar(getString(R.string.promo_remove))
                calculateCartCharges(cartList)
            } else {
                if (dataManager.getCurrentUserLoggedIn())
                    checkPromoApi(etPromoCode.text.toString().trim())
                else {
                    appUtils.checkLoginFlow(requireContext()).apply {
                        (DataNames.REQUEST_CART_LOGIN_PROMO)
                    }
                }
            }
        }

        tvClearTip.setOnClickListener {
            isTipClear = true
            tipPercentage = 0
            mTipCharges = 0.0f
            tvTipAmount.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
            tvTipCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)

            if (rvTax.visibility == View.GONE) {
                calculateCartCharges(cartList)
            } else {
                tipClearCalculateCartCharges(cartList)
            }
            if (addTipInPercentage == "1")
                tipAdapter?.clearTipSelection()


        }

        cardView2.setOnClickListener {
            dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
                val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
                val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)

                activity?.launchActivity<PaymentListActivity>(AppConstants.REQUEST_PAYMENT_OPTION) {
                    putParcelableArrayListExtra("feature_data", featureList)
                    putExtra("mSelectPayment", payment_gateways)
                    putExtra("mTotalAmt", totalAmt.toFloat())
                }
            }
        }

        tvRedeemed.setOnClickListener {

            if (tvRedeemed.text.toString() == getString(R.string.remove)) {
                isReferrale = false
                redeemedAmt = 0.0
                group_referral.visibility = View.GONE
                tvRedeemed.text = getString(R.string.apply)
                calculateCartCharges(cartList)
            } else {
                isReferrale = true
                tvRedeemed.text = getString(R.string.remove)
                calculateCartCharges(cartList)
                group_referral.visibility = View.VISIBLE
            }

        }

        cartList = appUtils.getCartList().cartInfos


        mDeliveryType = cartList?.firstOrNull()?.deliveryType
        if (mDeliveryType == 1) {
            mDeliveryType = 2
        }
        Log.e("mdeliverytype", mDeliveryType.toString())
        if (cartList?.isNotEmpty() == true) {
            productData = cartList?.first()
        }

        supplierBranchId = productData?.suplierBranchId ?: 0
        supplierId = productData?.supplierId ?: 0

        settingToosettingToolbar()

        settingAdrs()
        initTipAdapter()
        extraFunctionality()

        if (productData?.appType == AppDataType.HomeServ.type) {
            cnst_service_selc.visibility = View.VISIBLE
            gp_action.visibility = View.GONE
            if (mAgentParam != null) {
                tv_change_agent.visibility = View.GONE
                group_mainlyt.visibility = View.VISIBLE
            } else {
                tv_change_agent.visibility = View.VISIBLE
                group_mainlyt.visibility = View.GONE
            }
        } else {
            cnst_service_selc.visibility = View.GONE
        }

        change_time_slot.setOnClickListener {

            openBookingDateTime(false)
        }

        tv_change_agent.setOnClickListener {
            openBookingDateTime(true)
        }


        if (productData?.appType == AppDataType.HomeServ.type) {
            deliver_text.text = getString(R.string.service_at)
            tv_checkout.text = getString(R.string.place_booking)
        } else {
            tv_checkout.text = getString(R.string.order_now)
        }

        val term = Gson().fromJson(settingData?.terminology, SettingModel.DataBean.Terminology::class.java)
        if (term?.english?.order_now?.isNotEmpty() == true)
            tv_checkout?.text = term.english.order_now
        if (term?.english?.choose_payment?.isNotEmpty() == true)
            tv_pay_option?.text = term.english.choose_payment


        if (settingData?.payment_method != "null" && settingData?.payment_method?.toInt() ?: 0 == PaymentType.CASH.payType) {
            tv_pay_option.text = getString(R.string.cash_on)
            cardView2.isEnabled = false
            // group_tip.visibility = View.GONE
        } else {
            cardView2.isEnabled = true
        }

        if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
            tv_deliver_adrs?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            deliver_text?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            viewBackground.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.white))
            ivChange?.visibility = View.VISIBLE
            tv_change_adrs?.visibility = View.GONE
            viewBottom?.visibility = View.VISIBLE
            ivLocation?.visibility = View.VISIBLE
            etPromoCode?.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.black_80))
            etPromoCode?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_coupon, 0, 0, 0)
        } else {
            viewBottom?.visibility = View.GONE
            viewBackground.setBackgroundColor(Color.parseColor(Configurations.colors.toolbarColor))
            tv_deliver_adrs?.setTextColor(Color.parseColor(Configurations.colors.toolbarText))
            deliver_text?.setTextColor(Color.parseColor(Configurations.colors.toolbarText))
            etPromoCode?.setHintTextColor(Color.parseColor(Configurations.colors.primaryColor))
        }

    }

    private fun openGroupDialog() {
        groupDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        groupDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        groupDialog?.setContentView(R.layout.dialog_add_show_group)
        groupDialog?.tvTitle?.text = "Group will be active for 2 minutes before order is sent to restaurant!"
        groupDialog?.ivBack?.setOnClickListener {
            groupDialog?.dismiss()
        }
        groupDialog?.tvCreateGroup?.setOnClickListener {
            if (dataManager.getCurrentUserLoggedIn()) {
                val groupName = groupDialog?.etGroup?.text.toString().trim()
                val hashMap = HashMap<String, String>()
                hashMap["authorization"] = dataManager.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString()
                hashMap["supplier_id"] = supplierId.toString()
                hashMap["group_name"] = groupName
                viewModel.createGroup(hashMap)
                groupDialog!!.dismiss()
            } else {
                appUtils.checkLoginFlow(requireContext()).apply {
                    (DataNames.REQUEST_CART_LOGIN)
                }
            }
        }
        groupDialog?.show()
    }
    fun method(str: String?): String? {
        var str = str
        if (str != null && str.length > 0 && str[str.length - 1] == ',') {
            str = str.substring(0, str.length - 1)
        }
        return str
    }

    private fun openBookingDateTime(isAgent: Boolean) {

        if (dataManager.getCurrentUserLoggedIn())
            activity?.launchActivity<ServSelectionActivity>(AppConstants.REQUEST_AGENT_DETAIL)
            {
                putExtra(DataNames.SUPPLIER_BRANCH_ID, supplierBranchId)
                putExtra("productIds", cartList?.map { it.productId.toString() }?.toTypedArray())
                if (isAgent) {
                    putExtra("mAgentData", mAgentParam)
                    putExtra("isAgent", isAgent)
                }
                putExtra("duration", cartList?.filter { it.serviceType == 0 }?.sumBy {
                    it.serviceDuration.times(it.quantity)
                })
                putExtra("screenType", "order")
            }
        else {

            appUtils.checkLoginFlow(requireContext()).apply {
                (AppConstants.REQUEST_CART_LOGIN_BOOKING)
            }

        }
    }

    private fun openCardScreen() {

//        dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
//            val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
//            val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)
//
//            activity?.launchActivity<PaymentListActivity>(AppConstants.REQUEST_PAYMENT_OPTION) {
//                putParcelableArrayListExtra("feature_data", featureList)
//                putExtra("mSelectPayment", payment_gateways)
//                putExtra("mTotalAmt", totalAmt.toFloat())
//            }
//        }


        dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
            val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
            val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)


            val stripe = featureList.filter { feature ->
                (feature.name == "Stripe")
            }

            activity?.launchActivity<SaveCardsActivity>(AppConstants.REQUEST_PAYMENT_OPTION) {
                putExtra("fromSettings", false)
                putExtra("amount", totalAmt.toFloat())
                putExtra("payItem", CustomPayModel(getString(R.string.online_payment), R.drawable.ic_payment_card,
                        stripe[0].key_value_front?.get(0)?.value, "stripe", addCard = true))
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        viewModel.compositeDisposable.clear()
    }


    private fun settingToosettingToolbar() {
        var hashMap: HashMap<String, String>? = null
        val loc = dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)
        hashMap = HashMap()
        hashMap["latitude"] = loc?.latitude.toString()
        hashMap["longitude"] = loc?.longitude.toString()
        hashMap["addressLineFirst"] = loc?.address.toString()
        hashMap["customer_address"] = loc?.address.toString()
        if (dataManager.getCurrentUserLoggedIn()) {
            mViewModel?.addAddress(hashMap)
        }

        if (ShowRestaurantPersonalAddress == "1") {


//            if (mDeliveryType == FoodAppType.Pickup.foodType && dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java) != null) {
//                dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)?.address
//                        ?: ""
//            } else

            /*    if (dataManager.getGsonValue(PrefenceConstants.SUPPLIER_ADDRESS_DATA, SupplierLocation::class.java) != null) {

                    dataManager.getGsonValue(PrefenceConstants.SUPPLIER_ADDRESS_DATA, SupplierLocation::class.java)?.also {
                        locationData = it
                        deliveryId = it.location.toString()
                    }.let { tv_deliver_adrs.text = "${it?.location}" }*/
            setData()
//            } else {

//                if (cartList?.isNotEmpty() == true && dataManager.getCurrentUserLoggedIn()) {
//                    CommonUtils.setBaseUrl(BuildConfig.BASE_URL, retrofit)
//                    viewModel.getLocationsList(cartList?.firstOrNull()?.suplierBranchId ?: 0)
//                } else
//                    setData()

            //
        } else {

            tv_deliver_adrs.text = if (mDeliveryType == FoodAppType.Pickup.foodType && dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java) != null) {
                dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)?.address
                        ?: ""
            } else if (dataManager.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java) != null) {
                dataManager.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)?.also {
                    adrsData = it
                    deliveryId = it.id.toString()
                }.let { "${it?.address_line_1} , ${it?.customer_address}" }
            } else {
                dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)?.also {
                    adrsData = AddressBean(latitude = it.latitude, longitude = it.longitude, customer_address = it.address)
                }.let { it?.address }
            }

            if (dataManager.getCurrentUserLoggedIn() && cartList?.isNotEmpty() == true) {
                CommonUtils.setBaseUrl(BuildConfig.BASE_URL, retrofit)
                viewModel.getAddressList(cartList?.firstOrNull()?.suplierBranchId ?: 0)
            } else {
                setData()
            }
        }
    }


    private fun settingAdrs() {

        tv_change_adrs.setOnClickListener {
            if (dataManager.getCurrentUserLoggedIn()) {
                if (cartList?.get(0)?.deliveryType == 0) {
                    AddressDialogFragment.newInstance(productData?.suplierBranchId
                            ?: 0).show(childFragmentManager, "dialog")
                } else {
                    openTakeAwayDialog()
                }
            } else {
                appUtils.checkLoginFlow(requireContext()).apply {
                    (AppConstants.REQUEST_ADDRESS_ADD)
                }
            }
        }

        ivChange.setOnClickListener {
            tv_change_adrs.callOnClick()
        }

        tv_deliver_adrs.setOnClickListener {
            tv_change_adrs.callOnClick()
        }

        tv_checkout.setOnClickListener {
            if (isNetworkConnected) {
                if (dataManager.getCurrentUserLoggedIn()) {
                    val orderCharges = appUtils.calculateCartTotal()
                    if (minOrder != null && ((minOrder?.toDouble() ?: 0.0) > orderCharges))
                        showMinOrderAlert()
                    else
                    //if(is_table_enabled==0) {
                        if (mDeliveryType == 0) {
                            if (is_table_enabled == null) {
                                is_table_enabled = 0
                            }
                            AddressDialogFragment.newInstance(is_table_enabled!!, productData?.suplierBranchId
                                    ?: 0).show(childFragmentManager, "dialog")
                        } else {
                            handleBothDelivery()
                        }
                    //}else{

                    //}
                } else {
                    appUtils.checkLoginFlow(requireContext()).apply {
                        (DataNames.REQUEST_CART_LOGIN)
                    }
                }
            }
        }

        btn_donate_someone.setOnClickListener {
            if (::adrsData.isInitialized) {
                openDonateDialog()
            }
        }

    }

    private fun openTakeAwayDialog() {
        //  val mLocUser = dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)

        val dialog = Dialog(requireContext(), R.style.Theme_Dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.dialog_take_away)
        dialog.tvTime.text = takeOffPrepTime + " Min"
        dialog.tvRestaurantAddress.text = supplierAddress
        dialog.tvDirection.text = requireActivity().getString(R.string.click_here_to_directions)
        dialog.tvDirection.setOnClickListener {
            val uri = Uri.parse("http://maps.google.com/maps?saddr=${lat},${lng}&daddr=${supplierLatitude},${supplierLongitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireActivity().startActivity(intent)

        }

        dialog.tvDone.setOnClickListener {
            onSuccessListener()
            dialog.dismiss()
            //  StaticFunction.sweetDialogueSuccess11(activity, getString(R.string.success), getString(R.string.succesfully_order, appUtils.loadAppConfig(0).strings?.order), false, 1001, this)
        }
        dialog.show()
    }

    private fun showMinOrderAlert() {
        AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.net_total_greater_than, AppConstants.CURRENCY_SYMBOL, minOrder))
                .setPositiveButton(R.string.Ok, null)
                .show()
    }

    private fun settingLayout(maxHandlingAdminCharges: Float, maxDeliveryCharge: Float) {

        totalAmt = appUtils.calculateCartTotal()

        subTotal = totalAmt.toFloat()

        tvSubTotal.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, totalAmt)

        settingData?.user_service_fee.let {

            if (it == "1" && ::adrsData.isInitialized) {
                restServiceTax = totalAmt.div(100f).times(adrsData.user_service_charge ?: 0.0)
                group_service.visibility = View.VISIBLE
                tvRestService.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, restServiceTax)
            }
        }

        if (dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java) != null) {
            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
            group_discount.visibility = View.VISIBLE
            totalAmt = totalAmt.minus(promoData?.discountPrice ?: 0f)
            etPromoCode.setText(promoData?.promoCode)
            tvRedeem.text = getString(R.string.remove)
            etPromoCode.isEnabled = false

            totalAmt += maxDeliveryCharge + restServiceTax + questionAddonPrice

            tvDiscount.text = "-" + getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, promoData?.discountPrice)
        } else {
            etPromoCode.setText("")
            etPromoCode.isEnabled = true
            tvRedeem.text = getString(R.string.apply)
            group_discount.visibility = View.GONE
            tvDiscount.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, 0.0f)

            totalAmt = appUtils.calculateCartTotal() + maxDeliveryCharge + restServiceTax + questionAddonPrice
        }

        if (maxHandlingAdminCharges == 0.0f) {

            group_tax.visibility = View.GONE
        }

        // tvTaxCharges.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, maxHandlingAdminCharges)

        if (mReferralAmt > 0 && isReferrale) {

            redeemedAmt = if (totalAmt >= mReferralAmt) {
                mReferralAmt.toDouble()
            } else {
                totalAmt
            }
            totalAmt -= mReferralAmt

            tvReferralCode.text = "-" + getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, redeemedAmt)
        }



        mTipCharges = ((totalAmt * tipPercentage) / 100).toFloat()

        tvTipAmount.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
        tvTipCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)

        totalAmt += mTipCharges
        if (dataManager.getKeyValue(IS_VAT_INCLUDED, TYPE_INT) == 1) {
            if (categories.size > 0) {
                group_tax.visibility = View.VISIBLE
                tvTaxCharges.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, categories[0].price)
                totalAmt += categories[0].price
            }
        }

        tvNetTotal.text = AppConstants.CURRENCY_SYMBOL + decimalFormat.format(totalAmt)

    }

    private fun initTipAdapter() {
        tvTipCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
        tipList = ArrayList<TiPModel>()
        val mLayoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        rvTip.layoutManager = mLayoutManager
        tipAdapter = TipAdapter(activity
                ?: requireContext(), tipList, screenFlowBean, appUtils, addTipInPercentage)
        rvTip.adapter = tipAdapter
        tipAdapter?.tipCallback(this)
    }


    private fun setData() {

        cartList = appUtils.getCartList().cartInfos


        isPaymentConfirm = cartList?.any { it.isPaymentConfirm == 1 } ?: false
        // isPaymentConfirm=true

        setCartVisibility(cartList)

        cartList.takeIf { it?.isNotEmpty() == true }?.let { cartInfo ->

            cartAdapter = CartAdaptor(activity
                    ?: requireContext(), cartInfo, screenFlowBean, appUtils)
            cartAdapter?.settingCallback(this)
            recyclerview.adapter = cartAdapter

            refreshDeliveryAdrs(productData?.deliveryType ?: 0)

            /* val list=ArrayList<CartItem>()
             cartInfo.forEach{
                list.add(CartItem(product_ids = it.productId,supplier_id = it.supplierId))
             }*/

            if (isNetworkConnected) {
                if (::adrsData.isInitialized) {
                    viewModel.refreshCart(CartReviewParam(product_ids = cartInfo.map { it.productId },
                            latitude = adrsData.latitude ?: "", self_pickup = "" + mDeliveryType
                            ?: "", longitude = adrsData.longitude
                            ?: ""))
                } else {
                    dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)?.also {
                        viewModel.refreshCart(CartReviewParam(
                                product_ids = cartInfo.map { it.productId }, latitude = it.latitude
                                ?: "0.0", self_pickup = "" + mDeliveryType ?: "",
                                longitude = it.longitude ?: "0.0"))
                    }
                }


            }

            if (isNetworkConnected) {
                if (dataManager.getCurrentUserLoggedIn() && settingData?.referral_feature != null && settingData?.referral_feature == "1") {
                    viewModel.referralAmount()
                }
            }
        }

        setQuestAdapter()


        // init swipe to dismiss logic
        val swipeToDismissTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // callback for drag-n-drop, false to skip this feature
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // callback for swipe to dismiss, removing item from data and adapter

                val mCartProd = cartList?.get(viewHolder.adapterPosition)


                if (mCartProd?.productAddonId ?: 0 > 0) {
                    StaticFunction.removeFromCart(activity, mCartProd?.productId, mCartProd?.productAddonId
                            ?: 0)
                } else {
                    StaticFunction.removeFromCart(activity, mCartProd?.productId, 0)
                }

                //    dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)
                cartList?.removeAt(viewHolder.adapterPosition)
                cartAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
                setCartVisibility(cartList)
                calculateCartCharges(cartList)
            }

        })
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerview)

        if (mDeliveryType == FoodAppType.Both.foodType && productData?.appType == AppDataType.Food.type) {
            //openDeliveryDialog()
        }



        settingData?.cart_image_upload?.let {
            if (it == "1") {
                group_presc.visibility = View.VISIBLE

                rvPhotoList.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)

                mAdapter = ImageListAdapter(ImageListAdapter.UserChatListener({

                    if (it.is_imageLoad == false) {
                        if (imageList?.count() ?: 0 >= 4) {
                            mBinding?.root?.onSnackbar(getString(R.string.max_limit_reached))

                        } else {
                            if (dataManager.getCurrentUserLoggedIn())
                                uploadImage()
                            else {

                                appUtils.checkLoginFlow(requireContext()).apply {
                                    (DataNames.REQUEST_CART_LOGIN_PRECRIPTION)
                                }

                            }
                        }
                    }

                }, { it1 ->
                    if (it1 < 0) return@UserChatListener
                    imageList?.removeAt(it1)
                    mAdapter?.submitMessageList(imageList, "cart")
                }))

                mAdapter?.submitMessageList(imageList, "cart")
                rvPhotoList.adapter = mAdapter

            } else {
                group_presc.visibility = View.GONE
            }
        }

        settingData?.order_instructions?.let {
            if (it == "1") {
                edAdditionalRemarks.visibility = View.VISIBLE
            } else {
                edAdditionalRemarks.visibility = View.GONE
            }
        }
    }

    private fun setCartVisibility(cartList: MutableList<CartInfo>?) {
        viewModel.setIsCartList(cartList?.size ?: 0)

        if (isPaymentConfirm || cartList?.count() == 0) {
            cardView2.visibility = View.GONE
            btn_donate_someone.visibility = View.GONE
        } else {
            cardView2.visibility = View.VISIBLE
            settingData?.show_donate_popup?.let {
                if (it == "1" && mDeliveryType == FoodAppType.Delivery.foodType) {
                    btn_donate_someone.visibility = View.VISIBLE
                }
            }
        }

        cardView2?.visibility = View.GONE
    }

    private fun setQuestAdapter() {

        mQuestionList = cartList?.distinctBy {
            it.question_list?.distinctBy { it1 -> it1.questionId }
        }?.flatMap { it2 ->
            it2.question_list ?: mutableListOf()
        } ?: listOf()


        if (mQuestionList.isEmpty()) return
        group_question.visibility = View.VISIBLE


        val filterLIst = mQuestionList.flatMap { it.optionsList }

        questionAddonPrice = filterLIst.sumByDouble {

            if (it.flatValue > 0) {
                it.flatValue.toDouble()
            } else {
                it.productPrice.div(100.0f).times(it.percentageValue).toDouble()
            }
        }.toFloat()

        tvAddonCharges.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, questionAddonPrice)
        questAdapter = SelectedQuestAdapter(activity ?: requireContext(), mQuestionList)
        recyclerviewQuest.layoutManager = LinearLayoutManager(activity
                ?: requireContext(), RecyclerView.VERTICAL, false)
        recyclerviewQuest.adapter = questAdapter
        questAdapter?.notifyDataSetChanged()

        calculateCartCharges(cartList)
    }


    private fun settingInstructionLayout() {
        parkLyt.visibility = View.VISIBLE

        rButtonPetYes.setOnClickListener {
            rButtonPetYes.isChecked = true
            rButtonPetNo.isChecked = false
            havePets = 1
            cleaner_in = 0
        }

        rButtonPetNo.setOnClickListener {
            rButtonPetNo.isChecked = true
            rButtonPetYes.isChecked = false
            havePets = 0
            cleaner_in = 1

        }

        rButtonCleanInYes.setOnClickListener {
            rButtonCleanInYes.isChecked = true
            rButtonCleanInNo.isChecked = false
            cleaner_in = 1
            havePets = 0
        }

        rButtonCleanInNo.setOnClickListener {
            rButtonCleanInYes.isChecked = false
            rButtonCleanInNo.isChecked = true
            cleaner_in = 0
            havePets = 1

        }

        // havePets = rButtonPetYes.isChecked
        // cleaner_in=rButtonCleanInYes.isChecked

    }


    private fun showInstructionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.things_to_remember)
                .setMessage(settingData?.things_to_remember)
                .setPositiveButton(R.string.Ok) { dialog, id ->
                    if (isNetworkConnected) {
                        mViewModel?.addCart(productList, appUtils, questionAddonPrice, mQuestionList)
                    }
                }

        builder.show()
    }


    private fun calculateDelivery() {

        if ((productData?.appType == AppDataType.Food.type && regionDeliveryCharges == 0f || screenFlowBean?.is_single_vendor == VendorAppType.Single.appType)
                && ::adrsData.isInitialized && mDeliveryType == 0 && settingData?.delivery_charge_type != "1") {
            if (isNetworkConnected) {
                viewModel.getDistance(getString(R.string.google_map), LatLng(adrsData.latitude?.toDouble()
                        ?: 0.0, adrsData.longitude?.toDouble() ?: 0.0), LatLng(productData?.latitude
                        ?: 0.0, productData?.longitude ?: 0.0))
            }
        } else {
            calculateCartCharges(cartList)
        }
    }

    private fun refreshDeliveryAdrs(deliveryType: Int) {

        //  adrsLyt.visibility = View.VISIBLE

        when (deliveryType) {
            FoodAppType.Pickup.foodType -> {
                group_delivery.visibility = View.GONE
                tv_change_adrs.visibility = View.GONE
                deliver_text.text = getString(R.string.pickup_from)
            }
            FoodAppType.Delivery.foodType -> {
                // group_delivery.visibility = View.VISIBLE
                tv_change_adrs.visibility = View.VISIBLE
                deliver_text.text = getString(R.string.delivery_to)
            }
            else -> {
                adrsLyt.visibility = View.GONE
            }
        }

        if (productData?.appType == AppDataType.HomeServ.type) {
            group_delivery.visibility = View.GONE
        }


        /*   if (deliveryId.isEmpty()) {

           tv_deliver_adrs.text=  if (mDeliveryType == FoodAppType.Pickup.foodType && dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java) != null) {
                   val mLocUser = dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)
                           ?: return
                  mLocUser.address
               }else if( dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)!=null)
               {
                   dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)?.let {
                       it.address
                   }
               }else {
                     getString(R.string.delivery_location)
               }
           }*/
    }


    private fun handleBothDelivery() {
//        if (mDeliveryType == FoodAppType.Both.foodType && productData?.appType == AppDataType.Food.type) {
//            openDeliveryDialog()
//        } else {
        progress_bar.visibility = View.VISIBLE
        placeOrder()
//        }
    }

    private fun openDonateDialog() {
        val binding = DataBindingUtil.inflate<DialogDonateBinding>(LayoutInflater.from(activity), R.layout.dialog_donate, null, false)
        binding.color = Configurations.colors
        binding.strings = appUtils.loadAppConfig(0).strings

        val mDialog = mDialogsUtil.showDialog(activity ?: requireContext(), binding.root)
        mDialog.show()

        val edDelivery = mDialog.findViewById<TextInputEditText>(R.id.edDelivery)
        val btnDonate = mDialog.findViewById<MaterialButton>(R.id.btn_donate)
        val btnSkip = mDialog.findViewById<MaterialButton>(R.id.btn_skip)

        edDelivery.setText("${adrsData.address_line_1 ?: ""},${adrsData.customer_address ?: ""}")

        btnDonate.setOnClickListener {
            isDonate = true
            mDialog.dismiss()
        }

        btnSkip.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.setOnDismissListener {
            tv_checkout.callOnClick()
        }

    }

    private fun openDeliveryDialog() {

        val binding = DataBindingUtil.inflate<DialogBothDeliveryBinding>(LayoutInflater.from(activity), R.layout.dialog_both_delivery, null, false)
        binding.color = Configurations.colors
        binding.strings = appUtils.loadAppConfig(0).strings

        val mDialog = mDialogsUtil.showDialogFix(activity ?: requireContext(), binding.root)
        mDialog.show()

        val btn_pickup = mDialog.findViewById<MaterialButton>(R.id.btn_pickup)
        val btn_delivery = mDialog.findViewById<MaterialButton>(R.id.btn_delivery)

        btn_pickup.setOnClickListener {
            mDeliveryType = FoodAppType.Pickup.foodType
            if (mDialog.isShowing) {
                mDialog.dismiss()
            }
        }

        btn_delivery.setOnClickListener {
            mDeliveryType = FoodAppType.Delivery.foodType
            if (mDialog.isShowing) {
                mDialog.dismiss()
            }
        }

        mDialog.setOnDismissListener {
            val cart = CartList()

            cartList?.map {
                it.deliveryType = mDeliveryType ?: 0
            }

            if (mDeliveryType == 1) {
                mDeliveryCharge = 0.0f
                group_tip.visibility = View.GONE
            }

            cart.cartInfos = cartList

            dataManager.addGsonValue(DataNames.CART, Gson().toJson(cart))
            // calculateCartCharges(cartList.cartInfos)
            refreshDeliveryAdrs(mDeliveryType ?: 0)
            calculateDelivery()
        }
    }

    private fun placeOrder() {

        if (mSelectedPayment != null) {

            productList.clear()
            productList.addAll(StaticFunction.covertCartToArray(activity))

            if (productList.isNotEmpty()) {
                agentType = productList.first().agent_type ?: 0
            }
            if (is_table_enabled != 0) {
                if (mAgentParam == null && productData?.appType == AppDataType.HomeServ.type) {
                    mBinding?.root?.onSnackbar(getString(R.string.please_select_slot))
                } else if (validatePaymentOption(mSelectedPayment)) {
                    mBinding?.root?.onSnackbar(getString(R.string.choose_payment))
                } else if (imageList?.isEmpty() == true && settingData?.cart_image_upload == "1") {
                    mBinding?.root?.onSnackbar(getString(R.string.please_select_precription))
                } else if (settingData?.extra_instructions == "1") {
                    showInstructionDialog()
                } else {
                    if (cartList?.get(0)?.deliveryType == 2) {
                        //   openTakeAwayDialog()
                    } else {
                        if (isNetworkConnected) {
                            mViewModel?.addCart(productList, appUtils, questionAddonPrice, mQuestionList)
                        }
                    }
                    if (isNetworkConnected) {
                        mViewModel?.addCart(productList, appUtils, questionAddonPrice, mQuestionList)
                    }
                }
            } else {
                if (deliveryId.isEmpty() && mDeliveryType != FoodAppType.Pickup.foodType) {
                    // mBinding?.root?.onSnackbar("Please select address")
                    if (cartList?.get(0)?.deliveryType == 2) {
                        openTakeAwayDialog()
                    } else {
                        tv_change_adrs.callOnClick()
                    }


                } else if (mAgentParam == null && productData?.appType == AppDataType.HomeServ.type) {
                    mBinding?.root?.onSnackbar(getString(R.string.please_select_slot))
                } else if (validatePaymentOption(mSelectedPayment)) {
                    mBinding?.root?.onSnackbar(getString(R.string.choose_payment))
                } else if (imageList?.isEmpty() == true && settingData?.cart_image_upload == "1") {
                    mBinding?.root?.onSnackbar(getString(R.string.please_select_precription))
                } else if (settingData?.extra_instructions == "1") {
                    showInstructionDialog()
                } else {
                    if (cartList?.get(0)?.deliveryType == 1) {
                        //   openTakeAwayDialog()
                    } else {
                        if (isNetworkConnected) {
                            mViewModel?.addCart(productList, appUtils, questionAddonPrice, mQuestionList)
                        }
                    }
                    if (isNetworkConnected) {
                        mViewModel?.addCart(productList, appUtils, questionAddonPrice, mQuestionList)
                    }

                }
            }
        } else {
//            normal(requireContext(), getString(R.string.choose_payment_method_first))
            openCardScreen()
        }


    }

    private fun validatePaymentOption(mSelectedPayment: CustomPayModel?): Boolean {
        return if (mSelectedPayment == null && (settingData?.payment_method != "null" && settingData?.payment_method?.toInt() ?: 0 != PaymentType.CASH.payType) && !isPaymentConfirm) {
            true
        } else if (settingData?.payment_method != "null" && settingData?.payment_method?.toInt() ?: 0 == PaymentType.ONLINE.payType || settingData?.payment_method?.toInt() ?: 0 == PaymentType.BOTH.payType) {
            return false
        } else {
            return false
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DataNames.REQUEST_CART_LOGIN && resultCode == Activity.RESULT_OK) {
            if (dataManager.getCurrentUserLoggedIn()) {
                if (settingData?.referral_feature != null && settingData?.referral_feature == "1") {
                    viewModel.referralAmount()
                }
                handleBothDelivery()
            }
        } else if (requestCode == PAYPALREQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val result: DropInResult = data?.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT)!!

                    mSelectedPayment?.keyId = result.paymentMethodNonce?.nonce
                    mSelectedPayment?.payment_token = "venmo"
                    Log.e("hello", "jklhjgkjh55589")
                    onlinePayment(mSelectedPayment)
                    // use the result to update your UI and send the payment method nonce to your server
                }
                Activity.RESULT_CANCELED -> {
                    mBinding?.root?.onSnackbar("User Cancelled")
                }
                else -> {
                    // handle errors here, an exception may be available in
                    val error = data!!.getSerializableExtra(DropInActivity.EXTRA_ERROR) as java.lang.Exception
                    mBinding?.root?.onSnackbar(error.stackTrace.toString())
                }
            }

        } else if (requestCode == SADDED_PAYMENT_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    AppToasty.success(requireContext(), getString(R.string.payment_done_successful))
                    Log.e("hello", "jklhjgkjh555")
                    onlinePayment(mSelectedPayment)
                    // use the result to update your UI and send the payment method nonce to your server
                }
                else -> {
                    val message = if (data?.hasExtra("showError") == true)
                        getString(R.string.payment_failed)
                    else
                        getString(R.string.payment_unsuccessful)
                    AppToasty.error(requireContext(), message)
                }

            }

        } else if (requestCode == MY_FATOORAH_PAYMENT_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    AppToasty.success(requireContext(), getString(R.string.payment_done_successful))
                    if (data != null && data.hasExtra("paymentId"))
                        mSelectedPayment?.keyId = data.getStringExtra("paymentId")
                    Log.e("hello", "jklhjgkjh6789")
                    onlinePayment(mSelectedPayment)
                    // use the result to update your UI and send the payment method nonce to your server
                }
                else -> {

                    val message = if (data?.hasExtra("showError") == true)
                        getString(R.string.payment_failed)
                    else
                        getString(R.string.payment_unsuccessful)
                    AppToasty.error(requireContext(), message)
                }
            }

        } else if (requestCode == AppConstants.REQUEST_AGENT_DETAIL && resultCode == Activity.RESULT_OK) {
            if (data == null) return
            mAgentParam = data.getParcelableExtra("agentData")!!
            settingAgentData(mAgentParam)
        } else if (requestCode == AppConstants.REQUEST_CART_LOGIN_BOOKING && resultCode == Activity.RESULT_OK) {
            openBookingDateTime(false)
        } else if (requestCode == DataNames.REQUEST_CART_LOGIN_PROMO && resultCode == Activity.RESULT_OK) {
            checkPromoApi(etPromoCode.text.toString().trim())
        } else if (requestCode == DataNames.REQUEST_CART_LOGIN_PRECRIPTION && resultCode == Activity.RESULT_OK) {
            uploadImage()
        } else if (requestCode == AppConstants.REQUEST_PAYMENT_OPTION && resultCode == Activity.RESULT_OK) {


            mSelectedPayment = data?.getParcelableExtra("payItem")


            val hashMap: HashMap<String, String> = HashMap()
            val loc = dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)
            hashMap["latitude"] = loc?.latitude.toString()
            hashMap["longitude"] = loc?.longitude.toString()
            hashMap["addressLineFirst"] = loc?.address.toString()
            hashMap["customer_address"] = loc?.address.toString()
            if (dataManager.getCurrentUserLoggedIn()) {
                mViewModel?.addAddress(hashMap)
            }


            tv_pay_option.text = mSelectedPayment?.payName

            if (tipList.isNotEmpty()) {
                group_tip.visibility = View.VISIBLE
            }

            if (mSelectedPayment?.payName == getString(R.string.zelle)) {
                Glide.with(activity ?: requireActivity()).load(mSelectedPayment?.keyId
                        ?: "").into(iv_doc)
                group_zelle.visibility = View.VISIBLE
            } else if (mSelectedPayment?.payName == getString(R.string.cash_on)) {
                group_tip.visibility = View.GONE
                tvClearTip.callOnClick()
            } else {
                group_zelle.visibility = View.GONE
            }

        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            mBinding?.root?.onSnackbar(getString(R.string.returned_from_app_settings_to_activity))
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.CameraPicker) {

            if (isNetworkConnected) {
                if (photoFile?.isRooted == true) {
                    viewModel.uploadImage(imageUtils.compressImage(photoFile?.absolutePath
                            ?: ""))
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.GalleyPicker) {
            if (data != null) {
                if (isNetworkConnected) {
                    //data.getData return the content URI for the selected Image
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    // Get the cursor
                    val cursor = activity?.contentResolver?.query(selectedImage!!, filePathColumn, null, null, null)
                    // Move to first row
                    cursor?.moveToFirst()
                    //Get the column index of MediaStore.Images.Media.DATA
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    //Gets the String value in the column
                    val imgDecodableString = cursor?.getString(columnIndex ?: 0)
                    cursor?.close()

                    if (imgDecodableString?.isNotEmpty() == true) {
                        viewModel.uploadImage(imageUtils.compressImage(imgDecodableString))
                    }
                }
            }
        } else if (requestCode == AppConstants.REQUEST_SQUARE_PAY && resultCode == Activity.RESULT_OK) {

            CardEntry.handleActivityResult(data) { result ->
                if (result.isSuccess()) {
                    val cardResult: CardDetails = result.getSuccessValue()
                    val card: sqip.Card = cardResult.card

                    mSelectedPayment?.keyId = cardResult.nonce
                    //mSelectedPayment?.payment_token = "square"
                    Log.e("hello", "jklhjgkjh67")
                    onlinePayment(mSelectedPayment)

                } else if (result.isCanceled()) {

                    mBinding?.root?.onSnackbar("Canceled")
                }
            }
        }
    }

    private fun settingAgentData(mAgentParam: AgentCustomParam?) {


        tvSerDate.text = mDateTime.convertDateOneToAnother(mAgentParam?.serviceDate
                ?: "", "yyyy-MM-dd", "EEE, dd MMM")
        tvSerTime.text = mDateTime.convertDateOneToAnother(mAgentParam?.serviceTime
                ?: "", "HH:mm", "hh:mm aaa")

        // if (mAgentParam?.agentData?.image != null) {
        iv_userImage.loadUserImage(mAgentParam?.agentData?.image ?: "")
        // }

        if (mAgentParam?.agentData?.name != null) {
            tv_name.text = mAgentParam.agentData?.name ?: ""
        }

        if (mAgentParam?.agentData?.occupation != null) {
            tv_occupation.text = mAgentParam.agentData?.occupation ?: ""
        }

        if (mAgentParam?.agentData?.experience != 0) {
            tv_experience.text = getString(R.string.experience_tag, mAgentParam?.agentData?.experience)
        }
        group_mainlyt.visibility = View.VISIBLE
        change_time_slot.visibility = View.GONE
        gp_action.visibility = View.GONE

    }

    override fun onDeleteCart(position: Int) {

        if (position == -1) return

        val mCartProd = cartList?.get(position)
        StaticFunction.removeFromCart(activity, mCartProd?.productId, mCartProd?.productAddonId
                ?: 0)
        //  dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)

        cartList?.removeAt(position)
        cartAdapter?.notifyItemRemoved(position)

        setCartVisibility(cartList)

        manageStock(cartList)

        if (!cartList.isNullOrEmpty()) {

            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
            if (promoData != null) {

                promoData.discountPrice = promoData.tempPrice

                if (cartList?.sumByDouble {
                            it.price.times(it.quantity).toDouble()
                        } ?: 0.0 >= promoData.minOrder) {
                    calculatePromo(promoData, cartList, etPromoCode.text.toString().trim())
                } else {
                    mBinding?.root?.onSnackbar(getString(R.string.sorry_cart_value, "${AppConstants.CURRENCY_SYMBOL}${promoData.minOrder}"))
                }
            } else
                calculateCartCharges(cartList)

        } else
            dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)


    }

    override fun addItem(position: Int) {
        if (position != -1) {

            //   dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)
            val mCartProd = cartList?.get(position)

            cartUtils.addItemToCart(mCartProd).let {
                if (it != null) {
                    cartAdapter?.notifyItemChanged(position)
                    cartAdapter?.notifyDataSetChanged()
                }
            }


            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
            if (promoData != null) {

                promoData.discountPrice = promoData.tempPrice

                if (cartList?.sumByDouble {
                            it.price.times(it.quantity).toDouble()
                        } ?: 0.0 >= promoData.minOrder) {
                    calculatePromo(promoData, cartList, etPromoCode.text.toString().trim())
                } else {
                    mBinding?.root?.onSnackbar(getString(R.string.sorry_cart_value, "${AppConstants.CURRENCY_SYMBOL}${promoData.minOrder}"))
                }
            } else
                calculateCartCharges(cartList)


        }
    }

    override fun removeItem(position: Int) {
        isDecClick = true
        val mCartProd = cartList?.get(position)

        mCartProd.let {
            cartUtils.removeItemToCart(mCartProd)
        }

        if (mCartProd?.quantity == 0) {
            cartList?.removeAt(position)
        }

        cartAdapter?.notifyItemChanged(position)
        cartAdapter?.notifyDataSetChanged()

        setCartVisibility(cartList)
        manageStock(cartList)


        if (!cartList.isNullOrEmpty()) {

            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
            if (promoData != null) {

                promoData.discountPrice = promoData.tempPrice
                if (cartList?.sumByDouble {
                            it.price.times(it.quantity).toDouble()
                        } ?: 0.0 >= promoData.minOrder) {
                    calculatePromo(promoData, cartList, etPromoCode.text.toString().trim())
                } else {
                    mBinding?.root?.onSnackbar(getString(R.string.sorry_cart_value, "${AppConstants.CURRENCY_SYMBOL}${promoData.minOrder}"))
                }
            } else
                calculateCartCharges(cartList)
        } else
            dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)


    }

    private fun extraFunctionality() {

        if (settingData?.extra_functionality == "1") {
            edAdditionalRemarks.hint = activity?.getString(R.string.enter_instructionc).toString()
        }
    }


    private fun calculateCartCharges(cartList: MutableList<CartInfo>?) {
        if (firstt == 0) {
        val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)

        maxHandlingAdminCharges = cartList?.sumByDouble {
            ((it.price * it.quantity) * it.handlingAdmin) / (100 + it.handlingAdmin).toDouble()
        }?.toFloat() ?: 0.0f

        if (settingData?.disable_tax == "1") {
            maxHandlingAdminCharges = 0.0f
        }


        val tempList: ArrayList<Int> = ArrayList()


        cartList?.forEach { item ->

            var promoAmount = 0.0F

            promoData?.let {

                promoAmount = if ((item.price * item.quantity) > 0 && promoData.discountType == 0) {
                    if (promoData.tempPrice > (item.price * item.quantity))
                        item.price * item.quantity
                    else
                        promoData.tempPrice
                } else {
                    (item.price * item.quantity) / 100.0f * promoData.tempPrice
                }

            }

            if (firstt == 0) {
            if (tempList.contains(item.categoryId)) {

                val product = categories.filter { it.categoryId == item.categoryId }
                if (product.isNotEmpty()) {
//                    product[0].price += (((item.price - promoAmount) * item.handlingAdmin) / 100).toDouble()
                    product[0].price += (((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / (100 + item.handlingAdmin).toDouble()

                }


            } else {
                if (isTipClear) {
                    isTipClear = false
                    categories.clear()
                }

                    val items = categories.filter { it.handleAdminCharges == item.handlingAdmin }
//                (handlingAdmin/100)*(price - discount)/((100 + handlingAdmin)/100)
                    if (items.isNotEmpty()) {
                        if (!isDecClick) {
//                            items[0].price += (((item.price - promoAmount) * item.handlingAdmin) / 100).toDouble()
                            items[0].price += (((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / (100 + item.handlingAdmin).toDouble()

                        } else {
                            isDecClick = false
                            if (item.quantity == 1) {
//                                items[0].price =
//                                    (((item.price - promoAmount) * item.handlingAdmin) / 100).toDouble()
                                items[0].price = (((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / (100 + item.handlingAdmin).toDouble()

                            } else {
//                                items[0].price -= (((item.price - promoAmount) * item.handlingAdmin) / 100).toDouble()
                                items[0].price -= (((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / (100 + item.handlingAdmin).toDouble()
                            }
                        }
                    } else {
                        tempList.add(item.categoryId)
                        categories.add(
                            TaxModel(
                                categoryId = item.categoryId,
//                                price = ((((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / 100).toDouble(),
                                    price = (((item.price * item.quantity) - promoAmount) * item.handlingAdmin) / (100 + item.handlingAdmin).toDouble(),

                                    subCategoryName = item.subCategoryName,
                                handleAdminCharges = item.handlingAdmin,
                                promoAmount = promoAmount
                            )
                        )
                    }

                }


            }



            mBinding?.rvTax?.adapter = TaxAdaptor(requireContext(), categories)
        }
        }
        mDeliveryCharge = (if (productData?.appType == AppDataType.Ecom.type) {
            cartList?.maxBy { it.deliveryCharges }?.deliveryCharges
                    ?: 0.0f.plus(baseDeliveryCharges)
        } else if (settingData?.delivery_charge_type == "1" && mDeliveryType != FoodAppType.Pickup.foodType) {
            productData?.radius_price ?: 0.0f.plus(baseDeliveryCharges)
        } else if (regionDeliveryCharges > 0f) {
            regionDeliveryCharges.plus(baseDeliveryCharges)
        } else {
            this.mDeliveryCharge
        })

        if (productData?.appType == AppDataType.Food.type && mDeliveryType != FoodAppType.Delivery.foodType) {
            mDeliveryCharge = 0.0f
        }

        tvDeliveryCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mDeliveryCharge)

        settingLayout(maxHandlingAdminCharges, mDeliveryCharge)

    }

    private fun tipClearCalculateCartCharges(cartList: MutableList<CartInfo>?) {

        val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)

        maxHandlingAdminCharges = cartList?.sumByDouble {
            ((it.price * it.quantity) * it.handlingAdmin) / (100 + it.handlingAdmin).toDouble()
        }?.toFloat() ?: 0.0f

        if (settingData?.disable_tax == "1") {
            maxHandlingAdminCharges = 0.0f
        }


        val tempList: ArrayList<Int> = ArrayList()


        cartList?.forEach { item ->

            var promoAmount = 0.0F

            promoData?.let {

                promoAmount = if ((item.price * item.quantity) > 0 && promoData.discountType == 0) {
                    if (promoData.tempPrice > (item.price * item.quantity))
                        item.price * item.quantity
                    else
                        promoData.tempPrice
                } else {
                    (item.price * item.quantity) / 100.0f * promoData.tempPrice
                }

            }


            if (tempList.contains(item.categoryId)) {

                val product = categories.filter { it.categoryId == item.categoryId }
                if (product.isNotEmpty()) {
                    product[0].price += (((item.price  - promoAmount) * item.handlingAdmin) / 100).toDouble()
                }


            } else {

                val items = categories.filter { it.handleAdminCharges == item.handlingAdmin }

                if (items.isNotEmpty()) {
                    items[0].price += (((item.price  - promoAmount) * item.handlingAdmin) / 100).toDouble()
                } else {
                    tempList.add(item.categoryId)
                    categories.add(TaxModel(
                            categoryId = item.categoryId,
                            price = (((item.price  - promoAmount) * item.handlingAdmin) / 100).toDouble(),
                            subCategoryName = item.subCategoryName,
                            handleAdminCharges = item.handlingAdmin,
                            promoAmount = promoAmount
                    ))
                }

            }


        }
        // mBinding?.rvTax?.adapter = TaxAdaptor(requireContext(), categories)


        mDeliveryCharge = (if (productData?.appType == AppDataType.Ecom.type) {
            cartList?.maxBy { it.deliveryCharges }?.deliveryCharges
                    ?: 0.0f.plus(baseDeliveryCharges)
        } else if (settingData?.delivery_charge_type == "1" && mDeliveryType != FoodAppType.Pickup.foodType) {
            productData?.radius_price ?: 0.0f.plus(baseDeliveryCharges)
        } else if (regionDeliveryCharges > 0f) {
            regionDeliveryCharges.plus(baseDeliveryCharges)
        } else {
            this.mDeliveryCharge
        })

        if (productData?.appType == AppDataType.Food.type && mDeliveryType != FoodAppType.Delivery.foodType) {
            mDeliveryCharge = 0.0f
        }

        tvDeliveryCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mDeliveryCharge)

        settingLayout(maxHandlingAdminCharges, mDeliveryCharge)

    }


    private fun checkPromoApi(length: String) {

        val amount = appUtils.calculateCartTotal().plus(questionAddonPrice)

        val checkPromo = CheckPromoCodeParam(length, StaticFunction.getAccesstoken(activity), amount.toString(), StaticFunction.getLanguage(activity).toString(),
                cartList?.map { it.supplierId.toString() }, cartList?.map { it.categoryId.toString() })

        if (isNetworkConnected) {
            hideKeyboard()
            mViewModel?.validatePromo(checkPromo)
        }
    }

    private fun calculatePromo(promoData: PromoCodeModel.DataBean, cartList: MutableList<CartInfo>?, promoCode: String) {

        hideKeyboard()
        var filter_cart_total = 0f

        cartList?.mapIndexed { index, cartInfo ->

            filter_cart_total += if (promoData.supplierIds.isNotEmpty()) {
                promoData.supplierIds.filter { it == cartInfo.supplierId }.sumByDouble { cartInfo.price.times(cartInfo.quantity).toDouble() }.toFloat()
            } else {
                promoData.categoryIds.filter { it == cartInfo.categoryId }.sumByDouble { cartInfo.price.times(cartInfo.quantity).toDouble() }.toFloat()
            }
        }

        promoData.discountPrice = if (filter_cart_total > 0 && promoData.discountType == 0) {
            promoData.discountPrice
        } else {
            filter_cart_total / 100.0f * promoData.discountPrice
        }

        if (promoData.discountPrice > 0) {
            promoData.promoCode = promoCode

            etPromoCode.setText(promoCode)
            etPromoCode.isEnabled = false
            tvRedeem.text = getString(R.string.remove)

            dataManager.addGsonValue(DataNames.DISCOUNT_AMOUNT, Gson().toJson(promoData))
            calculateCartCharges(cartList)
        } else {
            mBinding?.root?.onSnackbar(getString(R.string.no_promocode_cart))
        }

    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_cart
    }

    override fun getViewModel(): CartViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(CartViewModel::class.java)
        return mViewModel as CartViewModel
    }

    override fun onUpdateCart() {
        if (isNetworkConnected) {

            when {
                mSelectedPayment?.keyId == DataNames.PAYMENT_CASH.toString() || (settingData?.payment_method != "null" &&
                        settingData?.payment_method?.toInt() ?: 0 == PaymentType.CASH.payType) || isPaymentConfirm -> {
                    val aa = String.format(java.util.Locale.US, "%.2f", mTipCharges);
                    mTipCharges = aa.toFloat()
                    if (table_name.equals("")) {
                        location_data = tv_deliver_adrs.text.toString()
                    } else {
                        location_data = ""
                    }
                    if (first3 == 0) {
                        Log.e("hellohhj", "hhhhhh")
                        vat_filter.clear()
                        item_filter.clear()
                        item_concate=""
                        vat_concate=""
                        for(i in 0 until categories.size)
                        {
                            item_filter.append(String.format("%.2f", categories[i]?.price) + ",")
                            item_concate = method(item_filter.toString())
                            vat_filter.append(
                                    String.format("%.2f", categories[i]?.handleAdminCharges.toDouble() )+ ",")
                            vat_concate = method(vat_filter.toString())
                        }

                        mViewModel?.generateOrder( item_concate,vat_concate,appUtils, table_name, mDeliveryType1, if (isPaymentConfirm) DataNames.PAYMENT_AFTER_CONFIRM else DataNames.PAYMENT_CASH,
                                mAgentParam, "", "", redeemedAmt, imageList
                                ?: mutableListOf(), edAdditionalRemarks.text.toString().trim(), mTipCharges, restServiceTax, mQuestionList, questionAddonPrice,
                                productData?.appType
                                        ?: 0, mSelectedPayment, havePets, cleaner_in, etParkingInstruction.text.toString().trim(), etAreaFocus.text.toString().trim(), isPaymentConfirm, isDonate,
                                ShowRestaurantPersonalAddress, location_data, group_id)
                        first3 = 1
                    }
                }

                mSelectedPayment?.payName == getString(R.string.razor_pay) -> {
                    initRazorPay(mSelectedPayment)
                }
                mSelectedPayment?.payName == getString(R.string.paypal) -> {
                    initPaypal(mSelectedPayment)
                }
                mSelectedPayment?.payName == "Square Pay" -> {
                    initSquare(mSelectedPayment)
                }

                mSelectedPayment?.payName == getString(R.string.zelle) -> {
                    Log.e("hello", "jklhjgkjh23")
                    onlinePayment(mSelectedPayment)
                }
                mSelectedPayment?.payName == getString(R.string.paystack) -> {
                    onlinePayStack(mSelectedPayment)
                }
                mSelectedPayment?.payName == getString(R.string.saded) -> {
                    getSadedPayment()
                }
                mSelectedPayment?.payName == getString(R.string.myFatoora) -> {
                    initMyFatooraGateway()
                }
                else -> {
                    Log.e("hello", "jklhjgkjh")
                    if (first3 == 0) {
                        if (mSelectedPayment?.addCard == true) {
                            onlinePayment(mSelectedPayment)
                        } else {
                            val promoData = dataManager.getGsonValue(DataNames.DISCOUNT_AMOUNT, PromoCodeModel.DataBean::class.java)
                            totalAmt.minus(promoData?.discountPrice ?: 0f)
                            CardDialogFrag.newInstance(mSelectedPayment, totalAmt.toFloat()).show(childFragmentManager, "paymentDialog")
                        }
                        first3 = 1
                    }
                }
            }
        }
    }

    private fun initSquare(mSelectedPayment: CustomPayModel?) {

        if (mSelectedPayment?.payment_token == "squareup") {
            sqip.InAppPaymentsSdk.squareApplicationId = mSelectedPayment.keyId ?: ""
        }

        CardEntry.startCardEntryActivity(requireActivity(), true,
                AppConstants.REQUEST_SQUARE_PAY)

    }

    private fun onlinePayStack(mSelectedPayment: CustomPayModel?) {
        CardDialogFrag.newInstance(mSelectedPayment, totalAmt.toFloat()).show(childFragmentManager, "paymentDialog")

    }

    private fun getSadedPayment() {
        val userInfo = dataManager.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)
        if (isNetworkConnected) {
            mViewModel?.getSaddedPaymentUrl(userInfo?.data?.email ?: "", userInfo?.data?.firstname
                    ?: "", totalAmt.toString())
        }
    }

    private fun initMyFatooraGateway() {
        //  val currency = dataManager.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)
        val currency = dataManager.getKeyValue(PrefenceConstants.CURRENCY_NAME, PrefenceConstants.TYPE_STRING)
        if (isNetworkConnected) {
            mViewModel?.getMyFatoorahPaymentUrl(currency.toString() ?: "", totalAmt.toString())
        }
    }

    override fun getSaddedPaymentSuccess(data: AddCardResponseData?) {
        mSelectedPayment?.keyId = data?.transaction_reference
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
        startActivityForResult(intent, SADDED_PAYMENT_REQUEST)
    }

    override fun getMyFatoorahPaymentSuccess(data: AddCardResponseData?) {
        val intent = Intent(requireContext(), PaymentWebViewActivity::class.java).putExtra("paymentData", data)
                .putExtra("payment_gateway", getString(R.string.myFatoora))
        startActivityForResult(intent, MY_FATOORAH_PAYMENT_REQUEST)
    }

    override fun onAddAddress(data: AddressBean?) {
        addressId = data?.id ?: 0
    }

    override fun onGroupResponse(data: GroupData?) {
        group_id = data?.result?.get(0)?.id.toString()!!
        if (data?.type.equals("Created")) {
            mBinding?.root?.onSnackbar("Group is created")
        } else {
            mBinding?.root?.onSnackbar("Group is Joined")

        }
        groupDialog?.etGroup?.setText(data?.result?.get(0)?.group_name)
    }

    override fun onRefreshCartError() {
        progress_bar.visibility=View.GONE
        calculateDelivery()
    }

    private fun onlinePayment(mSelectedPayment: CustomPayModel?) {

        val aa = String.format(java.util.Locale.US, "%.2f", mTipCharges);

        if (table_name.equals("")) {
            location_data = tv_deliver_adrs.text.toString()
        } else {
            location_data = ""
        }
        mTipCharges = aa.toFloat()
        vat_filter.clear()
        item_filter.clear()
        item_concate=""
        vat_concate=""
        for(i in 0 until categories.size)
        {
            item_filter.append(String.format("%.2f", categories[i]?.price) + ",")
            item_concate = method(item_filter.toString())
            vat_filter.append(
                    String.format("%.2f", categories[i]?.handleAdminCharges.toDouble() )+ ",")
            vat_concate = method(vat_filter.toString())
        }
        mViewModel?.generateOrder( item_concate,vat_concate,appUtils, table_name, mDeliveryType1, DataNames.PAYMENT_CARD, mAgentParam, mSelectedPayment?.keyId
                ?: "", mSelectedPayment?.payment_token ?: "", redeemedAmt, imageList
                ?: mutableListOf(), edAdditionalRemarks.text.toString().trim(), mTipCharges, restServiceTax, mQuestionList, questionAddonPrice, productData?.appType
                ?: 0, mSelectedPayment, havePets, cleaner_in, etParkingInstruction.text.toString().trim(), etAreaFocus.text.toString().trim(), isPaymentConfirm, isDonate,
                ShowRestaurantPersonalAddress, location_data, group_id)
    }

    private fun initPaypal(mSelectedPayment: CustomPayModel?) {
        val dropInRequest: DropInRequest = DropInRequest()
                .clientToken(mSelectedPayment?.keyId)
        startActivityForResult(dropInRequest.getIntent(requireContext()), PAYPALREQUEST)
    }


    private fun initRazorPay(mSelectedPayment: CustomPayModel?) {

        val userInfo = dataManager.getGsonValue(DataNames.USER_DATA, PojoSignUp::class.java)

        Checkout.preload(activity ?: requireContext())
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
            options.put("amount", totalAmt.times(100))
            options.put("payment_capture", true)
            // options.put("order_id", orderId)

            val preFill = JSONObject()
            preFill.put("email", userInfo?.data?.email)
            preFill.put("contact", userInfo?.data?.mobile_no)

            options.put("prefill", preFill)

            co.open(activity, options)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCartAdded(cartdata: AddtoCartModel.CartdataBean?) {

        /*   val adminCharges = cartList?.sumByDouble {
            (it.fixed_price?.times(it.quantity.toDouble()))?.times(it.handlingAdmin.div(100))
                    ?: 0.0
        }?.toFloat() ?: 0.0f*/

        // mTotalAmt = appUtils.calculateCartTotal().toFloat().plus(mDeliveryCharge).plus(adminCharges).plus(questionAddonPrice)

        if (isNetworkConnected) {
            mViewModel?.updateCartInfo(cartList, appUtils, mDeliveryType, mDeliveryCharge, totalAmt, maxHandlingAdminCharges, productData?.deliveryMax, mQuestionList, questionAddonPrice, settingData?.addRestaurantFloor
                    ?: "", addressId)
        }
    }

    override fun onOrderPlaced(data: ArrayList<Int>) {
        progress_bar.visibility = View.GONE
        orderId = data
        dataManager.removeValue(DataNames.DISCOUNT_AMOUNT)

        if (mDeliveryType == 2) {
            if (first2 == 0) {
                openTakeAwayDialog()
                first2 = 1
            }
        } else {
            if (first == 0) {
                StaticFunction.sweetDialogueSuccess11(activity, getString(R.string.success), getString(R.string.succesfully_order, appUtils.loadAppConfig(0).strings?.order), false, 1001, this)
                first = 1
            }
        }
        StaticFunction.clearCart(activity)
    }

    override fun onOutOfStock(product_name: String) {
        if (first1 == 0) {
            StaticFunction.sweetDialogue(activity, product_name)
            first1 = 1
        }
    }

    override fun onValidatePromo(data: PromoCodeModel.DataBean) {

        data.tempPrice = data.discountPrice

        if (cartList?.sumByDouble {
                    it.price.times(it.quantity).toDouble()
                } ?: 0.0 >= data.minOrder) {
            calculatePromo(data, cartList, etPromoCode.text.toString().trim())
        } else {
            mBinding?.root?.onSnackbar(getString(R.string.sorry_cart_value, "${AppConstants.CURRENCY_SYMBOL}${data.minOrder}"))
        }
    }

    override fun onRefreshCart(mCartData: CartData?) {


        takeOffPrepTime = mCartData?.takeoff_prep_time.toString()
        supplierAddress = mCartData?.address.toString()
        supplierLatitude = mCartData?.latitude ?: 0.0
        supplierLongitude = mCartData?.longitude ?: 0.0
        is_table_enabled = mCartData?.is_table_enabled
        mDeliveryType1 = mCartData?.self_pickup_request
        Log.e("is_table_enable", "" + mDeliveryType1 + "")
        prefHelper.setkeyValue(IS_VAT_INCLUDED, mCartData?.is_vat_included!!)
        if (prefHelper.getKeyValue(IS_VAT_INCLUDED, TYPE_INT) == 1) {
            rvTax.visibility = View.GONE
        } else {
            rvTax.visibility = View.VISIBLE
        }
        regionDeliveryCharges = mCartData?.region_delivery_charge ?: 0.0f
        //  minOrder = mCartData?.min_order

        payment_gateways = mCartData?.payment_gateways

        calculateTipCharges(mCartData?.tips ?: arrayListOf())

        val prodList = mCartData?.result ?: listOf()

        cartList?.map { cart ->

            var productDta = prodList.filter {
                it.product_id == cart.productId
            }

            if (productDta.size > 1) {

                val prodlist = productDta.filter {
                    it.discount == cart.isDiscount
                }

                productDta = prodlist

            }

            if (productDta.isNotEmpty()) {
                productData?.distance_value = productDta.firstOrNull()?.distance_value
                cart.distance_value = productDta.firstOrNull()?.distance_value
                cart.avgRating = productDta.first().avg_rating
                cart.radius_price = productDta.first().radius_price
                cart.purchasedQuant = productDta.first().purchased_quantity
                cart.prodQuant = productDta.first().quantity

                if (productData?.appType == AppDataType.Ecom.type) {
                    cart.deliveryCharges = productDta.first().delivery_charges ?: 0.0f
                }

                cart.handlingAdmin = productDta.first().handling_admin ?: 0.0f


                cart.add_ons?.mapIndexed { _, productAddon ->

                    val mFilterAddon = productDta.first().adds_on?.findLast { it?.name == productAddon?.name }?.value?.findLast {
                        it.type_id == productAddon?.type_id
                    }
                    if (mFilterAddon != null) {
                        productAddon?.price = mFilterAddon.price ?: 0f
                    }
                }

                cart.price = if (cart.add_ons?.isNotEmpty() == true) {
                    cart.add_ons?.sumByDouble {
                        it?.price?.toDouble() ?: 0.0
                    }?.toFloat()?.plus(productDta.first().fixed_price?.toFloatOrNull() ?: 0.0f)
                            ?: 0f
                } else {
                    if (cart.priceType == 1) {
                        cart.price
                    } else {
                        productDta.first().fixed_price?.toFloatOrNull() ?: 0f
                    }

                }

                // cartInfo.gstTotal = productDta[0].gst_price

                takeIf { productDta.first().purchased_quantity == productDta.first().quantity || productDta.first().quantity == 0 }.let {
                    cart.isQuantity = 0
                }

            } else {
                cart.isStock = false
                /*           StaticFunction.removeFromCart(activity, cartList?.get(index)?.productId, 0, false)
                       cartList?.removeAt(index)*/
            }
        }

        mCartData?.defaultCard?.let { defaultCard ->

            dataManager.getKeyValue(DataNames.FEATURE_DATA, PrefenceConstants.TYPE_STRING).toString().let {
                val listType: Type = object : TypeToken<ArrayList<SettingModel.DataBean.FeatureData?>?>() {}.type
                val featureList: ArrayList<SettingModel.DataBean.FeatureData> = Gson().fromJson(it, listType)


                val stripe = featureList.filter { feature ->
                    (feature.name == "Stripe")
                }

                var keyId: String? = null
                if (stripe.isNotEmpty())
                    keyId = stripe[0].key_value_front?.get(0)?.value

                mSelectedPayment = CustomPayModel(
                        addCard = true,
                        cardId = defaultCard.card_id,
                        customerId = defaultCard.customer_payment_id,
                        image = R.drawable.ic_payment_card,
                        payName = getString(R.string.online_payment),
                        payement_front = null,
                        payment_token = defaultCard.card_source,
                        keyId = keyId)
            }

        }

        calculateDelivery()

    }

    private fun calculateTipCharges(data: ArrayList<Int>) {
        if (mDeliveryType == FoodAppType.Pickup.foodType || data.isEmpty()) return

        group_tip.visibility = View.VISIBLE

        tipList.clear()
        for (i in 0 until data.size) {
            var tipmodel: TiPModel = TiPModel(data.get(i), false)
            tipList.add(tipmodel)
        }

        tipAdapter?.notifyDataSetChanged()
        if (tipList.size > 0) {
            layoutTip.visibility = View.VISIBLE
        } else {
            layoutTip.visibility = View.GONE
        }
    }


    private fun manageStock(cartInfos: MutableList<CartInfo>?) {
        tv_checkout.isEnabled = cartInfos?.any { it.isStock == false } != true
    }

    override fun onCalculateDistance(value: Int?) {
        val valueInKm = (value?.toFloat())?.div(1000f)
        mDeliveryCharge = if (valueInKm?.minus(productData?.distance_value ?: 0f) ?: 0f > 0f) {
            ((valueInKm?.minus(productData?.distance_value ?: 0f))?.times(productData?.radius_price
                    ?: 0f) ?: 0f)
                    .plus(baseDeliveryCharges)
        } else
            valueInKm?.times(productData?.radius_price ?: 0.0f) ?: 0f

        //  tvDeliveryCharges.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mDeliveryCharge)
        calculateCartCharges(cartList)
    }

    override fun onAddress(data: DataBean?) {

        if (data?.address?.isNotEmpty() == true) {

            adrsData = (if (deliveryId.isEmpty() || deliveryId == "0") {
                data.address?.first()
            } else {
                data.address?.filter { it.id.toString() == deliveryId }?.get(0)
            }) ?: AddressBean()

            adrsData.user_service_charge = data.user_service_charge
            adrsData.preparation_time = data.preparation_time

            updateAddress(adrsData)

        }

        baseDeliveryCharges = data?.base_delivery_charges ?: 0.0f
        minOrder = data?.min_order

        setData()

    }

    override fun onSupplierLocations(data: SupplierLocationBean?) {

        if (data?.address?.isNotEmpty() == true) {

            locationData = (if (deliveryId.isEmpty() || deliveryId == "0") {
                data.supplier_locations.first()
            } else {
                data.supplier_locations.filter { it.location.toString() == deliveryId }.get(0)
            })

            locationData.user_service_charge = data.user_service_charge
            locationData.preparation_time = data.preparation_time

            updateLocationAddress(locationData)

        }

        baseDeliveryCharges = data?.base_delivery_charges ?: 0.0f
        minOrder = data?.min_order

        setData()

    }

    override fun onReferralAmt(value: Float?) {

        if (value ?: 0.0f > 0) {
            mReferralAmt = value ?: 0.0f
            llReferral.visibility = View.VISIBLE

            etReferralPoint.text = getString(R.string.referral_amt_tag, "${AppConstants.CURRENCY_SYMBOL} ${value.toString()}")
        } else {
            llReferral.visibility = View.GONE
            group_referral.visibility = View.GONE
        }

    }

    private fun updateAddress(adrsData: AddressBean) {

        if (mDeliveryType != FoodAppType.Pickup.foodType) {

            val locUser = LocationUser((adrsData.latitude
                    ?: 0.0).toString(), (adrsData.longitude
                    ?: 0.0).toString(), "${adrsData.customer_address} , ${adrsData.address_line_1}")
            dataManager.addGsonValue(DataNames.LocationUser, Gson().toJson(locUser))

            dataManager.setkeyValue(DataNames.PICKUP_ID, adrsData.id.toString())
            dataManager.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsData))
            deliveryId = adrsData.id.toString()
            tv_deliver_adrs.text = "${adrsData.address_line_1 ?: ""},${adrsData.customer_address ?: ""}"
        }
    }

    private fun updateLocationAddress(adrsData: SupplierLocation) {

        if (mDeliveryType != FoodAppType.Pickup.foodType) {

            val locUser = LocationUser((0.0).toString(), (0.0).toString(), "${adrsData.location}")
            dataManager.addGsonValue(DataNames.LocationUser, Gson().toJson(locUser))

            dataManager.setkeyValue(DataNames.SUPPLIER_LOCATION_ID, adrsData.location.toString())
            dataManager.addGsonValue(PrefenceConstants.SUPPLIER_ADDRESS_DATA, Gson().toJson(adrsData))

            deliveryId = adrsData.location.toString()
            tv_deliver_adrs.text = adrsData.location
        }
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        adrsData = adrsBean

        baseDeliveryCharges = adrsData.base_delivery_charges ?: 0.0f
        minOrder = adrsData.min_order

        if (isNetworkConnected) {
            if (::adrsData.isInitialized)
                viewModel.refreshCart(CartReviewParam(product_ids = cartList?.map { it.productId },
                        latitude = adrsData.latitude ?: "", self_pickup = "" + mDeliveryType
                        ?: "", longitude = adrsData.longitude
                        ?: ""))
        }

        //   adrsData.user_service_charge= mAddressBean.user_service_charge
        // adrsData.preparation_time= mAddressBean.preparation_time
        updateAddress(adrsData)
    }

    override fun onLocationSelect(location: SupplierLocation) {
firstt=1
        locationData = location

        baseDeliveryCharges = locationData.base_delivery_charges ?: 0.0f
        minOrder = locationData.min_order

        if (isNetworkConnected) {
            if (::locationData.isInitialized)
                viewModel.refreshCart(CartReviewParam(product_ids = cartList?.map { it.productId },
                        latitude = "0.0", self_pickup = "" + mDeliveryType
                        ?: "", longitude = "0.0"))
        }

        //   adrsData.user_service_charge= mAddressBean.user_service_charge
        // adrsData.preparation_time= mAddressBean.preparation_time
        updateLocationAddress(locationData)
        handleBothDelivery()
        // mBinding?.tvCheckout?.callOnClick()
    }

    override fun onDestroyDialog() {

    }

    override fun onSuccessListener() {

        AppConstants.DELIVERY_OPTIONS = DeliveryType.DeliveryOrder.type

        //   if (productData?.appType ?: 0 > 0) {
        screenFlowBean?.app_type = AppConstants.APP_SAVED_SUB_TYPE
        dataManager.setkeyValue(DataNames.SCREEN_FLOW, Gson().toJson(screenFlowBean))
        // }

        //  AppConstants.APP_SUB_TYPE=0

        val action = CartDirections.actionCartToMainFragment()
        navController(this@Cart).navigate(action)
        activity?.startActivity(Intent(activity, OrderDetailActivity::class.java)
                .putExtra(DataNames.REORDER_BUTTON, false).putIntegerArrayListExtra("orderId", orderId))
    }

    override fun onSucessListner() {

    }

    override fun onErrorListener() {

    }

    override fun paymentToken(token: String, paymentMethod: String, savedCard: SaveCardInputModel?) {

        if (isNetworkConnected) {
            if (table_name.equals("")) {
                location_data = tv_deliver_adrs.text.toString()
            } else {
                location_data = ""
            }
            Log.e("hellohhj", "hhhhhhsdsdfds")
            vat_filter.clear()
            item_filter.clear()
            item_concate=""
            vat_concate=""
            for(i in 0 until categories.size)
            {
                item_filter.append(String.format("%.2f", categories[i]?.price) + ",")
                item_concate = method(item_filter.toString())
                vat_filter.append(
                        String.format("%.2f", categories[i]?.handleAdminCharges.toDouble() )+ ",")
                vat_concate = method(vat_filter.toString())
            }
            mViewModel?.generateOrder( item_concate,vat_concate,appUtils, table_name, mDeliveryType1, DataNames.PAYMENT_CARD, mAgentParam, token, paymentMethod,
                    redeemedAmt, imageList
                    ?: mutableListOf(), edAdditionalRemarks.text.toString().trim(), mTipCharges, restServiceTax, mQuestionList, questionAddonPrice, productData?.appType
                    ?: 0, mSelectedPayment,
                    havePets, cleaner_in, etParkingInstruction.text.toString().trim(), etAreaFocus.text.toString().trim(), isPaymentConfirm, isDonate,
                    ShowRestaurantPersonalAddress, location_data, group_id)
        }
    }


    private fun uploadImage() {
        if (permissionFile.hasCameraPermissions(activity ?: requireContext())) {
            if (isNetworkConnected) {
                showImagePicker()
            }
        } else {
            permissionFile.cameraAndGalleryTask(this)
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showImagePicker() {
        imageDialog.settingCallback(this)
        imageDialog.show(
                childFragmentManager,
                "image_picker"
        )
    }


    override fun onGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, AppConstants.GalleyPicker)
    }

    override fun onCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    ImageUtility.filename(imageUtils)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            activity?.packageName ?: "",
                            it)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, AppConstants.CameraPicker)
                }
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == AppConstants.CameraGalleryPicker) {
            if (isNetworkConnected) {
                showImagePicker()
            }
        }
    }


    override fun onTipSelected(position: Int) {
        if (firstt == 0) {
//        tvTipAmount.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
//        tvTipCharges.text = activity?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
            isTipClear = true
            tipPercentage = tipList[position].tip
            if (addTipInPercentage == "1") {


                if (subTotal != 0.0F) {

                    mTipCharges = (subTotal * tipList[position].tip.toFloat()) / 100

                    tvTipAmount.text =
                        getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
                    tvTipCharges.text = activity?.getString(
                        R.string.currency_tag,
                        AppConstants.CURRENCY_SYMBOL,
                        mTipCharges
                    )
                    if (rvTax.visibility == View.GONE) {
                        calculateCartCharges(cartList)
                    } else {
                        tipClearCalculateCartCharges(cartList)
                    }
                }

            } else {
                mTipCharges = mTipCharges + tipList[position].tip.toFloat()
                tvTipAmount.text =
                    getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mTipCharges)
                tvTipCharges.text = activity?.getString(
                    R.string.currency_tag,
                    AppConstants.CURRENCY_SYMBOL,
                    mTipCharges
                )
                calculateCartCharges(cartList)
                if (rvTax.visibility == View.GONE) {
                    mBinding?.rvTax?.adapter = TaxAdaptor(requireContext(), categories)
                }
            }

        }
    }

    override fun onTableAdded(table_name: String) {
        this.table_name = table_name
        // if (isNetworkConnected) {
        firstt=1
        progress_bar.visibility=View.VISIBLE
        viewModel.refreshCart(CartReviewParam(product_ids = cartList?.map { it.productId },
                latitude = "0.0", self_pickup = "" + mDeliveryType ?: "", longitude = "0.0"))
        val hashMap: HashMap<String, String> = HashMap()
        val loc = dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)
        hashMap["latitude"] = loc?.latitude.toString()
        hashMap["longitude"] = loc?.longitude.toString()
        hashMap["addressLineFirst"] = loc?.address.toString()
        hashMap["customer_address"] = loc?.address.toString()
        if (dataManager.getCurrentUserLoggedIn()) {
            mViewModel?.addAddress(hashMap)
        }
        val r = Runnable {
            handleBothDelivery()
        }
        Handler().postDelayed(r, 4000)

        //}
        // Toast.makeText(requireActivity(),table_name, Toast.LENGTH_SHORT).show()
    }
}