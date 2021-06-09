package com.codebrew.clikat.module.home_screen.resturant_home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppToasty
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.loadUserImage
import com.codebrew.clikat.app_utils.navController
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.DeliveryType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentResturantHomeBinding
import com.codebrew.clikat.modal.LocationUser
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.module.home_screen.HomeFragment
import com.codebrew.clikat.module.home_screen.resturant_home.pickup.PickupResturantFrag
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_resturant_home.*
import kotlinx.android.synthetic.main.toolbar_home.*
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class ResturantHomeFrag : Fragment(), TabLayout.OnTabSelectedListener, AddressDialogFragment.Listener {


    private lateinit var tabNames: Array<String>

    @Inject
    lateinit var prefHelper: PreferenceHelper

    @Inject
    lateinit var appUtils: AppUtils

    private var clientInform: SettingModel.DataBean.SettingData? = null
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null

    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var mAdapter: PagerAdapter? = null

    private var ShowRestaurantPersonalAddress: kotlin.String = "1"



    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        clientInform = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)
        bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment


        val binding = DataBindingUtil.inflate<FragmentResturantHomeBinding>(inflater, R.layout.fragment_resturant_home, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = appUtils.loadAppConfig(0).strings
        
        return binding.root
    }
    override fun onTableAdded(table_name: String) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mlistFrag: List<Fragment>?

        mlistFrag = if (bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType) {
            tl_home.visibility=View.VISIBLE
            settingTabLayout()
            tl_home.addOnTabSelectedListener(this)
            listOf(HomeFragment.newInstance(), PickupResturantFrag.newInstance())
        } else {
            tl_home.visibility=View.GONE
            listOf(PickupResturantFrag.newInstance())
        }

        mAdapter = this@ResturantHomeFrag.let { PagerAdapter(it,mlistFrag) }
        //viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = mAdapter

        viewPager.isUserInputEnabled = false

        viewPager.isSaveFromParentEnabled = true


        if (bookingFlowBean?.is_pickup_order == FoodAppType.Both.foodType) {

            if (AppConstants.DELIVERY_OPTIONS == DeliveryType.PickupOrder.type)
            {
                tabSetting(tabNames[1])
            }else
            {
                tabSetting(tabNames[0])
            }
        }
        if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
            iv_supplier_logo.setImageResource(R.drawable.ic_back_home)

            iv_supplier_logo.setOnClickListener {
                navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_customHomeFrag)
            }
        } else {
            if (clientInform?.app_selected_template == null || clientInform?.app_selected_template == "0") {
                iv_supplier_logo.visibility = View.VISIBLE
                iv_supplier_logo.loadUserImage(clientInform?.logo_url ?: "")
            } else {
                iv_supplier_logo.visibility = View.GONE
            }
        }

    /*    TabLayoutMediator(tl_home, viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()*/

        updateLyt()


        if (ShowRestaurantPersonalAddress == "1") {
            toolbar_layout?.visibility = View.GONE
        } else
            toolbar_layout?.visibility = View.VISIBLE

    }





    private fun updateLyt() {

        if (prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java) != null) {
            val adrsData = prefHelper.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)!!

            tvArea.text = "${adrsData.address_line_1},${adrsData.customer_address}"
        } else {
            val mLocUser= prefHelper.getGsonValue(DataNames.LocationUser,LocationUser::class.java)
                    ?: return
            tvArea.text =  mLocUser.address?:""
        }
        val isUserLoggedIn = prefHelper.getCurrentUserLoggedIn()
        tvArea.setOnClickListener {
                AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
        }

        iv_search.setOnClickListener {
            navController(this@ResturantHomeFrag).navigate(R.id.action_resturantHomeFrag_to_searchFragment)
        }
        iv_notification.setOnClickListener {
            if (isUserLoggedIn)
                navController(this@ResturantHomeFrag).navigate(R.id.action_home_to_notificationFrag)
            else
                AppToasty.error(requireContext(), getString(R.string.login_first))
        }
    }




    private fun settingTabLayout() {

        tabNames = arrayOf(getString(R.string.delivery_txt), getString(R.string.pickup))
    }



    override fun onTabSelected(tab: TabLayout.Tab) {

        tabSetting(tabNames[tab.position])
    }

    override fun onAddressSelect(adrsBean: AddressBean) {

        val locUser = LocationUser((adrsBean.latitude
                ?: 0.0).toString(), (adrsBean.longitude
                ?: 0.0).toString(), "${adrsBean.customer_address} , ${adrsBean.address_line_1}")
        prefHelper.addGsonValue(DataNames.LocationUser, Gson().toJson(locUser))


        prefHelper.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsBean))
        tvArea.text ="${adrsBean.customer_address} , ${adrsBean.address_line_1}"

        //viewPager.adapter = null
        viewPager.adapter = mAdapter
    }

    override fun onLocationSelect(location: SupplierLocation) {

    }

    override fun onDestroyDialog() {

    }

    private fun tabSetting(tabName: String) {
        if (tabName == getString(R.string.delivery_txt)) {
            AppConstants.DELIVERY_OPTIONS= DeliveryType.DeliveryOrder.type
            viewPager.currentItem = 0
            tl_home.getTabAt(0)?.select()
        } else {
            AppConstants.DELIVERY_OPTIONS= DeliveryType.PickupOrder.type
            viewPager.currentItem = 1
            tl_home.getTabAt(1)?.select()
        }

        mAdapter?.notifyDataSetChanged()
    }


    override fun onTabUnselected(tab: TabLayout.Tab) {
       // tabSetting(tabNames[tab.position])
       // Log.e("onTabUnselected",tabNames[tab.position])
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
      //  Log.e("onTabReselected",tabNames[tab.position])
    }
}// Required empty public constructor
