package com.codebrew.clikat.module.dialog_adress

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.codebrew.clikat.BuildConfig
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.CommonUtils.Companion.isNetworkConnected
import com.codebrew.clikat.app_utils.extension.afterTextChanged
import com.codebrew.clikat.base.BaseDialog
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.SupplierLocationBean
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.DataBean
import com.codebrew.clikat.data.model.others.MapInputParam
import com.codebrew.clikat.data.preferences.PreferenceHelper
import com.codebrew.clikat.databinding.FragmentDialogAddressBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.modal.DataCommon
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.dialog_adress.adapter.*
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_dialog_address.*
import retrofit2.Retrofit
import javax.inject.Inject

const val LOCATION_PARAM = 157
private const val ARG_PARAM1 = "branchId"
private const val ARG_PARAM2 = "tableId"
class AddressDialogFragment : BaseDialog(), PlacesAutoCompleteAdapter.ClickListener, AddressNavigator {

private var is_table_enabled=0
    private var mListener: Listener? = null

    private lateinit var addressAdapter: AddressAdapter

    private lateinit var locationAdaptor: LocationAdaptor

    private var addresslist = mutableListOf<AddressBean>()

    private var locationslist = mutableListOf<SupplierLocation>()


    private var addressId = 0

    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var prefHelper: PreferenceHelper

    lateinit var addressBean: AddressBean

    lateinit var locationBean: SupplierLocation

    private var mAddressViewModel: AddressViewModel? = null

    private var mSupplierId = 0

    private var mAddressData: DataBean? = null

    private var mLocationData: SupplierLocationBean? = null

    private var ShowRestaurantPersonalAddress: String = "1"
private var table_name=""
    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    private var terminologyBean: SettingModel.DataBean.Terminology? = null
    private var settingData: SettingModel.DataBean.SettingData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentDialogAddressBinding>(inflater, R.layout.fragment_dialog_address, container, false)
        AndroidSupportInjection.inject(this)
        mAddressViewModel = ViewModelProviders.of(this, factory).get(AddressViewModel::class.java)
        binding.viewModel = mAddressViewModel
        binding.color = Configurations.colors

        mAddressViewModel?.navigator = this
        if(is_table_enabled==0)
        {
            binding.rlTable.visibility=View.GONE
            binding.rvAddressList.visibility=View.VISIBLE
        }else{
            binding. rlTable.visibility=View.VISIBLE
            binding. rvAddressList.visibility=View.GONE
        }
        binding.tvEnter.setOnClickListener {
    table_name=  binding.etNumber.text.toString()
    if(table_name.equals(""))
    {
        Toast.makeText(requireActivity(),"Enter table number", Toast.LENGTH_SHORT).show()
    }else{
        mListener?.onTableAdded(table_name)
        dismissDialog("dialog")
    }
}

