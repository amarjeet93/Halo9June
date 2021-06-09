package com.codebrew.clikat.module.subcategory

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.DialogsUtil
import com.codebrew.clikat.app_utils.ProdUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DataManager
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.supplier_detail.DataSupplierDetail
import com.codebrew.clikat.databinding.FragmentSubCategoryBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.ExampleAllSupplier
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.home_screen.adapter.BrandsListAdapter
import com.codebrew.clikat.module.product.product_listing.DialogListener
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.subcategory.adapter.SubCatAdapter
import com.codebrew.clikat.module.subcategory.adapter.SubCategorySelectAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_sub_category.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.toolbar_app.*
import javax.inject.Inject

/*
 * Created by Harman Sandhu on 20/4/16.
 */

class SubCategory : BaseFragment<FragmentSubCategoryBinding, SubCategoryViewModel>(),
        SubCategorySelectAdapter.SubCategoryCallback, BrandsListAdapter.BrandCallback,
        ProductListingAdapter.ProductCallback, DialogListener, SubCatAdapter.SubCatCallback,
        SubCategoryNavigator, AddonFragment.AddonCallback {


    private var supplierId: Int = 0
    private var categoryId: Int = 0
    private var isSuplier = false

    private var madapter: SubCatAdapter? = null

    private var mPoductAdapter: ProductListingAdapter? = null

    private var mSubCatLists = mutableListOf<SubCatList>()

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var supplerDetail: DataSupplierDetail? = null

    var category: ArrayList<SubCategoryData>? = null


    private var list = mutableListOf<ProductDataBean>()

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prodUtils: ProdUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    @Inject
    lateinit var factory: ViewModelProviderFactory


    private var mBinding: FragmentSubCategoryBinding? = null

    private lateinit var mViewModel: SubCategoryViewModel

    private val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    private var supplierBean: SupplierInArabicBean? = null


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sub_category
    }

    override fun getViewModel(): SubCategoryViewModel {
        mViewModel = ViewModelProviders.of(this, factory).get(SubCategoryViewModel::class.java)
        return mViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)

        subCatObserver()
        supplierDetailObserver()
        suppliersListObserver()

        arguments?.let {
            categoryId = it.getInt("categoryId", 0)
            supplierId = it.getInt("supplierId", 0)
            isSuplier = it.getBoolean("is_supplier", false)
            category = it.getParcelableArrayList("subcategory")

            supplierBean = it.getParcelable("supplierData")

            if(supplierBean!=null)
            {
                categoryId=category?.get(0)?.category_id ?: 0
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        mBinding?.colors = Configurations.colors
        mBinding?.strings = textConfig

        searchView.typeface = AppGlobal.regular

        settingToolbar()

        madapter = SubCatAdapter(mSubCatLists, appUtils)
        madapter?.setFragCallback(this, this)
        recyclerview.adapter = madapter


        if (isSuplier && supplierBean!=null) {
            if (mViewModel.supllierLiveData.value == null) {
                callSuplierApi()
            }else
            {
                loadSupplieCategory(mViewModel.supllierLiveData.value)
            }

        }else
        {
            if (mViewModel.subCatLiveData.value == null) {
                apiGetSubCategories(categoryId, supplierId)
            } else {
                setdata(mViewModel.subCatLiveData.value)
            }
        }

        setPriceLayout()


    }


    private fun callSuplierApi() {

        mViewModel.fetchSuplierDetail(supplierBean?.supplier_branch_id
                ?: 0, category?.get(0)?.category_id ?: 0, supplierBean?.id ?: 0)


    }

    private fun settingToolbar() {
        tb_title.text = getString(R.string.select_category)

        tb_back.setOnClickListener {
            Navigation.findNavController(mBinding?.root!!).popBackStack()
        }
    }


    private fun subCatObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<SubCatData> { resource ->

            setdata(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.subCatLiveData.observe(this, catObserver)
    }

    private fun supplierDetailObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataSupplierDetail> { resource ->
            // Update the UI, in this case, a TextView.
            loadSupplieCategory(resource)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mViewModel.supllierLiveData.observe(this, catObserver)

    }

    private fun loadSupplieCategory(resource: DataSupplierDetail?) {

        mSubCatLists.clear()

        supplerDetail = resource
        mSubCatLists.add(SubCatList("subcategory", supplerDetail?.category as ArrayList<SubCategoryData>))
        viewModel.setSubCat(mSubCatLists.size)
        madapter?.notifyDataSetChanged()
    }


    private fun apiGetSubCategories(categoryId: Int, supplierId: Int) {

        val hashMap = dataManager.updateUserInf()
        if (supplierId != 0) {
            hashMap["supplier_id"] = "" + supplierId
        }
        hashMap["category_id"] = "" + categoryId

        if (isNetworkConnected) {
            mViewModel.getSubCategory(hashMap)
        }

    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    private fun setdata(exampleSubCategories: SubCatData?) {

        mSubCatLists.clear()
        list.clear()
        if (exampleSubCategories?.sub_category_data?.isNotEmpty() == true) {
            mSubCatLists.add(SubCatList("subcategory", exampleSubCategories.sub_category_data))
        }

        if (exampleSubCategories?.offer?.isNotEmpty() == true) {
            changeProductList(exampleSubCategories.offer)

            list.addAll(exampleSubCategories.offer)

            mSubCatLists.add(SubCatList("offer", exampleSubCategories.offer))
        }

        if (exampleSubCategories?.brand?.isNotEmpty() == true) {
            mSubCatLists.add(SubCatList("brand", exampleSubCategories.brand))
        }

        viewModel.setSubCat(mSubCatLists.size)
        madapter?.notifyDataSetChanged()

    }


    override fun onSubCategoryDtail(dataBean: SubCategoryData) {

        val bundle = bundleOf("title" to dataBean.name,
                "supplierId" to supplierId,
                "cat_id" to categoryId,
                "categoryId" to dataBean.category_id,
                "is_supplier" to isSuplier)


        if (dataBean.is_cub_category == 1) {

            navController(this@SubCategory)
                    .navigate(R.id.action_subCategory_self, bundle)

        } else {
            if (dataBean.is_question == 1) {
                bundle.putBoolean("is_Category", true)
                navController(this@SubCategory)
                        .navigate(R.id.action_subCategory_to_questionFrag, bundle)
            } else {
                if (screenFlowBean?.app_type == AppDataType.HomeServ.type && arguments?.getBoolean("is_supplier") == false
                        && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {

                    bundle.putInt("categoryId", categoryId)
                    bundle.putInt("subCatId", dataBean.category_id ?: 0)

                    getAllSuppliers(dataBean.category_id ?: 0, bundle)

                } else {
                    bundle.putBoolean("has_subcat", true)
                    navController(this@SubCategory)
                            .navigate(R.id.action_productListing, bundle)
                }
            }
        }

    }

    private var bundleAllSuppliers: Bundle? = null
    private fun getAllSuppliers(subCategory: Int, bundle: Bundle) {
        bundleAllSuppliers = bundle
        val hashMap = dataManager.updateUserInf()
        /* hashMap["subCat"] = subCategory.toString()*/
        hashMap["categoryId"] = categoryId.toString()

        if (isNetworkConnected) {
            mViewModel.getSuppliers(hashMap)
        }
    }

    private fun suppliersListObserver() {
        // Create the observer which updates the UI.
        val supplierObserver = Observer<ExampleAllSupplier> { resource ->
            if (resource?.data?.supplierList?.isNotEmpty() == true && resource.data?.supplierList?.size == 1) {
                bundleAllSuppliers?.putBoolean("has_subcat", true)
                bundleAllSuppliers?.putInt("supplierId", resource.data?.supplierList?.firstOrNull()?.id
                        ?: 0)
                if (bundleAllSuppliers?.containsKey("subCatId") == true)
                    bundleAllSuppliers?.putInt("cat_id", bundleAllSuppliers?.getInt("subCatId", 0)
                            ?: 0)
                navController(this).navigate(R.id.action_productListing, bundleAllSuppliers)
            } else {
                navController(this@SubCategory)
                        .navigate(R.id.action_supplierAll, bundleAllSuppliers)
            }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.suppliersList.observe(this, supplierObserver)
    }


    override fun onBrandSelect(brandsBean: Brand) {

        val bundle = bundleOf("brand_id" to brandsBean.id,
                "has_brands" to true,
                "title" to brandsBean.name)

        navController(this@SubCategory)
                .navigate(R.id.action_productListing, bundle)
    }

    override fun addToCart(position: Int, agentType: Boolean) {

        if (bookingFlowBean?.vendor_status == 0 && appUtils.checkVendorStatus(list[position].supplier_id)) {

            mDialogsUtil.openAlertDialog(activity
                    ?: requireContext(), getString(R.string.clearCart, textConfig?.supplier
                    ?: ""), "Yes", "No", this)
        } else {
            if (list[position].adds_on?.isNotEmpty() == true) {
                val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

                if (appUtils.checkProdExistance(list[position].product_id)) {
                    val savedProduct = cartList?.cartInfos?.filter {
                        it.supplierId == list[position].supplier_id && it.productId == list[position].product_id
                    } ?: emptyList()

                    SavedAddon.newInstance(list[position], list[position].self_pickup
                            ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")
                } else {
                    AddonFragment.newInstance(list[position], list[position].self_pickup
                            ?: -1, this).show(childFragmentManager, "addOn")
                }

            } else {
                if (appUtils.checkBookingFlow(requireContext(), list[position].product_id
                                ?: 0, this)) {
                    addCart(position)
                }
            }
        }
    }

    override fun removeToCart(position: Int) {

        val mProduct = list[position]

        if (mProduct.adds_on?.isNotEmpty() == true) {

            val savedProduct = appUtils.getCartList().cartInfos?.filter {
                it.supplierId == mProduct.supplier_id && it.productId == mProduct.product_id
            } ?: emptyList()

            SavedAddon.newInstance(mProduct, list[position].self_pickup
                    ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")

        } else {
            list[position] = mProduct.apply { prodUtils.removeItemToCart(mProduct) }
            mPoductAdapter?.notifyItemChanged(position)
        }

        updateProductList(position)
    }

    override fun productDetail(bean: ProductDataBean?) {

        navController(this@SubCategory)
                .navigate(R.id.action_productDetail, ProductDetails.newInstance(bean, 0, false))

    }

    override fun publishResult(count: Int) {

    }

    override fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?) {

    }

    private fun addCart(position: Int) {

        val mProduct = list[position]

        mProduct.type = screenFlowBean?.app_type
        mProduct.apply { prodUtils.addItemToCart(mProduct) }

        updateProductList(position)

    }

    override fun onSucessListner() {

        list.map {
            it.quantity == 0
        }
        updateProductList(-1)
    }

    override fun onErrorListener() {

    }


    private fun changeProductList(product: List<ProductDataBean>) {

        product.map {
            it.quantity = StaticFunction.getCartQuantity(activity
                    ?: requireContext(), it.product_id)
            it.netPrice = it.fixed_price?.toFloatOrNull() ?: 0f

            it.let {
                prodUtils.changeProductList(it)
            }
        }
    }

    private fun updateProductList(position: Int) {

        if (position != -1) {
            mSubCatLists[position] = mSubCatLists.filter { it.name == "offer" }[position]
            mPoductAdapter?.notifyItemChanged(position)
        } else {
            mPoductAdapter?.notifyDataSetChanged()
        }
    }


    private fun setPriceLayout() {

        val appCartModel = appUtils.getCartData()

        bottom_cart.visibility = View.VISIBLE

        if (appCartModel.cartAvail) {
            tv_total_price.text = activity?.getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)
            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener {
                navController(this@SubCategory).navigate(R.id.action_subCategory_to_cart)
            }
        } else {
            bottom_cart.visibility = View.GONE
        }
    }

    override fun onProdItemUpdate(adpater: ProductListingAdapter) {

        mPoductAdapter = adpater
    }

    override fun onAddonAdded(productModel: ProductDataBean) {

    }


}