package com.codebrew.clikat.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.activities.NoInternetActivity
import com.codebrew.clikat.app_utils.ClearCartBroadCastReceiver
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.OrderStatus
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.QuestionData
import com.codebrew.clikat.data.model.others.OrderEvent
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.eventBus.UpdateCartEvent
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.pending_orders.UpcomingOrdersFargment
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.splash.SplashActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.services.SchedulerReciever
import com.codebrew.clikat.utils.configurations.Configurations
import com.facebook.FacebookSdk
import com.google.gson.Gson
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import org.greenrobot.eventbus.EventBus
import java.net.URLConnection
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/*
 * Created by cbl45 on 7/5/16.
 */
object StaticFunction {

    private var cartlist: CartList? = null

    val accessToken: String
        get() {
            val signUp = Prefs.getPrefs().getObject(DataNames.USER_DATA, PojoSignUp::class.java)
            return if (signUp != null && signUp.data != null && signUp.data.access_token != null)
                signUp.data.access_token
            else
                ""
        }

    fun addToCart(context: Context?, cartInfo: CartInfo) {

        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
        // Prefs.with(context).save(DataNames.SUPPLIER_LOGO_CART, supplierImage)
        // Prefs.with(context).save(DataNames.SUPPLIER_LOGO_NAME_CART, "" + supplierName)
        if (cartlist == null) {
            cartlist = CartList()
        }

        val position = checkIfProductExist(cartInfo.productId)

        if (position != -1) {
            cartInfo.quantity = 1
            cartlist!!.cartInfos!![position] = cartInfo
        } else {
            cartlist!!.cartInfos!!.add(cartInfo)
        }

        saveCartQuantity(context, cartInfo.productId, cartInfo.quantity)
        Prefs.with(context).save(DataNames.CART, cartlist)
        /*    try {
            ((MainActivity)context).updateCartCount(isVisible, isDark);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        updateNetTotal(cartlist, context)
        // alarmFunction(context)

        startClearCartService(context!!)

    }


    private fun alarmFunction(context: Context?) {
        val rightNow = Calendar.getInstance()
        val myIntent = Intent(context, SchedulerReciever::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0)
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, rightNow.timeInMillis + 7200000, pendingIntent)
    }

    fun saveDeliveryAddress(address: AddressBean, context: Context?) {
        Prefs.with(context).save("deliveryAdddress", address)
    }

    fun getDeliveryAddress(context: Context?): Address? {
        return Prefs.with(context).getObject("deliveryAdddress", Address::class.java)
    }

    fun addToCartLaundry(context: Context?, cartInfo: CartInfo, supplierImage: String, supplierName: String) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_CART_LAUNDRY, supplierImage)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_NAME_CART_LAUNDRY, "" + supplierName)

        if (cartlist == null) {
            cartlist = CartList()
        }

        val position = checkIfProductExist(cartInfo.productId)

        if (position != -1) {
            cartInfo.quantity = 1
            cartlist!!.cartInfos!![position] = cartInfo
        } else {
            cartlist!!.cartInfos!!.add(cartInfo)
        }

        saveCartQuantityLaundry(context, cartInfo.productId, cartInfo.quantity)
        Prefs.with(context).save(DataNames.CART_LAUNDRY, cartlist)

        updateNetTotalLaundry(cartlist!!, context)
        (context as MainActivity).updateCartCount(true, true)
        // check if supplier changes, Clear previous cart
        //        if (checkIfSupplierChange(cartInfo, context)) {
        //            clearCartDialog(context,cartlist);
        //        }

    }

    fun saveCartQuantityLaundry(context: Context?, productId: Int, quantity: Int) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_laundry, 0)?.edit()
        editor?.putInt("" + productId, quantity)
        editor?.apply()
    }


    //
    fun addToCartSallonHome(context: Context?, cartInfo: CartInfo, isVisible: Boolean, isDark: Boolean,
                            supplierImage: String, supplierName: String) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_CART_SALLON_HOME, supplierImage)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_NAME_CART_SALLON_HOME, "" + supplierName)

        if (cartlist == null) {
            cartlist = CartList()
        }

        val position = checkIfProductExist(cartInfo.productId)

        if (position != -1) {
            cartInfo.quantity = 1
            cartlist!!.cartInfos!![position] = cartInfo
        } else {
            cartlist!!.cartInfos!!.add(cartInfo)
        }

        saveCartQuantitySalloonHome(context, cartInfo.productId, cartInfo.quantity)
        Prefs.with(context).save(DataNames.CART_SALOON_HOME, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)

        updateNetTotalSallonHome(cartlist!!, context)

        // check if supplier changes, Clear previous cart
        //        if (checkIfSupplierChange(cartInfo, context)) {
        //            clearCartDialog(context,cartlist);
        //        }

    }

    //
    fun addToCartSallonPlace(context: Context?, cartInfo: CartInfo, isVisible: Boolean, isDark: Boolean,
                             supplierImage: String, supplierName: String) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_CART_SALLON_PLACE, supplierImage)
        Prefs.with(context).save(DataNames.SUPPLIER_LOGO_NAME_CART_SALLON_PLACE, "" + supplierName)

        if (cartlist == null) {
            cartlist = CartList()
        }

        val position = checkIfProductExist(cartInfo.productId)

        if (position != -1) {
            cartInfo.quantity = 1
            cartlist!!.cartInfos!![position] = cartInfo
        } else {
            cartlist!!.cartInfos!!.add(cartInfo)
        }

        saveCartQuantitySallonPlace(context, cartInfo.productId, cartInfo.quantity)
        Prefs.with(context).save(DataNames.CART_SALOON_PLACE, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)

        updateNetTotal(cartlist, context)

        // check if supplier changes, Clear previous cart
        //        if (checkIfSupplierChange(cartInfo, context)) {
        //            clearCartDialog(context,cartlist);
        //        }

    }

    //
    fun updateCartLaundry(context: Context?, productId: Int, quantity: Int, b: Boolean, b1: Boolean, netPrice: Float) {
        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!![i].quantity = quantity
                cartlist!!.cartInfos!![i].price = netPrice
                saveCartQuantityLaundry(context, cartlist!!.cartInfos!![i].productId, cartlist!!.cartInfos!![i].quantity)
            }
        }
        Prefs.with(context).save(DataNames.CART_LAUNDRY, cartlist)
        updateNetTotalLaundry(cartlist!!, context)
        (context as MainActivity).updateCartCount(b, b1)
    }

    fun clearCartLaundry(context: Context?) {
        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART_LAUNDRY, cartlist)
        }
        removeAllCartLaundry(context)

    }

    fun clearCart(context: Context?) {
        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART, cartlist)
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART_SALOON_PLACE, cartlist)
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART_SALOON_HOME, cartlist)
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART_SALOON_PLACE, cartlist)
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)

        if (cartlist == null) {
            cartlist = CartList()
        } else {
            cartlist!!.cartInfos!!.clear()
            Prefs.with(context).save(DataNames.CART_LAUNDRY, cartlist)
        }
        Prefs.with(context).save(DataNames.SUPPLIERBRANCHID, "")
        Prefs.with(context).save(DataNames.CATEGORY_ID, "")
        removeAllCart(context)

        cancelClearCartService(context!!)

    }



    fun clearCartDialog(context: Context?, listener: DialogListener) {
        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        //        builder.setTitle(context.getString(R.string.update));
        builder.setMessage(context.getString(R.string.clearCart, Configurations.strings.supplier))
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->
            dialog.cancel()

            removeAllCart(context)
            clearCart(context)

            //(context as MainActivity).updateCartCount(isVisible, isDark)

            listener.onSucessListner()

        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->

            listener.onErrorListener()
            dialog.cancel()


        }
        builder.setCancelable(false)
        builder.show()
    }

    fun errorMsgDialog(context: Context?) {
        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        builder.setTitle("Error Message")
        builder.setMessage("This product is not available to buy")
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which -> dialog.cancel() }
        builder.setCancelable(false)
        builder.show()
    }


    fun clearCartMsgDialog(context: Context?, message: String, isVisible: Boolean, isDark: Boolean, listener: DialogListener) {

        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        //        builder.setTitle(context.getString(R.string.update));
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->
            dialog.cancel()

            removeAllCart(context)
            clearCart(context)


            if (context is MainActivity) {
                context.updateCartCount(isVisible, isDark)
            } else {
                EventBus.getDefault().post(UpdateCartEvent(isVisible, isDark))
            }

            listener.onSucessListner()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
            dialog.cancel()
            listener.onErrorListener()
        }
        builder.setCancelable(false)
        builder.show()
    }


    fun clearCartDialog(context: Context?, isVisible: Boolean, isDark: Boolean, onClickListener: DialogInterface.OnClickListener, message: String) {
        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        //        builder.setTitle(context.getString(R.string.update));
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->
            dialog.cancel()
            onClickListener.onClick(dialog, which)
            removeAllCart(context)
            clearCart(context)
            try {
                if (context is MainActivity) {
                    context.updateCartCount(isVisible, isDark)
                } else {
                    EventBus.getDefault().post(UpdateCartEvent(isVisible, isDark))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
        builder.setCancelable(false)
        builder.show()
    }

    fun clearCartDialogLaundry(context: Context?, isVisible: Boolean, isDark: Boolean, onClickListener: DialogInterface.OnClickListener, message: String) {
        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        //        builder.setTitle(context.getString(R.string.update));
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->
            dialog.cancel()
            removeAllCartLaundry(context)
            clearCartLaundry(context)
            try {
                (context as MainActivity).updateCartCount(isVisible, isDark)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            onClickListener.onClick(dialog, which)
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which -> dialog.cancel() }
        builder.setCancelable(false)
        builder.show()
    }

    fun clearCartDialog(context: Context?, isVisible: Boolean, isDark: Boolean, onClickListener: DialogInterface.OnClickListener, negOnClickListener: DialogInterface.OnClickListener, message: String) {
        val builder = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
        //        builder.setTitle(context.getString(R.string.update));
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->
            dialog.cancel()
            onClickListener.onClick(dialog, which)
            removeAllCart(context)
            clearCart(context)

            (context as MainActivity).updateCartCount(isVisible, isDark)
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
            dialog.cancel()
            negOnClickListener.onClick(dialog, which)
        }
        builder.setCancelable(false)
        builder.show()
    }

    fun updateNetTotal(cartList: CartList?, context: Context?) {
        if (cartList != null && cartList.cartInfos != null) {
            var netTotal = 0f
            for (i in 0 until cartList.cartInfos!!.size) {
                netTotal = netTotal + cartList.cartInfos!![i].price * cartList.cartInfos!![i].quantity
            }
            Prefs.with(context).save("netTotal", netTotal)
        }
      //  Prefs.with(context).remove(DataNames.DISCOUNT_AMOUNT)
    }

    fun updateNetTotalSallonPlace(cartList: CartList, context: Context?) {

        var netTotal = 0f
        for (i in 0 until cartList.cartInfos!!.size) {
            netTotal = netTotal + cartList.cartInfos!![i].price * cartList.cartInfos!![i].quantity
        }
        Prefs.with(context).save("netTotalSallonPlace", netTotal)
    }

    fun updateNetTotalSallonHome(cartList: CartList, context: Context?) {

        var netTotal = 0f
        for (i in 0 until cartList.cartInfos!!.size) {
            netTotal = netTotal + cartList.cartInfos!![i].price * cartList.cartInfos!![i].quantity
        }
        Prefs.with(context).save("netTotalSallonHome", netTotal)
    }

    fun updateNetTotalLaundry(cartList: CartList, context: Context?) {

        var netTotal = 0f
        for (i in 0 until cartList.cartInfos!!.size) {
            netTotal = netTotal + cartList.cartInfos!![i].price * cartList.cartInfos!![i].quantity
        }
        Prefs.with(context).save("netTotalLaundry", netTotal)
    }

    fun netTotal(context: Context?, flow: Int): Float {
        val netTotal: Float
        when (flow) {
            DataNames.FLOW_BEAUTY_SALOON -> netTotal = Prefs.with(context).getFloat("netTotalSallonHome", 0f)
            DataNames.FLOW_BEAUTY_SALOON_PLACE -> netTotal = Prefs.with(context).getFloat("netTotalSallonPlace", 0f)
            DataNames.FLOW_LAUNDRY -> netTotal = netTotalLaundry(context)
            else -> netTotal = Prefs.with(context).getFloat("netTotal", 0f)
        }

        return roundToDecimals(netTotal)

    }

    private fun roundToDecimals(`val`: Float): Float {
        try {
            val s = String.format("%.2f", `val`)
            return java.lang.Float.parseFloat(s)
        } catch (e: Exception) {
            e.printStackTrace()
            return `val`

        }

    }

    fun savenetTotal(context: Context?, flow: Int, netToatal: Float) {

        when (flow) {
            DataNames.FLOW_BEAUTY_SALOON -> {
                Prefs.with(context).save("netTotalSallonHome11", netToatal)
                Prefs.with(context).save("netTotalSallonPlace11", netToatal)
                Prefs.with(context).save("netTotalLaundry11", netToatal)
                Prefs.with(context).save("netTotal111", netToatal)
            }
            DataNames.FLOW_BEAUTY_SALOON_PLACE -> {
                Prefs.with(context).save("netTotalSallonPlace11", netToatal)
                Prefs.with(context).save("netTotalLaundry11", netToatal)
                Prefs.with(context).save("netTotal111", netToatal)
            }
            DataNames.FLOW_LAUNDRY -> {
                Prefs.with(context).save("netTotalLaundry11", netToatal)
                Prefs.with(context).save("netTotal111", netToatal)
            }
            else -> Prefs.with(context).save("netTotal111", netToatal)
        }


    }

    fun netTotalFinal(context: Context?, flow: Int): Float {
        val netTotal: Float
        when (flow) {
            DataNames.FLOW_BEAUTY_SALOON -> netTotal = Prefs.with(context).getFloat("netTotalSallonHome11", 0f)
            DataNames.FLOW_BEAUTY_SALOON_PLACE -> netTotal = Prefs.with(context).getFloat("netTotalSallonPlace11", 0f)
            DataNames.FLOW_LAUNDRY -> netTotal = Prefs.with(context).getFloat("netTotalLaundry11", 0f)
            else -> netTotal = Prefs.with(context).getFloat("netTotal111", 0f)
        }

        return roundToDecimals(netTotal)

    }

    fun netTotalLaundry(context: Context?): Float {

        return Prefs.with(context).getFloat("netTotalLaundry", 0f)

    }

    private fun checkIfProductExist(productId: Int): Int {
        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                return i
            }
        }
        return -1
    }


    fun removeFromCart(context: Context?, productId: Int?, addonProdId: Long) {

        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)

        val pos: Int = if (addonProdId > 0) {
            cartlist?.cartInfos?.indexOfFirst { it.productAddonId == addonProdId } ?: -1
        } else {
            // removeCart(context, productId)
            cartlist?.cartInfos?.indexOfFirst { it.productId == productId } ?: -1
        }

        if (cartlist?.cartInfos?.any { it.productId == productId } == true || cartlist == null) {
            removeCart(context, productId)
        }

        if (pos != -1) cartlist?.cartInfos?.removeAt(pos)

        /*if (cartlist!!.cartInfos!!.size == 0) {
            Prefs.with(context).save(DataNames.SUPPLIERBRANCHID, "")
            Prefs.with(context).save(DataNames.CATEGORY_ID, "")
        }*/

        if (cartlist != null) {
            Prefs.with(context).save(DataNames.CART, cartlist)
        }
        /*  if (context is MainActivity) {
              context.updateCartCount(isVisible, isDark)
          } else {
              EventBus.getDefault().post(UpdateCartEvent(isVisible, isDark))
          }*/

        updateNetTotal(cartlist, context)

        startClearCartService(context!!)
    }

