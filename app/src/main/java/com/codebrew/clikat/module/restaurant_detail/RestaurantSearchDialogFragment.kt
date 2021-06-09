package com.codebrew.clikat.module.restaurant_detail

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.databinding.FragmentRestaurantDetailNewBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter
import com.codebrew.clikat.module.restaurant_detail.adapter.SupplierProdListAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_restaurant_detail_new.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.search_dialog_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class RestaurantSearchDialogFragment : BaseFragment<FragmentRestaurantDetailNewBinding, RestDetailViewModel>(),
        ProdListAdapter.ProdCallback, DialogListener,
        RestDetailNavigator, AddonFragment.AddonCallback {


    private var parentPos: Int = 0
    private var childPos: Int = 0
    private var adapterSearch: SupplierProdListAdapter? = null

    private var productBeans = mutableListOf<ProductBean>()
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var prodlytManager: LinearLayoutManager? = null
    private var deliveryType = 0
    private var supplierId = 0
    private var isResutantOpen: Boolean = false
    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var mBinding: FragmentRestaurantDetailNewBinding? = null
    var settingBean: SettingModel.DataBean.SettingData? = null

    private lateinit var viewModel: RestDetailViewModel


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_restaurant_detail_new
    }

    override fun getViewModel(): RestDetailViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(RestDetailViewModel::class.java)
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        settingBean = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        restDetailObserver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding

        viewDataBinding.color = Configurations.colors
        viewDataBinding.drawables = Configurations.drawables
        viewDataBinding.strings = Configurations.strings

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainmenu?.foreground?.alpha = 0
        }
        initialise()
        getValues()
        settingLayout()
        setclikListener()
        showBottomCart()
    }

    private fun initialise() {
        layoutSearchView?.visibility = View.VISIBLE
    }

    private fun settingLayout() {

        productBeans.clear()

        adapterSearch = SupplierProdListAdapter(productBeans, this@RestaurantSearchDialogFragment,settingBean)
        rvSearch?.adapter = adapterSearch

        if (arguments != null) {
            if (arguments?.containsKey("deliveryType") == true) {
                deliveryType = arguments?.getInt("deliveryType")?:0
            }

            if (arguments?.containsKey("supplierId") == true) {
                supplierId = arguments?.getInt("supplierId") ?: 0
                getProductList(supplierId)
            }
        }

    }
    private fun getProductList(supplierId: Int) {
        if (viewModel.supplierLiveData.value == null) {
            if (isNetworkConnected)
                viewModel.getProductList(supplierId.toString())
        } else {
            refreshAdapter(viewModel.supplierLiveData.value)
        }

    }

    private fun setclikListener() {

        iv_close?.setOnClickListener {
            hideKeyboard()
            navController(this@RestaurantSearchDialogFragment).popBackStack()
        }

        etSearch?.afterTextChanged {
            if (it.isNotEmpty()) {
                adapterSearch?.filter?.filter(it.toLowerCase())
            } else {
                adapterSearch?.filter?.filter("")
            }
        }
    }

    private fun restDetailObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataBean> { resource ->
            /*productBeans.clear()*/
            isResutantOpen = checkResturntTiming(resource.supplier_detail?.timing)
           refreshAdapter(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.supplierLiveData.observe(this, catObserver)
    }

    private fun refreshAdapter(data: DataBean?) {
        viewDataBinding.supplierModel = data?.supplier_detail

        if (data?.product?.isNotEmpty() == true) {
            productBeans.clear()
            productBeans.addAll(data.product ?: emptyList())
            for (i in productBeans.indices) {
                changeProductList(productBeans[i].value!!)
            }
            adapterSearch?.notifyDataSetChanged()
        }
    }

    private fun changeProductList(product: List<ProductDataBean>) {

        for (j in product.indices) {
            val dataProduct = product[j]
            val productId = dataProduct.product_id

            product[j].prod_quantity = StaticFunction.getCartQuantity(requireActivity(), productId)

            dataProduct.netPrice = if (dataProduct.fixed_price?.toFloatOrNull() ?: 0.0f > 0) dataProduct.fixed_price?.toFloatOrNull() else 0f
        }


    }
    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData()

        if (screenFlowBean?.app_type == AppDataType.Ecom.type) {
            bottom_cart?.visibility = View.GONE
        } else {
            bottom_cart?.visibility = View.VISIBLE
        }

        if (appCartModel.cartAvail) {

            tv_total_price.text = getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)

            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener {
                val navOptions: NavOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.restaurantDetailFragNew, false)
                        .build()

                navController(this@RestaurantSearchDialogFragment).navigate(R.id.action_restaurantDetailFrag_to_cart, null, navOptions)
            }
        } else {
            bottom_cart.visibility = View.GONE
        }

    }

    private fun getValues() {
        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
    }


    override fun onProdAdded(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean) {

        if (isOpen) {
            mDialogsUtil.openAlertDialog(activity
                    ?: requireContext(), getString(R.string.offline_supplier_tag,Configurations.strings.suppliers), getString(R.string.ok),"",this)
            return
        }

        this.parentPos = parentPosition
        this.childPos = childPosition

        if (deliveryType == FoodAppType.Pickup.foodType) {
            if (viewModel.supplierLiveData.value == null) return

            val mRestUser = LocationUser()
            mRestUser.address = "${viewModel.supplierLiveData.value?.supplier_detail?.name} , ${viewModel.supplierLiveData.value?.supplier_detail?.address}"
            dataManager.addGsonValue(PrefenceConstants.RESTAURANT_INF, Gson().toJson(mRestUser))
        }

        if (screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            if (cartList != null && cartList.cartInfos?.isNotEmpty() == true) {
                if (cartList.cartInfos?.any { it.deliveryType != deliveryType } == true) {
                    appUtils.clearCart()
                    refreshAdapter(viewModel.supplierLiveData.value)
                }
            }
        }

        if (productBean?.adds_on?.isNotEmpty() == true) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            if (appUtils.checkProdExistance(productBean.product_id)) {
                val savedProduct = cartList?.cartInfos?.filter {
                    it.supplierId == supplierId && it.productId == productBean.product_id
                } ?: emptyList()

                SavedAddon.newInstance(productBean, deliveryType, savedProduct, this).show(childFragmentManager, "savedAddon")
            } else {
                AddonFragment.newInstance(productBean, deliveryType, this).show(childFragmentManager, "addOn")
            }

        } else {
            if (bookingFlowBean?.vendor_status == 0 && appUtils.checkVendorStatus(productBean?.supplier_id)) {
                mDialogsUtil.openAlertDialog(activity
                        ?: requireContext(), getString(R.string.clearCart, Configurations.strings.supplier?: ""), "Yes", "No", this)
            } else {
                if (appUtils.checkBookingFlow(requireContext(), productBean?.product_id, this)) {
                    addCart(productBean)
                }
            }
            showBottomCart()
        }
    }


    override fun onProdDelete(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean) {
        this.parentPos = parentPosition
        this.childPos = childPosition

        if (productBean?.adds_on?.isNotEmpty() == true) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            val savedProduct = cartList?.cartInfos?.filter {
                it.supplierId == supplierId && it.productId == productBean.product_id
            } ?: emptyList()

            SavedAddon.newInstance(productBean, deliveryType, savedProduct, this).show(childFragmentManager, "savedAddon")

        } else {

            var quantity = productBean?.prod_quantity ?: 0

            if (quantity > 0) {
                quantity--
                productBean?.prod_quantity = quantity

                if (quantity == 0) {
                    StaticFunction.removeFromCart(activity, productBean?.product_id, 0)

                } else {
                    StaticFunction.updateCart(activity, productBean?.product_id, quantity, productBean?.price?.toFloat()
                            ?: 0.0f)
                }
                productBean?.let { productBeans[parentPosition].value?.set(childPosition, it) }
                if (adapterSearch != null) {
                    adapterSearch?.notifyDataSetChanged()
                }

            }

            showBottomCart()
        }

    }

    override fun onProdDetail(productBean: ProductDataBean?) {
        navController(this).navigate(R.id.action_search_to_product_details,
                ProductDetails.newInstance(productBean, 0, false))
    }

    override fun onDescExpand(tvDesc: TextView?, productBean: ProductDataBean?, childPosition: Int) {
        CommonUtils.expandTextView(tvDesc)
    }

    private fun addCart(productModel: ProductDataBean?) {

        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            if (cartList != null && cartList.cartInfos?.size!! > 0) {
                if (cartList.cartInfos?.any { cartList.cartInfos?.get(0)?.deliveryType != it.deliveryType } == true) {
                    appUtils.clearCart()

                    mBinding?.root?.onSnackbar("Cart Clear Successfully")
                }
            }
        }

        if (productModel?.prod_quantity == 0) {

            productModel.prod_quantity = 1

            productModel.self_pickup = deliveryType

            appUtils.addProductDb(activity ?: requireContext(), screenFlowBean?.app_type
                    ?: 0, productModel)


        } else {
            var quantity = productModel?.prod_quantity ?: 0

            quantity++

            val remaingProd = productModel?.quantity?.minus(productModel.purchased_quantity ?: 0)
                    ?: 0

            if (quantity <= remaingProd) {
                productModel?.prod_quantity = quantity

                StaticFunction.updateCart(activity, productModel?.product_id, quantity, productModel?.netPrice
                        ?: 0.0f)
            } else {
                mBinding?.root?.onSnackbar(getString(R.string.maximum_limit_cart))
            }
        }


        productBeans.mapIndexed { index, productBean ->

            if (productBean.sub_cat_name == productModel?.sub_category_name) {
                productModel?.let {
                    productBean.value?.set(productBean.value?.indexOf(productModel) ?: 0, it)
                }
            }

        }




        if (adapterSearch != null) {
            adapterSearch?.notifyDataSetChanged()
        }

    }

    override fun onErrorListener() {

    }

    private fun clearCart() {

        productBeans.map {
            it.value?.map {
                it.prod_quantity = 0
            }
        }

        /*     for (i in productBeans.indices) {

                 productBeans[i].value?.mapIndexed { index, valueBean ->
                     productBeans[i].value?.get(index)?.prod_quantity = 0
                 }
             }*/


        if (adapterSearch != null)
            adapterSearch?.notifyDataSetChanged()

        showBottomCart()
    }

    override fun onSucessListner() {

        if (isResutantOpen) {
            appUtils.clearCart()
            clearCart()
        }
    }

    override fun onAddonAdded(productModel: ProductDataBean) {

        if (productModel.adds_on?.isNullOrEmpty() == false && appUtils.checkProdExistance(productModel.product_id)) {
            val cartList: CartList? = appUtils.getCartList()
            productModel.prod_quantity = cartList?.cartInfos?.filter { productModel.product_id == it.productId }?.sumBy { it.quantity }
                    ?: 0
        }

        showBottomCart()


        productBeans.mapIndexed { index, productBean ->

            if (productBean.sub_cat_name == productModel.sub_category_name) {
                productModel.let {
                    productBean.value?.set(productBean.value?.indexOf(productModel) ?: 0, it)
                }
            }

        }

        if (adapterSearch != null) {
            adapterSearch?.notifyDataSetChanged()
        }
    }

    override fun favResponse() {

    }

    override fun unFavResponse() {

    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    private fun checkResturntTiming(timing: List<TimeDataBean>?): Boolean {
        // val calendar = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        val currentDate = Calendar.getInstance()

        var isCheckStatus = false

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timing?.forEachIndexed { index, timeDataBean ->
            if (timeDataBean.week_id == (appUtils.getDayId(currentDate.get(Calendar.DAY_OF_WEEK))
                            ?: "-1").toInt()) {
                isCheckStatus = timeDataBean.is_open == 1

                startDate.time = sdf.parse(timeDataBean.start_time ?: "")!!
                startDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR))
                startDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH))
                startDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH))

                endDate.time = sdf.parse(timeDataBean.end_time ?: "")!!
                endDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR))
                endDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH))
                endDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH))
            }
        }

        return if (isCheckStatus) {
            currentDate.time.after(startDate.time) && currentDate.time.before(endDate.time)
        } else {
            isCheckStatus
        }
    }

}
