package com.codebrew.clikat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.modal.ExamplePackagesSupplier
import com.codebrew.clikat.modal.FilterFinalData
import com.codebrew.clikat.modal.ListPackagesSupplier
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.StaticFunction.isInternetConnected
import com.codebrew.clikat.utils.StaticFunction.showNoInternetDialog
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_packages.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
 * Created by cbl80 on 2/5/16.
 */
class PackageSupplierListing : Fragment() {
    private var locationUser: LocationUser? = null
    private var examplePackages: ExamplePackagesSupplier? = null
    private var isFilterApplied = false
  //  private var packageAdapter: PackageExpandableAdapter? = null
    private val textConfig = Configurations.strings
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_packages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noData!!.visibility = View.GONE
        (activity as MainActivity?)!!.tvTitleMain.text = getString(R.string.packages)
    }

    override fun onStart() {
        super.onStart()
        val bundle = arguments
        if (bundle != null) {
            if (bundle.containsKey("title")) {
                val title = bundle.getString("title", getString(R.string.select_supplier, textConfig.supplier))
                (activity as MainActivity?)!!.tvTitleMain.text = title
            }
        }
        locationUser = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
        if (!isFilterApplied) {
            if (examplePackages != null) {
                setData()
            } else {
                if (isInternetConnected(activity)) {
                    apiUpdateCartInfo()
                } else {
                    showNoInternetDialog(activity)
                }
            }
        }
        isFilterApplied = false
    }

    override fun onResume() {
        super.onResume()
        /*      if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }*/(activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
        (activity as MainActivity?)!!.setIcons(true)
    }

    private fun apiUpdateCartInfo() {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val hashMap = HashMap<String, String>()
        hashMap["languageId"] = "" + getLanguage(activity)
        val call = RestClient.getModalApiService(activity).packageCategory(hashMap)
        call.enqueue(object : Callback<ExamplePackagesSupplier?> {
            override fun onResponse(call: Call<ExamplePackagesSupplier?>, response: Response<ExamplePackagesSupplier?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    examplePackages = response.body()
                    Prefs.with(activity).save(DataNames.SUPPLIER_LIST_PACKAGE, examplePackages)
                    setData()
                }
            }

            override fun onFailure(call: Call<ExamplePackagesSupplier?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    private fun setData() {
        if (examplePackages!!.data.list.size == 0) {
            noData!!.visibility = View.VISIBLE
            recyclerview!!.visibility = View.GONE
        } else {
            recyclerview!!.layoutManager = LinearLayoutManager(activity)
          //  packageAdapter = PackageExpandableAdapter(activity, examplePackages!!.data.list)
          //  recyclerview!!.adapter = packageAdapter
            recyclerview!!.visibility = View.VISIBLE
            noData!!.visibility = View.GONE
        }
    }

    @Throws(CloneNotSupportedException::class)
    fun onEvent(filterFinalData: FilterFinalData?) {
        if (filterFinalData != null && examplePackages != null) {
            val exampleAllSupplier2 = Prefs.with(activity).getObject(DataNames.SUPPLIER_LIST_PACKAGE, ExamplePackagesSupplier::class.java)
            val lists: MutableList<ListPackagesSupplier> = ArrayList()
            if (filterFinalData.discoverable.size == 0) {
                lists.addAll(examplePackages!!.data.list)
            } else {
                for (i in filterFinalData.discoverable.indices) {
                    for (j in examplePackages!!.data.list.indices) {
                        if (examplePackages!!.data.list[j].status.toInt() == filterFinalData.discoverable[i]) {
                            lists.add(examplePackages!!.data.list[j])
                        }
                    }
                }
            }
            if (filterFinalData.delivery.size != 0) {
                val listsTemp: MutableList<ListPackagesSupplier> = ArrayList()
                listsTemp.addAll(lists)
                lists.clear()
                for (i in filterFinalData.delivery.indices) {
                    for (j in listsTemp.indices) {
                        if (listsTemp[j].paymentMethod.toInt() == filterFinalData.delivery[i]) {
                            lists.add(listsTemp[j])
                        }
                    }
                }
            }
            if (filterFinalData.suuplierType.size != 0) {
                val listsTemp: MutableList<ListPackagesSupplier> = ArrayList()
                listsTemp.addAll(lists)
                lists.clear()
                for (i in filterFinalData.suuplierType.indices) {
                    for (j in listsTemp.indices) {
                        if (listsTemp[j].commissionPackage == filterFinalData.suuplierType[i]) {
                            lists.add(listsTemp[j])
                        }
                    }
                }
            }
            if (filterFinalData.rating.size != 0) {
                val listsTemp: MutableList<ListPackagesSupplier> = ArrayList()
                listsTemp.addAll(lists)
                lists.clear()
                for (i in filterFinalData.rating.indices) {
                    for (j in listsTemp.indices) {
                        if (listsTemp[j].rating.toInt() == filterFinalData.rating[i]) {
                            lists.add(listsTemp[j])
                        }
                    }
                }
            }
            /*
            if (filterFinalData.minOrderTime!=0)
            {
                List<ListPackagesSupplier> listsTemp = new ArrayList<>();
                listsTemp.addAll(lists);
                lists.clear();
                for (int i=0;i<listsTemp.size();i++)
                {
                    if (listsTemp.get(i).getDeliveryMinTime() >=filterFinalData.minOrderTime)
                    {
                        lists.add(listsTemp.get(i));
                    }
                }
            }

            if (filterFinalData.minOrderAmount!=0)
            {
                List<ListPackagesSupplier> listsTemp = new ArrayList<>();
                listsTemp.addAll(lists);
                lists.clear();
                for (int i=0;i<listsTemp.size();i++)
                {
                    if (listsTemp.get(i).getMinOrder()>=filterFinalData.minOrderAmount)
                    {
                        lists.add(listsTemp.get(i));
                    }
                }
            }*/exampleAllSupplier2.data.list.clear()
            exampleAllSupplier2.data.list = lists
            isFilterApplied = true
            if (lists.size != 0) {
             //   packageAdapter!!.setdata(lists)
             //   packageAdapter = PackageExpandableAdapter(activity, lists)
              //  recyclerview!!.adapter = packageAdapter
                recyclerview!!.visibility = View.VISIBLE
                noData!!.visibility = View.GONE
            } else {
                recyclerview!!.visibility = View.GONE
                noData!!.visibility = View.VISIBLE
            }
        }
    }
}