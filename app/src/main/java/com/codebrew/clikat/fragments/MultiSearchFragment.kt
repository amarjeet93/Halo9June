package com.codebrew.clikat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.SearchAdapter
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.PojoMultiSearch
import com.codebrew.clikat.modal.Result
import com.codebrew.clikat.modal.SearchItem
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.ConnectionDetector
import com.codebrew.clikat.utils.Dialogs.MultiSearchDialog
import com.codebrew.clikat.utils.Dialogs.MultiSearchDialog.OnOkClickListener
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getCartQuantity
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import kotlinx.android.synthetic.main.fragment_multisearch.*
import kotlinx.android.synthetic.main.nothing_found.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 6/5/16.
 */
class MultiSearchFragment : Fragment() {
  //  private var flLayout: FlowLayout? = null
    private var strings: MutableList<String>? = null
    private var categoryId = 0
    private var supplierBranchId = 0
    private var list: List<SearchItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_multisearch, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
   //     flLayout = view.findViewById(R.id.flLayout)
        tvText!!.typeface = AppGlobal.semi_bold
        val mLayoutManager: LayoutManager = LinearLayoutManager(activity)
        recyclerview.setLayoutManager(mLayoutManager)
        strings = arguments!!.getStringArrayList("TagSerach")
        if (arguments!!.containsKey("categoryId")) categoryId = arguments!!.getInt("categoryId")
        supplierBranchId = (activity as MainActivity?)!!.supplierBranchId
        setTags(strings)
        if (isInternetConnected(activity)) {
            getdata(strings, categoryId, supplierBranchId)
        } else {
            showNoInternetDialog(activity)
        }
        if (list != null) {
            setdata(list)
        }


        lvSearch.setOnClickListener {

            val dialog = MultiSearchDialog(activity, false, OnOkClickListener { list: List<String>? ->
                setTags(list)
                strings!!.clear()
                strings!!.addAll(list!!)
                getdata(list, categoryId, supplierBranchId)
            }, strings)
            dialog.show()
        }
    }

    private fun getdata(strings: List<String>?, categoryId: Int, supplierBranchId: Int) {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        var s = strings!![0]
        for (i in 1 until strings.size) {
            s = s + "," + strings[i]
        }
        val hashMap = HashMap<String, String>()
        hashMap["supplierBranchId"] = "" + supplierBranchId
        hashMap["categoryId"] = "" + categoryId
        hashMap["searchList"] = s
        val call = RestClient.getModalApiService(activity).multisearch(hashMap, getLanguage(activity))
        call.enqueue(object : Callback<PojoMultiSearch?> {
            override fun onResponse(call: Call<PojoMultiSearch?>, response: Response<PojoMultiSearch?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val multiSearch = response.body()
                    if (multiSearch!!.status == ClikatConstants.STATUS_SUCCESS) {
                        list = multiSearch.data.list
                        setdata(list)
                    } else if (multiSearch.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                        ConnectionDetector(activity).loginExpiredDialog()
                    } else {
                        GeneralFunctions.showSnackBar(view, multiSearch.message, activity)
                    }
                }
            }

            override fun onFailure(call: Call<PojoMultiSearch?>, t: Throwable) {
                GeneralFunctions.showSnackBar(view, t.message, activity)
                barDialog.dismiss()
            }
        })
    }

    private fun setdata(list: List<SearchItem>?) {
        var productId: Int
        val image = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO, "")
        val supplierI = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_ID, "")
        val supplierName = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_NAME, "")
        var branchId = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_BRANCH_ID, "")
        (activity as MainActivity?)!!.setSupplierImage(true, image, supplierI.toInt(), supplierName, branchId.toInt(), categoryId)
        branchId = Prefs.with(activity).getString(DataNames.SUPPLIERBRANCHID, "")
        for (i in list!!.indices) {
            val dataProduct: List<Result> = list[i].result
            for (j in dataProduct.indices) {
                productId = dataProduct[j].id
                if (branchId.isEmpty() || branchId == "" + supplierBranchId) {
                    dataProduct[j].quantity = getCartQuantity(activity, productId)
                }
                if (dataProduct[j].price_type == 0) {
                    if (dataProduct[j].fixed_price != null) {
                        dataProduct[j].netPrice = java.lang.Float.valueOf(dataProduct[j].fixed_price)
                    } else {
                        dataProduct[j].netPrice = 0f
                    }
                } else {
                    val qunat: Int = dataProduct[j].quantity
                    if (qunat == 0) {
                        dataProduct[j].netPrice = dataProduct[j].hourly_price.get(0).price_per_hour?: 0.0f
                    } else {
                        for (k in dataProduct[j].hourly_price.indices) {
                            if (qunat >= dataProduct[j].hourly_price.get(k).min_hour?: 0 && qunat <= dataProduct[j].hourly_price.get(k).max_hour?: 0) {
                                dataProduct[j].netPrice = dataProduct[j].hourly_price.get(k).price_per_hour?: 0.0f
                            }
                        }
                    }
                }
            }
        }
        if (list.size == 0) {
            recyclerview!!.visibility = View.GONE
            noData!!.visibility = View.VISIBLE
        } else {
            recyclerview!!.visibility = View.VISIBLE
            noData!!.visibility = View.GONE
            recyclerview!!.adapter = SearchAdapter(activity, list)
        }
    }

    private fun setTags(list: List<String>?) {
       // flLayout!!.removeAllViews()
        for (i in list!!.indices) {
            val view = LayoutInflater.from(activity).inflate(R.layout.item_tag_dummy, null)
            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            val ivDelete = view.findViewById<ImageView>(R.id.ivDelete)
            ivDelete.visibility = View.GONE
            tvTitle.text = list[i]
         //   flLayout!!.addView(view)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.multisercah)
        (activity as MainActivity?)!!.setIcons(true)
    }


}