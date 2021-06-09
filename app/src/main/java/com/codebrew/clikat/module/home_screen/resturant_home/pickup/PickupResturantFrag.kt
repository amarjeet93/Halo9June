package com.codebrew.clikat.module.home_screen.resturant_home.pickup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.AppDataManager
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentPickupResturantBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.other.*
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.home_screen.HomeNavigator
import com.codebrew.clikat.module.home_screen.HomeViewModel
import com.codebrew.clikat.module.home_screen.adapter.HomeItemAdapter
import com.codebrew.clikat.module.home_screen.adapter.HomeItemAdapter.SupplierListCallback
import com.codebrew.clikat.module.home_screen.adapter.SpecialListAdapter
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.ProgressBarDialog
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_pickup_resturant.*
import kotlinx.android.synthetic.main.layout_bottom_cart.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class PickupResturantFrag : BaseFragment<FragmentPickupResturantBinding, HomeViewModel>(), SupplierListCallback, HomeNavigator, SwipeRefreshLayout.OnRefreshListener {


    private var barDialog: ProgressBarDialog? = null
    @Inject
    lateinit var dataManager: AppDataManager

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    private lateinit var viewModel: HomeViewModel

    private var mBinding: FragmentPickupResturantBinding? = null
    private var homeItemAdapter: HomeItemAdapter? = null
    private var mSupplierList: MutableList<SupplierDataBean>? = null
    private var screenFlowBean: ScreenFlowBean? = null
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var settingData:SettingModel.DataBean.SettingData?=null
    private lateinit var pickupImage:Array<String?>
    private lateinit var bannerImage:IntArray
    var catId=0
    private var categoryModel: Data? = null

    @Inject
    lateinit var prefHelper: PreferenceHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this
        catId = if (prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().isNotEmpty()) {
            prefHelper.getKeyValue(PrefenceConstants.CATEGORY_ID, PrefenceConstants.TYPE_STRING).toString().toInt()
        } else {
            0
        }
        if (isNetworkConnected) {
            viewModel.getCategories()
        }
        categoryObserver()
        supplierObserver()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding

        mBinding?.color = Configurations.colors
        mBinding?.drawables = Configurations.drawables
        mBinding?.strings = appUtils.loadAppConfig(0).strings

        screenFlowBean = dataManager.getGsonValue(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)

        bookingFlowBean = dataManager.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        settingData = dataManager.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        if (isNetworkConnected) {
            viewModel.getCategories()
        }
        categoryObserver()
        values()

        if (isNetworkConnected) {
            viewModel.getSupplierList("2")
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
    private fun updateCategoryData(resource: Data?) {

        categoryModel = resource



    }

    override fun onResume() {
        super.onResume()

        showBottomCart()

        homeItemAdapter?.notifyDataSetChanged()
    }

    //  homeItemAdapter.settingPickupBanner();
    private fun values() {
        mSupplierList = ArrayList()
        barDialog = ProgressBarDialog(activity)
        homeItemAdapter = HomeItemAdapter(mSupplierList, 1, appUtils, settingData, "1")
        rv_resturant_list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        rv_resturant_list.adapter = homeItemAdapter
        homeItemAdapter!!.settingCallback(this)
        swiprRefresh.setOnRefreshListener(this)
        //  homeItemAdapter.settingPickupBanner();
    }// setViewType(HomeItemAdapter.SEARCH_TYPE, 0);

    //  rvSupplier.setVisibility(response.body().getData().size() > 0 ? View.VISIBLE : View.GONE);

    private fun setViewType(viewType: String, supplierCount: Int) {
        val itemModel = HomeItemModel()
        val dataBean = SupplierDataBean()
        dataBean.viewType = viewType
        if (viewType == HomeItemAdapter.BANNER_TYPE) {
            val bannerList: MutableList<TopBanner> = ArrayList()

            if (settingData?.pickup_url_one?.isEmpty() ==false && settingData?.pickup_url_two?.isEmpty()  ==false
                    && settingData?.pickup_url_three?.isEmpty()==false){
               pickupImage= arrayOf(settingData?.pickup_url_one,settingData?.pickup_url_two,settingData?.pickup_url_three)
                var bannerBean: TopBanner
                for (i1 in pickupImage) {
                    bannerBean = TopBanner()
                    bannerBean.pickupImage = i1
                    bannerBean.isEnabled = false
                    bannerList.add(bannerBean)
                }

            } else {
//                bannerImage = intArrayOf(R.drawable.ic_pickup_banner_2, R.drawable.ic_pickup_banner_3, R.drawable.ic_pickup_banner_1)
              //    val bannerImage = intArrayOf(R.drawable.ic_phar_1, R.drawable.ic_phar_2, R.drawable.ic_phar_3)
               //  val bannerImage = intArrayOf(R.drawable.ic_phar_1, R.drawable.ic_groc_2, R.drawable.ic_pickup_banner_1)
                // }
                if (categoryModel?.topBanner?.isNotEmpty() == true) {
                    bannerList.addAll(categoryModel?.topBanner ?: emptyList())
                }

//                var bannerBean: TopBanner
//                for (i1 in bannerImage) {
//                    bannerBean = TopBanner()
//                    bannerBean.bannerImage = i1
//                    bannerBean.isEnabled = false
//                    bannerList.add(bannerBean)
               // }

            }



            if (bannerList.size > 0) {
                itemModel.bannerWidth=1
                itemModel.bannerList = bannerList
            }
        } else if (viewType == HomeItemAdapter.FILTER_TYPE) {
            itemModel.supplierCount = supplierCount
        }
        itemModel.screenType = screenFlowBean!!.app_type
        dataBean.itemModel = itemModel
        mSupplierList!!.add(dataBean)
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
            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
            // setViewType(HomeItemAdapter.SEARCH_TYPE, 0);
            setViewType(HomeItemAdapter.FILTER_TYPE, resource.size)
            for (i in resource.indices) {
                resource[i].viewType = HomeItemAdapter.SUPL_TYPE
                mSupplierList!!.add(resource[i])
            }
            homeItemAdapter!!.notifyDataSetChanged()
        } else {
            setViewType(HomeItemAdapter.BANNER_TYPE, 0)
            setViewType(HomeItemAdapter.FILTER_TYPE, 0)
        }
    }
    override fun unFavSupplierResponse(data: SupplierInArabicBean?) {

    }

    override fun favSupplierResponse(supplierId: SupplierInArabicBean?) {
    }

    private fun showBottomCart() {

        val appCartModel = appUtils.getCartData()

        if (appCartModel.cartAvail) {

            bottom_cart.visibility = View.GONE

            tv_total_price.text = String.format("%s %s %s", getString(R.string.total), AppConstants.CURRENCY_SYMBOL, appCartModel.totalPrice)

            tv_total_product.text = getString(R.string.total_item_tag, appCartModel.totalCount)


            if (appCartModel.supplierName.isNotEmpty() && screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
                tv_supplier_name.visibility = View.VISIBLE
                tv_supplier_name.text = getString(R.string.supplier_tag, appCartModel.supplierName)
            } else {
                tv_supplier_name.visibility = View.GONE
            }

            bottom_cart.setOnClickListener { v ->


                val navOptions: NavOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.pickupResturantFrag, false)
                        .build()


                if (bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType) {
                    navController(this@PickupResturantFrag).navigate(R.id.action_resturantHomeFrag_to_cart, null, navOptions)
                } else {
                    navController(this@PickupResturantFrag).navigate(R.id.action_pickupResturantFrag_to_cart, null, navOptions)
                }

            }
        } else {
            bottom_cart.visibility = View.GONE
        }

    }


    override fun onSupplierDetail(supplierBean: SupplierDataBean?) {
        val bundle = bundleOf("supplierId" to supplierBean?.id
        , "deliveryType" to "pickup")
        if (settingData?.app_selected_template != null
                && settingData?.app_selected_template == "1")
            Navigation.findNavController(requireView()).navigate(R.id.restaurantDetailFragNew, bundle)
        else
        Navigation.findNavController(requireView()).navigate(R.id.restaurantDetailFrag, bundle)
    }

    override fun onSpclView(specialListAdapter: SpecialListAdapter?) {}
    override fun onFilterScreen() {
        Navigation.findNavController(view!!).navigate(R.id.action_pickupResturantFrag_to_bottomSheetFragment)
    }

    override fun onSearchItem(text: String?) {

    }

    override fun onHomeCategory(position: Int) {

    }

    override fun onViewMore() {
        Navigation.findNavController(view!!).navigate(R.id.action_pickupResturantFrag_to_offerProductListingFragment)
    }

    companion object {
        fun newInstance() = PickupResturantFrag()
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_pickup_resturant
    }

    override fun getViewModel(): HomeViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        return viewModel
    }

    override fun onFavStatus() {

    }


    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    override fun onRefresh() {
        swiprRefresh.isRefreshing = false
        mSupplierList?.clear()
        if (isNetworkConnected) {
            viewModel.getSupplierList("2")
        }
    }
}