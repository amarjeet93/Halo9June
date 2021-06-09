package com.codebrew.clikat.module.splash

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.codebrew.clikat.BR
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.CommonUtils.Companion.getaddress
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseActivity
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.GetDbKeyModel
import com.codebrew.clikat.data.network.HostSelectionInterceptor
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.ActivitySplashBinding
import com.codebrew.clikat.databinding.DialogEnterCodeBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.DataCount
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.bottom_navigation.MainScreenActivity
import com.codebrew.clikat.module.instruction_page.InstructionActivity
import com.codebrew.clikat.module.location.LocationActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.retrofit.Config
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction
import com.google.android.material.button.MaterialButton
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_splash.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.inject.Inject

/*
 * Created by Ankit Jindal on 18/4/16.
 */

const val TAG = "DeepLink"

class SplashActivity : BaseActivity<ActivitySplashBinding, SplashViewModel>(), SplashNavigator, EasyPermissions.PermissionCallbacks {


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper


    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var interceptor: HostSelectionInterceptor

    @Inject
    lateinit var mDialogUtil: DialogsUtil

    @Inject
    lateinit var appUtils: AppUtils


    private var dialog: Dialog? = null


    private var mSplashViewModel: SplashViewModel? = null

    private lateinit var mGson: Gson

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        return R.layout.activity_splash
    }

    override fun getViewModel(): SplashViewModel {
        mSplashViewModel = ViewModelProviders.of(this, factory).get(SplashViewModel::class.java)
        return mSplashViewModel as SplashViewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        mGson = Gson()
        StaticFunction.setStatusBarColor(this, Color.parseColor("#ffffff"))
        Glide.with(this).asGif().load(R.raw.halo).into(ivSplashLogo);

        settingObserver()
        orderObserver()
        userCodeObserver()

        handleDynamicLink()

        /* if(BuildConfig.DEBUG)
         {*/
        printHashKey(this)
        //}

        /*  checkAppDBKey(prefHelper.getKeyValue(
                  PrefenceConstants.DB_SECRET,
                  PrefenceConstants.TYPE_STRING)!!.toString())
          fetchSetting()*/
    }

    private fun handleDynamicLink() {

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    val deepLink: Uri?
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link

                        if (!deepLink?.getQueryParameter("uniqueId").isNullOrBlank()) {

                            val uniqueCode = deepLink?.getQueryParameter("uniqueId")

                            if (uniqueCode == prefHelper.getKeyValue(PrefenceConstants.APP_UNIQUE_CODE, PrefenceConstants.TYPE_STRING)) {
                                checkAppDBKey(prefHelper.getKeyValue(
                                        PrefenceConstants.DB_SECRET,
                                        PrefenceConstants.TYPE_STRING)!!.toString())
                                fetchSetting()
                            } else {
                                CommonUtils.setBaseUrl(retrofit = retrofit, baseUrl = BuildConfig.ONBOARD_URL)

                                if (isNetworkConnected) {
                                    viewModel.validateUserCode(deepLink?.getQueryParameter("uniqueId")
                                            ?: "")
                                }
                            }
                        } else {
                            validateSecretCode()
                        }

                    } else {
                        validateSecretCode()
                    }
                }
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    //parul_0267

    private fun validateSecretCode() {

        val mClientCode = BuildConfig.CLIENT_CODE

        if (prefHelper.getKeyValue(PrefenceConstants.DB_SECRET, PrefenceConstants.TYPE_STRING)?.toString()?.isEmpty() == true
                || mClientCode != prefHelper.getKeyValue(PrefenceConstants.APP_UNIQUE_CODE, PrefenceConstants.TYPE_STRING)?.toString()) {
            CommonUtils.setBaseUrl(retrofit = retrofit, baseUrl = BuildConfig.ONBOARD_URL)
            //      showDialog()
            if (isNetworkConnected) {
                prefHelper.onClear()
                viewModel.validateUserCode(mClientCode)
            }
        } else {
            Config.dbSecret = prefHelper.getKeyValue(PrefenceConstants.DB_SECRET, PrefenceConstants.TYPE_STRING).toString()
            checkAppDBKey(prefHelper.getKeyValue(
                    PrefenceConstants.DB_SECRET,
                    PrefenceConstants.TYPE_STRING)!!.toString())
            fetchSetting()
        }
    }


    private fun checkAppDBKey(dBKey: String) {

        if (dBKey.isNotEmpty()) {
            if (interceptor.secret_key == null || interceptor.secret_key
                    !== dBKey) {
                interceptor.secret_key = dBKey
            }
            CommonUtils.setBaseUrl(BuildConfig.BASE_URL, retrofit)
        }
    }


    private fun showDialog() {

        val binding = DataBindingUtil.inflate<DialogEnterCodeBinding>(LayoutInflater.from(this), R.layout.dialog_enter_code, null, false)
        // binding.appData= ColorConfig()
        dialog = mDialogUtil.showDialogFix(this, binding.root)


        val button = dialog?.findViewById<MaterialButton>(R.id.btn_submit)
        val editText = dialog?.findViewById<EditText>(R.id.edit_code)


        button?.setOnClickListener {
            if (editText?.text?.toString()?.isNotEmpty() == true) {
                if (isNetworkConnected) {
                    viewModel.validateUserCode(editText.text.toString())
                }
            } else {
                rvContainer.onSnackbar(getString(R.string.enter_supplier_code))
            }
        }

        dialog?.show()
    }

    private fun fetchSetting() {
        if (isNetworkConnected) {
            viewModel.fetchSetting()
        }
    }


    private fun fetchOrderCount(accessToken: String) {
        if (isNetworkConnected) {
            viewModel.fetchOrderCount(accessToken)
        }
    }


    private fun settingObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<SettingModel.DataBean> { resource ->
            updateSettingData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.settingLiveData.observe(this, catObserver)
    }


    private fun orderObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataCount> { resource ->

            updateOrderCount(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.orderCountLiveData.observe(this, catObserver)

    }

    private fun userCodeObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<GetDbKeyModel> { resource ->
            checkAppDBKey(resource?.data?.data?.get(0)?.cbl_customer_domains?.get(0)?.db_secret_key
                    ?: "")
            dialog?.dismiss()

            fetchSetting()
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.userCodeLiveData.observe(this, catObserver)

    }

    private fun updateOrderCount(resource: DataCount?) {


        mGson.toJson(resource)?.let { prefHelper.setkeyValue(DataNames.ORDERS_COUNT, it) }

        runNormal11()
    }


    private fun updateSettingData(resource: SettingModel.DataBean?) {

        //  resource?.key_value?.secondary_language="ar"

        AppConstants.APP_SUB_TYPE = resource?.screenFlow?.first()?.app_type?:0
        AppConstants.APP_SAVED_SUB_TYPE = resource?.screenFlow?.first()?.app_type?:0

        resource?.dialog_token?.let { prefHelper.setkeyValue(DataNames.DIALOG_TOKEN, it) }


        resource?.screenFlow?.get(0)?.app_type?.let { prefHelper.setkeyValue(DataNames.APP_TYPE, it) }


        if (resource?.bookingFlow?.isNotEmpty() == true) {

            val bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

            if (bookingFlowBean != null && bookingFlowBean.cart_flow != resource.bookingFlow?.get(0)?.cart_flow) {
                StaticFunction.removeAllCart(this@SplashActivity)
                StaticFunction.clearCart(this@SplashActivity)
            }

            /*    //cart flow
            // 0->single product with single fixed_quantity
            // 1->single product with multiple fixed_quantity
            // 2->multiple product with single fixed_quantity
            // 3->multiple product with multiple fixed_quantity
            response.body().getData().getBookingFlow().get(0).setCart_flow(3);

            //vendor_status 1 for multiple 0 for single
            response.body().getData().getBookingFlow().get(0).setVendor_status(1);*/

            // resource.bookingFlow?.get(0)?.vendor_status=0

            if(prefHelper.getKeyValue(PrefenceConstants.APP_UNIQUE_CODE, PrefenceConstants.TYPE_STRING)?.toString()=="royobeauty_0254")
            {
                resource.bookingFlow?.get(0)?.vendor_status=0
                resource.bookingFlow?.get(0)?.cart_flow=1
            }

            prefHelper.setkeyValue(DataNames.BOOKING_FLOW, mGson.toJson(resource.bookingFlow?.get(0)))
        }

        prefHelper.setkeyValue(DataNames.SETTING_DATA, mGson.toJson(resource?.key_value))

        //var terminology:SettingModel.DataBean.Terminology?=null

        if (resource?.key_value?.terminology?.isNotEmpty() == true) {
            // terminology=mGson.fromJson<SettingModel.DataBean.Terminology>(resource.key_value.terminology,SettingModel.DataBean.Terminology::class.java)

            prefHelper.addGsonValue(PrefenceConstants.APP_TERMINOLOGY, resource.key_value.terminology?:"")
        }

        if (resource?.key_value?.login_icon_url?.isNotEmpty()==true){
            prefHelper.addGsonValue(PrefenceConstants.LOGIN_ICON_URL,resource.key_value.login_icon_url)

        }
    /*    resource?.key_value?.secondary_language.let {
            prefHelper.setkeyValue(DataNames.SELECTED_LANGUAGE,Locale(it?:ClikatConstants.ENGLISH_FULL).displayLanguage.toLowerCase(Locale.getDefault()))
        }*/


        prefHelper.setkeyValue(PrefenceConstants.GENRIC_SUPPLIERID, resource?.supplier_id ?: 0)

        if (resource?.termsAndConditions?.isNotEmpty() == true) {
            prefHelper.addGsonValue(PrefenceConstants.TERMS_CONDITION, Gson().toJson(resource.termsAndConditions?.get(0)))
        }


        prefHelper.setkeyValue(PrefenceConstants.GENERIC_SPL_BRANCHID, resource?.supplier_branch_id
                ?: 0)

        if (resource?.screenFlow?.isNotEmpty() == true) {

            // dataManager.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java
            //  val latitude:Double?= null
            //        val longitude:Double?= null
            //        val supplierName:String?=null

            if (resource.screenFlow?.get(0)?.is_single_vendor == VendorAppType.Single.appType) {
                val mResturantDetail = LocationUser(resource.latitude.toString(), resource.longitude.toString()
                        ?: "",
                        "${resource.supplierName} , ${getaddress(this, resource.latitude
                                ?: 0.0, resource.longitude ?: 0.0)}")
                prefHelper.addGsonValue(PrefenceConstants.RESTAURANT_INF, Gson().toJson(mResturantDetail))
            }

            prefHelper.setkeyValue(DataNames.SCREEN_FLOW, mGson.toJson(resource.screenFlow?.get(0)))
            prefHelper.setkeyValue(PrefenceConstants.SELF_PICKUP,resource.self_pickup?:"")
        }

        if (prefHelper.getCurrentUserLoggedIn()) {


            fetchOrderCount(prefHelper.getKeyValue(PrefenceConstants.ACCESS_TOKEN, PrefenceConstants.TYPE_STRING).toString())
        } else {
            runNormal11()
        }
    }


    private fun selectLanguage(language: String) {

        val selectedLang=Locale(language).displayLanguage.toLowerCase(Locale.getDefault())

        when (selectedLang) {
            "arabic" -> {
                setLocale(selectedLang)
                GeneralFunctions.force_layout_to_RTL(this)
                prefHelper.setkeyValue(DataNames.SELECTED_LANGUAGE, selectedLang)
            }
            "spanish"-> {
                setLocale(selectedLang)
                GeneralFunctions.force_layout_to_LTR(this)
                prefHelper.setkeyValue(DataNames.SELECTED_LANGUAGE, selectedLang)
            }
            else -> {
                setLocale("english")
                GeneralFunctions.force_layout_to_LTR(this)
                prefHelper.setkeyValue(DataNames.SELECTED_LANGUAGE, "english")
            }
        }
    }

        private fun setLocale(langCode:String) {
        //Log.e("Lan",session.getLanguage());
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val res = resources
            val config = res.configuration
            if (Build.VERSION.SDK_INT >= 21) {
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                createConfigurationContext(config)
            } else {
                config.locale = locale
                res.updateConfiguration(config, res.displayMetrics)
            }
    }


    private fun runNormal11() {

        //selectLanguageId()
        selectLanguage(prefHelper.getKeyValue(DataNames.SELECTED_LANGUAGE,PrefenceConstants.TYPE_STRING) as String)
        appUtils.getCurrencySymbol()

        val isGuest = viewModel.settingLiveData.value?.key_value?.login_template.isNullOrEmpty() || viewModel.settingLiveData.value?.key_value?.login_template == "0"

        if (prefHelper.getKeyValue(DataNames.FIRST_TIME, PrefenceConstants.TYPE_BOOLEAN) as Boolean) {
            //after instruction screen
            if(prefHelper.getGsonValue(DataNames.LocationUser,LocationUser::class.java)==null)
            {
                launchActivity<LocationActivity>()
            }else
            {
                if (isGuest || prefHelper.getCurrentUserLoggedIn()) {
                    //  Log.e("data",""+intent.extras)
                    launchActivity<MainScreenActivity>(intent.extras ?: Bundle.EMPTY)
                } else {
                    appUtils.checkLoginFlow(this@SplashActivity)
                }
            }
        } else {
            launchActivity<InstructionActivity>()
        }
        finish()
    }

    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }


    override fun onErrorOccur(message: String) {

        rvContainer.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == AppConstants.REQUEST_CODE_LOCATION) {
            if (isNetworkConnected) {
                handleDynamicLink()
            }
        }
    }


}