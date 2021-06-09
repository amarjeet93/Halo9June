package com.codebrew.clikat.module.restaurant_detail


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.CommonUtils.Companion.areDrawablesIdentical
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.constants.PrefenceConstants.Companion.IS_VAT_INCLUDED
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.databinding.FragmentRestaurantDetailBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.restaurant_detail.adapter.MenuCategoryAdapter
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter
import com.codebrew.clikat.module.restaurant_detail.adapter.SupplierProdListAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import com.quest.intrface.ImageCallback
import com.quest.utils.dialogintrface.ImageDialgFragment
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.android.synthetic.main.fragment_restaurant_detail.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class RestaurantDetailFrag : BaseFragment<FragmentRestaurantDetailBinding, RestDetailViewModel>(),
        ProdListAdapter.ProdCallback, DialogListener, MenuCategoryAdapter.MenuCategoryCallback,
        RestDetailNavigator, AddonFragment.AddonCallback, ImageCallback, EasyPermissions.PermissionCallbacks,
        AddressDialogFragment.Listener {


    private var parentPos: Int = 0
    private var childPos: Int = 0

    private var adapter: SupplierProdListAdapter? = null
    private var productBeans = mutableListOf<ProductBean>()

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null


    private var prodlytManager: LinearLayoutManager? = null


    private var deliveryType = 0

    private var supplierId = 0


    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    @Inject
    lateinit var imageUtils: ImageUtility

    @Inject
    lateinit var permissionFile: PermissionFile

    private var photoFile: File? = null

    private val imageDialog by lazy { ImageDialgFragment() }

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    var settingBean: SettingModel.DataBean.SettingData? = null

    private var mBinding: FragmentRestaurantDetailBinding? = null

    private lateinit var viewModel: RestDetailViewModel


    private var isResutantOpen: Boolean = false

    private var colorConfig = Configurations.colors

    var tooltip: SimpleTooltip? = null

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    private var adrsBean: AddressBean? = null


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_restaurant_detail
    }

    override fun getViewModel(): RestDetailViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(RestDetailViewModel::class.java)
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
        settingBean = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)


        restDetailObserver()

        imageObserver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mBinding = viewDataBinding

        viewDataBinding.color = Configurations.colors
        viewDataBinding.drawables = Configurations.drawables
        viewDataBinding.strings = textConfig

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainmenu.foreground.alpha = 0
        }

        getValues()
        settingLayout()
        setclikListener()

        showBottomCart()

        setPrescListener()
    }


    private fun restDetailObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataBean> { resource ->
            refreshAdapter(resource)
            resource.supplier_detail?.let { settingSupplierDetail(it) }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.supplierLiveData.observe(this, catObserver)
    }
    override fun onTableAdded(table_name: String) {

    }
    private fun imageObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<String> { resource ->
            hideLoading()
            group_presc.visibility = View.VISIBLE
            presc_image.text = photoFile?.absoluteFile?.name.toString()
            mBinding?.root?.onSnackbar(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.prescLiveData.observe(this, catObserver)
    }

    private fun setPrescListener() {
        ivUploadPresc.setOnClickListener {
            if (dataManager.getCurrentUserLoggedIn()) {
                AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
            } else {
                appUtils.checkLoginFlow(requireContext()).apply {
                    (AppConstants.REQUEST_PRES_UPLOAD)
                }
            }
        }

        iv_cross_pres.setOnClickListener {
            group_presc.visibility = View.GONE
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun setclikListener() {

        iv_search_prod.afterTextChanged {

            if (it.isNotEmpty()) {
                adapter?.filter?.filter(it.toLowerCase())
            } else {
                adapter?.filter?.filter("")
            }
        }

/*
        iv_search_prod.setOnSearchClickListener { v: View? ->
            btn_menu.visibility = View.INVISIBLE
            appbar.setExpanded(false)
        }


        iv_search_prod.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                adapter!!.filter.filter(s)
                adapter!!.notifyDataSetChanged()
                Log.e("search_view", "setOnQueryTextListener$s")
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                //when the text change
                adapter!!.filter.filter(s)
                adapter!!.notifyDataSetChanged()
                Log.e("search_view", "onQueryTextChange$s")
                return true
            }
        })
        iv_search_prod.setOnCloseListener {

            //when canceling the search
            appbar.setExpanded(true)
            hideKeyboard()
            btn_menu.setVisibility(View.VISIBLE)
            false
        }
*/




        ic_back.setOnClickListener {
            navController(this@RestaurantDetailFrag).popBackStack()
        }

        iv_favourite.setOnClickListener {

            if (isNetworkConnected) {
                if (dataManager.getCurrentUserLoggedIn()) {
                    if (checkFavouriteImage(iv_favourite)) {
                        viewModel.markFavSupplier(supplierId.toString())
                    } else {
                        viewModel.unFavSupplier(supplierId.toString())
                    }
                } else {
                    appUtils.checkLoginFlow(requireContext())
                }
            }
        }

        btn_menu.setOnClickListener {
            //displayPopupWindow(it)
            showPopUp(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPopUp(view: View) {

        tooltip = SimpleTooltip.Builder(activity)
                .anchorView(view)
                .text(R.string.menu)
                .gravity(Gravity.TOP)
                .dismissOnOutsideTouch(true)
                .dismissOnInsideTouch(false)
                .modal(true)
                .showArrow(false)
                .animated(false)
                .showArrow(true)
                .arrowDrawable(R.drawable.ic_popup)
                .textColor(Color.parseColor(colorConfig.primaryColor))
                .arrowColor(ContextCompat.getColor(requireContext(), R.color.white))
                .transparentOverlay(true)
                .overlayOffset(0f)
                // .highlightShape(OverlayView.INVISIBLE)
                //.overlayMatchParent(true)
                .padding(0.0f)
                .margin(0.0f)
                //.animationPadding(SimpleTooltipUtils.pxFromDp(50f))
                .onDismissListener {

                    mainmenu.foreground.alpha = 0;
                }
                .onShowListener {
                    mainmenu.foreground.alpha = 120;
                }
                .contentView(R.layout.popup_restaurant_menu, R.id.menu)
                .focusable(true)
                .build()


        val stringList = ArrayList<String>()

        if (productBeans.isNotEmpty()) {
            for (productBean in productBeans) {
                stringList.add(productBean.sub_cat_name ?: "")
            }
        }

        // tooltip?.color = Configurations.colors

        val rvCategory = tooltip?.findViewById<RecyclerView>(R.id.rvmenu_category)
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rvCategory?.layoutManager = layoutManager
        val adapter = MenuCategoryAdapter(stringList)
        rvCategory?.adapter = adapter
        adapter.settingCallback(this)

        val itemDecoration: ItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        rvCategory?.addItemDecoration(itemDecoration)

        tooltip?.show()

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun settingLayout() {

        productBeans.clear()

        adapter = SupplierProdListAdapter(productBeans, this, settingBean)

        prodlytManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        rvproductList.layoutManager = prodlytManager

        rvproductList.adapter = adapter

        if (arguments != null) {

            if (arguments?.containsKey("deliveryType") == true) {
                deliveryType = 1
            }

            if (arguments?.containsKey("supplierId") == true) {
                supplierId = arguments?.getInt("supplierId") ?: 0

                getProductList(supplierId)
            }
        }

        settingBean?.showRestaurantName?.let {
            if (it == "1")
                tv_name?.visibility = View.VISIBLE
            else
                tv_name?.visibility = View.GONE
        }

        settingBean?.hide_restaurant_image?.let {
            if (it == "1")
                ivSupplierIcon?.visibility = View.GONE
            else
                ivSupplierIcon?.visibility = View.VISIBLE
        }

    }

    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData()

        if (screenFlowBean?.app_type == AppDataType.Ecom.type) {
            bottom_cart.visibility = View.GONE
        } else {
            bottom_cart.visibility = View.VISIBLE
        }


        if (appCartModel.cartAvail) {

            tv_total_price.text = getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)

            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount ?: 0)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener {

                val navOptions: NavOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.restaurantDetailFrag, false)
                        .build()


                navController(this@RestaurantDetailFrag).navigate(R.id.action_restaurantDetailFrag_to_cart, null, navOptions)

            }
        } else {
            bottom_cart.visibility = View.GONE
        }

    }

    private fun getValues() {
        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
    }


    private fun getProductList(supplierId: Int) {

        if (viewModel.supplierLiveData.value == null) {
            if (isNetworkConnected)
                viewModel.getProductList(supplierId.toString())
        } else {
            settingSupplierDetail(viewModel.supplierLiveData.value?.supplier_detail
                    ?: SupplierDetailBean())
            refreshAdapter(viewModel.supplierLiveData.value)
        }

    }

    private fun refreshAdapter(data: DataBean?) {

        viewDataBinding.supplierModel = data?.supplier_detail

        var subCatName: String? = null

        productBeans.clear()

        if (data?.product?.isNotEmpty() == true) {

            data.product?.map { prod ->

                prod.is_SubCat_visible = true

                prod.value?.map {
                    it.prod_quantity = StaticFunction.getCartQuantity(requireActivity(), it.product_id)
                    it.netPrice = if (it.fixed_price?.toFloatOrNull() ?: 0.0f > 0) it.fixed_price?.toFloatOrNull() else 0f
                }

                if (prod.detailed_category_name?.count() ?: 0 > 0) {
                    prod.detailed_category_name?.distinctBy { it.detailed_sub_category_id }?.forEach { detailProd ->
                        val prodBean = prod.copy()
                        prodBean.detailed_sub_category = detailProd.name
                        prod.value?.map {
                            it.detailed_sub_name = detailProd.name
                        }

                        prodBean.value = prod.value?.filter { it.detailed_sub_category_id == detailProd.detailed_sub_category_id }?.toMutableList()

                        if (prodBean.value?.isEmpty() == true) return@forEach

                        if (subCatName == prodBean.sub_cat_name) {
                            prodBean.is_SubCat_visible = false
                        }
                        subCatName = prodBean.sub_cat_name


                        if (prodBean.value?.isNotEmpty() == true) {
                            productBeans.add(prodBean.copy())
                        }
                    }
                } else {
                    productBeans.add(prod.copy())
                }
            }


            adapter?.notifyDataSetChanged()
        }
    }


    private fun settingSupplierDetail(supplier_detail: SupplierDetailBean) {
        AppConstants.CURRENCY_SYMBOL = supplier_detail.currency_symbol ?: ""


        dataManager.setkeyValue(PrefenceConstants.CURRENCY_NAME, supplier_detail.currency_name.toString())
        dataManager.setkeyValue("CurrencySymbol", supplier_detail.currency_symbol.toString())
        isResutantOpen = checkResturntTiming(supplier_detail.timing)

        if (adapter != null && !isResutantOpen) {
            adapter?.checkResturantOpen(isResutantOpen)
        }


        StaticFunction.loadImage(supplier_detail.logo, ivSupplierIcon, false)


        if (supplier_detail.supplier_image?.isNotEmpty() == true) {
            StaticFunction.loadImage(supplier_detail.supplier_image?.get(0)
                    ?: "", ivSupplierImage, false)
        }

        iv_favourite?.visibility = if (settingBean?.is_supplier_wishlist == "1") View.VISIBLE else View.GONE

        tv_name.text = supplier_detail.name

        tv_rating.text = if (supplier_detail.rating == 0f) {
            "New"
        } else supplier_detail.rating.toString()


        if (supplier_detail.Favourite == 1) {
            iv_favourite.setImageResource(R.drawable.ic_favourite)
        } else {
            iv_favourite.setImageResource(R.drawable.ic_unfavorite)
        }

        if (settingBean?.show_prescription_requests != null && settingBean?.show_prescription_requests == "1") {
            supplier_detail.user_request_flag.let {
                if (it == 1) {
                    group_presc_doc.visibility = View.VISIBLE
                    group_presc.visibility = View.GONE
                } else {
                    group_presc_doc.visibility = View.GONE
                    group_presc.visibility = View.GONE
                }
            }
        }
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
            currentDate.time.after(startDate.time) ?: false && currentDate.time.before(endDate.time) ?: false
        } else {
            isCheckStatus
        }
    }

    override fun onProdAdded(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean) {


        if (isOpen) {
            mDialogsUtil.openAlertDialog(activity
                    ?: requireContext(), getString(R.string.offline_supplier_tag, textConfig?.suppliers), getString(R.string.ok), "", this)
            return
        }

        productBean?.type = screenFlowBean?.app_type

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
                        ?: requireContext(), getString(R.string.clearCart, textConfig?.supplier
                        ?: ""), "Yes", "No", this)
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

                if (adapter != null) {
                    adapter!!.notifyDataSetChanged()
                }

            }

            showBottomCart()
        }

    }

    override fun onProdDetail(productBean: ProductDataBean?) {


        val bundle = Bundle()
        bundle.putInt("productId", productBean?.product_id ?: 0)
        bundle.putString("title", productBean?.name)
        bundle.putInt("categoryId", productBean?.category_id ?: 0)
        bundle.putInt("supplier_id", productBean?.supplier_id ?: 0)
        bundle.putInt("supplier_branch_id", productBean?.supplier_branch_id ?: 0)
        bundle.putInt("mDeliveryType", deliveryType)
        // ProductDetails productDetails = new ProductDetails();
        bundle.putInt("offerType", 0)

        productBean?.self_pickup = deliveryType
        productBean?.type = screenFlowBean?.app_type
        if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
            productBean?.payment_after_confirmation = settingBean?.payment_after_confirmation?.toInt()
                    ?: 0
        }

        Navigation.findNavController(requireView()).navigate(R.id.action_restaurantDetailFrag_to_productDetails,
                ProductDetails.newInstance(productBean, 0, false))
    }

    override fun onDescExpand(tvDesc: TextView?, productBean: ProductDataBean?, childPosition: Int) {
        /*   if (productBean?.isExpand==true) {
               CommonUtils.collapseTextView(tvDesc, tvDesc?.lineCount)
           } else {
               productBean?.isExpand==true*/
        CommonUtils.expandTextView(tvDesc)
        // }
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

            productModel.type = screenFlowBean?.app_type

            if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
                productModel.payment_after_confirmation = settingBean?.payment_after_confirmation?.toInt()
                        ?: 0
            }
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


        val productBean = productBeans[parentPos]

        productModel?.let { productBean.value?.set(childPos, it) }


        if (adapter != null) {
            adapter?.notifyDataSetChanged()
        }

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


        if (adapter != null)
            adapter!!.notifyDataSetChanged()

        showBottomCart()
    }

    override fun onSucessListner() {

        if (isResutantOpen) {
            appUtils.clearCart()
            clearCart()
        }
    }

    override fun onErrorListener() {

    }

    override fun getMenuCategory(position: Int) {

        if (tooltip != null && tooltip?.isShowing == true) {
            tooltip?.dismiss()
        }

        appbar.setExpanded(false)
        prodlytManager?.scrollToPosition(position)

    }


    private fun checkFavouriteImage(tbFavourite: ImageView): Boolean {
        return areDrawablesIdentical(tbFavourite.drawable, ContextCompat.getDrawable(activity
                ?: requireContext(), R.drawable.ic_unfavorite)!!)

    }


    override fun favResponse() {
        iv_favourite.setImageResource(R.drawable.ic_favourite)
        mBinding?.root?.onSnackbar(getString(R.string.successFavourite))
    }

    override fun unFavResponse() {

        iv_favourite.setImageResource(R.drawable.ic_unfavorite)
        mBinding?.root?.onSnackbar(getString(R.string.successUnFavourite))
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
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

        if (adapter != null) {
            adapter?.notifyDataSetChanged()
        }
    }

    private fun uploadImage() {
        if (permissionFile.hasCameraPermissions(activity ?: requireContext())) {
            if (isNetworkConnected) {
                showImagePicker()
            }
        } else {
            permissionFile.cameraAndGalleryTask(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            mBinding?.root?.onSnackbar(getString(R.string.returned_from_app_settings_to_activity))
        }

        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.CameraPicker) {

            if (isNetworkConnected) {
                if (photoFile?.isRooted == true) {

                    uploadPrescImage(imageUtils.compressImage(photoFile?.absolutePath
                            ?: ""), screenFlowBean?.app_type.toString())
                }
            }
        } else if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AppConstants.GalleyPicker) {
            if (data != null) {
                if (isNetworkConnected) {
                    //data.getData return the content URI for the selected Image
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    // Get the cursor
                    val cursor = activity?.contentResolver?.query(selectedImage!!, filePathColumn, null, null, null)
                    // Move to first row
                    cursor?.moveToFirst()
                    //Get the column index of MediaStore.Images.Media.DATA
                    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                    //Gets the String value in the column
                    val imgDecodableString = cursor?.getString(columnIndex ?: 0)
                    cursor?.close()


                    if (imgDecodableString?.isNotEmpty() == true) {
                        photoFile = File(imgDecodableString)
                        uploadPrescImage(imageUtils.compressImage(imgDecodableString), screenFlowBean?.app_type.toString())
                    }
                }
            }
        } else if (requestCode == AppConstants.REQUEST_PRES_UPLOAD && resultCode == Activity.RESULT_OK) {
            AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
        }
    }

    private fun uploadPrescImage(compressImage: String, appType: String) {

        if (isNetworkConnected) {
            showLoading()
            viewModel.uploadPresImage(compressImage,
                    viewModel.supplierLiveData.value?.supplier_detail?.supplier_branch_id.toString()
                            ?: "", adrsBean?.id, appType)
        }
    }

    override fun onGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, AppConstants.GalleyPicker)
    }

    override fun onCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    ImageUtility.filename(imageUtils)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            activity ?: requireContext(),
                            activity?.packageName ?: "",
                            it)

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, AppConstants.CameraPicker)
                }
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == AppConstants.CameraGalleryPicker) {

            if (isNetworkConnected) {
                showImagePicker()
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showImagePicker() {
        imageDialog.settingCallback(this)
        imageDialog.show(
                childFragmentManager,
                "image_picker"
        )
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        this.adrsBean = adrsBean

        val locUser = LocationUser((adrsBean.latitude
                ?: 0.0).toString(), (adrsBean.longitude
                ?: 0.0).toString(), "${adrsBean.customer_address} , ${adrsBean.address_line_1}")
        dataManager.addGsonValue(DataNames.LocationUser, Gson().toJson(locUser))

        dataManager.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsBean))


        uploadImage()

    }

    override fun onLocationSelect(location: SupplierLocation) {

    }

    override fun onDestroyDialog() {

    }

}
