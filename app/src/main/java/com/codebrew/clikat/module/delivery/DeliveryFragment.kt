package com.codebrew.clikat.module.delivery


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.codebrew.clikat.R
import com.codebrew.clikat.module.login.LoginActivity
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.app_utils.extension.launchActivity
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.data.AppDataManager
import com.codebrew.clikat.data.constants.PrefenceConstants
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.data.model.api.CustomerAddressModel
import com.codebrew.clikat.data.model.api.DataBean
import com.codebrew.clikat.databinding.FragmentDeliveryBinding
import com.codebrew.clikat.fragments.DatePickerFragment
import com.codebrew.clikat.fragments.OrderSummaryFragment
import com.codebrew.clikat.fragments.TimePickerFragment
import com.codebrew.clikat.modal.*
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.dialog_adress.AddressDialogFragment
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.retrofit.RestClient
import com.codebrew.clikat.utils.*
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.gson.Gson
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_delivery.*
import kotlinx.android.synthetic.main.layout_adrs_time.*
import kotlinx.android.synthetic.main.toolbar_app.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/*
 * Created by cbl80 on 5/5/16.
 */
class DeliveryFragment : Fragment(), RadioGroup.OnCheckedChangeListener, AddressDialogFragment.Listener, View.OnClickListener {


    private var deliveryType = DataNames.DELIVERY_TYPE_STANDARD
    private var supplierBranchId: Int = 0

    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var deliveryId: Int? = null
    var mYear: Int = 0
    var dateUTC = ""
    private var time24Format: String? = null
    private var deliveryAdress: String? = null
    private var deliveryName: String? = null
    private var data: DataBean? = null
    private var exampleAllAdresses: CustomerAddressModel? = null
    private var maxDeliveryCharge = 0f
    private var maxUrgentPrice = 0f
    private var isUrgentVisible = true
    private var hourOfDay: Int = 0
    private var barDialog: ProgressBarDialog? = null

    private val tabUnselected = Color.parseColor(Configurations.colors.tabUnSelected)


    lateinit var adrsData: AddressBean

    lateinit var viewDataBinding: FragmentDeliveryBinding

    @Inject
    lateinit var dataManger: AppDataManager


    internal var onTimeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute11 ->
        var getmin11: Long = 0

        try {
            getmin11 = GeneralFunctions.getmin(mYear.toString() + "-" + String.format("%02d", mMonth) + "-" + String.format("%02d", mDay) + " "
                    + String.format("%02d", hour) + ":" + String.format("%02d", minute11))
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val now = Calendar.getInstance()

        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            val time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
            val date1 = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
            try {
                val da = GeneralFunctions.getDayOfweek11("$date1 $time")
                now.time = da
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }

        now.add(Calendar.MINUTE, exampleAllAdresses!!.data!!.delivery_max_time!!)

        if (getmin11 > now.timeInMillis) {
            time24Format = hour.toString() + ":" + String.format("%02d", minute11)

            val time = GeneralFunctions.get12HrFormatedtime(time24Format!!)
            tvTime.text = time

            try {
                dateUTC = GeneralFunctions.getFormattedUTC(mYear.toString() + "-" + String.format("%02d", mMonth) + "-" + String.format("%02d", mDay) + " "
                        + String.format("%02d", hour) + ":" + String.format("%02d", minute11))
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        } else {
            StaticFunction.sweetDialogueFailure(activity, getString(R.string.alert), getString(R.string.wrong_date_selection), false, 1000,false)
        }
    }

    private var dateYYMMDD: String? = null
    internal var ondate: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        mYear = year
        mMonth = monthOfYear + 1
        mDay = dayOfMonth

        dateYYMMDD = mYear.toString() + "-" + String.format("%02d", mMonth) + "-" + String.format("%02d", mDay)
        tvDate!!.text = GeneralFunctions.getFormattedDate("$year-$mMonth-$dayOfMonth")
        if (tvTime!!.text.toString().isEmpty()) {
            showTimePicker()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentDeliveryBinding>(inflater, R.layout.fragment_delivery, container, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings

        viewDataBinding = binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //((MainActivity)getActivity()).tvTitleMain.setText(getString(R.string.delivery,textConfig.order));


        settypeface()
        val bundle = arguments


        if (bundle != null) {
            supplierBranchId = bundle.getInt(DataNames.SUPPLIER_BRANCH_ID, 0)

        }



        barDialog = ProgressBarDialog(activity)
        if (exampleAllAdresses != null) {
            setdataAPI()
        } else {
            if (StaticFunction.isInternetConnected(activity)) {
                apiGetAllAddress(supplierBranchId)
            } else {
                StaticFunction.showNoInternetDialog(activity!!)
            }
        }

        tv_change_adrs.setOnClickListener {
            if (dataManger.getCurrentUserLoggedIn()) {
                AddressDialogFragment.newInstance(0).show(childFragmentManager, "dialog")
            } else {
                activity?.launchActivity<LoginActivity>()
            }
        }


        if (dataManger.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java) != null) {
            adrsData = dataManger.getGsonValue(PrefenceConstants.ADRS_DATA, AddressBean::class.java)!!
            tv_deliver_adrs.text = adrsData.address_line_1 + "," + adrsData.customer_address
        } else {
            tv_deliver_adrs.text = "No Location"
        }


        clickListner()


        tb_title.text = getString(R.string.delivery, Configurations.strings.order)

        tb_back.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

    }

    private fun clickListner() {

        btnContinue.setOnClickListener(this)
        tvDate.setOnClickListener(this)
        tvTime.setOnClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)


    }

