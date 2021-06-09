package com.codebrew.clikat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.PromotionAdapter
import com.codebrew.clikat.databinding.FragmentBaseOrderBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.PromotionListModel
import com.codebrew.clikat.modal.other.PromotionListModel.DataBean.ListBean
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.configurations.Configurations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.fragment_base_order.*
import kotlinx.android.synthetic.main.nothing_found.*
/*
 * Created by cbl80 on 4/5/16.
 */
class PromotionFragment : Fragment() {
    private var productList: MutableList<ListBean>? = null

    private var mAdapter: PromotionAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBaseOrderBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_order, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvOrders!!.layoutManager = GridLayoutManager(activity, 3)
        tvText!!.typeface = AppGlobal.semi_bold
        val locationUser = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
        productList = mutableListOf()
        mAdapter = PromotionAdapter(activity, productList)
        rvOrders!!.adapter = mAdapter
        if (isInternetConnected(activity)) {
            apiPromotion()
        } else {
            showNoInternetDialog(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.seticonsSidepnael()
        (activity as MainActivity?)!!.tbShare.visibility = View.GONE
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
        (activity as MainActivity?)!!.tvTitleMain.text = resources.getString(R.string.promotions)
        if (productList != null && productList!!.size > 0) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun apiPromotion() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val hashMap = HashMap<String, String>()
        hashMap["languageId"] = "" + getLanguage(activity)
        val call = RestClient.getModalApiService(activity).apiGetPromotion(hashMap)
        call.enqueue(object : Callback<PromotionListModel?> {
            override fun onResponse(call: Call<PromotionListModel?>, response: Response<PromotionListModel?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val pojoPromotion = response.body()
                    if (pojoPromotion!!.status == ClikatConstants.STATUS_SUCCESS) {
                        setdata(pojoPromotion.data.list)
                    }
                } else if (response.code() == 401) {
                    ConnectionDetector(activity).loginExpiredDialog()
                }
            }

            override fun onFailure(call: Call<PromotionListModel?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    private fun setdata(list: List<ListBean>) {
        productList?.addAll(list)
        if (list.size == 0) {
            tvText!!.visibility = View.VISIBLE
            rvOrders!!.visibility = View.GONE
        } else {
            tvText!!.visibility = View.GONE
            rvOrders!!.visibility = View.VISIBLE
            mAdapter!!.notifyDataSetChanged()
        }
    }
}