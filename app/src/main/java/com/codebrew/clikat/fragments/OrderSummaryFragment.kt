package com.codebrew.clikat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.OSLoyalityProductAdapter
import com.codebrew.clikat.adapters.OrderSummaryItemAdapter
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.FragmentOrderSummaryBinding
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.PromoCodeModel.DataBean
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.allCart
import com.codebrew.clikat.utils.StaticFunction.allCartlAUNDRY
import com.codebrew.clikat.utils.StaticFunction.convertDateOneToAnother
import com.codebrew.clikat.utils.StaticFunction.getAccesstoken
import com.codebrew.clikat.utils.StaticFunction.getCurrency
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.getLoyalityCart
import com.codebrew.clikat.utils.StaticFunction.netTotal
import com.codebrew.clikat.utils.StaticFunction.savenetTotal
import com.codebrew.clikat.utils.StaticFunction.sweetDialogueSuccess
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.MessageFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.fragment_order_summary.*
import kotlinx.android.synthetic.main.toolbar_app.*
/*
 * Created by cbl80 on 12/5/16.
 */
class OrderSummaryFragment : Fragment(), OnClickListener {


    private var deliveryAddress: String? = null
    private var deliveryDate: String? = null
    private var deliveryTime: String? = null
    private var deliveryName: String? = null
    private var maxDeliveryCharge = 0f
    private var handlingSum = 0f
    private var urgentPrice = 0f
    private var deliveryType = 0
    private var totalPoints = 0
    private var orderType = false
    private var deliveryOption = 0
    private val textConfig = Configurations.strings

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentOrderSummaryBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_summary, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //((MainActivity)getActivity()).setSupplierImage(false, "", 0, "", 0, 0);
        settypeface(view)
        setData()
        if (deliveryOption == 1) {
            settingOrderType(true)
            deliveryAddress = ""
            tvDeliveryAddress!!.visibility = View.GONE
        } else {
            settingOrderType(false)
            tvDeliveryAddress!!.visibility = View.VISIBLE
        }
        tb_title!!.text = getString(R.string.order_summary, textConfig.order)
        tb_back!!.setOnClickListener { v: View? -> Navigation.findNavController(view).popBackStack() }
    }

    private fun settingOrderType(orderType: Boolean) {
        if (orderType) {
            if (deliveryAddress!!.isEmpty()) tvPickupAddress!!.visibility = View.GONE
            rvDelivery!!.visibility = View.GONE
            rvHandling!!.visibility = View.GONE
        } else {
            rvDelivery!!.visibility = View.VISIBLE
            rvHandling!!.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            deliveryOption = bundle.getInt("deliveryOption", 0)
            deliveryType = bundle.getInt("deliveryType", 0)
            urgentPrice = bundle.getFloat("urgentPrice", 0f)
            deliveryAddress = bundle.getString("deliveryAddress", "")
            deliveryDate = bundle.getString("deliveryDate", "")
            deliveryTime = bundle.getString("deliveryTime", "")
            deliveryName = bundle.getString("deliveryName", "")
            if (bundle.containsKey("orderType")) {
                orderType = bundle.getBoolean("orderType", false)
            }
        }
    }

    private fun setData() {
        var netTotal = 0f
        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.LOYALITY_POINT_FLOW) {
            val loyalityPoints = getLoyalityCart(activity)
            if (loyalityPoints != null && loyalityPoints.list.size > 0) {
                val mLayoutManager: LayoutManager = LinearLayoutManager(this.activity!!.applicationContext)
                recyclerview!!.layoutManager = mLayoutManager
                recyclerview!!.layoutManager = LinearLayoutManager(activity)
                recyclerview!!.adapter = OSLoyalityProductAdapter(activity, loyalityPoints.list)
                for (i in loyalityPoints.list.indices) {
                    totalPoints = totalPoints + loyalityPoints.list[i].loyaltyPoints
                }
            }
        } else {
            val cartList: CartList
            var catId = 0
            try {
                catId = Prefs.with(activity).getString(DataNames.CATEGORY_ID, "0").toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
                cartList = allCartlAUNDRY(activity)
                val image = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO, "")
                val supplierI = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_ID, "")
                val supplierName = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_NAME, "")
                val branchId = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_BRANCH_ID, "")
                //((MainActivity)getActivity()).setSupplierImage(true, image, Integer.parseInt(supplierI), supplierName, Integer.parseInt(branchId), catId);
            } else {
                if (arguments != null && arguments!!.containsKey("prodList")) {
                    val productList: CartInfoServer = arguments?.getParcelable("prodList")?:CartInfoServer()

                    cartList = CartList()
                    val cartInfoList: MutableList<CartInfo> = ArrayList()
                    val cartInfo = CartInfo()
                    cartInfo.deliveryCharges = productList.deliveryCharges?:0.0f //
                    cartInfo.handlingSupplier = productList.handlingSupplier?:0.0f
                    cartInfo.handlingAdmin = productList.handlingAdmin?:0.0f
                    cartInfo.quantity = productList.quantity?:0
                    cartInfo.productName = productList.name //
                    cartInfo.price = productList.price?:0.0f //
                    cartInfo.subCategoryName = productList.subCatName?:"" //
                    cartInfoList.add(cartInfo)
                    cartList.cartInfos = cartInfoList
                } else {
                    cartList = allCart(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
                }
                //  if (cartList.getCartInfos().size() != 0)
/*                    ((MainActivity) getActivity()).setSupplierImage(true, cartList.getSupplierImage(), cartList.getCartInfos().get(0)
                            .getSupplierId(), cartList.getSupplierName(), cartList.getCartInfos().get(0).getSuplierBranchId(), catId);*/
            }
            if (cartList != null && cartList.cartInfos!!.size > 0) {
                val mLayoutManager: LayoutManager = LinearLayoutManager(this.activity!!.applicationContext)
                recyclerview!!.layoutManager = mLayoutManager
                recyclerview!!.layoutManager = LinearLayoutManager(activity)
                recyclerview!!.adapter = OrderSummaryItemAdapter(activity, cartList)
            }
            for (i in cartList.cartInfos!!.indices) {
                netTotal += cartList.cartInfos!![i].price * cartList.cartInfos!![i].quantity
                if (cartList.cartInfos!![i].deliveryCharges > maxDeliveryCharge) {
                    maxDeliveryCharge = cartList.cartInfos!![i].deliveryCharges
                }
                val charges = cartList.cartInfos!![i].handlingAdmin + cartList.cartInfos!![i].handlingSupplier
                if (charges > handlingSum) {
                    handlingSum = charges
                }
            }
        }
        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            llPickup!!.visibility = View.VISIBLE
            vDivider!!.visibility = View.VISIBLE
        } else {
            llPickup!!.visibility = View.GONE
            vDivider!!.visibility = View.GONE
        }
        tvDeliveryAddress!!.text = deliveryAddress
        tvDeliveryDate!!.text = deliveryDate
        tvDeliveryName!!.text = deliveryName
        tvPickupName!!.text = deliveryName
        tvDeliveryTime!!.text = deliveryTime
        tvPickupAddress!!.text = deliveryAddress
        val date = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
        tvPickupDate!!.text = GeneralFunctions.getFormattedDate(date)
        val time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
        tvPickupTime!!.text = GeneralFunctions.get12HrFormatedtime(time)
        if (netTotal == 0f) {
            netTotal = netTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
        }
        tvNetTotalValue!!.text = getCurrency(activity) + " " + netTotal + ""
        if (deliveryType == DataNames.DELIVERY_TYPE_URGENT) {
            tvDeliveryChargesValue!!.text = getCurrency(activity) + " " + urgentPrice + ""
            tvNetPayableValue!!.text = (getCurrency(activity)
                    +
                    " " + (netTotal + handlingSum + urgentPrice) + "")
            savenetTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0)
                    , netTotal + maxDeliveryCharge + handlingSum + urgentPrice)
        } else {
            tvDeliveryChargesValue!!.text = getCurrency(activity) + " " + maxDeliveryCharge + ""
            tvNetPayableValue!!.text = (getCurrency(activity)
                    +
                    " " + (netTotal + maxDeliveryCharge + handlingSum) + "")
            savenetTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0)
                    , netTotal + maxDeliveryCharge + handlingSum)
        }
        tvHandlingValue!!.text = getCurrency(activity) + " " + handlingSum + ""
        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.LOYALITY_POINT_FLOW) {
            tvNetTotalValue!!.text = totalPoints.toString() + " " + getString(R.string.points)
            tvNetPayableValue!!.text = totalPoints.toString() + " " + getString(R.string.points)
            tvHandlingValue!!.text = "0 " + getString(R.string.points)
            tvDeliveryChargesValue!!.text = "0 " + getString(R.string.points)
        }
        if (Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java) != null) {
            rlDiscount!!.visibility = View.VISIBLE
            val promoData = Prefs.with(activity).getObject(DataNames.DISCOUNT_AMOUNT, DataBean::class.java)
            val dis = promoData.discountPrice.toFloat()
            val ff = netTotal - dis
            if (deliveryType == DataNames.DELIVERY_TYPE_URGENT) {
                tvNetPayableValue!!.text = (getCurrency(activity)
                        +
                        " " + (ff + handlingSum + maxDeliveryCharge + urgentPrice) + "")
            } else {
                tvNetPayableValue!!.text = (getCurrency(activity)
                        +
                        " " + (ff + handlingSum + maxDeliveryCharge) + "")
            }
            //            holder2.tvNetTotal.setText(MessageFormat.format("{0} {1}", StaticFunction.getCurrency(mContext), ff));
            tvDiscount.text = getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, promoData.discountPrice)
        } else {
            rlDiscount.visibility = View.GONE
            val amount = netTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
            // holder2.tvNetTotal.setText(MessageFormat.format("{0} {1}", StaticFunction.getCurrency(mContext), amount));
        }
    }

    private fun settypeface(view: View) {
        (view.findViewById<View>(R.id.tvPickup) as TextView).typeface = AppGlobal.semi_bold
        (view.findViewById<View>(R.id.tvDelivery) as TextView).typeface = AppGlobal.semi_bold
        (view.findViewById<View>(R.id.tvPickupName) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvPickupAddress) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvPickupDate) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvPickupTime) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryName) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryAddress) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryDate) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryTime) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvitems) as TextView).typeface = AppGlobal.semi_bold
        (view.findViewById<View>(R.id.tvNetTotal) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvNetTotalValue) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryCharge) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvDeliveryChargesValue) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvHandling) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvHandlingValue) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvNetPayable) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvNetPayableValue) as TextView).typeface = AppGlobal.regular
        (view.findViewById<View>(R.id.tvAdditionalRemarks) as TextView).typeface = AppGlobal.semi_bold
        placeOrder!!.typeface = AppGlobal.semi_bold
        placeOrder!!.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        // ((MainActivity)getActivity()).tvTitleMain.setText(getString(R.string.order_summary, textConfig.order));