    private fun startClearCartService(context: Context) {

        cancelClearCartService(context)

        val myIntent = Intent(FacebookSdk.getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                FacebookSdk.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)


        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager[AlarmManager.RTC, System.currentTimeMillis() + 7200000] = pendingIntent

    }

    private fun cancelClearCartService(context: Context) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(FacebookSdk.getApplicationContext(), ClearCartBroadCastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                FacebookSdk.getApplicationContext(), 1, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager!!.cancel(pendingIntent)

    }

    fun removeFromCartSaloonHome(context: Context?, productId: Int, isVisible: Boolean, isDark: Boolean) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)

        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!!.removeAt(i)
                removeCart(context, productId)
            }
        }

        if (cartlist!!.cartInfos!!.size == 0) {
            Prefs.with(context).save(DataNames.SUPPLIERBRANCHID, "")
            Prefs.with(context).save(DataNames.CATEGORY_ID, "")
        }
        Prefs.with(context).save(DataNames.CART_SALOON_HOME, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)
        updateNetTotalSallonHome(cartlist!!, context)
    }

    fun removeFromCartSallonPlace(context: Context?, productId: Int, isVisible: Boolean, isDark: Boolean) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)

        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!!.removeAt(i)
                removeCart(context, productId)
            }
        }

        if (cartlist!!.cartInfos!!.size == 0) {
            Prefs.with(context).save(DataNames.SUPPLIERBRANCHID, "")
            Prefs.with(context).save(DataNames.CATEGORY_ID, "")
        }
        Prefs.with(context).save(DataNames.CART_SALOON_PLACE, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)
        updateNetTotalSallonPlace(cartlist!!, context)
    }


    fun removeFromCartLaundry(context: Context?, productId: Int) {

        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)

        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!!.removeAt(i)
                removeCartLaundry(context, productId)
            }
        }

        if (cartlist!!.cartInfos!!.size == 0) {
            Prefs.with(context).save(DataNames.SUPPLIERBRANCHID, "")
            Prefs.with(context).save(DataNames.CATEGORY_ID, "")
        }
        Prefs.with(context).save(DataNames.CART_LAUNDRY, cartlist)
        updateNetTotalLaundry(cartlist!!, context)
        (context as MainActivity).updateCartCount(false, false)
    }

    fun updateCart(context: Context?, productId: Int?, quantity: Int?, netPrice: Float) {
        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!![i].quantity = quantity ?: 0
                cartlist!!.cartInfos!![i].price = netPrice
                saveCartQuantity(context, cartlist!!.cartInfos!![i].productId, cartlist!!.cartInfos!![i].quantity)
            }
        }


        Prefs.with(context).save(DataNames.CART, cartlist)

        /*  if (context is MainActivity) {
              context.updateCartCount(isVisible, isDark)
          } else {
              EventBus.getDefault().post(UpdateCartEvent(isVisible, isDark))
          }*/
        updateNetTotal(cartlist, context)
        alarmFunction(context)
    }

    fun checkDeliveryType(context: Context?, deliveryType: Int): Boolean {
        var status = true

        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
        for (i in 0 until cartlist!!.cartInfos!!.size) {

            if (deliveryType != cartlist!!.cartInfos!![i].deliveryType) {
                status = false
            }

        }
        return status
    }

    fun updateCartSallonHome(context: Context?, productId: Int, quantity: Int, isVisible: Boolean, isDark: Boolean, netPrice: Float) {
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)
        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!![i].quantity = quantity
                cartlist!!.cartInfos!![i].price = netPrice
                saveCartQuantitySalloonHome(context, cartlist!!.cartInfos!![i].productId, cartlist!!.cartInfos!![i].quantity)
            }
        }


        Prefs.with(context).save(DataNames.CART_SALOON_HOME, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)
        updateNetTotalSallonHome(cartlist!!, context)
    }

    fun updateCartSallonPlace(context: Context?, productId: Int, quantity: Int, isVisible: Boolean, isDark: Boolean, netPrice: Float) {
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)
        for (i in 0 until cartlist!!.cartInfos!!.size) {
            if (productId == cartlist!!.cartInfos!![i].productId) {
                cartlist!!.cartInfos!![i].quantity = quantity
                cartlist!!.cartInfos!![i].price = netPrice
                saveCartQuantitySallonPlace(context, cartlist!!.cartInfos!![i].productId, cartlist!!.cartInfos!![i].quantity)
            }
        }


        Prefs.with(context).save(DataNames.CART_SALOON_PLACE, cartlist)
        (context as MainActivity).updateCartCount(isVisible, isDark)
        updateNetTotalSallonPlace(cartlist!!, context)
    }

    fun allCart(context: Context?, flow: Int): CartList {
        val suuplierImage: String
        val supplierName: String
        when (flow) {
            DataNames.FLOW_BEAUTY_SALOON -> {
                cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)
                supplierName = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_NAME_CART_SALLON_HOME, "")
                suuplierImage = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_CART_SALLON_HOME, "")
            }
            DataNames.FLOW_BEAUTY_SALOON_PLACE -> {
                cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)
                supplierName = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_NAME_CART_SALLON_PLACE, "")
                suuplierImage = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_CART_SALLON_PLACE, "")
            }
            DataNames.FLOW_LAUNDRY -> {
                cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
                supplierName = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_NAME_CART_LAUNDRY, "")
                suuplierImage = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_CART_LAUNDRY, "")
            }
            else -> {
                cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
                supplierName = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_NAME_CART, "")
                suuplierImage = Prefs.with(context).getString(DataNames.SUPPLIER_LOGO_CART, "")
            }
        }

        if (cartlist == null)
            cartlist = CartList()

        cartlist!!.supplierImage = suuplierImage
        cartlist!!.supplierName = supplierName
        if (cartlist!!.cartInfos!!.size != 0) {
            cartlist!!.supplierBranchId = cartlist!!.cartInfos!![0].suplierBranchId.toString()
            cartlist!!.supplierId = cartlist!!.cartInfos!![0].supplierId.toString()
        }
        return cartlist!!
    }

    fun allCartlAUNDRY(context: Context?): CartList {

        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
        if (cartlist == null)
            cartlist = CartList()
        return cartlist!!
    }

    fun cartCount(context: Context?, flow: Int): Int {

        when (flow) {
            DataNames.FLOW_BEAUTY_SALOON -> cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)
            DataNames.FLOW_BEAUTY_SALOON_PLACE -> cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)
            DataNames.FLOW_LAUNDRY -> cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
            else -> cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
        }
        var size = 0
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                size = size + cartlist!!.cartInfos!![i].quantity
            }
        }
        return size
    }

    fun cartCount(context: Context?): Int {

        cartlist = CartList()
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_HOME, CartList::class.java)
        var size = 0
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                size = size + cartlist!!.cartInfos!![i].quantity
            }
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART_SALOON_PLACE, CartList::class.java)
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                size = size + cartlist!!.cartInfos!![i].quantity
            }
        }
        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                size = size + cartlist!!.cartInfos!![i].quantity
            }
        }

        return size
    }

    fun cartCountLaundry(context: Context?): Int {

        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
        var size = 0
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                size = size + cartlist!!.cartInfos!![i].quantity
            }
        }
        return size
    }

    fun covertCartToArray(context: Context?): List<CartInfoServer> {

        cartlist = Prefs.with(context).getObject(DataNames.CART, CartList::class.java)

        val listCartInfoServers = ArrayList<CartInfoServer>()
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                val cartInfoServer = CartInfoServer()
                Prefs.with(context).save(DataNames.FLOW_PA, cartlist!!.cartInfos!![0].packageType)

                if (cartlist?.cartInfos?.get(i)?.hourlyPrice.isNullOrEmpty()) {
                    cartInfoServer.quantity = cartlist!!.cartInfos!![i].quantity
                } else {
                    cartInfoServer.quantity = 1
                }

                cartInfoServer.pricetype = cartlist!!.cartInfos!![i].priceType
                cartInfoServer.productId = cartlist!!.cartInfos!![i].productId.toString()
                cartInfoServer.category_id = cartlist!!.cartInfos!![i].categoryId
                cartInfoServer.handlingAdmin = cartlist!!.cartInfos!![i].handlingAdmin
                cartInfoServer.supplier_branch_id = cartlist!!.cartInfos!![i].suplierBranchId
                cartInfoServer.handlingSupplier = cartlist!!.cartInfos!![i].handlingSupplier
                cartInfoServer.supplier_id = cartlist!!.cartInfos!![i].supplierId
                cartInfoServer.agent_type = cartlist!!.cartInfos!![i].agentType
                cartInfoServer.agent_list = cartlist!!.cartInfos!![i].agentList
                cartInfoServer.deliveryType = cartlist!!.cartInfos!![i].deliveryType
                cartInfoServer.fixed_price = cartlist!!.cartInfos!![i].fixed_price
                cartInfoServer.name = cartlist!!.cartInfos!![i].productName
                cartInfoServer.subCatName = cartlist!!.cartInfos!![i].subCategoryName
                cartInfoServer.deliveryCharges = cartlist!!.cartInfos!![i].deliveryCharges
                cartInfoServer.add_ons = cartlist?.cartInfos?.get(i)?.add_ons
                cartInfoServer.price = cartlist!!.cartInfos!![i].price
                cartInfoServer.variants = cartlist?.cartInfos?.get(i)?.varients
                cartInfoServer.question_list = cartlist?.cartInfos?.get(i)?.question_list
                cartInfoServer.isPaymentConfirm = cartlist?.cartInfos?.get(i)?.isPaymentConfirm
                cartInfoServer.appType = cartlist?.cartInfos?.get(i)?.appType
                listCartInfoServers.add(cartInfoServer)
            }
        }
        return listCartInfoServers
    }

    fun covertCartToArrayLaundry(context: Context?): List<CartInfoServer> {

        cartlist = Prefs.with(context).getObject(DataNames.CART_LAUNDRY, CartList::class.java)
        val listCartInfoServers = ArrayList<CartInfoServer>()
        if (cartlist != null) {
            for (i in 0 until cartlist!!.cartInfos!!.size) {
                val cartInfoServer = CartInfoServer()
                cartInfoServer.quantity = cartlist!!.cartInfos!![i].quantity
                cartInfoServer.pricetype = cartlist!!.cartInfos!![i].priceType
                cartInfoServer.productId = cartlist!!.cartInfos!![i].productId.toString()
                cartInfoServer.handlingAdmin = cartlist!!.cartInfos!![i].handlingAdmin
                cartInfoServer.supplier_branch_id = cartlist!!.cartInfos!![i].suplierBranchId
                cartInfoServer.handlingSupplier = cartlist!!.cartInfos!![i].handlingSupplier
                cartInfoServer.supplier_id = cartlist!!.cartInfos!![i].supplierId

                listCartInfoServers.add(cartInfoServer)
            }
        }
        return listCartInfoServers
    }

    fun saveCartQuantity(context: Context?, productId: Int?, quantity: Int) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0)?.edit()
        editor?.putInt("" + productId, quantity)
        editor?.apply()
    }

    fun getCartQuantity(context: Context?, productId: Int?): Int {
        val prefs = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0)
        return prefs?.getInt("" + productId, 0) ?: 0
    }

    fun getCartQuantityLaundry(context: Context?, productId: Int): Int {
        val prefs = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_laundry, 0)
        return prefs?.getInt("" + productId, 0) ?: 0
    }


    fun saveCartQuantitySalloonHome(context: Context?, productId: Int, quantity: Int) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Home, 0)?.edit()
        editor?.putInt("" + productId, quantity)
        editor?.apply()
    }

    fun getCartQuantitySallonHome(context: Context?, productId: Int): Int {
        val prefs = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Home, 0)
        return prefs?.getInt("" + productId, 0) ?: 0
    }

    fun saveCartQuantitySallonPlace(context: Context?, productId: Int, quantity: Int) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Place, 0)?.edit()
        editor?.putInt("" + productId, quantity)
        editor?.apply()
    }

    fun getCartQuantitySallonPlace(context: Context?, productId: Int): Int {
        val prefs = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Place, 0)
        return prefs?.getInt("" + productId, 0) ?: 0
    }


    fun saveCart(activity: Context?) {
        Prefs.with(activity).save(DataNames.CART, cartlist)
    }

    fun removeCartLaundry(context: Context?, productId: Int) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_laundry, 0)?.edit()
        editor?.remove("" + productId)
        editor?.apply()
    }

    fun removeCart(context: Context?, productId: Int?) {
        val editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0)?.edit()
        editor?.remove("" + productId)
        editor?.apply()
    }

    fun removeAllCartLaundry(context: Context?) {
        var editor: SharedPreferences.Editor? = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_laundry, 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences("netTotalLaundry", 0)?.edit()
        editor?.clear()
        editor?.apply()

    }

    fun removeAllCart(context: Context?) {
        var editor: SharedPreferences.Editor? = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity, 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Home, 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_Sallon_Place, 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences(DataNames.Pref_Cart_Quantity_laundry, 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences("netTotalLaundry", 0)?.edit()
        editor?.clear()
        editor?.apply()
        editor = context?.getSharedPreferences(DataNames.CATEGORY_ID_TEST, 0)?.edit()
        editor?.clear()
        editor?.apply()

    }

    fun getCurrency(context: Context?): String {
        return Prefs.with(context).getString(DataNames.CURRENCY, "kr")
    }

    fun getCurrencyId(context: Context?): String {
        return Prefs.with(context).getString(DataNames.CURRENCY_ID, "")
    }

    fun dialogue(context: Context?, message: String,
                 title: String, negativeButton: Boolean, dialogIntrface: DialogIntrface) {
        val builder = AlertDialog.Builder(context!!)
        if (title.isEmpty()) {
            builder.setTitle(context.getString(R.string.update))
        } else
            builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.Ok)) { dialog, which ->

            dialogIntrface.onSuccessListener()

            dialog.cancel()


            /*  if (flag == 0) {
                ((MainActivity)context).finish();
            } else if (flag == 3) {
                ((MainActivity)context).homeTab();
                StaticFunction.removeAllCart(context);
                StaticFunction.clearCart(context);
                StaticFunction.saveCart(context);
            } else if (flag == 307) {


                SettingModel.DataBean.ScreenFlowBean screenFlowBean = Prefs.with(context).getObject(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean.class);

                if (screenFlowBean.getType() == 1 || screenFlowBean.getType() == 3 || screenFlowBean.getType() == 4|| screenFlowBean.getType() == 6) {
                    SupplierDetailFragment restaurantDetail = (SupplierDetailFragment)((MainActivity)context).getSupportFragmentManager().findFragmentByTag("supplierDetail");
                    restaurantDetail.makeOrder();
                } else {
           *//*         SupplierDetails supplierDetail = (SupplierDetails)((MainActivity)context).getSupportFragmentManager().findFragmentByTag("supplierDetail");
                    supplierDetail.makeOrder();*//*
                }


            }*/

        }

        if (negativeButton) {
            builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                dialogIntrface.onErrorListener()
                dialog.cancel()
            }
        }
        builder.setCancelable(false)
        builder.show()


    }


    fun getLanguage(context: Context?): Int {

        val selectedLang = Prefs.with(context).getString(DataNames.SELECTED_LANGUAGE, ClikatConstants.ENGLISH_FULL)

        return when (selectedLang) {
            ClikatConstants.ENGLISH_FULL, ClikatConstants.ENGLISH_SHORT -> ClikatConstants.LANGUAGE_ENGLISH
            else -> ClikatConstants.LANGUAGE_OTHER
        }
    }


    fun getAccesstoken(context: Context?): String {
        val signUp = Prefs.with(context).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        return if (signUp?.data != null && signUp.data.access_token != null)
            signUp.data.access_token
        else
            ""
    }


    fun isLoginProperly(activity: Context?): PojoSignUp {
        var pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)

        if (pojoSignUp?.data != null &&
                pojoSignUp.data.otp_verified != null && pojoSignUp.data.otp_verified == 1) {
            Log.d("", "")
        } else {
            pojoSignUp = PojoSignUp()
        }
        Prefs.with(activity).save(DataNames.USER_DATA, pojoSignUp)

        return pojoSignUp
    }

    fun isInternetConnected(context: Context?): Boolean {
        val detector = ConnectionDetector(context)
        return detector.isConnectingToInternet
    }

    fun showNoInternetDialog(context: Context?) {
        context?.startActivity(Intent(context, NoInternetActivity::class.java))
    }

    fun saveLoyalityCart(context: Context?, productLoyalityPoints: CartLoyalityPoints) {
        Prefs.with(context).save("LoyalityCart", productLoyalityPoints)
    }

    fun getLoyalityCart(context: Context?): CartLoyalityPoints? {
        return Prefs.with(context).getObject("LoyalityCart", CartLoyalityPoints::class.java)
    }


    fun colorStatusProduct(status: Int?): Int {

        when (status) {
            2, 8 -> return R.color.red
            0 -> return R.color.brown
            3, 4, 7, 11 -> return R.color.yellow
            5, 9, 10, 6, 1 -> return R.color.light_green
            else -> return R.color.light_green
        }

    }

    fun colorStatusProduct(tvOrder: TextView?, status: Double?, mContext: Context?, isRec: Boolean) {

        when (status) {

            OrderStatus.Pending.orderStatus, OrderStatus.Rejected.orderStatus, OrderStatus.Rating_Given.orderStatus, OrderStatus.Customer_Canceled.orderStatus -> {
                Log.e("API ERROr in status", "API error in status")
                if (isRec)
                    tvOrder?.setBackgroundResource(R.drawable.red_rec)
                tvOrder?.setTextColor(ContextCompat.getColor(mContext!!, R.color.red))
            }
            OrderStatus.Approved.orderStatus -> {
                if (isRec)
                    tvOrder?.setBackgroundResource(R.drawable.brown_rec)
                tvOrder?.setTextColor(ContextCompat.getColor(mContext!!, R.color.brown))
            }

            OrderStatus.On_The_Way.orderStatus, OrderStatus.Near_You.orderStatus, OrderStatus.Track.orderStatus, OrderStatus.Packed.orderStatus -> {
                if (isRec)
                    tvOrder?.setBackgroundResource(R.drawable.yellow_rec)
                tvOrder?.setTextColor(ContextCompat.getColor(mContext!!, R.color.yellow))
            }


            OrderStatus.Reached.orderStatus, OrderStatus.Ended.orderStatus, OrderStatus.Scheduled.orderStatus -> {
                if (isRec)
                    tvOrder?.setBackgroundResource(R.drawable.green_rec)
                tvOrder?.setTextColor(ContextCompat.getColor(mContext!!, R.color.light_green))
            }
        }

    }


    fun statusProduct(status: Double?, appType: Int, selfPickup: Int?, context: Context?, orderTerminology: String): String? {

        val terminologyBean = if (orderTerminology.isNotEmpty()) {
            Gson().fromJson(orderTerminology, SettingModel.DataBean.Terminology::class.java)
        } else {
            Prefs.getPrefs().getObject(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)
        }

        val languageId = Prefs.getPrefs().getString(DataNames.SELECTED_LANGUAGE, ClikatConstants.ENGLISH_FULL)

        var appTerminology: SettingModel.DataBean.AppTerminology? = null
        if (terminologyBean != null) {
            appTerminology = if (languageId == ClikatConstants.ENGLISH_FULL || languageId == ClikatConstants.ENGLISH_SHORT) {
                terminologyBean.english
            } else {
                terminologyBean.other
            }
        }

        when (status) {
            OrderStatus.Pending.orderStatus -> return if (appTerminology != null && appTerminology.status.PENDING?.isNotEmpty() == true)
                appTerminology.status.PENDING else when (appType) {
                AppDataType.Food.type -> context?.resources?.getString(R.string.placed)
                else -> context?.resources?.getString(R.string.pending)
            }
            OrderStatus.Approved.orderStatus, OrderStatus.Confirmed.orderStatus -> return if (appTerminology != null && appTerminology.status.ACCEPTED?.isNotEmpty() == true)
                appTerminology.status.ACCEPTED else when (appType) {
                AppDataType.Ecom.type -> context?.resources?.getString(R.string.approved)
                else -> context?.resources?.getString(R.string.confirmed)
            }

            OrderStatus.Rejected.orderStatus -> return if (appTerminology != null && appTerminology.status.REJECTED?.isNotEmpty() == true)
                appTerminology.status.REJECTED else context?.resources?.getString(R.string.reject)

            OrderStatus.On_The_Way.orderStatus, OrderStatus.Started.orderStatus -> {
                return if (appTerminology != null && appTerminology.status.ON_THE_WAY?.isNotEmpty() == true)
                    appTerminology.status.ON_THE_WAY else when (appType) {
                    AppDataType.HomeServ.type -> context?.resources?.getString(R.string.started)
                    AppDataType.Ecom.type -> context?.resources?.getString(R.string.out_delivery)
                    else -> context?.resources?.getString(R.string.on_the_way)
                }
            }

            OrderStatus.Near_You.orderStatus -> return if (appTerminology != null && appTerminology.status.NEAR_YOU?.isNotEmpty() == true)
                appTerminology.status.NEAR_YOU else context?.resources?.getString(R.string.near_you)

            OrderStatus.Ended.orderStatus, OrderStatus.Delivered.orderStatus -> {
                return if (appTerminology != null && appTerminology.status.DELIVERED?.isNotEmpty() == true)
                    appTerminology.status.DELIVERED else when (appType) {
                    AppDataType.HomeServ.type -> context?.resources?.getString(R.string.ended)
                    AppDataType.Food.type -> if (selfPickup == FoodAppType.Pickup.foodType) context?.resources?.getString(R.string.picked_up)
                    else context?.resources?.getString(R.string.delivered)
                    else -> context?.resources?.getString(R.string.delivered)
                }
            }


            OrderStatus.Reached.orderStatus, OrderStatus.Shipped.orderStatus, OrderStatus.Ready_to_be_picked.orderStatus -> {
                return if (appTerminology != null && appTerminology.status.SHIPPED?.isNotEmpty() == true)
                    appTerminology.status.SHIPPED else when (appType) {
                    AppDataType.HomeServ.type -> context?.resources?.getString(R.string.reached)
                    AppDataType.Food.type -> context?.resources?.getString(R.string.ready_to_pick)
                    else -> context?.resources?.getString(R.string.shipped)
                }
            }


            OrderStatus.Rating_Given.orderStatus -> return if (appTerminology != null && appTerminology.status.RATE_GIVEN?.isNotEmpty() == true)
                appTerminology.status.RATE_GIVEN else context?.resources?.getString(R.string.rating_given)

            OrderStatus.Customer_Canceled.orderStatus -> return if (appTerminology != null && appTerminology.status.CUSTOMER_CANCEL?.isNotEmpty() == true)
                appTerminology.status.CUSTOMER_CANCEL else context?.resources?.getString(R.string.customer_cancelled)


            OrderStatus.Packed.orderStatus, OrderStatus.In_Kitchen.orderStatus -> {
                return if (appTerminology != null && appTerminology.status.PACKED?.isNotEmpty() == true)
                    appTerminology.status.PACKED else when (appType) {
                    AppDataType.Ecom.type -> context?.resources?.getString(R.string.packed)
                    AppDataType.Food.type -> context?.resources?.getString(R.string.in_kitchen)
                    else -> context?.resources?.getString(R.string.on_the_way)
                }
            }
        }

        return ""
    }

    fun sweetDialogueSuccess11(context: Context?, title: String, message: String,
                               negativeButton: Boolean, flag: Int, clickListener: DialogIntrface) {


        val sweetAlertDialog = AlertDialog.Builder(context!!)
        sweetAlertDialog.setTitle(title)
        sweetAlertDialog.setMessage(message)

        if (negativeButton) {
            sweetAlertDialog.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
        }
        sweetAlertDialog.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->

            when (flag) {
                202 -> {
                    (context as Activity).finish()
                    val clearPreviousData = Intent(context, MainActivity::class.java)
                    clearPreviousData.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    clearPreviousData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    clearPreviousData.putExtra("intent", 11)
                    context.startActivity(clearPreviousData)
                    context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
                501 -> {
                    removeAllCart(context)
                    Prefs.with(context).remove(DataNames.USER_DATA)
                    (context as MainActivity).initialize()
                    context.homeTab()
                }
                101 -> {
                    val upcomingOrdersFargment = (context as MainActivity).supportFragmentManager
                            .findFragmentByTag("order") as UpcomingOrdersFargment?
                    upcomingOrdersFargment!!.apiCancelOrder()
                }
                1001 -> {

                    clickListener.onSuccessListener()
                }
            }

            dialog.dismiss()
        }
        sweetAlertDialog.setOnDismissListener { clearCart(context) }
        sweetAlertDialog.show()

    }

    fun sweetDialogueSuccess(context: Context?, title: String, message: String,
                             negativeButton: Boolean, flag: Int, history2: OrderHistory2?, cartIds: ArrayList<Int>?) {

        val sweetAlertDialog = AlertDialog.Builder(context!!)
        sweetAlertDialog.setTitle(title)
        sweetAlertDialog.setMessage(message)
        if (negativeButton) {
            sweetAlertDialog.setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
        }

        sweetAlertDialog.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            dialog.dismiss()

            when (flag) {
                202 -> {
                    (context as AppCompatActivity).finish()
                    val clearPreviousData = Intent(context, MainActivity::class.java)
                    clearPreviousData.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    clearPreviousData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    clearPreviousData.putExtra("intent", 11)
                    context.startActivity(clearPreviousData)
                    context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
                501 -> {
                    removeAllCart(context)
                    Prefs.with(context).removeAll()
                    context.startActivity(Intent(context, SplashActivity::class.java))
                    (context as Activity).finishAffinity()
                    // ((MainActivity)context).initialize();
                    // ((MainActivity)context).onBackPressed();

                }
                101 -> {

                }
                1001 -> {
                    (context as AppCompatActivity).finish()
                    context.finish()
                    val clearPreviousData = Intent(context, MainActivity::class.java)
                    clearPreviousData.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    clearPreviousData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    if (cartIds != null) {
                        clearPreviousData.putIntegerArrayListExtra("orderId", cartIds)
                    }
                    clearPreviousData.putExtra("intent", 11)
                    context.startActivity(clearPreviousData)
                }
            }

        }
        sweetAlertDialog.show()

    }

    fun sweetDialogue(context: Context?, title: String) {
        val sweetAlertDialog = AlertDialog.Builder(context!!)
       // sweetAlertDialog.setTitle(title)
        sweetAlertDialog.setMessage("We're sorry! "+title+" is currently out of stock. Please remove it from your cart to continue.")
        sweetAlertDialog.setNegativeButton(context.getString(R.string.cancel))
        { dialog, which ->
            dialog.dismiss()
        }

        sweetAlertDialog.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            //dialogIntrface.onSuccessListener()
            dialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    fun sweetDialogueFailure(context: Context?, title: String, message: String,
                             negativeButton: Boolean, flag: Int, upcomingFragment: Boolean) {

        val sweetAlertDialog = AlertDialog.Builder(context!!)
        sweetAlertDialog.setTitle(title)
        sweetAlertDialog.setMessage(message)

        if (negativeButton) {

            if (upcomingFragment)
                sweetAlertDialog.setNegativeButton(context.getString(R.string.no), null)
            else
                sweetAlertDialog.setNegativeButton(context.getString(R.string.cancel), null)


        }

        if (upcomingFragment) {
            sweetAlertDialog.setPositiveButton(context.getString(R.string.yes)) { dialog, which ->
                if (flag == 500) {
                    sweetDialogueSuccess(context, context.getString(R.string.success), context.getString(R.string.success_logout), false, 501, null, null)
                } else if (flag == 101) {
                    EventBus.getDefault().post(OrderEvent(AppConstants.CANCEL_EVENT))
                }
                dialog.dismiss()
            }

        } else {

            sweetAlertDialog.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
                if (flag == 500) {
                    sweetDialogueSuccess(context, context.getString(R.string.success), context.getString(R.string.success_logout), false, 501, null, null)
                } else if (flag == 101) {
                    EventBus.getDefault().post(OrderEvent(AppConstants.CANCEL_EVENT))
                }
                dialog.dismiss()
            }

        }


        sweetAlertDialog.show()
    }

    fun pxToDp(px: Float, context: Context?): Float {
        return px / ((context?.resources?.displayMetrics?.densityDpi?.toFloat()
                ?: 0f) / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun dpFromPx(px: Int, context: Context?): Int {
        return (px / (context?.resources?.displayMetrics?.density ?: 0f)).toInt()
    }

    fun pxFromDp(dp: Int, context: Context?): Int {
        return (dp * (context?.resources?.displayMetrics?.density ?: 0f)).toInt()
    }

    fun isValidColorHex(color: String?): Boolean {
        if (color == null) return false
        val colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
        val m = colorPattern.matcher(color.toLowerCase())
        return m.matches()
    }

    fun getPrimaryColor(context: Context?): Int {
        val color = Configurations.colors.primaryColor
        val defaultColor = ContextCompat.getColor(context!!, R.color.brown)
        if (color == null) return defaultColor
        try {
            return Color.parseColor(color)
        } catch (e: Exception) {
            return defaultColor
        }

    }

    fun getTintDrawable(context: Context, drawableId: Int): Drawable? {
        try {
            val drawable = ContextCompat.getDrawable(context, drawableId)
            DrawableCompat.setTint(drawable!!, Color.parseColor(Configurations.colors.primaryColor))
            return drawable
        } catch (e: Exception) {
            return ContextCompat.getDrawable(context, drawableId)
        }

    }


    fun setStatusBarColor(activity: AppCompatActivity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            if (color == Color.BLACK && window.navigationBarColor == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            window.statusBarColor = color
        }
    }

    fun changeStrokeColor(color: String): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = GradientDrawable.RECTANGLE
        gradient.setStroke(1, Color.parseColor(color))
        gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        return gradient
    }


    fun changeBorderColor(color: String, strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape
        if (strokeColor.isEmpty()) {
            gradient.setColor(Color.parseColor(color))
            gradient.setStroke(3, Color.parseColor(color))
        } else
            gradient.setStroke(2, Color.parseColor(strokeColor))


        if (shape == GradientDrawable.RADIAL_GRADIENT) {
            gradient.cornerRadius = 20f
            // gradient.setCornerRadii(new float[]{3, 3, 3, 3, 0, 0, 0, 0});
        } else {
            gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        }
        return gradient
    }


    fun varientColor(color: String, strokeColor: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape

        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(3, Color.parseColor(strokeColor))

        if (shape == GradientDrawable.RADIAL_GRADIENT) {
            gradient.cornerRadius = 20f
        } else {
            gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        }


        return gradient
    }


    fun changeBorderTextColor(color: String, shape: Int): GradientDrawable {
        val gradient = GradientDrawable()
        gradient.shape = shape

        gradient.setColor(Color.parseColor(color))
        gradient.setStroke(3, Color.parseColor(color))

        gradient.cornerRadii = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)

        return gradient
    }


    fun changeBadgeColor(fillcolor: Int): GradientDrawable {
        val strokeWidth = 1
        /*        int strokeColor = Color.parseColor("#03dc13");
        int fillColor = Color.parseColor("#ff0000");*/
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(fillcolor)
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setStroke(strokeWidth, fillcolor)
        return gradientDrawable
    }

    fun changeLocationStroke(color: String, type: String): GradientDrawable {

        val strokeWidth: Int
        if (type == "rate")
            strokeWidth = 30
        else
            strokeWidth = 20

        val gradient = GradientDrawable()
        gradient.shape = GradientDrawable.RECTANGLE
        gradient.cornerRadius = strokeWidth.toFloat()
        gradient.setColor(Color.parseColor(color))

        return gradient
    }


    @SuppressLint("WrongConstant")
    fun changeCategoryColor(): GradientDrawable {
        val ButtonColors = intArrayOf(Color.parseColor("#00FFFFFF"), Color.parseColor("#A8000000"))

        val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, ButtonColors)
        gradientDrawable.shape = GradientDrawable.LINEAR_GRADIENT
        gradientDrawable.cornerRadius = 10f

        return gradientDrawable
    }


    fun changeGradientColor(): GradientDrawable {
        val ButtonColors = intArrayOf(Color.parseColor("#1A000000"), Color.parseColor("#1A000000"))

        return GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, ButtonColors)
    }

    fun getCartId(cartIds: List<Int>): String {
        return Gson().toJson(cartIds)
    }


    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun convertDateOneToAnother(dateToConvert: String, toDate: String, fromDate: String): String {
        var outputDateStr = ""
        val inputFormat = SimpleDateFormat(toDate, Locale.getDefault())
        val outputFormat = SimpleDateFormat(fromDate, Locale.getDefault())
        val date: Date?
        try {
            date = inputFormat.parse(dateToConvert)
            outputDateStr = outputFormat.format(date!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return outputDateStr
    }


    fun addQuestion(context: Context?, questions: QuestionData?) {
        Prefs.with(context).save(DataNames.QUESTION_DATA, questions)
    }


    fun priceFormatter(price: String): String {
        val formatter = DecimalFormat("#,###.00")
        return formatter.format(java.lang.Double.valueOf(price))
    }

    fun isVideoFile(path: String): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }

    fun loadImage(url: String?, imageView: ImageView, roundedShape: Boolean) {
        var thumbUrl = ""

        if (url != null) {
            thumbUrl = url.substring(0, url.lastIndexOf("/") + 1) + "thumb_" + url.substring(url.lastIndexOf("/") + 1)
        }

        val glide = Glide.with(imageView.context)


        val requestOptions = RequestOptions
                .bitmapTransform(RoundedCornersTransformation(8, 0,
                        RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.iv_placeholder)
                .error(R.drawable.iv_placeholder)

        // Log.e("imgaeUrl",imageUrl)


        glide.load(url)
                .thumbnail(Glide.with(imageView.context).load(thumbUrl))
                .apply(requestOptions).into(imageView)

    }


    fun loadUserImage(url: String?, imageView: ImageView, roundedShape: Boolean) {
        var thumbUrl = ""

        if (url != null) {
            thumbUrl = url.substring(0, url.lastIndexOf("/") + 1) + "thumb_" + url.substring(url.lastIndexOf("/") + 1)
        }

        val glide = Glide.with(imageView.context)


        val requestOptions = RequestOptions
                .bitmapTransform(RoundedCornersTransformation(8, 0,
                        RoundedCornersTransformation.CornerType.ALL))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)


        glide.load(url)
                .thumbnail(Glide.with(imageView.context).load(thumbUrl))
                .apply(requestOptions).into(imageView)

    }


    fun openCustomChrome(activity: Context, url: String) {
        try {
            var uri = url
            if (!uri.startsWith("https://") && !uri.startsWith("http://")) {
                uri = "http://$uri"
            }

            val builder = CustomTabsIntent.Builder().build()
            builder.launchUrl(activity, Uri.parse(uri))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }


    fun getStatus(context: Context?, status: String): String? {
        return when (status) {
            "awaiting_shipment" -> context?.getString(R.string.awaiting_payment)
            "cancelled" -> context?.getString(R.string.cancelled)
            "shipped" -> context?.getString(R.string.shipped)
            "onhold" -> context?.getString(R.string.onhold)
            else -> status
        }
    }

    fun convertFromUtcFormat(dateString: String?): Long {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            dateFormat.timeZone = TimeZone.getDefault()
            val date = dateFormat.parse(dateString ?: "")
            return date?.time ?: 0L

        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0L
    }

    fun convertFromUtcFormat(format: String, time: String): String {
        return if (time != "") {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val mDate = sdf.parse(time) ?: Date()
            val cal = Calendar.getInstance()
            if (cal.timeZone.inDaylightTime(mDate)) {
                cal.timeInMillis = mDate.time + cal.timeZone.rawOffset + cal.timeZone.dstSavings
            } else
                cal.timeInMillis = mDate.time + cal.timeZone.rawOffset
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            dateFormat.format(cal.time)
        } else {
            ""
        }
    }
}