        screenFlowBean = prefHelper.getGsonValue(DataNames.SCREEN_FLOW, SettingModel.DataBean.ScreenFlowBean::class.java)
        bookingFlowBean = prefHelper.getGsonValue(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)
        terminologyBean = prefHelper.getGsonValue(PrefenceConstants.APP_TERMINOLOGY, SettingModel.DataBean.Terminology::class.java)
        settingData = prefHelper.getGsonValue(DataNames.SETTING_DATA, SettingModel.DataBean.SettingData::class.java)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mSupplierId = it.getInt(ARG_PARAM1)
            is_table_enabled=it.getInt(ARG_PARAM2)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PARAM) {
            if (resultCode == Activity.RESULT_OK) {
                val mapInputParam = data?.getParcelableExtra<MapInputParam>("mapParam")

                // tvArea.text = mapInputParam?.first_address.toString()

                var hashMap: HashMap<String, String>? = null

                when (mapInputParam?.requestType) {
                    "add", "edit", "current" -> {

                        hashMap = HashMap()
                        // hashMap["name"] = prefHelper.getKeyValue(PrefenceConstants.USER_NAME, PrefenceConstants.TYPE_STRING).toString()
                        hashMap["latitude"] = mapInputParam.latitude
                        hashMap["longitude"] = mapInputParam.longitude
                        hashMap["addressLineFirst"] = mapInputParam.first_address
                        hashMap["customer_address"] = mapInputParam.second_address
                    }
                }


                if (isNetworkConnected(activity?.applicationContext ?: requireContext())) {

                    if (prefHelper.getCurrentUserLoggedIn()) {
                        when (mapInputParam?.requestType) {
                            "add", "current" -> {
                                hashMap?.let { mAddressViewModel?.addAddress(it) }
                            }
                            "edit" -> {
                                hashMap?.set("addressId", mapInputParam.addressId)
                                hashMap?.let { mAddressViewModel?.editAddress(it) }
                            }
                        }
                    } else {
                        addressBean = AddressBean()
                        addressBean.latitude = mapInputParam?.latitude ?: ""
                        addressBean.longitude = mapInputParam?.longitude ?: ""
                        addressBean.customer_address = """${mapInputParam?.second_address
                                ?: ""} , ${mapInputParam?.first_address ?: ""}"""
                        mListener?.onAddressSelect(addressBean)
                        dismiss()
                    }

                }

            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Places.initialize(activity ?: requireContext(), getString(R.string.google_map))


        if (settingData?.addRestaurantFloor=="1") {
            locationObserver()

            locationAdaptor = LocationAdaptor(requireActivity(),LocationListener { model ->

                if (!::locationBean.isInitialized) {
                    locationBean = SupplierLocation()
                    locationBean = model
                } else
                    locationBean = model


                locationBean.user_service_charge = mLocationData?.user_service_charge
                locationBean.preparation_time = mLocationData?.preparation_time
                locationBean.min_order = mLocationData?.min_order
                locationBean.base_delivery_charges = mLocationData?.base_delivery_charges

                mListener?.onLocationSelect(locationBean)
                dismissDialog("dialog")

            })

            rv_addressList.adapter = locationAdaptor

            locationAdaptor.addHeaderAndSubmitList(null, prefHelper.getCurrentUserLoggedIn())


        } else {

            addressObserver()
            editAddressObserver()
            deleteAddressObserver()
            addAddressObserver()

            addressAdapter = AddressAdapter(AddressListener(
                    { model ->

                        if (!::addressBean.isInitialized) {
                            addressBean = AddressBean()
                            addressBean = model
                        } else {
                            addressBean = model
                        }

                        addressBean.user_service_charge = mAddressData?.user_service_charge
                        addressBean.preparation_time = mAddressData?.preparation_time
                        addressBean.min_order = mAddressData?.min_order
                        addressBean.base_delivery_charges = mAddressData?.base_delivery_charges

                        // loadMapScreen("select", addressBean)
                        mListener?.onAddressSelect(addressBean)
                        dismissDialog("dialog")
                    },
                    { adapterView, addressBean ->

                        val popup = PopupMenu(activity, adapterView)
                        //inflating menu from xml resource
                        popup.inflate(R.menu.popup_address);
                        //adding click listener


                        popup.setOnMenuItemClickListener { menuItem ->

                            when (menuItem.itemId) {
                                R.id.menu_edit -> {
                                    loadMapScreen("edit", addressBean)
                                }

                                R.id.menu_delete -> {
                                    if (isNetworkConnected) {

                                        addressId = addressBean.id

                                        mAddressViewModel?.deleteAddress(addressBean.id.toString())
                                    }
                                }
                            }
                            true
                        }

                        //displaying the popup
                        popup.show()
                    }, { mType ->
                when (mType) {
                    "footer" -> {
                        loadMapScreen("add", null)
                    }
                    "header" -> {
                        loadMapScreen("current", null)
                    }
                }
            }),requireActivity())

            rv_addressList.adapter = addressAdapter

            val itemDecoration: ItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            rv_addressList.addItemDecoration(itemDecoration)

            val mAutoCompleteAdapter = PlacesAutoCompleteAdapter(activity)
            mAutoCompleteAdapter.setClickListener(this)

            ed_search.afterTextChanged {
                if (it.isEmpty()) {
                    rv_addressList.adapter = addressAdapter
                } else {
                    if (rv_addressList.adapter == addressAdapter) {
                        rv_addressList.adapter = mAutoCompleteAdapter
                    }

                    mAutoCompleteAdapter.filter.filter(it)
                }

            }

            addressAdapter.addHeaderAndSubmitList(null, prefHelper.getCurrentUserLoggedIn())

        }


        if (isNetworkConnected) {
            CommonUtils.setBaseUrl(BuildConfig.BASE_URL, retrofit)
            if (settingData?.addRestaurantFloor=="1")
                mAddressViewModel?.getSupplierLocations(mSupplierId)
            else
                mAddressViewModel?.getAddressList(mSupplierId)
        }

        iv_close.setOnClickListener {
            dismiss()
        }
    }

    private fun locationObserver() {

        // Create the observer which updates the UI.
        val catObserver = Observer<SupplierLocationBean> { resource ->

            mLocationData = resource

            locationslist.clear()

            locationslist.addAll(resource.supplier_locations)

            locationAdaptor.addHeaderAndSubmitList(locationslist, prefHelper.getCurrentUserLoggedIn())
        }

        mAddressViewModel?.supplierLocationsLiveData?.observe(this, catObserver)

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        mListener = if (parent != null) {
            parent as Listener
        } else {
            context as Listener
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (!::addressBean.isInitialized) {
            mListener?.onDestroyDialog()
        }
    }

    interface Listener {
        fun onAddressSelect(adrsBean: AddressBean)
        fun onLocationSelect(location: SupplierLocation)
        fun onTableAdded(table_name:String)
        fun onDestroyDialog()
    }

    private fun addressObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataBean> { resource ->

            mAddressData = resource

            addresslist.clear()

            addresslist.addAll(resource.address!!)

            addresslist.sortByDescending { it.id }

            addressAdapter.addHeaderAndSubmitList(addresslist, prefHelper.getCurrentUserLoggedIn())
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mAddressViewModel?.addressLiveData?.observe(this, catObserver)
    }

    private fun editAddressObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<AddressBean> { resource ->

            addresslist.mapIndexed { index, addressBean ->

                if (resource.id == addressBean.id) {
                    addresslist[index] = resource
                }
            }
            addressAdapter.addHeaderAndSubmitList(addresslist, prefHelper.getCurrentUserLoggedIn())
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mAddressViewModel?.updateAdrsLiveData?.observe(this, catObserver)
    }


    private fun deleteAddressObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<DataCommon> { resource ->


            val updated_list = addresslist.filter { it.id != addressId }

            addresslist.clear()

            addresslist.addAll(updated_list)

            addressAdapter.addHeaderAndSubmitList(addresslist, prefHelper.getCurrentUserLoggedIn())
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mAddressViewModel?.deleteAdrsData?.observe(this, catObserver)
    }


    private fun addAddressObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<AddressBean> { resource ->

            ed_search.setText("")

            addresslist.add(resource)

            addresslist.sortByDescending { it.id }

            addressAdapter.addHeaderAndSubmitList(addresslist, prefHelper.getCurrentUserLoggedIn())
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        mAddressViewModel?.addAdrsLiveData?.observe(this, catObserver)
    }


    private fun loadMapScreen(type: String, model: AddressBean?) {
        val intent = Intent(activity, SelectlocActivity::class.java)
        intent.putExtra("type", type)
        if (model != null) {
            intent.putExtra("addressData", model)
        }
        startActivityForResult(intent, LOCATION_PARAM)
    }

    override fun click(place: Place?) {

        if (!::addressBean.isInitialized) {
            addressBean = AddressBean()
        }
        addressBean.latitude = place?.latLng?.latitude?.toString()
        addressBean.longitude = place?.latLng?.longitude?.toString()
        addressBean.address_line_1 = place?.address

        loadMapScreen("add", addressBean)
    }

    companion object {
        fun newInstance(supplierId: Int): AddressDialogFragment =
                AddressDialogFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM1, supplierId)
                    }
                }
                    fun newInstance(is_table_enabled: Int,supplierId: Int): AddressDialogFragment =
                            AddressDialogFragment().apply {
                                arguments = Bundle().apply {
                                    putInt(ARG_PARAM1, supplierId)
                                    putInt(ARG_PARAM2, is_table_enabled)
                                }
                            }
    }

    override fun onErrorOccur(message: String) {

    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }
}
