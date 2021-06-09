package com.codebrew.clikat.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.LoyalityOrderAdapter
import com.codebrew.clikat.adapters.LoyalityPointAdapter
import com.codebrew.clikat.databinding.FragmentLoyalityPointsBinding
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.module.delivery.DeliveryFragment
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.isLoginProperly
import com.codebrew.clikat.utils.StaticFunction.saveLoyalityCart
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_loyality_points.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 16/5/16.
 */
class LoyalityPointFragment : Fragment(), OnClickListener {
    private var locationUser: LocationUser? = null
    private var loyalityPointAdapter: LoyalityPointAdapter? = null
    private var exampleLoyalityPoints: ExampleLoyalityPoints? = null
    private var pojoSignUp: PojoSignUp? = null
    private var totalLoyalityPoints: Int? = null
    private val tab_selected = Color.parseColor(Configurations.colors.tabSelected)
    private val tab_unselected = Color.parseColor(Configurations.colors.tabUnSelected)
    private val text_white = Color.parseColor(Configurations.colors.appBackground)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentLoyalityPointsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_loyality_points, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottom_sheet!!.setOnClickListener(this)
        tvPoints!!.setOnClickListener(this)
        tvLoyalityPoints!!.setOnClickListener(this)
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.delivery)
        settypeface()
        if (exampleLoyalityPoints != null) {
            setTabs(0)
            setdata()
        } else {
            if (pojoSignUp!!.data != null && !pojoSignUp!!.data.access_token.trim { it <= ' ' }.isEmpty()) {
                apiLoyalityPoints(pojoSignUp!!.data.access_token)
            }
        }
    }

    private fun settypeface() {
        bottom_sheet!!.typeface = AppGlobal.semi_bold
        tvPoints!!.typeface = AppGlobal.regular
        noData!!.typeface = AppGlobal.semi_bold
        tvLoyalityPoints!!.typeface = AppGlobal.regular
        tvPoints!!.typeface = AppGlobal.regular
    }

    private fun setTabs(i: Int) {
        if (activity != null) {
            when (i) {
                0 -> {
                    tvLoyalityPoints!!.setTextColor(text_white)
                    tvLoyalityPoints!!.setBackgroundColor(tab_selected)
                    tvPoints!!.setTextColor(text_white)
                    tvPoints!!.setBackgroundColor(tab_unselected)
                    rlOrders!!.visibility = View.GONE
                    rlProducts!!.visibility = View.VISIBLE
                }
                1 -> {
                    tvLoyalityPoints!!.setTextColor(text_white)
                    tvLoyalityPoints!!.setBackgroundColor(tab_unselected)
                    tvPoints!!.setTextColor(text_white)
                    tvPoints!!.setBackgroundColor(tab_selected)
                    rlOrders!!.visibility = View.VISIBLE
                    rlProducts!!.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationUser = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
        pojoSignUp = isLoginProperly(activity)
        if (isInternetConnected(activity)) {
            val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
            if (pojoSignUp != null && !pojoSignUp.data.access_token.trim { it <= ' ' }.isEmpty()) {
                apiLoyalityPoints(pojoSignUp.data.access_token)
            }
        } else {
            showNoInternetDialog(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.tbShare.visibility = View.GONE
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.loyality_points)
        (activity as MainActivity?)!!.seticonsSidepnael()
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
    }

    private fun setdata() {
        if (arguments != null && arguments!!.getInt("IsOrder", 0) == 1) {
            setTabs(1)
        } else {
            setTabs(0)
        }
        totalLoyalityPoints = exampleLoyalityPoints!!.data.loyaltyPoints
        loyalityPointAdapter = LoyalityPointAdapter(activity, exampleLoyalityPoints!!.data.product, totalLoyalityPoints
                , bottom_sheet)
        tvPoints!!.text = totalLoyalityPoints.toString() + ""
        rvOrders!!.isNestedScrollingEnabled = false
        rvProducts!!.isNestedScrollingEnabled = false
        rvProducts!!.layoutManager = GridLayoutManager(activity, 3)
        if (loyalityPointAdapter!!.itemCount == 0) {
            noData!!.visibility = View.VISIBLE
            rvProducts!!.visibility = View.GONE
        } else {
            rvProducts!!.adapter = loyalityPointAdapter
            noData!!.visibility = View.GONE
            rvProducts!!.visibility = View.VISIBLE
        }
        val loyalityOrderAdapter = LoyalityOrderAdapter(activity, exampleLoyalityPoints!!.data.orders)
        rvOrders!!.layoutManager = LinearLayoutManager(activity)
        if (loyalityOrderAdapter.itemCount == 0) {
            noOrders!!.visibility = View.VISIBLE
            rvOrders!!.visibility = View.GONE
        } else {
            rvOrders!!.adapter = loyalityOrderAdapter
            noOrders!!.visibility = View.GONE
            rvOrders!!.visibility = View.VISIBLE
        }
    }

    private fun apiLoyalityPoints(accessToken: String) {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val hashMap = HashMap<String, String>()
        //hashMap["areaId"] = "" + areaID
        hashMap["languageId"] = "" + getLanguage(activity)
        hashMap["accessToken"] = "" + accessToken
        val call = RestClient.getModalApiService(activity).apiLoyalityPoints(hashMap)
        call.enqueue(object : Callback<ExampleLoyalityPoints?> {
            override fun onResponse(call: Call<ExampleLoyalityPoints?>, response: Response<ExampleLoyalityPoints?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    exampleLoyalityPoints = response.body()
                    setdata()
                }
            }

            override fun onFailure(call: Call<ExampleLoyalityPoints?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    override fun onClick(v: View) {
        if (v.id == R.id.bottom_sheet) {
            setLoyalityData()
            (activity as MainActivity?)!!.flow_1 = DataNames.LOYALITY_POINT_FLOW
            Prefs.with(activity).save(DataNames.FLOW_STROE, (activity as MainActivity?)!!.flow_1)
            val deliveryFragment = DeliveryFragment()
            val bundle = Bundle()
            bundle.putInt(DataNames.SUPPLIER_BRANCH_ID, loyalityPointAdapter!!.selectedSupplierBranchId)
            deliveryFragment.arguments = bundle
            (activity as MainActivity?)!!.pushFragments(DataNames.TAB1, deliveryFragment,
                    true, true, "", true)
        } else if (v.id == R.id.tvPoints) {
            setTabs(0)
        } else if (v.id == R.id.tvPoints) {
            setTabs(1)
        }
    }

    private fun setLoyalityData() {
        val cartLoyalityPoints = loyalityPointAdapter!!.redeemPointList
        val productLoyalityPoints = CartLoyalityPoints()
        productLoyalityPoints.list = cartLoyalityPoints
        productLoyalityPoints.supplierBranchId = loyalityPointAdapter!!.selectedSupplierBranchId
        productLoyalityPoints.totalPoints = totalLoyalityPoints!!
        saveLoyalityCart(activity, productLoyalityPoints)
    }
}