package com.codebrew.clikat.app_utils


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.Currency
import com.codebrew.clikat.data.model.others.AppCartModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.modal.CartInfo
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.ProductAddon
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.module.new_signup.SigninActivity
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.AppConfiguration
import com.codebrew.clikat.utils.configurations.TextConfig
import com.facebook.FacebookSdk
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.DateFormatSymbols
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.hypot

/**
 *
 *
 * Contains commonly used methods in an Android App
 */
class AppUtils @Inject constructor(private val mContext: Context) {


    @Inject
    lateinit var mPreferenceHelper: PreferenceHelper


    @Inject
    lateinit var mDialogsUtil: DialogsUtil


/*    private val mContext: Context? = null

    fun ImageUtility(context: Context): ??? {
        this.mContext = context
    }*/
    /**
     * check if user has enabled Gps of device
     *
     * @return true or false depending upon device Gps status
     */
    val isGpsEnabled: Boolean
        get() {
            val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    fun checkEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email.trim { it <= ' ' }).matches()
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    /**
     * Description : Hide the soft keyboard
     *
     * @param view : Pass the current view
     */
    fun hideSoftKeyboard(view: View) {
        val inputMethodManager = mContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideSoftKeyboardOut(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0)
    }

    /*
*getting name from int
* */
    fun getMonth(month: Int): String {
        return DateFormatSymbols().months[month]
    }

    /**
     * Show snackbar
     *
     * @param view view clicked
     * @param text text to be displayed on snackbar
     */
    fun showSnackBar(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }


