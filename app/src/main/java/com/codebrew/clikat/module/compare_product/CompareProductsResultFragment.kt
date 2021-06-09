package com.codebrew.clikat.module.compare_product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.databinding.ActivityCompareProductsBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.CompareResultDetail
import com.codebrew.clikat.modal.PojoCompareResultSupplier
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.module.compare_product.adapter.CompareSupplierAdapter
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.ClikatConstants
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_compare_products.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

/*
 * Created by cbl80 on 8/7/16.
 */
class CompareProductsResultFragment : Fragment() {


    @Inject
    lateinit var dataManager: DataManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: ActivityCompareProductsBinding = DataBindingUtil.inflate(inflater, R.layout.activity_compare_products,
                container, false)
        binding.color = Configurations.colors
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        settypeface()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)!!.setIconsCart(true)
        (activity as MainActivity?)!!.tvTitleMain.setText(R.string.results)
        (activity as MainActivity?)!!.setSupplierImage(false, "", 0, "", 0, 0)
    }

    private fun initialize() {
        assert(arguments != null)
        val product: ProductDataBean = arguments?.getParcelable("product")?:ProductDataBean()
        recyclerview!!.layoutManager = LinearLayoutManager(activity)
        tvProductName!!.text = product.name
        loadImage(product.image_path.toString(), sdvImage, false)
        /*   Glide.with(this)
                .load(Uri.parse(product.getImage_path()))
                .apply(new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.placeholder_product)
                        .override(150, 150))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(sdvProduct);*/getsupplierList(product.sku?:"")
    }

    private fun getsupplierList(sku: String) {
        val barDialog = ProgressBarDialog(activity)
        barDialog.show()
        val hashMap = dataManager.updateUserInf()
        hashMap["skuCode"] = sku
        val supplierCall = RestClient.getModalApiService(activity).compareResults(hashMap)
        supplierCall.enqueue(object : Callback<PojoCompareResultSupplier?> {
            override fun onResponse(call: Call<PojoCompareResultSupplier?>, response: Response<PojoCompareResultSupplier?>) {
                barDialog.dismiss()
                if (response.code() == 200) {
                    val resultSupplier = response.body()
                    if (resultSupplier!!.status == ClikatConstants.STATUS_SUCCESS) {
                        setData(resultSupplier)
                    } else {
                        GeneralFunctions.showSnackBar(view, resultSupplier.message, activity)
                    }
                } else {
                    GeneralFunctions.showSnackBar(view, response.message(), activity)
                }
            }

            override fun onFailure(call: Call<PojoCompareResultSupplier?>, t: Throwable) {
                barDialog.dismiss()
            }
        })
    }

    private fun setData(resultSupplier: PojoCompareResultSupplier?) {
        val list = resultSupplier!!.data.details
        for (i in list.indices) {
            if (list[i].price_type == 1) {
                list[i].netPrice = list[i].hourly_price[0].price_per_hour?: 0.0f
            } else {
                if (list[i].price != "") list[i].netPrice = list[i].price.toFloat()
            }
        }
        list.sortWith(Comparator { o1: CompareResultDetail, o2: CompareResultDetail -> o1.netPrice.compareTo(o2.netPrice) })
        val supplierAdapter = CompareSupplierAdapter(activity, list)
        recyclerview!!.adapter = supplierAdapter
    }

    private fun settypeface() {
        tvProductName!!.typeface = AppGlobal.regular
    }
}