    private fun showDatePicker() {
        val date = DatePickerFragment()

        /**
         * Set Up Current Date Into dialog
         */

        val now = Calendar.getInstance()

        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            val time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
            val date1 = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
            try {
                val da = GeneralFunctions.getDayOfweek11("$date1 $time")
                now.time = da
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }

        now.add(Calendar.MINUTE, exampleAllAdresses!!.data!!.delivery_max_time!!)


        val args = Bundle()
        args.putInt("year", now.get(Calendar.YEAR))
        args.putInt("month", now.get(Calendar.MONTH))
        args.putInt("day", now.get(Calendar.HOUR_OF_DAY))
        args.putLong("minTime", now.timeInMillis)
        date.arguments = args
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate)
        date.show(childFragmentManager, "Date Picker")
        llDateTime!!.visibility = View.VISIBLE

    }


    private fun showTimePicker() {
        val time = TimePickerFragment()
        /**
         * Set Up Current Time Into dialog
         */

        /**
         * Set Call back to capture selected Time
         */
        time.setCallBack(onTimeSetListener)
        time.show(childFragmentManager, "Time Picker")
    }

    override fun onResume() {
        super.onResume()

        //  ((MainActivity)getActivity()).setIconsCart(true);
        //  ((MainActivity)getActivity()).tvTitleMain.setText(getString(R.string.delivery,textConfig.order));

        if (exampleAllAdresses != null) {
            setdataAPI()
        }

    }

    private fun apiGetAllAddress(supplierBranchId: Int) {

        barDialog!!.show()

        val signUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
        val hashMap = HashMap<String, String>()
        hashMap["accessToken"] = signUp!!.data.access_token
        hashMap["languageId"] = "" + StaticFunction.getLanguage(activity)
        hashMap["supplierBranchId"] = "" + supplierBranchId
        //hashMap["areaId"] = "" + user!!.areaID

        val addresCall = RestClient.getModalApiService(activity)
                .getAllAddress(hashMap)
        addresCall.enqueue(object : Callback<CustomerAddressModel> {
            override fun onResponse(call: Call<CustomerAddressModel>, response: Response<CustomerAddressModel>) {
                barDialog!!.dismiss()
                exampleAllAdresses = response.body()
                if (exampleAllAdresses!!.status == ClikatConstants.STATUS_SUCCESS) {
                    exampleAllAdresses!!.data!!.min_order = arguments!!.getFloat(DataNames.MIN_ORDER)
                    setdataAPI()

                } else if (exampleAllAdresses!!.status == ClikatConstants.STATUS_INVALID_TOKEN) {
                    ConnectionDetector(activity).loginExpiredDialog()
                } else {
                    GeneralFunctions.showSnackBar(view, exampleAllAdresses!!.message, activity)
                }
            }

            override fun onFailure(call: Call<CustomerAddressModel>, t: Throwable) {
                GeneralFunctions.showSnackBar(view, t.message, activity)
                barDialog!!.dismiss()
            }
        })
    }


    private fun setdataAPI() {


        val bookingFlowBean = Prefs.with(activity).getObject(DataNames.BOOKING_FLOW, SettingModel.DataBean.BookingFlowBean::class.java)


        // adresses adapter Setup


        // deliveryType Setup
        data = exampleAllAdresses!!.data


        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.LOYALITY_POINT_FLOW) {

            //
            //rbPostpone.setVisibility(View.GONE);
            rbStandard!!.visibility = View.GONE
            rbUrgent!!.visibility = View.GONE
            tvSpeed!!.visibility = View.GONE

        } else {

            val cartList: CartList
            cartList = suuplierimage()

            var maxUrgentPerValue = 0f
            val maxUregentPerPrice: Float
            var maxUregtnPriceTotal = 0f
            var maxUrgentValueNormal = 0f
            for (i in 0 until (cartList.cartInfos?.size ?: 0)) {

                //for percentage
                if (cartList.cartInfos?.get(i)?.urgent_type == 1) {
                    //cart Total price

                    maxUregtnPriceTotal += cartList.cartInfos?.get(i)?.price?.times(cartList.cartInfos?.get(i)?.quantity
                            ?: 0) ?: 0.0f
                    //maximum urgent price
                    if (cartList.cartInfos?.get(i)?.urgentValue ?: 0.0f > maxUrgentPerValue) {
                        maxUrgentPerValue = cartList.cartInfos?.get(i)?.urgentValue ?: 0.0f
                    }
                } else {
                    // for fixed price
                    // maximum urgent price
                    if (cartList.cartInfos?.get(i)?.urgentValue ?: 0.0f > maxUrgentPerValue) {
                        maxUrgentValueNormal = cartList.cartInfos?.get(i)?.urgentValue ?: 0.0f
                    }
                }

            }
            // for getting the percentage value of urgent price
            //(maximum urgent_price * cartTotal) /100
            maxUregentPerPrice = maxUrgentPerValue * maxUregtnPriceTotal / 100

            // for getting the fixed price amount
            maxUrgentPrice = maxUregentPerPrice + maxUrgentValueNormal
            for (i in 0 until (cartList.cartInfos?.size ?: 0)) {
                if (cartList.cartInfos?.get(i)?.deliveryCharges ?: 0.0f > maxDeliveryCharge) {
                    maxDeliveryCharge = cartList.cartInfos?.get(i)?.deliveryCharges ?: 0.0f
                }
            }

            //clear urgent price when non-urgent price is added into cart
            for (i in 0 until (cartList.cartInfos?.size ?: 0)) {
                if (cartList.cartInfos?.get(i)?.isUrgent == 0) {
                    isUrgentVisible = false
                    maxUrgentPrice = 0f
                    break
                }
            }

        }

        setData(data!!.standard, data!!.urgent,
                data!!.postpone, "" + maxDeliveryCharge,
                data!!.urgent)

        if (data!!.is_urgent != null && data!!.is_urgent == 0) {
            rbUrgent!!.visibility = View.GONE
            data!!.urgent = ""
        }


        data!!.postpone = ""
        rbPostpone!!.visibility = if (bookingFlowBean!!.is_scheduled == 0) View.GONE else View.VISIBLE

        if (Prefs.with(activity).getBoolean(DataNames.AGENT_TYPE, false)) {
            rbPostpone!!.visibility = View.GONE
        }

    }

    private fun suuplierimage(): CartList {
        val cartList: CartList
        val cati = Prefs.with(activity).getString(DataNames.CATEGORY_ID, "0")
        var cat = 0
        try {
            cat = Integer.parseInt(cati)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            cartList = StaticFunction.allCartlAUNDRY(activity)
            val image = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO, "")
            val supplierI = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_ID, "")
            val supplierName = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_NAME, "")
            val branchId = Prefs.with(activity).getString(DataNames.SUPPLIER_LOGO_BRANCH_ID, "")
            (activity as MainActivity).setSupplierImage(true, image, Integer.parseInt(supplierI), supplierName, Integer.parseInt(branchId), cat)
        } else {
            cartList = StaticFunction.allCart(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
            if (cartList.cartInfos?.size ?: 0 > 0) {
                /*        ((MainActivity) getActivity()).setSupplierImage(true, cartList.getSupplierImage(), cartList.getCartInfos().get(0)
                                .getSupplierId(), cartList.getSupplierName(), cartList.getCartInfos().get(0).getSuplierBranchId()
                        , cat);*/
            }
        }
        return cartList
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvTime -> showTimePicker()

            R.id.tvDate -> showDatePicker()


            R.id.btnContinue -> {
                deliveryType = 0
                if (rdGroup!!.checkedRadioButtonId == R.id.rbStandard) {
                    deliveryType = DataNames.DELIVERY_TYPE_STANDARD

                    Prefs.with(activity).save(DataNames.DELIVERY_MAX_TIME, exampleAllAdresses!!.data!!.delivery_max_time)

                } else if (rdGroup!!.checkedRadioButtonId == R.id.rbUrgent) {
                    deliveryType = DataNames.DELIVERY_TYPE_URGENT
                } else if (rdGroup!!.checkedRadioButtonId == R.id.rbPostpone) {
                    deliveryType = DataNames.DELIVERY_TYPE_POSTPONED
                }
                Prefs.with(activity).save(DataNames.DELIVERY_DATE, tvDate!!.text.toString())
                Prefs.with(activity).save(DataNames.DELIVERY_TIME, tvTime!!.text.toString())
                val now = Calendar.getInstance()
                val date: Date
                date = now.time
                val dayFormat = SimpleDateFormat("MMM, d EEEE", Locale.getDefault())
                val day = dayFormat.format(date)
                Prefs.with(activity).save(DataNames.CREATED_DATE, day)


                if (!::adrsData.isInitialized) {
                    viewDataBinding.root.onSnackbar(getString(R.string.selectAdress))
                } else {
                    deliveryId = adrsData.id
                    deliveryAdress = adrsData.customer_address
                    deliveryName = adrsData.address_line_1


                    if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.LOYALITY_POINT_FLOW) {
                        val loyalityPoints = StaticFunction.getLoyalityCart(activity)
                        loyalityPoints?.deliveryAddressId = deliveryId!!
                        loyalityPoints?.deliveryDate = dateYYMMDD
                        loyalityPoints?.deliveryType = deliveryType
                        if (deliveryType == DataNames.DELIVERY_TYPE_POSTPONED)
                            loyalityPoints?.is_postponed = 1
                        else
                            loyalityPoints?.is_postponed = 0
                        if (deliveryType == DataNames.DELIVERY_TYPE_URGENT) {
                            loyalityPoints?.urgent = 1
                            loyalityPoints?.urgentPrice = exampleAllAdresses!!.data!!.urgentPrice!!
                        } else {
                            loyalityPoints?.urgent = 0
                            loyalityPoints?.urgentPrice = 0f
                        }
                        StaticFunction.saveLoyalityCart(activity, loyalityPoints
                                ?: CartLoyalityPoints())


                        val bundle = Bundle()
                        bundle.putString("deliveryAddress", deliveryAdress)
                        bundle.putInt("deliveryId", deliveryId!!)
                        bundle.putString("deliveryDate", tvDate!!.text.toString())
                        bundle.putString("deliveryTime", tvTime!!.text.toString())
                        bundle.putString("deliveryName", deliveryName)
                        bundle.putFloat("deliveryCharges", maxDeliveryCharge)
                        bundle.putInt("paymentMethod", exampleAllAdresses!!.data!!.payment_method!!)

                        bundle.putFloat("urgentPrice", maxUrgentPrice)

                        bundle.putInt("deliveryType", deliveryType)

                        if (arguments != null && arguments!!.containsKey("prodList")) {
                            bundle.putParcelable("prodList", arguments!!.getParcelable<CartInfoServer>("prodList"))
                        }

                        val orderSummaryFragment = OrderSummaryFragment()
                        orderSummaryFragment.arguments = bundle

                        (activity as MainActivity).pushFragments(DataNames.TAB1, orderSummaryFragment,
                                true, true, "", true)

                    } else {
                        val netTotal: Float
                        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
                            netTotal = StaticFunction.netTotalLaundry(activity)
                        } else {
                            netTotal = StaticFunction.netTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
                        }
                        var netTotalDum = netTotal + maxDeliveryCharge
                        if (deliveryType == DataNames.DELIVERY_TYPE_URGENT) {
                            netTotalDum = netTotal + maxUrgentPrice
                        }

                        // if (data!!.minOrder!! <= netTotalDum) {


                        val pickupId = 0
                        val pickupTime = ""
                        val pickupDate = ""
                        apiUpdateCartInfo(pickupId, pickupTime, pickupDate)


                    }
                }
            }
        }
    }


    private fun settypeface() {

        (view!!.findViewById<View>(R.id.tvSpeed) as TextView).typeface = AppGlobal.regular
        (view!!.findViewById<View>(R.id.tvDate) as TextView).typeface = AppGlobal.regular
        (view!!.findViewById<View>(R.id.tvTime) as TextView).typeface = AppGlobal.regular
        rbStandard!!.typeface = AppGlobal.regular
        rbUrgent!!.typeface = AppGlobal.regular
        rbPostpone!!.typeface = AppGlobal.regular
    }


    private fun setData(stan: String?, urg: String?,
                        post: String?,
                        deliveryCharges: String,
                        urgetCharges: String?) {

        rdGroup!!.setOnCheckedChangeListener(this)
        llDateTime!!.visibility = View.GONE
        deliveryType = DataNames.DELIVERY_TYPE_STANDARD
        val now = Calendar.getInstance()
        hourOfDay = now.get(Calendar.HOUR_OF_DAY)
        var time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
        if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
            val date = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
            try {
                val da = GeneralFunctions.getDayOfweek11("$date $time")
                now.time = da
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
        now.add(Calendar.MINUTE, exampleAllAdresses!!.data!!.delivery_max_time!!)
        hourOfDay = now.get(Calendar.HOUR_OF_DAY)

        time24Format = hourOfDay.toString() + ":" + String.format("%02d", now.get(Calendar.MINUTE))
        time = GeneralFunctions.get12HrFormatedtime(time24Format!!)
        tvTime!!.text = time
        val date: Date
        date = now.time
        @SuppressLint("SimpleDateFormat") val dayFormat = SimpleDateFormat("MMM, d EEEE")
        val day = dayFormat.format(date)
        tvDate!!.text = day
        val dayFormat2 = SimpleDateFormat("yyyy-MM-dd")
        dateYYMMDD = dayFormat2.format(date)

        var spannable: Spannable
        var startIndex: Int
        stan!!.replace("'", "")
        val standard = stan + "\n" +
                activity!!.getString(R.string.delivery_charges) + " " + deliveryCharges + " " + StaticFunction.getCurrency(activity)
        spannable = SpannableString(standard)
        startIndex = stan.length
        spannable.setSpan(RelativeSizeSpan(.8.toFloat()), startIndex, standard.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(tabUnselected), startIndex, standard.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        startIndex = standard.indexOf(deliveryCharges)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, standard.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        rbStandard!!.text = spannable


        if (urgetCharges != null && !urgetCharges.isEmpty()) {
            val price = maxUrgentPrice

            val urgent = (urg + "\n" +
                    activity!!.getString(R.string.delivery_charges)
                    + " " + (price.toString() + "") + " " + StaticFunction.getCurrency(activity))
            spannable = SpannableString(urgent)
            startIndex = urg!!.length
            spannable.setSpan(RelativeSizeSpan(.8.toFloat()), startIndex, urgent.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(tabUnselected), startIndex, urgent.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            startIndex = urgent.indexOf(price.toString() + "")
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + (price.toString() + "").length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            rbUrgent!!.text = spannable
        } else {
            rbUrgent!!.visibility = View.GONE
        }

        /*   if (post != null) {
            String postpone = post + "\n" +
                    getActivity().getString(R.string.delivery_charges_applicable);
            spannable = new SpannableString(postpone);
            startIndex = post.length();
            spannable.setSpan(new RelativeSizeSpan((float) .8), startIndex, postpone.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(tabUnselected), startIndex, postpone.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            rbPostpone.setText(spannable);
        }*/


    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (checkedId == R.id.rbPostpone) {
            deliveryType = DataNames.DELIVERY_TYPE_POSTPONED


            //Postpone for delivery date & time
            //Changes to show calendar instead of postpone delivery

            /*            Calendar now = Calendar.getInstance();
            hourOfDay = now.get(Calendar.HOUR_OF_DAY);
            String time = Prefs.with(getActivity()).getString(DataNames.PICKUP_TIME1, "");
            if ((Prefs.with(getActivity()).getInt(DataNames.FLOW_STROE, 0)) == DataNames.FLOW_LAUNDRY) {
                String date = Prefs.with(getActivity()).getString(DataNames.PICKUP_DATE, "");
                try {
                    Date da = GeneralFunctions.getDayOfweek11(date + " " + time);
                    now.setTime(da);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            now.add(Calendar.MINUTE, (int) exampleAllAdresses.getData().getDelivery_max_time());
            hourOfDay = now.get(Calendar.HOUR_OF_DAY);

            time24Format = hourOfDay + ":" + String.format("%02d", now.get(Calendar.MINUTE));
            time = GeneralFunctions.get12HrFormatedtime(time24Format);
            tvTime.setText(time);
            Date date;
            date = now.getTime();
            SimpleDateFormat dayFormat = new SimpleDateFormat("MMM, d EEEE");
            String day = dayFormat.format(date);
            tvDate.setText(day);
            mYear = now.get(Calendar.YEAR);
            mMonth = now.get(Calendar.MONTH);
            SimpleDateFormat dayFormat2 = new SimpleDateFormat("yyyy-MM-dd");
            dateYYMMDD = dayFormat2.format(date);*/


        } else if (checkedId == R.id.rbUrgent) {
            llDateTime!!.visibility = View.GONE
            deliveryType = DataNames.DELIVERY_TYPE_URGENT
            val now = Calendar.getInstance()
            hourOfDay = now.get(Calendar.HOUR_OF_DAY)
            var time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
            if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
                val date = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
                try {
                    val da = GeneralFunctions.getDayOfweek11("$date $time")
                    now.time = da
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

            }
            now.add(Calendar.MINUTE, exampleAllAdresses!!.data!!.delivery_max_time!!)
            hourOfDay = now.get(Calendar.HOUR_OF_DAY)

            time24Format = hourOfDay.toString() + ":" + String.format("%02d", now.get(Calendar.MINUTE))
            time = GeneralFunctions.get12HrFormatedtime(time24Format!!)
            tvTime!!.text = time
            val date: Date
            date = now.time
            val dayFormat = SimpleDateFormat("MMM, d EEEE")
            val day = dayFormat.format(date)
            tvDate!!.text = day
            val dayFormat2 = SimpleDateFormat("yyyy-MM-dd")
            dateYYMMDD = dayFormat2.format(date)
        } else {
            llDateTime!!.visibility = View.GONE
            deliveryType = DataNames.DELIVERY_TYPE_STANDARD
            val now = Calendar.getInstance()
            hourOfDay = now.get(Calendar.HOUR_OF_DAY)
            var time = Prefs.with(activity).getString(DataNames.PICKUP_TIME1, "")
            if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
                val date = Prefs.with(activity).getString(DataNames.PICKUP_DATE, "")
                try {
                    val da = GeneralFunctions.getDayOfweek11("$date $time")
                    now.time = da
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

            }
            now.add(Calendar.MINUTE, exampleAllAdresses!!.data!!.delivery_max_time!!)
            Prefs.with(activity).save(DataNames.DELIVERY_MAX_TIME, exampleAllAdresses!!.data!!.delivery_max_time)
            hourOfDay = now.get(Calendar.HOUR_OF_DAY)

            time24Format = hourOfDay.toString() + ":" + String.format("%02d", now.get(Calendar.MINUTE))
            time = GeneralFunctions.get12HrFormatedtime(time24Format!!)
            tvTime!!.text = time
            val date: Date
            date = now.time
            val dayFormat = SimpleDateFormat("MMM, d EEEE")
            val day = dayFormat.format(date)
            tvDate!!.text = day
            val dayFormat2 = SimpleDateFormat("yyyy-MM-dd")
            dateYYMMDD = dayFormat2.format(date)
        }
    }

    private fun apiUpdateCartInfo(pickupId: Int, pickupTime: String, pickupDate: String) {
        if (StaticFunction.isInternetConnected(activity)) {
            val pojoSignUp = Prefs.with(activity).getObject(DataNames.USER_DATA, PojoSignUp::class.java)

            barDialog!!.show()
            if (deliveryType == DataNames.DELIVERY_TYPE_URGENT) {
                maxDeliveryCharge = 0f
            } else {
                maxUrgentPrice = 0f
            }
            val hashMap = HashMap<String, String>()
            hashMap["accessToken"] = "" + pojoSignUp!!.data.access_token
            hashMap["cartId"] = "" + Prefs.with(activity).getString(DataNames.CART_ID, "0")
            hashMap["deliveryType"] = "" + deliveryType
            hashMap["deliveryId"] = "" + deliveryId!!


            if (!dateYYMMDD!!.trim { it <= ' ' }.isEmpty()) {
                hashMap["deliveryDate"] = "" + dateYYMMDD!!
                hashMap["deliveryTime"] = "" + time24Format!!
            }
            val c = Calendar.getInstance()
            try {
                c.time = GeneralFunctions.getDayOfweek(dateYYMMDD) // yourdate is a object of type Date
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            var dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == 1) {
                dayOfWeek = 6
            } else {
                dayOfWeek -= 2
            }
            hashMap["day"] = "" + dayOfWeek

            val cartList: CartList
            if (Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0) == DataNames.FLOW_LAUNDRY) {
                cartList = StaticFunction.allCartlAUNDRY(activity)
            } else {
                cartList = StaticFunction.allCart(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))
            }
            var handlingAdmin = 0f
            var handlingSupplier = 0f
            for (i in 0 until (cartList.cartInfos?.size ?: 0)) {

                if (cartList.cartInfos?.get(i)?.handlingSupplier ?: 0.0f > handlingSupplier) {
                    handlingSupplier = cartList.cartInfos?.get(i)?.handlingSupplier ?: 0.0f
                }
                if (cartList.cartInfos?.get(i)?.handlingAdmin ?: 0.0f > handlingAdmin) {
                    handlingAdmin = +cartList.cartInfos?.get(i)?.handlingAdmin!!
                }

            }


            hashMap["handlingAdmin"] = "" + handlingAdmin
            hashMap["handlingSupplier"] = "" + handlingSupplier

            //  var freeDeliveryAmount = 0f
            val netTotal: Float

            /*  if (data?.free_delivery_amount!! > 0) {
                  freeDeliveryAmount = data!!.free_delivery_amount!!
              }*/

            netTotal = StaticFunction.netTotal(activity, Prefs.with(activity).getInt(DataNames.FLOW_STROE, 0))

            /*     if (netTotal + handlingSupplier + handlingAdmin >= freeDeliveryAmount) {
                     hashMap["minOrderDeliveryCrossed"] = 1.toString() + ""
                 } else {*/
            hashMap["minOrderDeliveryCrossed"] = 0.toString() + ""
            //   }
            hashMap["netAmount"] = netTotal.toString() + ""
            hashMap["currencyId"] = "kr"
            hashMap["deliveryCharges"] = maxDeliveryCharge.toString() + ""


            //hashMap.put("remarks", );
            hashMap["urgentPrice"] = "" + maxUrgentPrice
            hashMap["languageId"] = "" + StaticFunction.getLanguage(activity)


            if (!pickupTime.trim { it <= ' ' }.isEmpty()) {
                hashMap["pickupTime"] = "" + pickupTime
                hashMap["pickupId"] = "" + pickupId
                hashMap["pickupDate"] = "" + pickupDate
            }

            if (deliveryType != DataNames.DELIVERY_TYPE_URGENT) {
                hashMap["delivery_max_time"] = dateYYMMDD.toString()
            }
            val call = RestClient.getModalApiService(activity).updateCartInfo(hashMap)


            call.enqueue(object : Callback<ExampleCommon> {
                override fun onResponse(call: Call<ExampleCommon>, response: Response<ExampleCommon>) {

                    barDialog!!.dismiss()

                    val exampleCommon = response.body()
                    if (response.code() == 200 && exampleCommon!!.status == 200) {
                        val bundle = Bundle()
                        bundle.putString("deliveryAddress", deliveryAdress)
                        bundle.putInt("deliveryId", deliveryId!!)
                        bundle.putString("deliveryDate", tvDate!!.text.toString())
                        bundle.putString("deliveryTime", tvTime!!.text.toString())
                        bundle.putString("deliveryName", deliveryName)
                        bundle.putFloat("deliveryCharges", maxDeliveryCharge)
                        bundle.putInt("paymentMethod", exampleAllAdresses!!.data!!.payment_method!!)
                        bundle.putFloat("urgentPrice", maxUrgentPrice)

                        bundle.putInt("deliveryType", deliveryType)

                        if (arguments != null && arguments!!.containsKey("prodList")) {
                            bundle.putParcelable("prodList", arguments!!.getParcelable<CartInfoServer>("prodList"))
                        }

                        Prefs.with(activity).save(DataNames.DELIVERY_TIME_final, time24Format)
                        val orderSummaryFragment = OrderSummaryFragment()
                        orderSummaryFragment.arguments = bundle

                        //  Navigation.findNavController(view!!).navigate(R.id.action_deliveryFragment_to_orderSummaryFragment,bundle)

/*                        (activity as MainActivity).pushFragments(DataNames.TAB1, orderSummaryFragment,
                                true, true, "", true)*/
                    } else {
                        StaticFunction.sweetDialogueFailure(activity, getString(R.string.alert), exampleCommon!!.message, true, 102, false)
                    }
                }

                override fun onFailure(call: Call<ExampleCommon>, t: Throwable) {
                    barDialog!!.dismiss()
                }


            })
        } else {
            StaticFunction.showNoInternetDialog(activity!!)
        }
    }


    override fun onAddressSelect(adrsBean: AddressBean) {

        adrsData = adrsBean

        dataManger.addGsonValue(PrefenceConstants.ADRS_DATA, Gson().toJson(adrsBean))

        tv_deliver_adrs.text = "${adrsData.address_line_1} , ${adrsData.customer_address}"
    }

    override fun onLocationSelect(location: SupplierLocation) {

    }

    override fun onDestroyDialog() {

    }
    override fun onTableAdded(table_name: String) {

    }


}
