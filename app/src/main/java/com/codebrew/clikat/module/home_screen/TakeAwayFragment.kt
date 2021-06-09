package com.codebrew.clikat.module.home_screen


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.*
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DeliveryType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.AppConstants.Companion.DELIVERY_OPTIONS
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.network.HostSelectionInterceptor
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentEcommerceBinding
import com.codebrew.clikat.databinding.PopupRestaurantMenuBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.PojoPendingOrders
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.filter.BottomSheetFragment
import com.codebrew.clikat.module.home_screen.adapter.*
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.restaurant_detail.adapter.MenuCategoryAdapter
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_ecommerce.*
import kotlinx.android.synthetic.main.item_search_view.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.toolbar_home.*
import kotlinx.android.synthetic.main.toolbar_home.view.*
import retrofit2.Retrofit
import javax.inject.Inject
import kotlin.String as String1

/**
 * A simple [Fragment] subclass.
 */


class TakeAwayFragment : BaseFragment<FragmentEcommerceBinding, HomeViewModel>(),
        SpecialListAdapter.OnProductDetail, CategoryListAdapter.CategoryDetail,
        SponsorListAdapter.SponsorDetail, DialogListener, BannerListAdapter.BannerCallback,
        HomeItemAdapter.SupplierListCallback, SwipeRefreshLayout.OnRefreshListener,
        BrandsListAdapter.BrandCallback, HomeNavigator, View.OnClickListener,
        AddressDialogFragment.Listener, ProdListAdapter.ProdCallback, MenuCategoryAdapter.MenuCategoryCallback,
        View.OnAttachStateChangeListener, AddonFragment.AddonCallback {


    private lateinit var viewModel: HomeViewModel

    private var mHomeScreenBinding: FragmentEcommerceBinding? = null

    private var catId = 0

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var interceptor: HostSelectionInterceptor


    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prodUtils: ProdUtils


    private var parentPos: Int = 0
    private var childPos: Int = 0

    private var speProductId = 0
    private var prodStatus = 0

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    // val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_ecommerce
    }

    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        return viewModel
    }


    private var specialOffers: MutableList<ProductDataBean>? = null
    private var mSupplierList: MutableList<SupplierDataBean>? = null
    private var mPopularList: MutableList<ProductDataBean>? = null

    private var homeItemAdapter: HomeItemAdapter? = null

    private var specialListAdapter: SpecialListAdapter? = null

    private var homelytManager: LinearLayoutManager? = null

    private var categoryCount = 0

    private var specialCount = 0

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    private var clientInform: SettingModel.DataBean.SettingData? = null

    private var categoryModel: Data? = null

    private var offerListModel: OfferDataBean? = null

    private var popup: PopupWindow? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this

        catId = if (prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
        } else {
            0
        }
        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        clientInform = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        categoryObserver()
        productObserver()
        offersObserver()
        supplierObserver()
        popularObserver()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mHomeScreenBinding = viewDataBinding

        viewDataBinding.color = Configurations.colors
        viewDataBinding.drawables = Configurations.drawables
        viewDataBinding.strings = textConfig

        if (screenFlowBean?.is_single_vendor == VendorAppType.Single.appType && screenFlowBean?.app_type == AppDataType.Food.type) {
            lyt_search.visibility = View.VISIBLE
            btn_menu.visibility = View.VISIBLE
            space.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                rv_homeItem.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                    if (scrollY < oldScrollY) {
                        btn_menu.hide()
                    } else {
                        btn_menu.show()
                    }
                }
            }

        } else {
            lyt_search.visibility = View.GONE
            btn_menu.visibility = View.GONE
            space.visibility = View.GONE
        }

        StaticFunction.removeAllCartLaundry(requireActivity())
        StaticFunction.clearCartLaundry(activity)
        if (prefHelper.getCurrentUserLoggedIn()) {
            try {
                val allCategories = prefHelper.getGsonValue(DataNames.ORDERS_COUNT, PojoPendingOrders::class.java)
                if (allCategories != null && allCategories.data != null) {
                    if (allCategories.data.pendingOrder == 1 && prefHelper.getKeyValue(DataNames.IS_DIALOG, PrefenceConstants.TYPE_INT) == 0) {
                        //sweetDialogueFailure(activity, getString(R.string.rate_order), getString(R.string.rate_order_title, textConfig.order), true)

                        //rate order screen

                        prefHelper.setkeyValue(DataNames.IS_DIALOG, 1)
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

        if (screenFlowBean?.app_type == AppDataType.Food.type && bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType &&
                screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
            toolbar_layout.visibility = View.GONE
        }



        settingLayout()
        if (screenFlowBean?.app_type == AppDataType.Ecom.type) {
            fetchPopular()
        }

        fetchCategories()

        // showBottomCart()

        settingToolbar()

        iv_search.setOnClickListener {
            navController(this@TakeAwayFragment)
                    .navigate(R.id.action_homeFragment_to_searchFragment)
        }

        btn_menu.setOnClickListener {
            displayPopupWindow(btn_menu)
        }

        checkSingleVendor()


    }

    private fun fetchPopular() {

        if (viewModel.popularLiveData.value != null) {
            popularOffer(viewModel.popularLiveData.value)
        } else {
            viewModel.getPopularProduct(catId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.compositeDisposable.clear()
    }

    private fun checkSingleVendor() {

        if (screenFlowBean?.app_type == AppDataType.Food.type && screenFlowBean?.is_single_vendor == VendorAppType.Single.appType) {
            group_deliver_option.visibility = View.VISIBLE

            if (DELIVERY_OPTIONS == DeliveryType.PickupOrder.type) {
                group_deliver_option.check(R.id.rb_pickup)
                changePickAdrs()
            } else {
                group_deliver_option.check(R.id.rb_delivery)
                location_txt.text = getString(R.string.location)
                settingToolbar()
            }

            if (prefHelper.getKeyValue(PrefenceConstants.SELF_PICKUP, PrefenceConstants.TYPE_STRING).toString() == FoodAppType.Both.foodType.toString()) {
                group_deliver_option.visibility = View.VISIBLE
            } else {
                group_deliver_option.visibility = View.GONE
            }

            iv_search.visibility = View.GONE

            rb_pickup.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    rb_delivery.isChecked = false
                    changePickAdrs()
                    DELIVERY_OPTIONS = DeliveryType.PickupOrder.type
                    StaticFunction.clearCart(activity)
                    checkSavedProd()
                    showBottomCart()
                }
            }

            rb_delivery.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    rb_pickup.isChecked = false
                    rb_delivery.isChecked = true
                    DELIVERY_OPTIONS = DeliveryType.DeliveryOrder.type
                    AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
                }
            }

        } else {
            iv_search.visibility = View.VISIBLE
            group_deliver_option.visibility = View.GONE
        }


        //if (screenFlowBean?.is_single_vendor == VendorAppType.Single.appType) {

        if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
            iv_supplier_logo.setImageResource(R.drawable.ic_back_home)

            iv_supplier_logo.setOnClickListener {
                navController(this@TakeAwayFragment).navigate(R.id.action_homeFragment_to_mainFragment)
            }
        } else {
            if (clientInform?.app_selected_template == null || clientInform?.app_selected_template == "0") {
                iv_supplier_logo.visibility = View.VISIBLE
                iv_supplier_logo.loadUserImage(clientInform?.logo_url ?: "")
            } else {
                iv_supplier_logo.visibility = View.GONE
            }
        }

    }

    private fun changePickAdrs() {
        location_txt.text = getString(R.string.pickup_from)
        val locUser = prefHelper.getGsonValue(PrefenceConstants.RESTAURANT_INF, LocationUser::class.java)
        updateToolbar(locUser?.address ?: "")
    }

    private fun settingToolbar() {

        val mLocUser = prefHelper.getGsonValue(DataNames.LocationUser, LocationUser::class.java)
                ?: return

        updateToolbar(mLocation = mLocUser.address ?: "")
    }


    private fun updateToolbar(mLocation: String1) {
        // tvArea.text = mLocation
        tvArea.visibility = View.INVISIBLE

        location_txt?.visibility = View.INVISIBLE
    }


    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData()


        if (appCartModel.cartAvail) {

            bottom_cart.visibility = View.VISIBLE

            tv_total_price.text = getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)
            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount)

            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
                tv_supplier_name.visibility = View.VISIBLE
            }


            bottom_cart.setOnClickListener {
                val navOptions: NavOptions = if (screenFlowBean?.app_type == AppDataType.Food.type
                        && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                    NavOptions.Builder()
                            //.setPopUpTo(R.id.resturantHomeFrag, false)
                            .build()
                } else {
                    NavOptions.Builder()
                            //.setPopUpTo(R.id.homeFragment, false)
                            .build()
                }

                if (bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType &&
                        screenFlowBean?.app_type == AppDataType.Food.type
                        && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                    navController(this@TakeAwayFragment).navigate(R.id.action_resturantHomeFrag_to_cart, null, navOptions)
                } else {
                    navController(this@TakeAwayFragment).navigate(R.id.action_homeFragment_to_cart, null, navOptions)
                }

            }
        } else {
            bottom_cart.visibility = View.GONE
        }


    }

    private fun fetchCategories() {
        if (isNetworkConnected) {
            categoryCount = 0
            specialCount = 0

            if (viewModel.homeDataLiveData.value != null) {
                updateCategoryData(viewModel.homeDataLiveData.value)
            } else {
                CommonUtils.checkAppDBKey(prefHelper.getKeyValue(
                        PrefenceConstants.DB_SECRET,
                        PrefenceConstants.TYPE_STRING)!!.toString(), interceptor)
                viewModel.getCategories()
            }
        }
    }


    private fun fetchOffers() {
        if (isNetworkConnected) {
            viewModel.getOfferList(catId)
        }
    }

    private fun fetchSupplierList() {
        if (isNetworkConnected) {
            viewModel.getSupplierList("2")
//            if (screenFlowBean?.app_type == AppDataType.Food.type
//                    && bookingFlowBean?.is_pickup_order == FoodAppType.Pickup.foodType) {
//                viewModel.getSupplierList("1")
//            } else {
//                viewModel.getSupplierList("0")
//            }
        }
    }

    private fun fetchProductList() {
        if (isNetworkConnected) {
            if (prefHelper.getKeyValue(PrefenceConstants.GENRIC_SUPPLIERID, PrefenceConstants.TYPE_INT) != 0)
                viewModel.getProductList(prefHelper.getKeyValue(PrefenceConstants.GENRIC_SUPPLIERID, PrefenceConstants.TYPE_INT).toString())
        }
    }


    private fun categoryObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<Data> { resource ->
            updateCategoryData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.homeDataLiveData.observe(this, catObserver)
    }

    private fun productObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataBean> { resource ->

            setViewType(HomeItemAdapter.SINGLE_PROD_TYPE, 0)
            homeItemAdapter?.notifyDataSetChanged()
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.productLiveData.observe(this, catObserver)
    }

    private fun updateCategoryData(resource: Data?) {

        categoryModel = resource

        categoryCount = 0
        specialCount = 0

        if (viewModel.offersLiveData.value != null) {
            updateOfferData(viewModel.offersLiveData.value)
        } else {
            fetchOffers()
        }

    }


    private fun supplierObserver() {
        // Create the observer which updates the UI.
        val supplierObserver = Observer<List<SupplierDataBean>> { resource ->
            updateSupplierData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.supplierLiveData.observe(this, supplierObserver)

    }

    private fun updateSupplierData(resource: List<SupplierDataBean>?) {

        if (resource?.isNotEmpty() == true) {

            if (resource.size >= 3) {
                for (i in resource.indices) {

                    when (i) {
                        0 -> {
                            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
                            // setViewType(HomeItemAdapter.SEARCH_TYPE, 0)
                            if (AppConstants.APP_SUB_TYPE < AppDataType.Custom.type) {
                                setViewType(HomeItemAdapter.CATEGORY_TYPE, 0)
                            }
                            setViewType(HomeItemAdapter.FILTER_TYPE, resource.size)
                        }
                        1 -> setViewType(HomeItemAdapter.RECOMEND_TYPE, 0)
                        2 -> {
                            setViewType(HomeItemAdapter.SPL_PROD_TYPE, 0)
                        }
                    }
                    resource[i].viewType = HomeItemAdapter.SUPL_TYPE
                    mSupplierList?.add(resource[i])

                }
            } else {
                setViewType(HomeItemAdapter.BANNER_TYPE, 0)
                // setViewType(HomeItemAdapter.SEARCH_TYPE, 0)
                if (AppConstants.APP_SUB_TYPE < AppDataType.Custom.type) {
                    setViewType(HomeItemAdapter.CATEGORY_TYPE, 0)
                }
                setViewType(HomeItemAdapter.FILTER_TYPE, resource.size)

                resource.mapIndexed { _, dataBean ->
                    dataBean.viewType = HomeItemAdapter.SUPL_TYPE
                }
                mSupplierList?.addAll(resource)
                setViewType(HomeItemAdapter.RECOMEND_TYPE, 0)
                setViewType(HomeItemAdapter.SPL_PROD_TYPE, 0)
            }
        } else {
            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
            //   setViewType(HomeItemAdapter.SEARCH_TYPE, 0)
            if (AppConstants.APP_SUB_TYPE < AppDataType.Custom.type) {
                setViewType(HomeItemAdapter.CATEGORY_TYPE, 0)
            }
        }

        homeItemAdapter?.notifyDataSetChanged()
    }


    private fun offersObserver() {
        // Create the observer which updates the UI.
        val offerObserver = Observer<OfferDataBean> { resource ->
            updateOfferData(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.offersLiveData.observe(this, offerObserver)

    }


    private fun popularObserver() {

        // Create the observer which updates the UI.
        val offerObserver = Observer<List<ProductDataBean>> { resource ->
            popularOffer(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.popularLiveData.observe(this, offerObserver)
    }

    private fun popularOffer(resource: List<ProductDataBean>?) {
        mPopularList?.clear()

        mPopularList?.addAll(resource ?: emptyList())
        mPopularList?.map {

            it.prod_quantity = StaticFunction.getCartQuantity(activity
                    ?: requireContext(), it.product_id)
            it.netPrice = it.fixed_price?.toFloatOrNull() ?: 0f

            it.let {
                prodUtils.changeProductList(it)
            }
        }
    }

    override fun onTableAdded(table_name: kotlin.String) {

    }
    private fun updateOfferData(resource: OfferDataBean?) {

        offerListModel = resource

        mSupplierList?.clear()
        if (resource?.offerEnglish?.isNotEmpty() == true) {

            specialOffers?.clear()

            resource.offerEnglish?.let { specialOffers?.addAll(it) }

            // if (screenFlowBean?.app_type == AppDataType.Food.type) {

            specialOffers?.map {

                it.prod_quantity = StaticFunction.getCartQuantity(activity
                        ?: requireContext(), it.product_id)
                it.netPrice = it.fixed_price?.toFloatOrNull() ?: 0f

                it.let {
                    prodUtils.changeProductList(it)
                }
            }
            //}
        }


        if (screenFlowBean?.app_type == AppDataType.Food.type) {


            if (screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                if (viewModel.supplierLiveData.value != null) {
                    updateSupplierData(viewModel.supplierLiveData.value)
                } else {
                    fetchSupplierList()
                }
            } else {
                setViewType(HomeItemAdapter.BANNER_TYPE, 0)
                // setViewType(HomeItemAdapter.SEARCH_TYPE, 0)
                setViewType(HomeItemAdapter.SPL_PROD_TYPE, 0)

                if (viewModel.productLiveData.value != null) {
                    setViewType(HomeItemAdapter.SINGLE_PROD_TYPE, 0)
                    homeItemAdapter?.notifyDataSetChanged()
                } else {
                    fetchProductList()
                }
            }


        } else if (screenFlowBean?.app_type == AppDataType.HomeServ.type) {

            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
            setViewType(HomeItemAdapter.FILTER_TYPE, 0)
            if (AppConstants.APP_SUB_TYPE < AppDataType.Custom.type) {
                setViewType(HomeItemAdapter.CATEGORY_TYPE, 0)
            }
            setViewType(HomeItemAdapter.SPL_PROD_TYPE, 0)

            if (screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                setViewType(HomeItemAdapter.RECOMEND_TYPE, 0)
            }
            setViewType(HomeItemAdapter.POPULAR_TYPE, 0)

        } else {
            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
            //setViewType(HomeItemAdapter.FILTER_TYPE, 0)
            setViewType(HomeItemAdapter.SPL_PROD_TYPE, 0)
            setViewType(HomeItemAdapter.BRAND_TYPE, 0)
            setViewType(HomeItemAdapter.RECOMEND_TYPE, 0)
            setViewType(HomeItemAdapter.POPULAR_TYPE, 0)

            homeItemAdapter?.notifyDataSetChanged()

        }

    }


    override fun onResume() {
        super.onResume()

        checkSavedProd()

        showBottomCart()
    }

    private fun checkSavedProd() {
        if (specialOffers != null && specialOffers?.isNotEmpty() == true) {

            specialOffers?.map {
                it.prod_quantity = StaticFunction.getCartQuantity(activity
                        ?: requireContext(), it.product_id)
                if (it.price_type != 1) {
                    it.netPrice = it.fixed_price?.toFloatOrNull() ?: 0f
                }
            }
            homeItemAdapter?.notifyDataSetChanged()
        }

        //Update Product Quantity for Single Vendor List
        if (viewModel.productLiveData.value != null && viewModel.productLiveData.value?.product?.isNotEmpty() == true) {
            mSupplierList?.map { supplierData ->
                if (supplierData.viewType == HomeItemAdapter.SINGLE_PROD_TYPE) {
                    supplierData.itemModel?.vendorProdList?.value?.map {
                        it.prod_quantity = StaticFunction.getCartQuantity(activity
                                ?: requireContext(), it.product_id)
                    }
                }
            }
            homeItemAdapter?.notifyDataSetChanged()
        }
    }


    private fun setViewType(viewType: String1, supplierCount: Int) {
        val dataBean = SupplierDataBean()

        val itemModel = HomeItemModel()
        itemModel.screenType = screenFlowBean?.app_type ?: -1
        itemModel.isSingleVendor = screenFlowBean?.is_single_vendor ?: -1

        dataBean.viewType = viewType

        if (viewType == HomeItemAdapter.BANNER_TYPE) {
            val bannerList = ArrayList<TopBanner>()

            if (categoryModel?.topBanner?.isNotEmpty() == true) {
                bannerList.addAll(categoryModel?.topBanner ?: emptyList())
            } else if (!clientInform?.banner_url.isNullOrEmpty()) {
                val bannerBean = TopBanner()
                bannerBean.isEnabled = false
                bannerBean.phone_image = clientInform?.banner_url ?: ""
                bannerList.add(bannerBean)
            }
            itemModel.bannerWidth = clientInform?.app_banner_width?.toInt() ?: 0

            //  if (bannerList.size() > 0) {
            itemModel.bannerList = if (AppConstants.APP_SUB_TYPE < AppDataType.Custom.type) bannerList else bannerList.filter { it.category_id == catId }

            dataBean.itemModel = itemModel
            mSupplierList?.add(dataBean)
            //}
        } else if (viewType == HomeItemAdapter.CATEGORY_TYPE) {
            val mainCat = categoryModel?.english

            if (mainCat?.isNotEmpty() == true) {

                val beanList = ArrayList<English>()

                if (screenFlowBean?.app_type == AppDataType.Beauty.type) {

                    if (categoryCount == 0 && mainCat.size > 2) {
                        beanList.add(mainCat[0])
                        beanList.add(mainCat[1])

                        categoryCount = 2
                    } else if (categoryCount == 2 && mainCat.size >= 4) {
                        beanList.add(mainCat[2])
                        beanList.add(mainCat[3])

                        categoryCount = 4
                    } else {
                        if (mainCat.size >= categoryCount) {
                            for (i in categoryCount until mainCat.size) {

                                beanList.add(mainCat[categoryCount])
                                categoryCount++
                            }
                        }
                    }

                } else {
                    beanList.addAll(mainCat)
                }

                // if (beanList.size() > 0) {
                itemModel.categoryList = beanList
                //}

                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)
            }
        } else if (viewType == HomeItemAdapter.FILTER_TYPE) {

            if (supplierCount > 0 || screenFlowBean?.app_type == AppDataType.Ecom.type) {
                itemModel.supplierCount = supplierCount

                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)

            }
        } else if (viewType == HomeItemAdapter.RECOMEND_TYPE) {

            if (!offerListModel?.supplierInArabic.isNullOrEmpty()) {
                itemModel.sponserList = offerListModel?.supplierInArabic

                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)
            }
        } else if (viewType == HomeItemAdapter.POPULAR_TYPE) {
            if (mPopularList?.isNotEmpty() == true) {
                itemModel.popularProdList = mPopularList
                itemModel.mSpecialType = 1
                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)
            }
        } else if (viewType == HomeItemAdapter.SINGLE_PROD_TYPE) {
            if (viewModel.productLiveData.value != null && viewModel.productLiveData.value?.product?.isNotEmpty() == true
                    && mSupplierList?.none { it.viewType == HomeItemAdapter.SINGLE_PROD_TYPE } == true) {
                viewModel.productLiveData.value?.product?.forEachIndexed { index, productBean ->

                    productBean.value?.map {
                        it.netPrice = it.fixed_price?.toFloatOrNull() ?: 0f
                        it.prod_quantity = StaticFunction.getCartQuantity(activity
                                ?: requireContext(), it.product_id)
                    }

                    val model = HomeItemModel()
                    model.vendorProdList = productBean
                    model.screenType = screenFlowBean?.app_type ?: -1
                    dataBean.itemModel = model
                    mSupplierList?.add(dataBean.copy())
                }

            }
        } else if (viewType == HomeItemAdapter.SPL_PROD_TYPE) {

            if (specialOffers?.isEmpty() == true) return

            val beanList = ArrayList<ProductDataBean>()

/*            if (screenFlowBean!!.app_type == AppDataType.Ecom.type) {

                itemModel.mSpecialType = 0

                if (specialCount == 0 && specialOffers!!.size > 1) {
                    beanList.add(specialOffers!![0])
                    beanList.add(specialOffers!![1])
                    specialCount = 2
                } else {
                    for (i in specialCount until specialOffers!!.size) {

                        beanList.add(specialOffers!![i])
                        specialCount++
                    }
                }

                itemModel.specialOffers = beanList
            } else {*/
            beanList.addAll(specialOffers ?: emptyList())

            val updatedList = beanList.map {
                prodUtils.changeProductList(it)
            }
            itemModel.mSpecialType = 1
            itemModel.specialOffers = updatedList.toMutableList()
            //}

            if (beanList.isNotEmpty()) {
                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)
            }
        } else if (viewType == HomeItemAdapter.BRAND_TYPE) {

            if (!categoryModel?.brands.isNullOrEmpty()) {
                itemModel.brandsList = categoryModel?.brands

                dataBean.itemModel = itemModel
                mSupplierList?.add(dataBean)
            }
        } else {
            dataBean.itemModel = itemModel
            mSupplierList?.add(dataBean)
        }

    }


    private fun settingLayout() {
        specialOffers = ArrayList()
        mSupplierList = ArrayList()
        mPopularList = ArrayList()


        homeItemAdapter = HomeItemAdapter(mSupplierList, bookingFlowBean?.is_pickup_order
                ?: 1, appUtils, clientInform,"1")
        homeItemAdapter?.setFragCallback(this)
        homeItemAdapter?.settingCallback(this)


        homelytManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        rv_homeItem.layoutManager = homelytManager
        rv_homeItem.adapter = homeItemAdapter

        rv_homeItem.setHasFixedSize(true)

        swiprRefresh.setOnRefreshListener(this)
        tvArea.setOnClickListener(this)


        ed_search.afterTextChanged {
            if (it.isEmpty()) {
                homeItemAdapter?.filter?.filter("")
                homelytManager?.scrollToPositionWithOffset(0, mSupplierList?.size ?: 0)
            } else {
                homelytManager?.scrollToPositionWithOffset(2, mSupplierList?.size ?: 0)
                homeItemAdapter?.filter?.filter(it)
            }
        }
        val isUserLoggedIn = prefHelper.getCurrentUserLoggedIn()

        iv_notification.setOnClickListener {
            if (isUserLoggedIn)
                navController(this@TakeAwayFragment).navigate(R.id.action_home_to_notificationFrag)
            else
                AppToasty.error(requireActivity(), getString(R.string.login_first))
        }
    }

    private fun displayPopupWindow(anchorView: View) {

        popup = PopupWindow()

        val binding = DataBindingUtil.inflate<PopupRestaurantMenuBinding>(LayoutInflater.from(activity), R.layout.popup_restaurant_menu, null, false)
        binding.color = Configurations.colors

        popup?.contentView = binding.root

        if (viewModel.productLiveData.value?.product?.isNotEmpty() == true) {
            val rvCategory = binding.root.findViewById<RecyclerView>(R.id.rvmenu_category)
            val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            rvCategory.layoutManager = layoutManager
            val adapter = MenuCategoryAdapter(viewModel.productLiveData.value?.product?.map {
                it.sub_cat_name ?: ""
            })
            rvCategory.adapter = adapter
            adapter.settingCallback(this)
        }

        popup?.animationStyle = R.style.MyAlertDialogStyle


        popup?.height = WindowManager.LayoutParams.WRAP_CONTENT
        popup?.width = WindowManager.LayoutParams.WRAP_CONTENT
        popup?.isOutsideTouchable = true
        popup?.isFocusable = true

        if (Build.VERSION.SDK_INT >= 24) {
            val a = IntArray(2) //getLocationInWindow required array of size 2
            anchorView.getLocationInWindow(a)
            popup?.showAtLocation(activity?.window?.decorView, Gravity.CENTER, 0, 0)
        } else {
            popup?.showAsDropDown(anchorView)
        }
        popup?.update()

        popup?.dimBehind()

        anchorView.addOnAttachStateChangeListener(this)
    }

    override fun onViewDetachedFromWindow(v: View?) {
        //appUtils.revealShow(v,btn_menu, false,null)
    }

    override fun onViewAttachedToWindow(v: View?) {
        if (v == null) return

        appUtils.revealShow(v, btn_menu, true, null)
    }


    override fun onProductDetail(bean: ProductDataBean?) {
        val bundle = Bundle()

        //if(screenFlowBean?.is_single_vendor==VendorAppType.Single.appType)

        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            bundle.putInt("supplierId", bean?.supplier_id ?: 0)
            bundle.putInt("branchId", bean?.supplier_branch_id ?: 0)
            bundle.putString("title", bean?.name ?: "")
            bundle.putInt("categoryId", bean?.category_id ?: 0)

            if (screenFlowBean?.app_type == AppDataType.Food.type) {
                if (bookingFlowBean?.is_pickup_order == FoodAppType.Pickup.foodType) {
                    bundle.putString("deliveryType", "pickup")
                }
                if (clientInform?.app_selected_template != null
                        && clientInform?.app_selected_template == "1")
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetailNew, bundle)
                else
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetail, bundle)
            } else
                navController(this@TakeAwayFragment)
                        .navigate(R.id.action_supplierDetail, bundle)
        } else {
            if (bean?.parent_id != null && bean.parent_id != 0) {
                bean.product_id = bean.parent_id
            }
            navController(this@TakeAwayFragment).navigate(R.id.action_productDetail,
                    ProductDetails.newInstance(bean, 1, false))
        }

    }

    override fun addToCart(position: Int, productBean: ProductDataBean?) {


        if (screenFlowBean?.is_single_vendor == VendorAppType.Single.appType) {
            if (group_deliver_option.checkedRadioButtonId == R.id.rb_pickup) {
                productBean?.self_pickup = FoodAppType.Pickup.foodType
            } else {
                productBean?.self_pickup = FoodAppType.Delivery.foodType
            }
        } else {
            productBean?.self_pickup = FoodAppType.Delivery.foodType
        }

        val cartList = appUtils.getCartList()

        if (cartList.cartInfos?.any { it.deliveryType == FoodAppType.Both.foodType } == true && productBean?.self_pickup != FoodAppType.Both.foodType) {
            cartList.cartInfos?.mapIndexed { index, cartInfo ->
                cartInfo.deliveryType = productBean?.self_pickup ?: 0
            }

            prefHelper.addGsonValue(DataNames.CART, Gson().toJson(cartList))

            productBean?.self_pickup = productBean?.self_pickup

        } else if (cartList.cartInfos?.any { it.deliveryType != FoodAppType.Both.foodType } == true && productBean?.self_pickup == FoodAppType.Both.foodType) {
            val cartInfo = cartList.cartInfos?.get(0)

            productBean.self_pickup = cartInfo?.deliveryType
        }


        if (productBean?.self_pickup == FoodAppType.Pickup.foodType || productBean?.self_pickup == FoodAppType.Both.foodType) {
            val mRestUser = LocationUser()
            mRestUser.address = "${productBean.supplier_name} , ${productBean.supplier_address
                    ?: ""}"
            prefHelper.addGsonValue(PrefenceConstants.RESTAURANT_INF, Gson().toJson(mRestUser))
        }


        if (screenFlowBean?.app_type == AppDataType.Food.type && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
            // val cartList: CartList? = prefHelper.getGsonValue(DataNames.CART, CartList::class.java)

            if (cartList.cartInfos?.isNotEmpty() == true) {
                if (cartList.cartInfos?.any { it.deliveryType != productBean?.self_pickup } == true) {
                    appUtils.clearCart()
                }
            }
        }



        if (productBean?.adds_on?.isNotEmpty() == true) {
            //    val cartList: CartList? = prefHelper.getGsonValue(DataNames.CART, CartList::class.java)

            if (appUtils.checkProdExistance(productBean.product_id)) {
                val savedProduct = cartList.cartInfos?.filter {
                    it.supplierId == productBean.supplier_id ?: 0 && it.productId == productBean.product_id
                } ?: emptyList()

                SavedAddon.newInstance(productBean, FoodAppType.Delivery.foodType, savedProduct, this).show(childFragmentManager, "savedAddon")
            } else {
                AddonFragment.newInstance(productBean, FoodAppType.Delivery.foodType, this).show(childFragmentManager, "addOn")
            }

        } else {
            if (bookingFlowBean?.vendor_status == 0 && appUtils.checkVendorStatus(productBean?.supplier_id)) {
                appUtils.mDialogsUtil.openAlertDialog(activity
                        ?: requireContext(), getString(R.string.clearCart, textConfig?.supplier
                        ?: ""), "Yes", "No", this)
            } else {

                if (appUtils.checkBookingFlow(requireContext(), productBean?.product_id, this)) {
                    addCart(productBean)
                }
            }
        }

    }

    private fun addCart(productBean: ProductDataBean?) {

        if (productBean?.is_question == 1 && prodUtils.addItemToCart(productBean) != null) {
            productBean.prod_quantity = 0
            val bundle = bundleOf("productBean" to productBean, "is_Category" to false
                    , "categoryId" to productBean.detailed_sub_category_id)
            navController(this@TakeAwayFragment).navigate(R.id.action_homeFragment_to_questionFrag, bundle)
        } else {
            productBean?.type = screenFlowBean?.app_type
            productBean.apply { prodUtils.addItemToCart(productBean) }
            refreshProductList(productBean)
        }

    }

    private fun refreshProductList(productBean: ProductDataBean?) {


        specialOffers?.map {

            it.prod_quantity = StaticFunction.getCartQuantity(activity
                    ?: requireContext(), it.product_id)
        }


        mPopularList?.map {

            it.prod_quantity = StaticFunction.getCartQuantity(activity
                    ?: requireContext(), it.product_id)
        }

        // homeItemAdapter?.notifyItemRangeChanged(2,mSupplierList?.size?:0)
        // homeItemAdapter?.notifyItemRangeChanged(4,mSupplierList?.size?:0)

        specialListAdapter?.notifyDataSetChanged()

        /*  val specialItem = specialOffers?.find { it.product_id == productBean?.product_id }

          if (specialItem != null) {
              specialItem.prod_quantity = StaticFunction.getCartQuantity(activity
                      ?: requireContext(), specialItem.product_id)

              specialOffers?.set(specialOffers?.indexOf(specialItem) ?: -1, specialItem)
             // specialListAdapter?.notifyItemChanged(specialOffers?.indexOf(specialItem) ?: -1)

          }*/


        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            mSupplierList?.mapIndexed { index, supplierDataBean ->

                if (supplierDataBean.viewType == HomeItemAdapter.SINGLE_PROD_TYPE &&
                        supplierDataBean.itemModel?.vendorProdList?.value?.any { it.product_id == productBean?.product_id } == true) {

                    supplierDataBean.itemModel?.vendorProdList?.value?.map { it1 ->
                        it1.prod_quantity = StaticFunction.getCartQuantity(activity
                                ?: requireContext(), it1.product_id)
                    }
                    if (homeItemAdapter != null) {
                        homeItemAdapter?.notifyItemChanged(index)
                    }
                }

            }
        } else {
            mSupplierList?.mapIndexed { index, supplierDataBean ->
                if (supplierDataBean.viewType == HomeItemAdapter.SINGLE_PROD_TYPE &&
                        supplierDataBean.itemModel?.specialOffers?.any { it.product_id == productBean?.product_id } == true) {

                    supplierDataBean.itemModel?.specialOffers?.map { productBean ->
                        productBean.prod_quantity = StaticFunction.getCartQuantity(activity
                                ?: requireContext(), productBean.product_id)
                    }

                    if (homeItemAdapter != null) {
                        homeItemAdapter?.notifyItemChanged(index)
                    }
                }
            }
        }
        showBottomCart()
    }

    override fun removeToCart(position: Int, productBean: ProductDataBean?) {

        if (productBean?.adds_on?.isNotEmpty() == true) {
            val cartList: CartList? = prefHelper.getGsonValue(DataNames.CART, CartList::class.java)

            val savedProduct = cartList?.cartInfos?.filter {
                it.supplierId == productBean.supplier_id && it.productId == productBean.product_id
            } ?: emptyList()

            SavedAddon.newInstance(productBean, productBean.self_pickup
                    ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")

        } else {
            productBean.apply { prodUtils.removeItemToCart(productBean) }
            refreshProductList(productBean)
        }
    }

    override fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?) {

        speProductId = productId ?: 0
        prodStatus = status ?: 0

        if (prefHelper.getCurrentUserLoggedIn()) {
            if (isNetworkConnected) {
                viewModel.markFavProduct(productId, status)
            }
        } else {
            startActivityForResult(Intent(activity, appUtils.checkLoginActivity()), AppConstants.REQUEST_WISH_PROD)
        }
    }

    override fun onSponsorWishList(supplier: SupplierInArabicBean?, parentPosition: Int) {
        if (prefHelper.getCurrentUserLoggedIn()) {
            if (isNetworkConnected) {
                supplier?.parentPosition = parentPosition
                if (supplier?.Favourite == null || supplier.Favourite == 0)
                    viewModel.markFavSupplier(supplier)
                else
                    viewModel.unFavSupplier(supplier)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppConstants.REQUEST_WISH_PROD && resultCode == Activity.RESULT_OK) {
            val pojoLoginData = StaticFunction.isLoginProperly(activity)
            if (pojoLoginData.data != null) {
                viewModel.markFavProduct(speProductId, prodStatus)
            }
        }
    }


    override fun onCategoryDetail(bean: English?) {

        val bundle = bundleOf("title" to bean?.name,
                "categoryId" to bean?.id,
                "subCategoryId" to 0)

        if (screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {

            if (bean?.sub_category?.count() ?: 0 > 0 && screenFlowBean?.app_type != AppDataType.Food.type) {
                navController(this@TakeAwayFragment).navigate(R.id.actionSubCategory, bundle)
            } else {
                navController(this@TakeAwayFragment)
                        .navigate(R.id.action_supplierAll, bundle)
            }
        } else {
            if (screenFlowBean?.app_type != AppDataType.Food.type) {
                if (bean?.sub_category?.count() ?: 0 > 0) {
                    navController(this@TakeAwayFragment).navigate(R.id.actionSubCategory, bundle)
                } else {
                    bundle.putBoolean("has_subcat", true)
                    bundle.putInt("supplierId", bean?.supplier_branch_id ?: 0)
                    navController(this@TakeAwayFragment).navigate(R.id.action_productListing, bundle)
                }
            } else {
                bundle.putInt("supplierId", bean?.id ?: 0)
                if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetailNew, bundle)
                } else {
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetail, bundle)
                }
            }
        }
    }

    override fun onSponsorDetail(supplier: SupplierInArabicBean) {

        val bundle = bundleOf("supplierId" to supplier.id,
                "title" to supplier.name)

        if (screenFlowBean?.app_type == AppDataType.Food.type) {

            if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                navController(this@TakeAwayFragment).navigate(R.id.action_restaurantDetailNew, bundle)
            } else {
                navController(this@TakeAwayFragment).navigate(R.id.action_restaurantDetail, bundle)
            }
        } else {
            if (!supplier.category.isNullOrEmpty()) {
                bundle.putBoolean("is_supplier", true)
                bundle.putParcelable("supplierData", supplier)
                bundle.putParcelableArrayList("subcategory", ArrayList<Parcelable>(supplier.category
                        ?: mutableListOf()))


                navController(this@TakeAwayFragment)
                        .navigate(R.id.actionSubCategory, bundle)
            } else {
                navController(this@TakeAwayFragment).navigate(R.id.action_productListing, bundle)
            }

        }


    }

    override fun onSucessListner() {

        appUtils.clearCart()

        refreshProductList(null)
    }

    override fun onErrorListener() {


    }

    override fun onBannerDetail(bannerBean: TopBanner?) {

        val bundle = bundleOf("supplierId" to bannerBean?.supplier_id,
                "branchId" to bannerBean?.branch_id,
                "title" to bannerBean?.supplier_name,
                "categoryId" to bannerBean?.category_id)


        when (screenFlowBean?.app_type) {
            AppDataType.Food.type -> {
                if (DELIVERY_OPTIONS == DeliveryType.PickupOrder.type) {
                    bundle.putString("deliveryType", "pickup")
                }
                if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetailNew, bundle)
                } else {
                    navController(this@TakeAwayFragment)
                            .navigate(R.id.action_restaurantDetail, bundle)
                }
            }
            else -> {
                navController(this@TakeAwayFragment).navigate(R.id.action_productListing, bundle)
            }
        }

    }

    override fun onSearchItem(text: kotlin.String?) {

    }

    override fun onHomeCategory(position: Int) {

        when (position) {
            0 -> homelytManager?.scrollToPositionWithOffset(4, mSupplierList?.size ?: 0)
            1 -> homelytManager?.scrollToPositionWithOffset(5, mSupplierList?.size ?: 0)
            2 -> homelytManager?.scrollToPositionWithOffset(7, mSupplierList?.size ?: 0)
        }


    }

    override fun onViewMore() {

        val bundle = bundleOf("cat_id" to catId)
        navController(this@TakeAwayFragment)
                .navigate(R.id.action_OfferListing, bundle)
    }

    override fun onSupplierDetail(supplierBean: SupplierDataBean?) {

        val bundle = bundleOf("supplierId" to supplierBean?.id)
        if (clientInform?.app_selected_template != null && clientInform?.app_selected_template == "1") {
            navController(this@TakeAwayFragment)
                    .navigate(R.id.restaurantDetailFragNew, bundle)
        } else {
            navController(this@TakeAwayFragment)
                    .navigate(R.id.restaurantDetailFrag, bundle)
        }
    }


    override fun onSpclView(specialListAdapter: SpecialListAdapter?) {

        this.specialListAdapter = specialListAdapter
    }

    override fun onFilterScreen() {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.show(childFragmentManager, "assasa")
    }

    override fun onRefresh() {

        swiprRefresh.isRefreshing = false

        clearViewModelApi()
    }

    private fun clearViewModelApi() {

/*        specialOffers?.clear()
        mSupplierList?.clear()*/

        viewModel.homeDataLiveData.value = null
        viewModel.supplierLiveData.value = null
        viewModel.offersLiveData.value = null
        viewModel.productLiveData.value = null
        fetchCategories()
    }

    override fun onBrandSelect(brandsBean: Brand) {

        val bundle = Bundle()
        bundle.putInt("brand_id", brandsBean.id ?: -1)
        bundle.putBoolean("has_brands", true)
        bundle.putString("title", brandsBean.name)

        navController(this@TakeAwayFragment).navigate(R.id.action_productListing, bundle)
    }

    override fun onFavStatus() {
        mSupplierList?.mapIndexed { index, supplierDataBean ->

            if (supplierDataBean.viewType == HomeItemAdapter.SPL_PROD_TYPE && supplierDataBean.itemModel?.specialOffers?.any { it.product_id == speProductId } == true) {
                supplierDataBean.itemModel?.specialOffers?.filter { it.product_id == speProductId }?.map {
                    it.is_favourite = prodStatus
                }

                homeItemAdapter?.notifyItemChanged(index)
            }


            if (supplierDataBean.viewType == HomeItemAdapter.POPULAR_TYPE && supplierDataBean.itemModel?.popularProdList?.any { it.product_id == speProductId } == true) {
                supplierDataBean.itemModel?.popularProdList?.filter { it.product_id == speProductId }?.map {
                    it.is_favourite = prodStatus
                }

                homeItemAdapter?.notifyItemChanged(index)
            }
        }
    }

    override fun favSupplierResponse(supplierId: SupplierInArabicBean?) {
        supplierId?.Favourite = 1
        if (supplierId?.parentPosition != null && supplierId.parentPosition != -1)
            homeItemAdapter?.notifyItemChanged(supplierId.parentPosition ?: 0)
    }

    override fun unFavSupplierResponse(data: SupplierInArabicBean?) {
        data?.Favourite = 0
        if (data?.parentPosition != null && data.parentPosition != -1)
            homeItemAdapter?.notifyItemChanged(data.parentPosition ?: 0)
    }

    override fun onErrorOccur(message: String1) {

        tvArea.isEnabled = true

        cnst_home_lyt.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvArea -> {
                if (DELIVERY_OPTIONS == DeliveryType.DeliveryOrder.type) {
                    AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
                }
            }
        }
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        location_txt.text = getString(R.string.location)

        val locUser = LocationUser((adrsBean.latitude
                ?: 0.0).toString(), (adrsBean.longitude
                ?: 0.0).toString(), "${adrsBean.customer_address} , ${adrsBean.address_line_1}")
        prefHelper.addGsonValue(DataNames.LocationUser, Gson().toJson(locUser))

        prefHelper.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsBean))
        tvArea.text = "${adrsBean.customer_address ?: ""} , ${adrsBean.address_line_1 ?: ""}"

        clearViewModelApi()
    }

    override fun onLocationSelect(location: SupplierLocation) {

    }

    override fun onDestroyDialog() {


        if (screenFlowBean?.app_type == AppDataType.Food.type) {
            rb_pickup.isChecked = true
            DELIVERY_OPTIONS = DeliveryType.PickupOrder.type

            //StaticFunction.clearCart(activity)
            checkSavedProd()
            showBottomCart()
        }
    }


    companion object {
        fun newInstance() = TakeAwayFragment()
    }

    override fun onProdAdded(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean) {
        this.parentPos = parentPosition
        this.childPos = childPosition

        addToCart(-1, productBean)
    }

    override fun onProdDelete(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean) {
        this.parentPos = parentPosition
        this.childPos = childPosition

        removeToCart(-1, productBean)
    }

    override fun onProdDetail(productBean: ProductDataBean?) {

    }

    override fun onDescExpand(tvDesc: TextView?, productBean: ProductDataBean?, childPosition: Int) {
        /*    if (productBean?.isExpand==true) {
                CommonUtils.collapseTextView(tvDesc, tvDesc?.lineCount)
            } else {*/
        CommonUtils.expandTextView(tvDesc)
        //  }
    }

    override fun getMenuCategory(position: Int) {

        homelytManager?.scrollToPositionWithOffset(position + 2, mSupplierList?.size ?: 0)

        if (popup?.isShowing == true) {
            popup?.dismiss()
        }
    }

    override fun onAddonAdded(productModel: ProductDataBean) {
        if (productModel.adds_on?.isNullOrEmpty() == false && appUtils.checkProdExistance(productModel.product_id)) {
            val cartList: CartList? = appUtils.getCartList()
            productModel.prod_quantity = cartList?.cartInfos?.filter { productModel.product_id == it.productId }?.sumBy { it.quantity }
                    ?: 0
        }


        // productModel.let { mSupplierList?.get(parentPos)?.itemModel?.vendorProdList?.value?.set(childPos, it) }
        refreshProductList(productModel)
    }


}
