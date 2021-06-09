package com.codebrew.clikat.module.product.product_listing

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
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
import com.codebrew.clikat.data.model.api.ProductData
import com.codebrew.clikat.data.model.api.ProductListModel
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.data.model.others.FilterInputModel
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentSubcategoryGroceryBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.CartList
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.FilterResponseEvent
import com.codebrew.clikat.modal.other.FilterVarientData
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.addon_quant.SavedAddon
import com.codebrew.clikat.module.filter.BottomSheetFragment
import com.codebrew.clikat.module.product.ProductNavigator
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter
import com.codebrew.clikat.module.product_addon.AddonFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_subcategory_grocery.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import kotlinx.android.synthetic.main.toolbar_app.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/*
 * Created by cbl45 on 15/4/16.
 */
private const val FILTERDATA = 788


class ProductTabListing : BaseFragment<FragmentSubcategoryGroceryBinding, ProductTabViewModel>(),
        ProductNavigator, ProductListingAdapter.ProductCallback, DialogListener, View.OnClickListener, AddonFragment.AddonCallback {


    private lateinit var viewModel: ProductTabViewModel

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var mPreferenceHelper: PreferenceHelper

    private lateinit var mProductBinding: FragmentSubcategoryGroceryBinding

    private var subCategoryId: Int = 0
    private var categoryId: Int = 0

    private var exampleAllProduct: ProductListModel? = null
    private var supplierBranchId: Int? = 0
    private var mAdapter: ProductListingAdapter? = null


    private val list = mutableListOf<ProductDataBean>()
    private var mQuestionList= arrayListOf<QuestionList>()

    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    var varientData: FilterVarientData? = null

    private var isLoading = false

    private val selectedColor = Color.parseColor(Configurations.colors.tabSelected)
    private val unselectedColor = Color.parseColor(Configurations.colors.tabUnSelected)

    private var brandId: Int = 0

    private var viewType: Boolean = false
    private var hasSubcat: Boolean = false
    private var hasBrands: Boolean = false
    private var catName:String=""

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var appUtils: AppUtils

    @Inject
    lateinit var prodUtils: ProdUtils

    @Inject
    lateinit var mDialogsUtil: DialogsUtil

    private var parentPos: Int = 0

    private  val textConfig by lazy { appUtils.loadAppConfig(0).strings }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_subcategory_grocery
    }

    override fun getViewModel(): ProductTabViewModel {

        viewModel = ViewModelProviders.of(this, factory).get(ProductTabViewModel::class.java)
        return viewModel
    }

    override fun onFavStatus() {

        markFavList()
    }

    private fun markFavList() {
        when (list[parentPos].is_favourite) {
            1 -> {
                list[parentPos].is_favourite = 0
            }
            else -> {
                list[parentPos].is_favourite = 1
            }
        }
        mAdapter?.notifyItemChanged(parentPos)
    }

    override fun onErrorOccur(message: String) {
        llContainer.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProductBinding = viewDataBinding
        viewDataBinding.colors = Configurations.colors
        viewDataBinding.drawables = Configurations.drawables
        viewDataBinding.strings = textConfig



        screenFlowBean = mPreferenceHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        bookingFlowBean =  mPreferenceHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)

        initialization()

        productListObserver()

        if (arguments != null) {

            if (arguments?.containsKey("title") == true) {
                val title = arguments?.getString("title", "Select")
                tb_title.text = title
            }

            if (arguments?.containsKey("has_subcat") == true) {
                hasSubcat = arguments?.getBoolean("has_subcat") ?: false
            }

            if (arguments?.containsKey("has_brands") == true) {
                hasBrands = arguments?.getBoolean("has_brands") ?: false
                brandId = arguments?.getInt("brand_id") ?: 0
            }


            if (arguments?.containsKey("varientData") == true) {
                list.clear()

                varientData = arguments?.getParcelable("varientData")

                if (arguments?.containsKey("result") == true) {
                    list.addAll(arguments?.getParcelableArrayList("result")!!)

                    isLoading = true
                    refreshData()
                }

                if (searchView.text.toString().isNotEmpty())
                    mAdapter?.filter?.filter(searchView.text.toString())
            }
        }


        if (StaticFunction.isInternetConnected(activity)) {
            apiAllProducts(subCategoryId, supplierBranchId ?: 0)
        } else {
            StaticFunction.showNoInternetDialog(activity)
        }

        tb_back.setOnClickListener {

           Navigation.findNavController(view).popBackStack()

           // requireActivity().onBackPressed()

        }

        iv_grid_view.setOnClickListener(this)
        tv_filter.setOnClickListener(this)
        iv_list_view.setOnClickListener(this)

        setPriceLayout()

    }


    private fun refreshData() {

        if (isLoading) {
            if (list.size > 0) {
                changeProductList(list)
                recyclerview.layoutManager = setLayoutManager(viewType)
                mAdapter?.settingLayout(viewType)
                recyclerview.adapter = mAdapter
                mAdapter?.notifyDataSetChanged()
            }


            recyclerview.visibility = if (list.size > 0) View.VISIBLE else View.GONE
            noData.visibility = if (list.size == 0) View.VISIBLE else View.GONE

            if (searchView.text.toString().isNotEmpty())
                mAdapter?.filter?.filter(searchView.text.toString())

            view_product_rel.visibility = if (list.size > 0) View.VISIBLE else View.GONE

            searchView.visibility = if (list.size > 0) View.VISIBLE else View.GONE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        val bundle = arguments


        EventBus.getDefault().register(this)


        if (bundle != null) {


            if (bundle.containsKey("categoryId")) {
                subCategoryId = bundle.getInt("categoryId", 0)
            }

            if (bundle.containsKey("cat_id")) {
                categoryId = bundle.getInt("cat_id", 0)
            }

            if (bundle.containsKey("supplierId")) {
                supplierBranchId = bundle.getInt("supplierId", 0)
            }

            if(bundle.containsKey("question_list"))
            {
                mQuestionList= bundle.getParcelableArrayList<QuestionList>("question_list") ?: arrayListOf()
            }
        }
    }

    private fun initialization() {


        mAdapter = ProductListingAdapter(activity ?: requireContext(), list,appUtils)
        mAdapter?.settingCallback(this)
        mAdapter?.settingLayout(viewModel.isViewType.get())

        recyclerview.isNestedScrollingEnabled = false

        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                mAdapter?.filter?.filter(s)
                recyclerview.scrollToPosition(0)
                searchView.clearFocus()
             //  val  keyboard = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
              //  keyboard.hideSoftInputFromWindow(view?.windowToken,0)


            }
        })


    }


    private fun productListObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<ProductData> { resource ->
            // Update the UI, in this case, a TextView.
            handleProductList(resource)
        }

        viewModel.productLiveData.observe(viewLifecycleOwner, catObserver)

    }

    private fun handleProductList(resource: ProductData?) {
        if (resource?.product?.size!! > 0) {
            changeProductList(resource.product)
            setdata(resource)

            tv_search_count.text = getString(R.string.result_tag, resource.product!!.size)
            tv_search_count.visibility = if (resource.product!!.size > 0) View.VISIBLE else View.GONE

        }

        recyclerview.visibility = if (resource.product!!.size > 0) View.VISIBLE else View.GONE
        noData.visibility = if (resource.product!!.size == 0) View.VISIBLE else View.GONE

        view_product_rel.visibility = if (resource.product!!.size > 0) View.VISIBLE else View.GONE

        searchView.visibility = if (resource.product!!.size > 0) View.VISIBLE else View.GONE
    }


    private fun apiAllProducts(subCategoryId: Int, supplierBranchId: Int) {

        val inputModel = FilterInputModel()
        inputModel.languageId = StaticFunction.getLanguage(activity).toString()

        if (subCategoryId != 0 && hasSubcat)
            inputModel.subCategoryId?.add(subCategoryId)


        if(categoryId!=0)
        {
            inputModel.subCategoryId?.add(categoryId)
        }


        val mLocUser = dataManager.getGsonValue(DataNames.LocationUser, LocationUser::class.java)

        if (mLocUser != null) {
            inputModel.latitude = mLocUser.latitude ?: ""
            inputModel.longitude = mLocUser.longitude ?: ""
        }
        inputModel.low_to_high = 1.toString()
        inputModel.is_availability = 1.toString()
        inputModel.is_discount = 0.toString()
        inputModel.max_price_range = 100000.toString()
        inputModel.min_price_range = 0.toString()


        if (hasBrands) {
            inputModel.brand_ids?.add(brandId)
        }

        if(screenFlowBean?.app_type==AppDataType.HomeServ.type)
        {
            inputModel.need_agent =1
        }


        if (supplierBranchId != 0) {
            inputModel.supplier_ids?.add(supplierBranchId.toString())
        }

        /*if (viewModel.productLiveData.value != null) {
            handleProductList(viewModel.productLiveData.value)
        } else {*/
            if (isNetworkConnected) {
                viewModel.getProductList(inputModel,viewType)
            }
        //}

    }

    private fun changeProductList(product: MutableList<ProductDataBean>?) {


        for (j in product?.indices!!) {
            val dataProduct = product[j]
            val productId = dataProduct.product_id

            dataProduct.prod_quantity = StaticFunction.getCartQuantity(activity, productId)

            //for fixed price
            dataProduct.let {
                prodUtils.changeProductList(dataProduct)
            }

        }

    }

    private fun setdata(dataAllProduct: ProductData) {



        tv_filter.visibility = if (screenFlowBean?.app_type == AppDataType.Food.type) View.INVISIBLE else View.VISIBLE


        list.clear()

        list.addAll(dataAllProduct.product!!)


        tv_search_count.text = getString(R.string.result_tag, list.size)
        tv_search_count.visibility = View.VISIBLE

        recyclerview.layoutManager = setLayoutManager(viewModel.isViewType.get())

        iv_list_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
        iv_list_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)

        recyclerview.adapter = mAdapter

        exampleAllProduct?.data = dataAllProduct

    }

    private fun setLayoutManager(type: Boolean): RecyclerView.LayoutManager {

        viewType = type

        return if (type) GridLayoutManager(activity, 2) else LinearLayoutManager(activity)
    }


    override fun addToCart(position: Int, agentType: Boolean) {

        parentPos = position

        // if vendor status ==0 single vendor && vendor status==1 multiple vendor
        if (bookingFlowBean?.vendor_status == 0 &&  appUtils.checkVendorStatus(list[position].supplier_id)) {

            mDialogsUtil.openAlertDialog(activity
                    ?: requireContext(), getString(R.string.clearCart,textConfig?.supplier?:""), "Yes", "No", this)
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




    override fun onDestroy() {
        super.onDestroy()

        varientData = FilterVarientData()

        EventBus.getDefault().unregister(this)
    }

    override fun removeToCart(position: Int) {

        parentPos = position

        val mProduct=list[position]

        if (mProduct.adds_on?.isNotEmpty() == true) {
            val cartList: CartList? = dataManager.getGsonValue(DataNames.CART, CartList::class.java)

            val savedProduct = cartList?.cartInfos?.filter {
                it.supplierId == mProduct.supplier_id && it.productId == mProduct.product_id
            } ?: emptyList()

            SavedAddon.newInstance(mProduct, list[position].self_pickup
                    ?: -1, savedProduct, this).show(childFragmentManager, "savedAddon")

        } else {
            list[position]= mProduct.apply {   prodUtils.removeItemToCart(mProduct) }
            mAdapter?.notifyItemChanged(position)
        }

        setPriceLayout()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(filterVarientData: FilterResponseEvent) {

        varientData = filterVarientData.filterModel


        if (filterVarientData.status == "success") {

            list.clear()

            if (filterVarientData.productlist.size > 0) {

                list.addAll(filterVarientData.productlist)

                isLoading = true
                refreshData()


                if (searchView.text.toString().isNotEmpty())
                    mAdapter?.filter?.filter(searchView.text.toString())
            } else {
                refreshData()
            }

            tv_search_count.visibility = if (list.size > 0) View.VISIBLE else View.GONE
            tv_search_count.text = getString(R.string.result_tag, list.size)
        }


        mAdapter?.notifyDataSetChanged()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == FILTERDATA) {

            isLoading = false

            if (resultCode == Activity.RESULT_OK) {

                list.clear()


                if (data?.hasExtra("varientData") == true) {
                    varientData = data.getParcelableExtra("varientData")
                }

                if (data?.hasExtra("result") == true) {
                    list.addAll(data.getParcelableArrayListExtra("result")!!)

                    isLoading = true
                    refreshData()
                }

                if (list.size > 0)
                    mAdapter?.filter?.filter(searchView.text.toString())

            }
            if (resultCode == Activity.RESULT_CANCELED) {

                refreshData()
            }

            mAdapter?.notifyDataSetChanged()
        } else if (requestCode == AppConstants.REQUEST_WISH_PROD && resultCode == Activity.RESULT_OK) {
            markFavList()
        }
    }

    private fun setPriceLayout() {


        val appCartModel = appUtils.getCartData()

        bottom_cart.visibility = View.VISIBLE


        if (appCartModel.cartAvail) {

            tv_total_price.text = getString(R.string.total).plus(" ").plus(AppConstants.CURRENCY_SYMBOL).plus(" ").plus(appCartModel.totalPrice)
            tv_total_product.text = appCartModel.totalCount.toString().plus(" ").plus(getString(R.string.items))

            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener {

                navController(this@ProductTabListing).navigate(R.id.action_productTabListing_to_cart)

            }
        } else {
            bottom_cart.visibility = View.GONE
        }
    }


    override fun productDetail(bean: ProductDataBean?) {


        viewModel.setViewType(viewType)

        if (screenFlowBean?.app_type != AppDataType.Food.type) {
            val bundle = Bundle()
            bundle.putInt("productId", bean?.product_id ?: 0)
            bundle.putString("title", bean?.name)
            bundle.putInt("categoryId", bean?.category_id ?: 0)

            if (screenFlowBean?.app_type == AppDataType.Food.type) {

                bundle.putInt("categoryId", bean?.category_id ?: 0)
                bundle.putInt("supplierId", bean?.supplier_id ?: 0)
                bundle.putInt("branchId", bean?.supplier_branch_id ?: 0)

                navController(this@ProductTabListing)
                        .navigate(R.id.action_productTabListing_to_restaurantDetailFrag, bundle)
            } else {
                bean?.selectQuestAns=mQuestionList
                navController(this@ProductTabListing)
                        .navigate(R.id.actionProductDetail, ProductDetails.newInstance(bean, 0,true))
            }
        }
    }

    override fun publishResult(count: Int) {

        if (count == 0) {
            tv_search_count.visibility = View.GONE
        } else {
            tv_search_count.visibility = View.VISIBLE
            tv_search_count.text = getString(R.string.result_tag, count)
        }

    }

    override fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?) {

        parentPos = adapterPosition

        if (dataManager.getCurrentUserLoggedIn()) {
            if (isNetworkConnected) {
                viewModel.markFavProduct(productId, status)
            }
        } else {

            appUtils.checkLoginFlow(requireContext()).apply {
                (AppConstants.REQUEST_WISH_PROD)
            }
        }
    }


    private fun addCart(position: Int) {

        val mProduct=list[position]

        if(mProduct.is_question ==1)
        {
            mProduct.selectQuestAns=mQuestionList
        }

        mProduct.type=screenFlowBean?.app_type
        prodUtils.addItemToCart(mProduct).let {
            mAdapter?.notifyItemChanged(position)
        }

        setPriceLayout()
    }


    override fun onSucessListner() {

        appUtils.clearCart()

        list.map {
            it.prod_quantity=0
        }
        mAdapter?.notifyDataSetChanged()

        setPriceLayout()
    }

    override fun onErrorListener() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_grid_view -> {
                iv_list_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                iv_grid_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)

                recyclerview.layoutManager = setLayoutManager(true)
                mAdapter?.settingLayout(true)
                recyclerview.adapter = mAdapter
            }

            R.id.iv_list_view -> {
                iv_list_view.setColorFilter(selectedColor, PorterDuff.Mode.MULTIPLY)
                iv_grid_view.setColorFilter(unselectedColor, PorterDuff.Mode.MULTIPLY)
                recyclerview.layoutManager = setLayoutManager(false)
                mAdapter?.settingLayout(false)
                recyclerview.adapter = mAdapter
            }

            R.id.tv_filter -> {

                //Intent intent = new Intent(getActivity(), FilterScreenActivity.class);


                if (varientData == null) {
                    varientData = FilterVarientData()

                    varientData?.catId = categoryId
                    varientData?.isAvailability = true
                    varientData?.sortBy = "Price: Low to High"
                    varientData?.isDiscount = false
                    varientData?.maxPrice = 100000
                    varientData?.minPrice = 0
                    varientData?.catNames?.addAll(MainActivity.catNames)
                    varientData?.varientID?.clear()
                    varientData?.subCatId?.clear()
                    if (supplierBranchId != 0) {
                        varientData?.supplierId?.add(supplierBranchId.toString())
                    }
                    varientData?.subCatId?.add(subCategoryId)
                }

                //   intent.putExtra("varientData", varientData);

                // startActivityForResult(intent, FILTERDATA);

                val bottomSheetFragment = BottomSheetFragment()
                val bundle = Bundle()
                bundle.putParcelable("varientData", varientData)
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(childFragmentManager, "bottom_sheet")
            }
        }
    }




    override fun onAddonAdded(productModel: ProductDataBean) {
        if (productModel.adds_on?.isNullOrEmpty() == false && appUtils.checkProdExistance(productModel.product_id)) {
            val cartList: CartList? = appUtils.getCartList()
            productModel.prod_quantity = cartList?.cartInfos?.filter { productModel.product_id == it.productId }?.sumBy { it.quantity }
                    ?: 0
        }

        list[parentPos] = productModel

        mAdapter?.notifyItemChanged(parentPos)
        mAdapter?.notifyDataSetChanged()

        setPriceLayout()
    }


}
