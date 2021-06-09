package com.codebrew.clikat.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codebrew.clikat.R
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.databinding.FragmentPaymentMethodBinding
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.PlaceOrderInput
import com.codebrew.clikat.modal.other.PlaceOrderModel
import com.codebrew.clikat.modal.other.PromoCodeModel.DataBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.BookingFlowBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.payment_gateway.PaymentListActivity
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.DialogIntrface
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.allCart
import com.codebrew.clikat.utils.StaticFunction.clearCart
import com.codebrew.clikat.utils.StaticFunction.getDeliveryAddress
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.netTotalFinal
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.StaticFunction.sweetDialogueSuccess11
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.android.synthetic.main.fragment_payment_method.*
import kotlinx.android.synthetic.main.toolbar_app.*

/*
 * Created by cbl80 on 5/5/16.
 */
class PaymentMethod : Fragment(), OnClickListener, DialogIntrface {

    private var paymenttype = 0
    private var isOrderConfirm = false
    private var orderHistory: OrderHistory2? = null
    private var barDialog: ProgressBarDialog? = null
    private var deliveryType = 0
    private var screenFlowBean: ScreenFlowBean? = null

    private var isConfirmOrder=false
    
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPaymentMethodBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_method, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenFlowBean = Prefs.with(activity).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        settypeface()
        btnFinish!!.setOnClickListener(this)
        barDialog = ProgressBarDialog(activity)
        val paymentMethod = arguments!!.getInt("paymentMethod", 2)
        when (paymentMethod) {
            0 -> {
                rbCOD!!.visibility = View.VISIBLE
                rbCard!!.visibility = View.GONE
            }
            1 -> {
                rbCOD!!.visibility = View.GONE
                rbCard!!.visibility = View.VISIBLE
            }
            2 -> {
                rbCOD!!.visibility = View.VISIBLE
                rbCard!!.visibility = View.VISIBLE
            }
        }
        if (arguments!!.containsKey("deliveryType")) {
            deliveryType = arguments!!.getInt("deliveryType")
        }
        rdGroup!!.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbCard) {
                rbCard!!.setTextColor(ContextCompat.getColor(activity!!, R.color.brown))
                rbCOD!!.setTextColor(ContextCompat.getColor(activity!!, R.color.light_text_color))
                paymenttype = DataNames.PAYMENT_CARD
            } else {
                paymenttype = DataNames.PAYMENT_CASH
                rbCard!!.setTextColor(ContextCompat.getColor(activity!!, R.color.light_text_color))
                rbCOD!!.setTextColor(ContextCompat.getColor(activity!!, R.color.brown))
            }
        }
        tb_title!!.text = getString(R.string.delivery, Configurations.strings.order)
        tb_back!!.setOnClickListener { v: View? -> Navigation.findNavController(view).popBackStack() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (isOrderConfirm) {
                    apiConfirmOrder()
                } else {
                    apiGenerateOrder()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ((MainActivity)getActivity()).setIconsCart(true);
// ((MainActivity)getActivity()).tvTitleMain.setText(R.string.payment_method);
        val catID = Prefs.with(activity).getString(DataNames.CATEGORY_ID, "0")
        var catId = 0
        try {
            catId = catID.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val cartList: CartList
        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            val image = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO, "")
            val supplierI = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_ID, "")
            val supplierName = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_NAME, "")
            val branchId = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_BRANCH_ID, "")
            //   ((MainActivity)getActivity()).setSupplierImage(true, image, Integer.parseInt(supplierI), supplierName, Integer.parseInt(branchId), catId);
        } else if (!isOrderConfirm) {
            try {
                cartList = allCart(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
                /*                ((MainActivity) getActivity()).setSupplierImage(true, cartList.getSupplierImage(), cartList.getCartInfos().get(0)
                        .getSupplierId(), cartList.getSupplierName(), cartList.getCartInfos().get(0).getSuplierBranchId(), catId);*/
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            isOrderConfirm = bundle.getBoolean("confirmOrder", false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.with(activity).save(DataNames.DB_AUTH, false)
    }

    private fun settypeface() {
        (view!!.findViewById<View>(R.id.tvPaymentOptions) as TextView).typeface = AppGlobal.semi_bold
        rbCard!!.typeface = AppGlobal.regular
        rbCOD!!.typeface = AppGlobal.regular
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnFinish) {
            if (isInternetConnected(activity)) {
                if (!isOrderConfirm) {
                    if (paymenttype == DataNames.PAYMENT_CARD) {
                        var amount = netTotalFinal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
                        if (Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java) != null) {
                            val promoData = Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java)
                            amount = amount - promoData.discountPrice
                        }
                        startActivityForResult(Intent(activity, PaymentListActivity::class.java)
                                .putExtra("orderId", Prefs.with(activity).getString(DataNames.CART_ID, "0"))
                                .putExtra("amount", amount), 100)
                    } else {
                        apiGenerateOrder()
                    }
                } else {
                    if (paymenttype == DataNames.PAYMENT_CARD) {
                        startActivityForResult(Intent(activity, PaymentListActivity::class.java)
                                .putExtra("orderId", orderHistory!!.orderId)
                                .putExtra("amount", netTotalFinal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))), 100)
                    } else {
                        apiConfirmOrder()
                    }
                }
            } else {
                showNoInternetDialog(activity)
            }
        }
    }

    private fun apiConfirmOrder() {
        orderHistory = Prefs.with(activity).getObject(DataNames.ORDER_DETAIL, OrderHistory2::class.java)
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        barDialog!!.show()
        val hashMap = HashMap<String, String>()
        hashMap["accessToken"] = "" + pojoSignUp.data.access_token
        hashMap["orderId"] = orderHistory?.orderId?.joinToString()?:""
        hashMap["paymentType"] = "" + paymenttype
       // hashMap["languageId"] = "" + Prefs.with(activity).getInt(DataNames.SELECTED_LANGUAGE, 14)
        val call = RestClient.getModalApiService(activity).confirmOrder(hashMap)
        call.enqueue(object : Callback<PlaceOrderModel?> {
            override fun onResponse(call: Call<PlaceOrderModel?>, response: Response<PlaceOrderModel?>) {
                barDialog!!.dismiss()
                val exampleCommon = response.body()
                if (response.code() == 200 && exampleCommon!!.status == 200) {
                    clearIntent(getString(R.string.succesfully_order, Configurations.strings.order), exampleCommon, true)
                } else {
                    Snackbar.make(llContainer!!, exampleCommon?.message?:"", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PlaceOrderModel?>, t: Throwable) {
                barDialog!!.dismiss()
            }
        })
    }

    private fun apiGenerateOrder() {
        Prefs.with(activity).save(DataNames.DB_AUTH, true)
        val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        barDialog!!.show()
        val placeOrderInput = PlaceOrderInput()
        if (Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java) != null) {
            val promoData = Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java)
            placeOrderInput.promoCode = promoData.promoCode
            placeOrderInput.promoId = promoData.id
            placeOrderInput.discountAmount = promoData.discountPrice.toFloat()
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        placeOrderInput.accessToken = pojoSignUp.data.access_token
        placeOrderInput.offset = SimpleDateFormat("ZZZZZ", Locale.getDefault()).format(System.currentTimeMillis())
        placeOrderInput.cartId = Prefs.with(activity).getString(DataNames.CART_ID, "0")
        placeOrderInput.paymentType = paymenttype
       // placeOrderInput.languageId = Prefs.with(activity).getInt(DataNames.SELECTED_LANGUAGE, 14)
        if (Prefs.with(activity).getInt(DataNames.FLOW_PA, 1) == 0) placeOrderInput.isPackage = 1 else placeOrderInput.isPackage = 0
        if (Prefs.with(activity).getBoolean(DataNames.AGENT_TYPE, false)) {
            if (Prefs.with(activity).getInt(DataNames.AGENT_ID, 0) > 0) placeOrderInput.agentIds.add(Prefs.with(activity).getInt(DataNames.AGENT_ID, 0))
            placeOrderInput.booking_date_time = convertDateOneToAnother(Prefs.with(activity).getString(DataNames.PICKUP_DATE, "") + " " + Prefs.with(activity).getString(DataNames.PICKUP_TIME1, ""),
                    "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss")
        } else {
            if (screenFlowBean?.app_type == AppDataType.Beauty.type) {
                placeOrderInput.booking_date_time = convertDateOneToAnother(Prefs.with(activity).getString(DataNames.PICKUP_DATE, "") + " " + Prefs.with(activity).getString(DataNames.PICKUP_TIME1, ""),
                        "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss")
            } else {
                val calendar = Calendar.getInstance()
                if (Prefs.with(activity).getString(DataNames.DELIVERY_MAX_TIME, "").isNotEmpty()) {
                  //  calendar.add(Calendar.MINUTE, Prefs.with(activity).getString(DataNames.DELIVERY_MAX_TIME, "0").toInt())
                } else {
                    calendar.add(Calendar.MINUTE, 15)
                }
                placeOrderInput.booking_date_time = dateFormat.format(calendar.time)
            }
        }
        if (Prefs.with(activity).getInt(DataNames.DURATION, 0) > 0) {
            placeOrderInput.duration = Prefs.with(activity).getInt(DataNames.DURATION, 0)
        }
        val call = RestClient.getModalApiService(activity).generateOrder(placeOrderInput)
        call.enqueue(object : Callback<PlaceOrderModel?> {
            override fun onResponse(call: Call<PlaceOrderModel?>, response: Response<PlaceOrderModel?>) {
                barDialog!!.dismiss()
                if (response.isSuccessful) {
                    val exampleCommon = response.body()
                    if (exampleCommon!!.status == 200) {
                        clearIntent(getString(R.string.succesfully_order, Configurations.strings.order), exampleCommon, false)
                    } else {
                        Snackbar.make(llContainer!!, exampleCommon.message?:"", Snackbar.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<PlaceOrderModel?>, t: Throwable) {
                barDialog!!.dismiss()
                Snackbar.make(llContainer!!, t.message!!, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun clearIntent(stringId: String, exampleCommon: PlaceOrderModel?, isConfirmOrder: Boolean) {
        Prefs.with(activity).remove(DataNames.DISCOUNT_AMOUNT)

        this.isConfirmOrder=isConfirmOrder

        sweetDialogueSuccess11(activity, getString(R.string.success), stringId, false, 1001
                , this)

        if (!isConfirmOrder) {
            val history2 = OrderHistory2()
            history2.status = 0
            val flow = Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0)
            history2.netAmount = netTotalFinal(activity, flow)
            val cartList = allCart(activity, flow)
            history2.productCount = cartList.cartInfos!!.size
            history2.orderId = exampleCommon?.data as ArrayList<Int>
            history2.createdOn = Prefs.with(activity).getString(DataNames.CREATED_DATE, "")
            history2.deliveredOn = (Prefs.with(activity).getString(DataNames.DELIVERY_DATE, "")
                    + " " + Prefs.with(activity).getString(DataNames.DELIVERY_TIME, ""))
            history2.shippedOn = ""
            history2.serviceDate = (Prefs.with(activity).getString(DataNames.DELIVERY_DATE, "")
                    + " " + Prefs.with(activity).getString(DataNames.DELIVERY_TIME, ""))
            history2.nearOn = ""
            history2.schedlueOrder = 0
            history2.status = 0
            history2.logo = cartList.supplierImage
            val products: MutableList<HistoryProduct> = ArrayList()
            for (i in cartList.cartInfos!!.indices) {
                val product = HistoryProduct()
                product.productName = cartList.cartInfos!![i].productName
                product.measuringUnit = cartList.cartInfos!![i].measuringUnit
                product.imagePath = cartList.cartInfos!![i].imagePath
                product.quantity = cartList.cartInfos!![i].quantity
                products.add(product)
            }
            history2.deliveryAddress = getDeliveryAddress(activity)
            history2.product = products
            history2.isFrom = 1
            history2.paymentType = paymenttype
            Prefs.with(activity).save(DataNames.ORDER_DETAIL, history2)
        }
        clearCart(activity)
    }

    private fun convertDateOneToAnother(datetoConvert: String, dateTo: String, dateFrom: String): String {
        val inputFormat = SimpleDateFormat(dateTo, Locale.getDefault())
        val outputFormat = SimpleDateFormat(dateFrom, Locale.getDefault())
        val date: Date
        try {
            date = inputFormat.parse(datetoConvert)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onSuccessListener() {
        if (!isConfirmOrder) {
            val bookingFlowBean = Prefs.with(activity).getObject(DataNames.BOOKING_FLOW, BookingFlowBean::class.java)
            /*  if (Prefs.with(getActivity()).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_PACKAGE) {*/ /*    if(deliveryType==DataNames.DELIVERY_TYPE_POSTPONED)
                        {
                            Intent schedulerIntent = new Intent(getActivity(), OrderschedulerActivity.class);
                            schedulerIntent.putExtra("flow", Prefs.with(getActivity()).getInt(DataNames.FLOW_STROE, 0));
                            schedulerIntent.putExtra("paymentMethod", paymenttype);

                            schedulerIntent.putIntegerArrayListExtra("orderId", exampleCommon.getData());
                            startActivity(schedulerIntent);

                        } else {*/
// getChildFragmentManager().popBackStack(getChildFragmentManager().getBackStackEntryAt(1).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
           // Navigation.findNavController(view!!).navigate(R.id.action_paymentMethod_to_mainFragment)
            /*        Intent clearPreviousData = new Intent(getActivity(), MainActivity.class);
                    clearPreviousData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    clearPreviousData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    clearPreviousData.putExtra("intent", 11);
                    clearPreviousData.putIntegerArrayListExtra("orderId", exampleCommon.getData());
                    startActivity(clearPreviousData);*/
// }
        } else {
            val allCategories = Prefs.with(activity).getObject(DataNames.ORDERS_COUNT, PojoPendingOrders::class.java)
            var count = allCategories.data.scheduleOrders
            count = count - 1
            allCategories.data.scheduleOrders = count
            Prefs.with(activity).save(DataNames.ORDERS_COUNT, allCategories)
            val history2 = OrderHistory2()
            history2.status = 0
            val flow = Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0)
            history2.netAmount = netTotalFinal(activity, flow)
            val cartList = allCart(activity, flow)
            history2.productCount = cartList.cartInfos!!.size
            history2.orderId = orderHistory!!.orderId
            history2.createdOn = Prefs.with(activity).getString(DataNames.CREATED_DATE, "")
            history2.deliveredOn = (Prefs.with(activity).getString(DataNames.DELIVERY_DATE, "")
                    + " " + Prefs.with(activity).getString(DataNames.DELIVERY_TIME, ""))
            history2.shippedOn = ""
            history2.nearOn = ""
            history2.schedlueOrder = 0
            history2.status = 0
            val products: MutableList<HistoryProduct> = ArrayList()
            for (i in cartList.cartInfos!!.indices) {
                val product = HistoryProduct()
                product.productName = cartList.cartInfos!![i].productName
                product.measuringUnit = cartList.cartInfos!![i].measuringUnit
                product.imagePath = cartList.cartInfos!![i].imagePath
                product.quantity = cartList.cartInfos!![i].quantity
                products.add(product)
            }
            history2.deliveryAddress = getDeliveryAddress(activity)
            history2.product = products
            history2.isFrom = 1
            history2.paymentType = paymenttype
            /*               Prefs.with(getActivity()).save(DataNames.ORDER_DETAIL, history2);
                    Intent clearPreviousData = new Intent(getActivity(), MainActivity.class);
                    clearPreviousData.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    clearPreviousData.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    clearPreviousData.putExtra("intent", 11);
                    clearPreviousData.putIntegerArrayListExtra("orderId", exampleCommon.getData());
                    startActivity(clearPreviousData);*/
//  getChildFragmentManager().popBackStack(getChildFragmentManager().getBackStackEntryAt(1).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
           // Navigation.findNavController(view!!).navigate(R.id.action_paymentMethod_to_mainFragment)
        }
        clearCart(activity)
    }

    override fun onErrorListener() {

    }
}