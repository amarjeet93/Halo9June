package com.codebrew.clikat.module.compareProduct

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.databinding.FragmentCompareProdBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.FilterVarientData
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SearchProductModel
import com.codebrew.clikat.module.searchProduct.adapter.SearchProductAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.EndlessRecyclerViewScrollListener
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.getLanguage
import com.codebrew.clikat.utils.configurations.Configurations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlinx.android.synthetic.main.fragment_compare_prod.*
/**
 * A simple [Fragment] subclass.
 */
class CompareProdFragment : Fragment() {

    private var listAllData: ArrayList<ProductDataBean>? = null
    private var paginationCount = 0
    private val productAdapter: SearchProductAdapter? = null
    private val timer: Timer? = null
    private var barDialog: ProgressBarDialog? = null
    private val layoutManager: LinearLayoutManager? = null
    private var isLoading = true
    var varientData: FilterVarientData? = null
    private val selectedColor = Color.parseColor(Configurations.colors.tabSelected)
    private val unselectedColor = Color.parseColor(Configurations.colors.tabUnSelected)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val binding: FragmentCompareProdBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_compare_prod, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAllData = ArrayList()
        searchView!!.typeface = AppGlobal.regular
        barDialog = ProgressBarDialog(activity)
        val layoutManager = LinearLayoutManager(activity)
        recyclerview!!.layoutManager = layoutManager
        if (arguments != null && arguments?.containsKey("compareSearch")==true) {
            val s = arguments?.getString("compareSearch")?:""
            searchView!!.setText(s)
            searchCompareProductApi(0)
        }
        searchCompareProductApi(0)
        recyclerview!!.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (isLoading) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        progressbAr!!.visibility = View.VISIBLE
                        paginationCount = listAllData!!.size
                        searchCompareProductApi(paginationCount + 1)
                    }
                }
            }

            override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(view, dx, dy)
                paginationCount = listAllData!!.size
            }
        })
        searchView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim().isNotEmpty()) {
                    if (s[s.length - 1] == '\n' || s[s.length - 1] == ' ') {
                        GeneralFunctions.hideKeyboard(searchView, activity)
                        searchCompareProductApi(0)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.compareProductIcons()
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.compareProduct)
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
    }

    private fun searchCompareProductApi(startValue: Int) { //barDialog.show();
        GeneralFunctions.hideKeyboard(searchView, activity)
        /*      if (startValue == 0)
            barDialog.show();*/
        val locationUser = Prefs.with(activity).getObject(DataNames.LocationUser, LocationUser::class.java)
        val hashMap = HashMap<String, String>()
        hashMap["productName"] = searchView!!.text.toString()
        hashMap["languageId"] = "" + getLanguage(activity)
        hashMap["startValue"] = "" + startValue
        val productCall = RestClient.getModalApiService(activity).compareProducts(hashMap)
        productCall.enqueue(object : Callback<SearchProductModel?> {
            override fun onResponse(call: Call<SearchProductModel?>, response: Response<SearchProductModel?>) { // barDialog.dismiss();
                progressbAr!!.visibility = View.GONE
                val compareProduct = response.body()
                listAllData!!.clear()
                if (compareProduct!!.status == ClikatConstants.STATUS_SUCCESS) {
                    isLoading = false
                    if (compareProduct.data.details.products.size == 0) {
                        noData!!.visibility = View.VISIBLE
                        ll!!.visibility = View.GONE
                    } else {
                        listAllData!!.addAll(compareProduct.data.details.products)
                        productAdapter!!.notifyDataSetChanged()
                        noData!!.visibility = View.GONE
                        ll!!.visibility = View.VISIBLE
                    }
                } else {
                    GeneralFunctions.showSnackBar(view, response.message(), activity)
                }
            }

            override fun onFailure(call: Call<SearchProductModel?>, t: Throwable) { //barDialog.dismiss();
            }
        })
    }

    companion object {
        private const val FILTERDATA = 788
    }
}