    /**
     * Show snackbar
     *
     * @param text text to be displayed on Toast
     */
    fun showToast(text: String) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show()
    }


    //return error message from webservice error code
    private fun getErrorMessage(throwable: Throwable): String {
        val errorMessage: String
        if (throwable is HttpException || throwable is UnknownHostException
                || throwable is ConnectException) {
            errorMessage = "Something went wrong"
        } else {
            errorMessage = "Unfortunately an error has occurred!"
        }
        return errorMessage
    }

    /**
     * Redirect user to enable GPS
     */
    fun goToGpsSettings() {
        val callGPSSettingIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        mContext.startActivity(callGPSSettingIntent)
    }

    /**
     * check if user has permissions for the asked permissions
     */
    fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }


    fun round(datavalue: Double, places: Int): Double {
        var value = datavalue
        if (places < 0) throw IllegalArgumentException()

        val factor = Math.pow(10.0, places.toDouble()).toLong()
        value = value * factor
        val tmp = Math.round(value)
        return tmp.toDouble() / factor
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun changeStatusBarColor(activity: Activity) {
        val window = activity.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimary)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun changeStatusBarTranparent(activity: Activity) {
        val window = activity.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        // finally change the color
        window.statusBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
    }

    fun formatPrice(price: String): String {
        val formatter = DecimalFormat("#,###,###")
        return formatter.format(java.lang.Double.valueOf(price))
    }


    /**
     * Convert date from one format to another
     *
     * @param dateToConvert date to be converted
     * @param formatFrom    the format of the date to be converted
     * @param formatTo      the format of date you want the output
     * @return date in string as per the entered formats
     */
    @SuppressLint("SimpleDateFormat")
    fun convertDateOneToAnother(dateToConvert: String, formatFrom: String, formatTo: String): String? {
        var outputDateStr: String? = null
        val inputFormat = SimpleDateFormat(formatFrom, Locale.ENGLISH)
        // inputFormat.timeZone = TimeZone.getDefault()
        val outputFormat = SimpleDateFormat(formatTo, Locale.ENGLISH)
        outputFormat.timeZone = TimeZone.getDefault()
        val date: Date
        try {
            date = inputFormat.parse(dateToConvert) ?: Date()
            outputDateStr = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDateStr
    }


    /**
     * Convert date from one format to another
     *
     * @param dateToConvert date to be converted
     * @param formatFrom    the format of the date to be converted
     * @param formatTo      the format of date you want the output
     * @return date in string as per the entered formats
     */
    @SuppressLint("SimpleDateFormat")
    fun addMinutesToDate(dateToConvert: String, formatFrom: String, formatTo: String, minute: Int): String? {
        var outputDateStr: String? = null
        val inputFormat = SimpleDateFormat(formatFrom, Locale.ENGLISH)
        // inputFormat.timeZone = TimeZone.getDefault()
        val outputFormat = SimpleDateFormat(formatTo, Locale.ENGLISH)
        outputFormat.timeZone = TimeZone.getDefault()
        val date: Date
        try {
            val calendar = Calendar.getInstance(Locale.getDefault())
            date = inputFormat.parse(dateToConvert) ?: Date()
            calendar.time = date
            calendar.add(Calendar.MINUTE, minute)
            outputDateStr = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDateStr
    }


    /**
     * Convert date from one format to another
     *
     * @param dateToConvert date to be converted
     * @param formatFrom    the format of the date to be converted
     * @param formatTo      the format of date you want the output
     * @return date in string as per the entered formats
     */
    @SuppressLint("SimpleDateFormat")
    fun convertDateToAddDate(dateToConvert: String, formatFrom: String, formatTo: String, duration: Int): String? {
        var outputDateStr: String? = null
        val inputFormat = SimpleDateFormat(formatFrom, Locale.getDefault())
        // inputFormat.timeZone = TimeZone.getDefault()
        val outputFormat = SimpleDateFormat(formatTo, Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        try {

            val cal = Calendar.getInstance()
            cal.time = inputFormat.parse(dateToConvert) ?: Date()
            cal.add(Calendar.MINUTE, duration)
            outputDateStr = outputFormat.format(cal.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDateStr
    }


    fun getCalendarFormat(dateToConvert: String, dateFormat: String): Calendar? {
        val inputFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        var date: Date? = null
        try {
            date = inputFormat.parse(dateToConvert)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date ?: Date()
        return calendar
    }

    fun getAddress(lat: Double, lng: Double): Address? {
        var addresses: List<Address?>? = null
        val geocoder: Geocoder = Geocoder(mContext, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (e: IOException) {
            e.printStackTrace()
        }
        /*  String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();*/
        return addresses?.get(0) ?: Address(Locale.getDefault())
    }


    fun getCartData(): AppCartModel {
        val cartModel = AppCartModel()

        val cartList: CartList? = getCartList()


        val count = cartList?.cartInfos?.size ?: 0
        if (count > 0) {

            cartModel.cartAvail = true
            var price = 0f
            val cartInfos = cartList?.cartInfos ?: mutableListOf()
            val supplierIds = ArrayList<Int>()
            var totalItem = 0






            for (i in cartInfos.indices) {


                if (cartInfos[i].hourlyPrice.isNullOrEmpty()) {
                    price += cartInfos[i].price * cartInfos[i].quantity

                } else {
                    price += cartInfos[i].price
                }



                totalItem += cartInfos[i].quantity

                if (!supplierIds.contains(cartInfos[i].supplierId)) {
                    supplierIds.add(cartInfos[i].supplierId)
                }
            }

            val aa = String.format(Locale.US, "%.2f", price);

            cartModel.totalPrice = aa
            cartModel.totalCount = totalItem

            if (supplierIds.size < 2) {
                cartModel.supplierName = cartInfos[0].supplierName ?: ""
            } else {
                cartModel.supplierName == ""
            }

        }

        return cartModel

    }

    fun checkVendorStatus(vendorId: Int?): Boolean {
        val cartList: CartList? = getCartList()

        return cartList?.cartInfos?.any {
            it.supplierId != vendorId
        } ?: false || checkAppType(cartList)
    }

    fun checkAppType(cartList: CartList?): Boolean {

        return if (cartList?.cartInfos?.count() ?: 0 == 0) {
            false
        } else {
            cartList?.cartInfos?.count() ?: 0 != cartList?.cartInfos?.filter { it.appType == cartList.cartInfos?.first()?.appType ?: 0 }?.count()
        }

    }


    fun checkBookingFlow(mContext: Context, productId: Int?, listener: DialogListener): Boolean {

        var bookingStatus = false

        val cartList: CartList? = getCartList()

        val cartCount = cartList?.cartInfos ?: mutableListOf()


        val bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

        if (cartCount.size > 0) {

            when (bookingFlowBean?.cart_flow) {
                0 -> {

                    if (cartCount.any { it.productId == productId }) {
                        mDialogsUtil.openAlertDialog(mContext, mContext.getString(R.string.clearCart_multiprod_quant), "Yes", "No", listener)
                    } else {
                        mDialogsUtil.openAlertDialog(mContext, mContext.getString(R.string.clearCart_product), "Yes", "No", listener)
                    }
                }
                2 ->
                    //check multiple product have single fixed_quantity

                    if (cartCount.any { it.productId == productId && it.quantity >= 1 }) {
                        mDialogsUtil.openAlertDialog(mContext, mContext.getString(R.string.clearCart_multiprod_quant), "Yes", "No", listener)
                    } else {
                        bookingStatus = true
                    }

                1 ->
                    //check single product have multiple fixed_quantity

                    if (cartCount.any { it.productId == productId }) {
                        bookingStatus = true
                    } else {
                        mDialogsUtil.openAlertDialog(mContext, mContext.getString(R.string.clearCart_multiprod_quant), "Yes", "No", listener)
                    }

                3 -> bookingStatus = true


            }
        } else {
            bookingStatus = true
        }

        return bookingStatus
    }

    fun checkProdExistance(mProdId: Int?): Boolean {
        val cartList: CartList? = getCartList()

        return cartList?.cartInfos?.any { it.productId == mProdId } ?: false
    }


    fun checkProductAddon(mAddonList: MutableList<ProductAddon?>?): CartInfo {


        val cartList: CartList? = getCartList()

        cartList?.cartInfos?.forEachIndexed { _, cartInfo ->

            if (!cartInfo.add_ons.isNullOrEmpty() && mAddonList?.size == cartInfo.add_ons?.size) {

                val status = mAddonList?.zip(cartInfo.add_ons
                        ?: mutableListOf())?.map { it.second?.type_id == it.first?.type_id }

                if (!status.isNullOrEmpty() && !status.any { !it }) {
                    return cartInfo
                }
            }
        }

        return CartInfo()
    }

    fun clearCart() {
        var mPrefs: SharedPreferences.Editor = mContext.getSharedPreferences(DataNames.Pref_Cart_Quantity, Context.MODE_PRIVATE).edit().clear()
        mPrefs.apply()

        mPrefs = mContext.getSharedPreferences("netTotalLaundry", Context.MODE_PRIVATE).edit().clear()
        mPrefs.apply()


        val cartList: CartList? = mPreferenceHelper.getGsonValue(DataNames.CART, CartList::class.java)
        cartList?.cartInfos?.clear()
        mPreferenceHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))

        cancelClearCartService()
    }

    private fun cancelClearCartService() {

        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(FacebookSdk.getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                FacebookSdk.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager!!.cancel(pendingIntent)

    }

    fun addItem(cartInfo: CartInfo) {
        val cartList: CartList? = getCartList()

        if (checkProductAddon(cartInfo.add_ons ?: mutableListOf()).productId != 0) {
            val index = cartList?.cartInfos?.indexOfFirst { it.add_ons == cartInfo.add_ons }
                    ?: -1 // -1 if not found
            //if (index != null) {
            if (index >= 0) {
                cartList?.cartInfos?.set(index, cartInfo)
            }
            //  }
        } else {
            cartList?.cartInfos?.add(cartInfo)
        }

        mPreferenceHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))


        updateTotalPrice(cartList!!)
        updateProductQuant(cartInfo.productId.toString())

    }


    fun updateItem(productModel: ProductDataBean?) {
        val cartList: CartList? = getCartList()

        val index = cartList?.cartInfos?.indexOfFirst { it.productAddonId == productModel?.productAddonId } // -1 if not found
        if (index != null) {
            if (index >= 0) {

                val cartInfo = cartList.cartInfos?.elementAt(index)
                cartInfo?.quantity = productModel?.fixed_quantity ?: 0

                cartInfo?.add_ons?.mapIndexed { _, productAddon ->
                    productAddon?.quantity = productModel?.fixed_quantity ?: 1
                }

                cartInfo?.let { cartList.cartInfos?.set(index, it) }

                mPreferenceHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))

                updateProductQuant(cartInfo?.productId.toString())

                updateTotalPrice(cartList)
            }
        }
    }

    fun updateCartItem(cartItem: CartInfo, position: Int) {
        val cartList: CartList? = getCartList()
        cartList?.cartInfos?.set(position, cartItem)
        mPreferenceHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))

        updateProductQuant(cartItem.productId.toString())
        cartList?.let { updateTotalPrice(it) }
    }

    fun calculateCartTotal(): Double {
        val cartList: CartList? = getCartList()

        var total = 0.0

        cartList?.cartInfos?.forEachIndexed { index, cartInfo ->

            total += if (cartInfo.hourlyPrice.isNullOrEmpty()) {
                cartInfo.price.times(cartInfo.quantity)
            } else {
                //cartInfo.price
                cartInfo.price.times(cartInfo.quantity)
            }

        }
        return total
    }

    private fun updateTotalPrice(cartList: CartList) {

        var totalAmount = 0.0f

        cartList.cartInfos?.mapIndexed { _, cartItem ->
            totalAmount += cartItem.price.times(cartItem.quantity)
        }
        mPreferenceHelper.setkeyValue(PrefenceConstants.NET_TOTAL, totalAmount)
    }

    fun removeCartItem(productModel: ProductDataBean) {
        val cartList: CartList? = getCartList()

        val index = cartList?.cartInfos?.indexOfFirst { it.productAddonId == productModel.productAddonId }
                ?: -1
        if (index >= 0) {
            cartList?.cartInfos?.removeAt(index)
            removeProductQuant(productId = productModel.product_id.toString())
        }

        mPreferenceHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))

        updateTotalPrice(cartList!!)

    }


    fun getCartList(): CartList {
        return mPreferenceHelper.getGsonValue(DataNames.CART, CartList::class.java) ?: CartList()
    }


    private fun updateProductQuant(productId: String) {

        val cartList: CartList? = getCartList()
        val quantity = cartList?.cartInfos?.filter { productId.toInt() == it.productId }?.sumBy { it.quantity }
                ?: 0

        val editor = mContext.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0).edit()
        editor.putInt(productId, quantity)
        editor.apply()
    }

    private fun removeProductQuant(productId: String) {
        val editor = mContext.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0).edit()
        editor.remove(productId)
        editor.apply()
    }

    fun addProductDb(context: Context?, appType: Int, productModel: ProductDataBean?) {
        val cartInfo = CartInfo()
        cartInfo.quantity = productModel?.prod_quantity ?: 1
        cartInfo.productName = productModel?.name
        //  cartInfo.supplierAddress=productModel.suppl

        cartInfo.subCategoryName = productModel?.detailed_name ?: ""
        cartInfo.productId = productModel?.product_id ?: 0
        cartInfo.imagePath = productModel?.image_path.toString()
        cartInfo.price = productModel?.netPrice ?: 0.0f
        cartInfo.fixed_price = productModel?.fixed_price?.toFloatOrNull() ?: 0.0f
        cartInfo.supplierName = productModel?.supplier_name
        cartInfo.suplierBranchId = productModel?.supplier_branch_id ?: 0
        cartInfo.measuringUnit = productModel?.measuring_unit
        cartInfo.deliveryCharges = productModel?.delivery_charges ?: 0.0f
        cartInfo.supplierId = productModel?.supplier_id ?: 0
        cartInfo.urgent_type = productModel?.urgent_type ?: 0
        cartInfo.isUrgent = productModel?.can_urgent ?: 0
        cartInfo.isPaymentConfirm = productModel?.payment_after_confirmation ?: 0
        cartInfo.avgRating = productModel?.avg_rating ?: 0.0f

        if (productModel?.prod_variants?.isNotEmpty() == true) {
            cartInfo.varients = (productModel.prod_variants)?.toMutableList()
        }


        // cartInfo.setUrgentValue(productModel.getUrgent_value());
        cartInfo.categoryId = productModel?.category_id ?: 0
        cartInfo.isDiscount = productModel?.discount

        if (!productModel?.hourly_price.isNullOrEmpty()) {

            cartInfo.hourlyPrice = productModel?.hourly_price ?: emptyList()
            cartInfo.fixed_price = productModel?.hourly_price?.get(0)?.discount_price
        }


        if (appType == AppDataType.HomeServ.type) {
            val bookingFlowBean = mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

            cartInfo.isQuant = productModel?.is_quantity ?: 0
            //set manually service Type 0 for service 1 for product
            cartInfo.serviceType = productModel?.is_product ?: 0
            cartInfo.agentType = if (productModel?.is_agent == 1 && productModel.is_product == 0) 1 else 0
            cartInfo.agentList = productModel?.agent_list ?: 0
            //set service duration amount as per duration or interval
            //  if (productModel?.is_product == 0) {

            cartInfo.serviceDuration = productModel?.duration ?: 0
            cartInfo.serviceDurationSum = productModel?.duration?.times(productModel.prod_quantity
                    ?: 0) ?: 0
            // productModel.serviceDuration =productModel.duration ?: 0
            //}
            cartInfo.priceType = productModel?.price_type ?: 0
        } else {
            cartInfo.serviceType = 1
        }

        cartInfo.latitude = productModel?.latitude
        cartInfo.longitude = productModel?.longitude
        cartInfo.radius_price = productModel?.radius_price
        cartInfo.deliveryMax = productModel?.delivery_max_time ?: 0
        cartInfo.purchasedQuant = productModel?.purchased_quantity
        cartInfo.prodQuant = productModel?.quantity

        cartInfo.deliveryType = productModel?.self_pickup ?: 0

        cartInfo.handlingAdmin = productModel?.handling_admin ?: 0.0f
        cartInfo.handlingSupplier = productModel?.handling_supplier ?: 0.0f

        cartInfo.question_list = productModel?.selectQuestAns ?: mutableListOf()
        cartInfo.appType = productModel?.type

        cartInfo.handlingCharges = (productModel?.handling_admin?.plus(productModel.handling_supplier
                ?: 0.0f)) ?: 0.0f

        StaticFunction.addToCart(context, cartInfo)
    }


    fun getDayId(dayId: Int): String? {
        return when (dayId) {
            Calendar.SUNDAY -> "6"
            Calendar.MONDAY -> "0"
            Calendar.TUESDAY -> "1"
            Calendar.WEDNESDAY -> "2"
            Calendar.THURSDAY -> "3"
            Calendar.FRIDAY -> "4"
            Calendar.SATURDAY -> "5"
            else -> "-1"
        }
    }


    fun revealShow(dialogView: View?, fab: View, b: Boolean, popView: PopupWindow?) {
        val view = dialogView?.findViewById<ConstraintLayout>(R.id.dialog)

        val width = view?.width?.toDouble()
        val height = view?.height?.toDouble()

        val endRadius = hypot(width ?: 0.0, height ?: 0.0).toFloat()

        val cx = (fab.x + (fab.width / 2)).toInt()
        val cy = ((fab.y) + fab.height + 56).toInt()


        if (b) {
            val revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, endRadius)

            view?.visibility = View.VISIBLE
            revealAnimator.duration = 700
            revealAnimator.start()

        } else {

            val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, endRadius, 0f)


            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    popView?.dismiss()
                    view?.visibility = View.INVISIBLE
                }
            })
            anim.duration = 700
            anim.start()
        }

    }

    fun getCurrencySymbol(): String {

        val currency = mPreferenceHelper.getGsonValue(PrefenceConstants.CURRENCY_INF, Currency::class.java)

        return if (currency == null) {
            CURRENCY_SYMBOL
        } else {
            CURRENCY_SYMBOL =  "kr"
            currency.currency_symbol ?: ""
        }
    }

    fun checkLoginActivity(): Class<*>? {
        val settingFlow = mPreferenceHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        return if (settingFlow?.user_register_flow != null && settingFlow.user_register_flow == "1") {
            SigninActivity::class.java
        } else {
            LoginActivity::class.java
        }
    }

    fun checkLoginFlow(mContext: Context) {
        val settingFlow = mPreferenceHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        return if (settingFlow?.user_register_flow != null && settingFlow.user_register_flow == "1") {
            mContext.launchActivity<SigninActivity>()
        } else {
            mContext.launchActivity<LoginActivity>()
        }
    }


    fun loadAppConfig(appType: Int): AppConfiguration {
        val screenFlowBean = mPreferenceHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        val terminologyBean = mPreferenceHelper.getGsonValue(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)

        if (appType > 0) {
            screenFlowBean?.app_type = appType
        }

        val stringConfig = TextConfig(screenFlowBean?.app_type
                ?: 0, terminologyBean, mPreferenceHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING) as String)

        val appConfig = AppConfiguration

        appConfig.strings = stringConfig

        return appConfig
    }

    fun getLangCode(languageParam: String?): Int {
        val selectedLang = languageParam
                ?: mPreferenceHelper.getKeyValue(DataNames.SELECTED_LANGUAGE, PrefenceConstants.TYPE_STRING).toString()
        return if (selectedLang == ClikatConstants.ENGLISH_SHORT || selectedLang == ClikatConstants.ENGLISH_FULL) {
            ClikatConstants.LANGUAGE_ENGLISH
        } else {
            ClikatConstants.LANGUAGE_OTHER
        }
    }


    companion object {

        /*    public AppUtils(Context context) {
        this.mContext = context;
    }*/

        /**
         * Description : Check if user is online or not
         *
         * @return true if online else false
         */


        private val EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        val EMAIL_ADDRESS_PATTERN = Pattern.compile(
                EMAIL_PATTERN
        )
    }


}