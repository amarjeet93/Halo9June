package com.codebrew.clikat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.databinding.FragmentBaseOrderBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.DataCount
import com.codebrew.clikat.modal.PojoPendingOrders
import com.codebrew.clikat.modal.PojoSignUp
import com.codebrew.clikat.modal.other.ScheduleListModel
import com.codebrew.clikat.modal.other.ScheduleListModel.DataBean.OrderHistoryBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.pending_orders.adapter.UpcomingAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_base_order.*
import kotlinx.android.synthetic.main.nothing_found.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 13/8/16.
 */
class SchedulerOrderFragment : Fragment() {
    private var exampleOrderHistory: ScheduleListModel? = null
    var upcomingAdapter: UpcomingAdapter? = null

    var appType:Int?=null

    private var orderHistory2List: List<OrderHistoryBean>? = null
    private val textConfig = Configurations.strings
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBaseOrderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_order, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvOrders!!.layoutManager = LinearLayoutManager(activity)
        tvText!!.typeface = AppGlobal.semi_bold
        tvText!!.setText(R.string.no_order_found)

        val screenFlowBean=Prefs.with(activity).getObject(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        appType=screenFlowBean.app_type

    }

    private fun apiUpcomingOrders(b: Boolean) {
        val barDialog = ProgressBarDialog(activity)
        if (b) {
            barDialog.show()
        }
        val hashMap = HashMap<String, String>()
        val signUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        hashMap["accessToken"] = signUp.data.access_token
        hashMap["languageId"] = getLanguage(activity).toString() + ""
        val historyCall = RestClient.getModalApiService(activity).scheduledOrder(hashMap)
        historyCall.enqueue(object : Callback<ScheduleListModel> {
            override fun onResponse(call: Call<ScheduleListModel>, response: Response<ScheduleListModel>) {
                barDialog.dismiss()
                if (response.body()!!.status == 200) {
                    exampleOrderHistory = response.body()
                    orderHistory2List = exampleOrderHistory!!.data.orderHistory
                    if (orderHistory2List != null && orderHistory2List!!.isEmpty()) {
                        noData!!.visibility = View.VISIBLE
                        rvOrders!!.visibility = View.GONE
                    } else {
                        noData!!.visibility = View.GONE
                        rvOrders!!.visibility = View.VISIBLE
                    }
                    try {
                        val pojoPendingOrders = PojoPendingOrders()
                        val dataCount = DataCount()
                        dataCount.scheduleOrders = orderHistory2List?.size
                        pojoPendingOrders.data = dataCount
                        (activity as MainActivity?)!!.tvScheduledOrderCount.text = orderHistory2List?.size.toString()
                        Prefs.with(activity).save(DataNames.ORDERS_COUNT, pojoPendingOrders)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                    if (b || upcomingAdapter == null) {
                      //  upcomingAdapter = UpcomingAdapter(activity, orderHistory2List, appType?:0)
                        setdata()
                    } else {
                      //  upcomingAdapter = UpcomingAdapter(activity, orderHistory2List, appType?:0)
                        setdata()
                    }
                } else if (response.code() == 401) {
                    ConnectionDetector(activity).loginExpiredDialog()
                }
            }

            override fun onFailure(call: Call<ScheduleListModel>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
        (activity as MainActivity?)!!.tvTitleMain.text = getString(R.string.scheduled_order, textConfig.order)
        (activity as MainActivity?)!!.tbShare.visibility = View.GONE
        (activity as MainActivity?)!!.seticonsSidepnael()
        if (exampleOrderHistory != null) {
            apiUpcomingOrders(false)
        } else if (isInternetConnected(activity)) {
            apiUpcomingOrders(true)
        } else {
            showNoInternetDialog(activity)
        }
    }

    private fun setdata() {
        rvOrders!!.isNestedScrollingEnabled = false
        rvOrders!!.adapter = upcomingAdapter
    }
}