//  ((MainActivity)getActivity()).setIconsCart(true);
    }

    private fun loyalityPlaceOrder(loyalityPoints: CartLoyalityPoints?) {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        loyalityPoints!!.accessToken = getAccesstoken(activity)
        loyalityPoints.languageId = getLanguage(activity)
        val list: MutableList<Int> = ArrayList()
        for (i in loyalityPoints.list.indices) {
            list.add(loyalityPoints.list[i].productId)
        }
        loyalityPoints.productList = list
        val cartServer = LoyalityCartServer()
        cartServer.supplierBranchId = loyalityPoints.supplierBranchId
        cartServer.deliveryType = loyalityPoints.deliveryType
        cartServer.deliveryAddressId = loyalityPoints.deliveryAddressId
        cartServer.totalPoints = totalPoints
        cartServer.deliveryDate = loyalityPoints.deliveryDate
        cartServer.is_postponed = loyalityPoints.is_postponed
        cartServer.urgent = loyalityPoints.urgent
        cartServer.urgentPrice = loyalityPoints.urgentPrice
        cartServer.remarks = loyalityPoints.remarks
        cartServer.accessToken = loyalityPoints.accessToken
        cartServer.languageId = loyalityPoints.languageId
        cartServer.productList = loyalityPoints.productList
        val call = RestClient.getModalApiService(activity).loyalityOrder(cartServer)
        call.enqueue(object : Callback<ExampleCommon?> {
            override fun onResponse(call: Call<ExampleCommon?>, response: Response<ExampleCommon?>) {
                barDialog.dismiss()
                val exampleCommon = response.body()
                if (response.code() == 200 && exampleCommon!!.status == 200) {
                    sweetDialogueSuccess(activity, getString(R.string.success), getString(R.string.succesfully_scheduled), false, 3, null, null)
                    Prefs.with(activity).save(DataNames.LOYALITY_ORDER_ID, "" + exampleCommon.data.orderId)
                    // ((MainActivity)getActivity()).homeTab();
                    val bundle = Bundle()
                    bundle.putInt("IsOrder", 1)
                    val loyalityPointFragment = LoyalityPointFragment()
                    loyalityPointFragment.arguments = bundle
                    (activity as MainActivity?)!!.pushFragments(DataNames.TAB1,
                            loyalityPointFragment,
                            true, true, "loyalityPoints", true)
                } else {
                    Snackbar.make(view!!, exampleCommon!!.message, Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ExampleCommon?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    override fun onClick(v: View) {
        if (R.id.placeOrder == v.id) {
            if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.LOYALITY_POINT_FLOW) {
                val loyalityPoints = getLoyalityCart(activity)
                loyalityPlaceOrder(loyalityPoints)
            } else { // PaymentMethod paymentMethod = new PaymentMethod();
                val bundle = Bundle()
                bundle.putInt("paymentMethod", arguments!!.getInt("paymentMethod"))
                bundle.putInt("deliveryType", deliveryType)
                //   paymentMethod.setArguments(bundle);
              //  Navigation.findNavController(v).navigate(R.id.action_paymentMethod, bundle)
                /*      ((MainActivity)getActivity()).
                        pushFragments(DataNames.TAB1, paymentMethod,
                                true, true, "", true);*/
            }
        }
    }

    private fun updateCart() {
        val calendar = Calendar.getInstance()
        val pickupId = 0
        val pickupTime = convertDateOneToAnother(calendar.time.toString(),
                "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss")
        // String pickupDate = Prefs.with(getActivity()).getString(DataNames.PICKUP_DATE, dateYYMMDD);
    }
}