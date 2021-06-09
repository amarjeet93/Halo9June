package com.codebrew.clikat.module.supplier_all


import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.FragmentAllSupplierBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.ExampleAllSupplier
import com.codebrew.clikat.modal.SupplierList
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.supplier_all.adapter.SupplierAllAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_all_supplier.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.nothing_found.*
import kotlinx.android.synthetic.main.toolbar_app.tb_back
import kotlinx.android.synthetic.main.toolbar_supplier.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject


/*
 * Created by Ankit Jindal on 20/4/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class SupplierAll : Fragment(), SupplierAllAdapter.SupplierListCallback {

    private var categoryId: Int = 0
    private var exampleAllSupplier: ExampleAllSupplier? = null
    private var mAdapter: SupplierAllAdapter? = null
    private var dataList: List<SupplierList> = ArrayList()
    private var isFilterApplied = false

    private var categoryFlow: ArrayList<String>? = null

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }
    private var clientInform: SettingModel.DataBean.SettingData? = null


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentAllSupplierBinding>(inflater, R.layout.fragment_all_supplier, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = textConfig
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvText.typeface = AppGlobal.semi_bold
        // barDialog = new ProgressBarDialog(getActivity());

        if (arguments != null) {
            categoryId = arguments?.getInt("categoryId", 0)!!
        }

        categoryFlow = ArrayList()

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val filteredModelList = filter(dataList, s.toString().trim())
                try {
                    mAdapter!!.animateTo(filteredModelList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (mAdapter != null && mAdapter!!.itemCount > 0) {
                    noData.visibility = View.GONE
                    recyclerview!!.visibility = View.VISIBLE
                    recyclerview.scrollToPosition(0)
                } else {
                    noData.visibility = View.VISIBLE
                    recyclerview!!.visibility = View.GONE
                }
            }
        })

        if (exampleAllSupplier != null) {

            progressBar.hide()
            noData.visibility = View.GONE
            recyclerview!!.visibility = View.VISIBLE


            dataList = exampleAllSupplier!!.data.supplierList
            val mLayoutManager = LinearLayoutManager(requireActivity().applicationContext)
            recyclerview.layoutManager = mLayoutManager
            mAdapter = SupplierAllAdapter(activity, exampleAllSupplier!!, categoryId)
            mAdapter!!.settingCallback(this)
            recyclerview.adapter = mAdapter
        } else {
            categoryId = requireArguments().getInt("categoryId", 0)

            //   barDialog.show();

            apiAllSupplier(categoryId)
        }

        tb_back.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        showBottomCart()
    }

    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData()

        if (screenFlowBean?.app_type == AppDataType.Ecom.type) {
            bottom_cart.visibility = View.GONE
        } else {
            bottom_cart.visibility = View.VISIBLE
        }


        if (appCartModel.cartAvail) {
            tv_total_price.text = String.format("%s %s %s", getString(R.string.total), AppConstants.CURRENCY_SYMBOL, appCartModel.totalPrice)

            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener { v ->

                val navOptions: NavOptions = if (screenFlowBean?.app_type == AppDataType.Food.type) {
                    NavOptions.Builder()
                            .setPopUpTo(R.id.resturantHomeFrag, false)
                            .build()
                } else {
                    NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, false)
                            .build()
                }


                navController(this@SupplierAll).navigate(R.id.action_supplierAll_to_cart, null, navOptions)

            }
        } else {
            bottom_cart.visibility = View.GONE
        }

    }


    override fun onResume() {
        super.onResume()

        if (!isFilterApplied) {
            Prefs.with(activity).remove(DataNames.FILTER)
            isFilterApplied = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)

        clientInform = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
    }


    private fun apiAllSupplier(categoryId: Int) {
        if (StaticFunction.isInternetConnected(activity)) {

            progressBar.show()

            val hashMap = dataManager.updateUserInf()


            if (arguments?.getInt("subCategoryId", 0) != 0) {
                hashMap["subCat"] = "" + arguments?.getInt("subCategoryId", 0)
            }

            if (categoryId != 0) {
                hashMap["categoryId"] = "" + categoryId
            }

            dataManager.getAllSuppliers(hashMap).enqueue(object : Callback<ExampleAllSupplier> {
                override fun onResponse(call: Call<ExampleAllSupplier>, response: Response<ExampleAllSupplier>) {

                    progressBar?.hide()
                    if (response.code() == 200) {
                        exampleAllSupplier = response.body()


                        if (exampleAllSupplier?.data?.supplierList?.isNotEmpty()==true) {
                            noData?.visibility = View.GONE
                            recyclerview?.visibility = View.VISIBLE

                            dataList = exampleAllSupplier?.data?.supplierList?: emptyList()
                            recyclerview?.layoutManager = LinearLayoutManager(activity)

                            Prefs.with(activity).save(DataNames.SUPPLIER_LIST, exampleAllSupplier)

                            mAdapter = SupplierAllAdapter(activity, exampleAllSupplier!!, categoryId)
                            recyclerview?.adapter = mAdapter
                            mAdapter?.settingCallback(this@SupplierAll)
                        } else {
                            noData.visibility = View.VISIBLE
                            recyclerview!!.visibility = View.GONE
                        }
                    }
                    // barDialog.dismiss();
                }

                override fun onFailure(call: Call<ExampleAllSupplier>, t: Throwable) {
                    progressBar?.hide()
                    noData?.visibility = View.VISIBLE
                    recyclerview?.visibility = View.GONE
                }

            })
        } else {
            StaticFunction.showNoInternetDialog(requireActivity())
        }
    }


    private fun filter(models: List<SupplierList>, query: String): List<SupplierList> {
        var query = query
        val filteredModelList = ArrayList<SupplierList>()
        if (query.isNotEmpty()) {
            query = query.toLowerCase()
            for (model in models) {
                val text: String
                text = model.name?.toLowerCase()?:""
                if (text.contains(query.toLowerCase())) {
                    filteredModelList.add(model)
                }
            }
        } else if (query.isEmpty()) {
            filteredModelList.addAll(models)
        }
        return filteredModelList
    }

    override fun onSupplierListDetail(position: Int, list: List<SupplierList>) {
        val supplierBean = list[position]

        val bundle = bundleOf("categoryId" to categoryId,
                "title" to supplierBean.name,
                "status" to supplierBean.status,
                "supplierId" to supplierBean.id,
                "branchId" to supplierBean.supplierBranchId)


        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            if(clientInform?.app_selected_template!=null
                    && clientInform?.app_selected_template=="1")
                navController(this@SupplierAll)
                        .navigate(R.id.action_resturantDetailNew, bundle)
            else
                navController(this@SupplierAll)
                        .navigate(R.id.action_resturantDetail, bundle)

        } else {
            bundle.putBoolean("has_subcat", true)
            bundle.putParcelableArrayList("question_list", arguments?.getParcelableArrayList("question_list"))
            bundle.putInt("cat_id", arguments?.getInt("subCatId", 0) ?: 0)
            navController(this@SupplierAll).navigate(R.id.action_productListing, bundle)
        }
        /*
                      "SubCategory" -> {
                          bundle.putString("title", list[position].name)
                          bundle.putStringArrayList("categoryFlow", categoryFlow)
                          bundle.putInt("categoryId", categoryId)
                          bundle.putInt("supplierId", list[position].id ?: 0)
                          navController(this@SupplierAll).navigate(R.id.action_supplierAll_to_subCategory, bundle)

                      }
      */
    }

}
