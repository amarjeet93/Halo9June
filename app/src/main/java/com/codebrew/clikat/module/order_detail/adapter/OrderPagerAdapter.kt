package com.codebrew.clikat.module.order_detail.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.OrderUtils
import com.codebrew.clikat.app_utils.extension.loadUserImage
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.FoodAppType
import com.codebrew.clikat.data.OrderStatus
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.QuestionList
import com.codebrew.clikat.data.model.api.orderDetail.Agent
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.data.model.others.ImageListModel
import com.codebrew.clikat.data.model.others.OrderStatusModel
import com.codebrew.clikat.databinding.PagerProductItemBinding
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.cart.adapter.ImageListAdapter
import com.codebrew.clikat.module.cart.adapter.ImageListAdapter.UserChatListener
import com.codebrew.clikat.module.cart.adapter.SelectedQuestAdapter
import com.codebrew.clikat.module.user_tracking.UserTracking
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.utils.StaticFunction.colorStatusProduct
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.StaticFunction.statusProduct
import com.codebrew.clikat.utils.configurations.Configurations
import com.github.vipulasri.timelineview.sample.example.OrderStatusAdapter
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_agent_list.view.*
import kotlinx.android.synthetic.main.pager_product_item.view.*
import java.text.ParseException

class OrderPagerAdapter(private val mContext: Context, private val orderHistoryBeans: MutableList<OrderHistory>,
                        private val appUtil: AppUtils,
                        private val callBackReturn: OrderDetailProductAdapter.OnReturnClicked,
                        private val orderUtils: OrderUtils) : RecyclerView.Adapter<OrderPagerAdapter.View_holder>() {

    private var categoryId = 0
    private var supplierName: String? = null
    private var supplierI = 0

    private var branchId = 0


    private var bookingFlowBean: SettingModel.DataBean.BookingFlowBean? = null
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null

    private var userDta: String? = null

    private var mCallback: OrderListener? = null

    private var appTerminology: SettingModel.DataBean.AppTerminology? = null

    private var settingData: SettingModel.DataBean.SettingData? = null

    private var gson = Gson()

    private var mSelectedPayment: CustomPayModel? = null

    fun setUserId(userDta: String?, bookingFlowBean: SettingModel.DataBean.BookingFlowBean?,
                  screenFlowBean: SettingModel.DataBean.ScreenFlowBean?,

                  settingData: SettingModel.DataBean.SettingData?) {
        this.userDta = userDta
        this.screenFlowBean = screenFlowBean
        this.bookingFlowBean = bookingFlowBean

        this.settingData = settingData
    }

    fun settingTerminology(appTerminology: SettingModel.DataBean.AppTerminology?) {
        this.appTerminology = appTerminology
    }


    fun settingCallback(mCallback: OrderListener) {
        this.mCallback = mCallback
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_holder {
        val binding: PagerProductItemBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.pager_product_item, parent, false)
        binding.color = Configurations.colors
        binding.strings = appUtil.loadAppConfig(0).strings
        return View_holder(binding.root)
    }


    override fun getItemCount(): Int {
        return orderHistoryBeans.size
    }

    override fun onBindViewHolder(holder: View_holder, position: Int) {

        holder.gpAction?.visibility = View.GONE
        try {
            initailize(holder, orderHistoryBeans[position])
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }


    private fun initailize(mHolder: View_holder, orderDetail: OrderHistory) {

        val mStatusList = mutableListOf<OrderStatusModel>()


        if (orderDetail.agent != null && orderDetail.agent.isNotEmpty()) {
            mHolder.agentLayout!!.visibility = View.VISIBLE
            mHolder.tvAgentChat!!.visibility = View.VISIBLE
            mHolder.tvAgentDetail!!.visibility = View.VISIBLE
            val agentBean = orderDetail.agent.get(0)
            // if (agentBean?.image != null) {
            mHolder.ivUserImage?.loadUserImage(agentBean?.image ?: "")
            //Glide.with(mContext).load(agentBean.getImage()).apply(new RequestOptions().placeholder(R.drawable.placeholder_product)).into(ivUserImage);
            // }
            if (agentBean?.name != null) {
                mHolder.tvName!!.text = agentBean.name
            }
            if (agentBean?.occupation != null) {
                mHolder.tvOccupation!!.text = agentBean.occupation
            }
            if (agentBean?.experience != 0) {
                mHolder.tvExperience!!.text = mContext.getString(R.string.experience_tag, agentBean?.experience)
            }
        } else {
            mHolder.agentLayout?.visibility = View.GONE
            mHolder.tvAgentChat!!.visibility = View.GONE
            mHolder.tvAgentDetail?.visibility = View.GONE
        }

        if (settingData?.extra_instructions == "1" && screenFlowBean?.app_type == AppDataType.HomeServ.type) {
            mHolder.grpExtInst?.visibility = View.VISIBLE
            if (orderDetail.have_pet == 1) {
                mHolder.tvHavePets?.text = mContext.getString(R.string.yes)
            } else {

                mHolder.tvHavePets?.text = mContext.getString(R.string.no)
            }

            if (orderDetail.cleaner_in == 1) {
                mHolder.tvCleanerIn?.text = mContext.getString(R.string.yes)
            } else {

                mHolder.tvCleanerIn?.text = mContext.getString(R.string.no)
            }
        } else {
            mHolder.grpExtInst?.visibility = View.GONE
        }


        /* mHolder.tvHavePets?.text=orderDetail.have_pet.toString()
         mHolder.tvCleanerIn?.text=orderDetail.cleaner_in.toString()*/

        mHolder.tvParkingInstructions?.text = orderDetail.parking_instructions
        mHolder.tvAreaToFoucus?.text = orderDetail.area_to_focus

        mHolder.rvStatus?.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        mHolder.rvStatus?.adapter = OrderStatusAdapter(mStatusList, orderDetail.type, orderDetail.self_pickup, orderDetail.terminology
                ?: "")


        loadImage(orderDetail.logo, mHolder.ivSupplierIcon!!, false)
        setSupplierImage(orderDetail.logo, orderDetail.supplier_id, "", orderDetail.supplier_branch_id, orderDetail.product?.get(0)?.category_id)
        mHolder.rvProduct?.layoutManager = LinearLayoutManager(mContext)

        mHolder.tvOrderNo?.text = orderDetail.order_id.toString()
        if(orderDetail.table_name.toString().equals("")||orderDetail.table_name.toString().equals("null")||orderDetail.table_name.toString()==null) {
            mHolder.tvTableNo?.visibility=View.GONE
            mHolder.txtTable?.visibility=View.GONE
        }else{
            mHolder.tvTableNo?.visibility=View.VISIBLE
            mHolder.txtTable?.visibility=View.VISIBLE
            mHolder.tvTableNo?.text = orderDetail.table_name.toString()
        }
        mHolder.tvTax?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, (orderDetail.handling_admin))

        mHolder.tvDelivery?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.delivery_charges)

        colorStatusProduct(mHolder.tvStatus1,
                orderDetail.status, mContext, false)




        if (orderDetail.status == 11.0) {
            orderDetail.status = OrderStatus.In_Kitchen.orderStatus
        }

        if (orderDetail.status == 10.0) {
            orderDetail.status = OrderStatus.Shipped.orderStatus
        }

        val paymentFlow = orderUtils.checkOrderListFlow(orderDetail)

        mHolder.tvStatus1?.text = if (paymentFlow) {
            "Payment Pending"
        } else {
            statusProduct(orderDetail.status, orderDetail.type
                    ?: 0, orderDetail.self_pickup ?: 0, mContext, orderDetail.terminology ?: "")
        }



        if (orderDetail.type == AppDataType.HomeServ.type) {
            mHolder.txtPlaced?.text = mContext.getString(R.string.start_time)

            if (orderDetail.status != OrderStatus.Delivered.orderStatus) {
                mHolder.tvDelivered?.text = convertAddDate(orderDetail.delivered_on
                        ?: "", orderDetail.duration ?: 0)
            } else {
                mHolder.tvDelivered?.text = convertDateNew(orderDetail.delivered_on ?: "")
            }
        } else {
            mHolder.tvDelivered?.text = convertDateNew(orderDetail.delivered_on ?: "")
        }

        mHolder.txtDelivered?.text = if (orderDetail.status != OrderStatus.Delivered.orderStatus) {
            orderDetail.delivered_on = ""
            when {
                orderDetail.type == AppDataType.HomeServ.type -> {
                    "End Time"
                }
                orderDetail.self_pickup == FoodAppType.Pickup.foodType -> {
                    mContext.getString(R.string.pickup_on)
                }
                else -> {
                    "Expected Delivery Time"
                }
            }
        } else {
            when {
                orderDetail.type == AppDataType.HomeServ.type -> {
                    "End Time"
                }
                orderDetail.self_pickup == FoodAppType.Pickup.foodType -> {
                    mContext.getString(R.string.pickup_on)
                }
                else -> {
                    mContext.getString(R.string.delivered_on)
                }
            }
        }

        // mHolder.txtDelivered?.text =


        mStatusList.add(OrderStatusModel(convertDate(orderDetail.created_on
                ?: "", orderDetail.status, OrderStatus.Pending.orderStatus)
                ?: "", OrderStatus.Pending, orderDetail.status ?: 0.0))
        when (orderDetail.type) {

            AppDataType.Food.type -> {
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.confirmed_on
                        ?: "", orderDetail.status, OrderStatus.Approved.orderStatus), OrderStatus.Approved, orderDetail.status
                        ?: 0.0))
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.progress_on
                        ?: "", orderDetail.status, OrderStatus.In_Kitchen.orderStatus), OrderStatus.In_Kitchen, orderDetail.status
                        ?: 0.0))
                if (orderDetail.self_pickup == 0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.shipped_on
                            ?: "", orderDetail.status, OrderStatus.On_The_Way.orderStatus), OrderStatus.On_The_Way, orderDetail.status
                            ?: 0.0))
                } else {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.shipped_on
                            ?: "", orderDetail.status, OrderStatus.Ready_to_be_picked.orderStatus), OrderStatus.Ready_to_be_picked, orderDetail.status
                            ?: 0.0))
                }
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.delivered_on
                        ?: "", orderDetail.status, OrderStatus.Delivered.orderStatus), OrderStatus.Delivered, orderDetail.status
                        ?: 0.0))
            }

            AppDataType.Ecom.type -> {
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.confirmed_on
                        ?: "", orderDetail.status, OrderStatus.Confirmed.orderStatus), OrderStatus.Confirmed, orderDetail.status
                        ?: 0.0))
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.progress_on
                        ?: "", orderDetail.status, OrderStatus.Packed.orderStatus), OrderStatus.Packed, orderDetail.status
                        ?: 0.0))
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.near_on
                        ?: "", orderDetail.status, OrderStatus.Shipped.orderStatus), OrderStatus.Shipped, orderDetail.status
                        ?: 0.0))
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.shipped_on
                        ?: "", orderDetail.status, OrderStatus.On_The_Way.orderStatus), OrderStatus.On_The_Way, orderDetail.status
                        ?: 0.0))
                mStatusList.add(OrderStatusModel(convertDate(orderDetail.delivered_on
                        ?: "", orderDetail.status, OrderStatus.Delivered.orderStatus), OrderStatus.Delivered, orderDetail.status
                        ?: 0.0))
            }

            AppDataType.HomeServ.type -> {
                if (orderDetail.status!! in 1.0..5.0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.confirmed_on
                            ?: "", orderDetail.status, OrderStatus.Confirmed.orderStatus), OrderStatus.Confirmed, orderDetail.status
                            ?: 0.0))
                } else {
                    mStatusList.add(OrderStatusModel(convertDate(""
                            ?: "", orderDetail.status, OrderStatus.Confirmed.orderStatus), OrderStatus.Confirmed, null
                            ?: 0.0))
                }


                if (orderDetail.status!! in 2.5..5.0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.progress_on
                            ?: "", orderDetail.status, OrderStatus.In_Kitchen.orderStatus), OrderStatus.In_Kitchen, orderDetail.status
                            ?: 0.0))
                } else {
                    mStatusList.add(OrderStatusModel(convertDate(""
                            ?: "", orderDetail.status, OrderStatus.In_Kitchen.orderStatus), OrderStatus.In_Kitchen, null
                            ?: 0.0))
                }

                if (orderDetail.status!! in 2.6..5.0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.near_on
                            ?: "", orderDetail.status, OrderStatus.Reached.orderStatus), OrderStatus.Reached, orderDetail.status
                            ?: 0.0))
                } else {

                    mStatusList.add(OrderStatusModel(convertDate(""
                            ?: "", orderDetail.status, OrderStatus.Reached.orderStatus), OrderStatus.Reached, null
                            ?: 0.0))
                }

                if (orderDetail.status!! in 3.0..5.0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.shipped_on
                            ?: "", orderDetail.status, OrderStatus.Started.orderStatus), OrderStatus.Started, orderDetail.status
                            ?: 0.0))
                } else {
                    mStatusList.add(OrderStatusModel(convertDate(""
                            ?: "", orderDetail.status, OrderStatus.Started.orderStatus), OrderStatus.Started, null
                            ?: 0.0))
                }

                if (orderDetail.status == 5.0) {
                    mStatusList.add(OrderStatusModel(convertDate(orderDetail.delivered_on
                            ?: "", orderDetail.status, OrderStatus.Ended.orderStatus), OrderStatus.Ended, orderDetail.status
                            ?: 0.0))
                } else {
                    mStatusList.add(OrderStatusModel(convertDate(""
                            ?: "", orderDetail.status, OrderStatus.Ended.orderStatus), OrderStatus.Ended, null
                            ?: 0.0))

                }
            }

        }

        val mStatusPos = mStatusList.indexOfFirst {
            it.status.orderStatus == orderDetail.status
        }

        mHolder.rvStatus?.layoutManager?.scrollToPosition(mStatusPos)


        mHolder.checkChatVisiblity()

        //        var gpPrescription: Group? = null
        //        var additionRemark: TextView? = null
        //        var rvPhoto: RecyclerView? = null

        if (orderDetail.pres_description.isNullOrEmpty()) {
            mHolder.additionRemark?.visibility = View.GONE
        } else {
            mHolder.additionRemark?.visibility = View.VISIBLE
            mHolder.additionRemark?.text = orderDetail.pres_description
        }

        val imageList = productPreciption(orderDetail)

        if (!imageList.isNullOrEmpty()) {

            mHolder.gpPrescription?.visibility = View.VISIBLE

            val mAdapter = ImageListAdapter(UserChatListener(
                    {

                    }, {

            }))

            mHolder.rvPhoto?.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            mHolder.rvPhoto?.adapter = mAdapter
            mAdapter.submitMessageList(imageList, "order")
        } else {
            mHolder.gpPrescription?.visibility = View.GONE
        }




        mHolder.tvPlaced?.text = if (orderDetail.type == AppDataType.HomeServ.type) {
            convertDateNew(orderDetail.service_date ?: "")
        } else {
            convertDateNew(orderDetail.created_on ?: "")
        }

        when (orderDetail.status) {
            OrderStatus.Rejected.orderStatus, OrderStatus.Customer_Canceled.orderStatus -> {
                mHolder.rvStatus?.visibility = View.GONE
            }
            else -> {
                mHolder.rvStatus?.visibility = View.VISIBLE
                mHolder.rvStatus?.adapter?.notifyDataSetChanged()
            }
        }

        if (orderDetail.promoCode != null) {
            mHolder.gpDiscount?.visibility = View.VISIBLE
            mHolder.tvPromoCode?.text = orderDetail.promoCode ?: ""
            mHolder.tvDiscount?.text = "-" + mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.discountAmount)
        } else {
            mHolder.gpDiscount?.visibility = View.GONE
        }


        mHolder.tvPament_method?.text = if (orderDetail.payment_type == 3 && orderDetail.payment_status == 0) {
            "None"
        } else if (DataNames.DELIVERY_CASH == orderDetail.payment_type) {

            mContext.getString(R.string.cash)
        } else if (orderDetail.payment_type == DataNames.DELIVERY_CARD) {
            mContext.getString(R.string.online_pay_tag, orderDetail.payment_source)
        } else {
            mContext.getString(R.string.online_payment)
        }


        if (orderDetail.self_pickup == FoodAppType.Pickup.foodType) {
            if (orderDetail.delivery_address == null) return
            mHolder.gpAddress?.visibility = View.VISIBLE
            mHolder.gpDelivery?.visibility = View.GONE

            mHolder.tvAddress_t?.text = mContext.getString(R.string.pick_up)
            mHolder.tvAddress?.text = "${orderDetail.supplier_name
                    ?: ""} ,${orderDetail.supplier_address ?: ""}"

        } else {

            if (!orderDetail.restaurantFloor.isNullOrEmpty()) {
                //   mHolder.tvAddress_t?.text = mContext.getString(R.string.address_detail)
                mHolder.tvAddress?.text = orderDetail.restaurantFloor
                mHolder.gpAddress?.visibility = View.VISIBLE
                mHolder.tvAddress_t?.text = mContext.getString(R.string.customer_location)
            } else {

                if (orderDetail.delivery_address == null) {

                } else {
                    if (orderDetail.delivery_address.customer_address.equals("null"))
                    {

                    }else {
                        mHolder.gpDelivery?.visibility = if (orderDetail.type == AppDataType.HomeServ.type) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }


                        mHolder.gpAddress?.visibility = View.VISIBLE
                        mHolder.tvAddress_t?.text = mContext.getString(R.string.address_detail)
                        mHolder.tvAddress?.text = "${
                            orderDetail.delivery_address.customer_address
                                    ?: ""
                        } ,${orderDetail.delivery_address.address_line_1 ?: ""}"
                    }
                }
            }


        }

        val totalProdList = calculateProdAddon(orderDetail.product)



        if (orderDetail.questions?.isNotEmpty() == true && orderDetail.questions != "[]") {
            val myType = object : TypeToken<List<QuestionList>>() {}.type
            val questionList = gson.fromJson<List<QuestionList>>(orderDetail.questions, myType)


            if (questionList.isEmpty()) return
            mHolder.gpQuestion?.visibility = View.VISIBLE


            mHolder.tvAddonPrice?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.addOn)
            val questAdapter = SelectedQuestAdapter(mContext, questionList)
            mHolder.rvQuestion?.adapter = questAdapter
            questAdapter.notifyDataSetChanged()
        }


        var subTotal = orderDetail.total_order_price
                ?: totalProdList?.sumByDouble {
                    (it?.fixed_price?.toFloatOrNull()?.times(it.prod_quantity ?: 0))?.toDouble()
                            ?: 0.0
                }?.toFloat()


        if (orderDetail.referral_amount ?: 0.0f > 0) {

            if (orderDetail.total_order_price == null && orderDetail.total_order_price == 0.0f) {
                subTotal = (subTotal?.minus(orderDetail.referral_amount ?: 0.0f))
            }

            mHolder.gpReferral?.visibility = View.VISIBLE
            mHolder.tvReferralAmt?.text = "-" + mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.referral_amount)
        } else {
            mHolder.gpReferral?.visibility = View.GONE
        }

        var totalAmt: Float? = 0.0F



        if (settingData?.removeDeliveryCharges == "1") {

            totalAmt = orderDetail.net_amount
                    ?: subTotal
                            ?.minus(orderDetail.discountAmount ?: 0.0f)?.plus(orderDetail.tip_agent
                                    ?: 0.0f)?.plus(orderDetail.user_service_charge
                                    ?: 0.0)?.plus(orderDetail.addOn ?: 0.0f)?.toFloat()

        } else {

            totalAmt = orderDetail.net_amount
                    ?: subTotal
                            ?.plus(orderDetail.delivery_charges ?: 0.0f)
                            ?.minus(orderDetail.discountAmount ?: 0.0f)?.plus(orderDetail.tip_agent
                                    ?: 0.0f)?.plus(orderDetail.user_service_charge
                                    ?: 0.0)?.plus(orderDetail.addOn ?: 0.0f)?.toFloat()

        }


        if (orderDetail.user_service_charge ?: 0.0 > 0) {
            mHolder.groupSupplierCharge?.visibility = View.VISIBLE
            mHolder.tvSupplierCharge?.text = mContext.getString(R.string.currency_tag,
                    AppConstants.CURRENCY_SYMBOL, orderDetail.user_service_charge)
        }

        mHolder.tvSubTotal?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, subTotal)

        val tipCharges = orderDetail.tip_agent ?: 0.0f


        mHolder.tvPayment?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, totalAmt)

        mHolder.tvAmount?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, totalAmt)

        if (orderDetail.remaining_amount ?: 0.0f > 0.0f && orderDetail.payment_status == 1) {
            mHolder.groupRemaining?.visibility = View.VISIBLE
            mHolder.tvRemaining?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.remaining_amount)
        }

        if (orderDetail.refund_amount ?: 0f > 0f && orderDetail.payment_status == 1) {
            mHolder.groupRefund?.visibility = View.VISIBLE
            mHolder.tvRefund?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, orderDetail.refund_amount)
        }


        if (mSelectedPayment?.payName == mContext.getString(R.string.zelle)) {
            mHolder.zelleDoc?.let {
                Glide.with(mContext).load(mSelectedPayment?.keyId
                        ?: "").into(it)
            }
            mHolder.groupZelle?.visibility = View.VISIBLE
        } else {
            mHolder.groupZelle?.visibility = View.GONE
        }


        if (tipCharges > 0) {
            mHolder.grouptipCharges?.visibility = View.VISIBLE
            mHolder.tvTipCharges?.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, tipCharges)
        } else {
            mHolder.grouptipCharges?.visibility = View.GONE

        }


        mHolder.tvItemsCount?.text = totalProdList?.size.toString()

        mHolder.rvProduct!!.adapter = OrderDetailProductAdapter(mContext, totalProdList
                ?: emptyList(), orderDetail.supplier_name
                ?: "", screenFlowBean, settingData, callBackReturn, orderDetail.status, appUtil)


        var isTrackBtn = false

        mHolder.btnTrackOrder?.visibility = if (orderDetail.type == AppDataType.HomeServ.type && orderDetail.status == OrderStatus.In_Kitchen.orderStatus) {
            isTrackBtn = true
            View.VISIBLE
        } else if (orderDetail.type != AppDataType.Ecom.type &&
                orderDetail.status != OrderStatus.Shipped.orderStatus && orderDetail.status == OrderStatus.On_The_Way.orderStatus
                && orderDetail.self_pickup != FoodAppType.Pickup.foodType) {
            isTrackBtn = true
            View.VISIBLE
        } else {
            isTrackBtn = false
            View.GONE
        }

        if (isTrackBtn && orderDetail.donate_to_someone == 1) {
            mHolder.btnTrackOrder?.visibility = View.GONE
        }

        if ((orderDetail.status == OrderStatus.Delivered.orderStatus || orderDetail.status == OrderStatus.Rating_Given.orderStatus)
                && orderDetail.is_supplier_rated == 0 && settingData?.is_supplier_rating == "1") {
            mHolder.btnRateSupplier?.visibility = View.VISIBLE
        } else {
            mHolder.btnRateSupplier?.visibility = View.GONE
        }

        if (orderDetail.status == OrderStatus.Delivered.orderStatus && settingData?.is_agent_rating == "1") {
            mHolder.btnRateAgent?.visibility = View.VISIBLE
        } else {
            mHolder.btnRateAgent?.visibility = View.GONE
        }


/*        mHolder.btnTrackOrder?.text = if (orderDetail.status == OrderStatus.On_The_Way.orderStatus)
            mContext.getString(R.string.track_order, Configurations.strings.order) else mContext.getString(R.string.rate_product, Configurations.strings.products)*/


        mHolder.btnTrackOrder?.text = mContext.getString(R.string.track_order, appUtil.loadAppConfig(orderDetail.type
                ?: 0).strings?.order)


        mHolder.btnTrackOrder?.setOnClickListener { v: View? ->
            /*    Intent intent = new Intent(mContext, RateProductActivity.class);
            intent.putExtra("rateProducts", calculateRateProduct(orderDetail.getProduct()));
            mContext.startActivity(intent);*/
            //  val signUp = Prefs.with(mContext).getObject(DataNames.USER_DATA, PojoSignUp::class.java)
            val intent = Intent(mContext, UserTracking::class.java)
            intent.putExtra("userId", userDta)
            intent.putExtra("orderData", orderDetail)
            mContext.startActivity(intent)
        }


        settingData?.removeDeliveryCharges?.let {
            if (it == "1")
                mHolder.gpDelivery?.visibility = View.GONE
        }

        settingData?.email?.let {
            if (it.isNotEmpty()) {
                mHolder.supportEmail?.visibility = View.VISIBLE
            }
        }

        mHolder.supportEmail?.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@halo-app.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.support_email_text,
                    orderDetail.order_id.toString() ?: ""))
            try {
                mContext.startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
            }
        }

        mHolder.tvExperience?.visibility = View.GONE

    }

    private fun productPreciption(orderDetail: OrderHistory): MutableList<ImageListModel>? {

        val imageList: MutableList<ImageListModel>? = mutableListOf()


        orderDetail.pres_image1.let {
            if (it?.isNotEmpty() == true) {
                imageList?.add(ImageListModel(is_imageLoad = true, image = it, _id = ""))
            }
        }

        orderDetail.pres_image2.let {
            if (it?.isNotEmpty() == true) {
                imageList?.add(ImageListModel(is_imageLoad = true, image = it, _id = ""))
            }
        }


        orderDetail.pres_image3.let {
            if (it?.isNotEmpty() == true) {
                imageList?.add(ImageListModel(is_imageLoad = true, image = it, _id = ""))
            }
        }


        orderDetail.pres_image4.let {
            if (it?.isNotEmpty() == true) {
                imageList?.add(ImageListModel(is_imageLoad = true, image = it, _id = ""))
            }
        }

        orderDetail.pres_image5.let {
            if (it?.isNotEmpty() == true) {
                imageList?.add(ImageListModel(is_imageLoad = true, image = it, _id = ""))
            }
        }

        return imageList

    }

    private fun calculateProdAddon(productList: List<ProductDataBean?>?): List<ProductDataBean?>? {

        val prodList = arrayListOf<ProductDataBean?>()

        productList?.mapIndexed { index, product ->

            if (product?.adds_on.isNullOrEmpty()) {
                product?.prod_quantity = product?.quantity
                prodList += product?.copy()
            } else {
                product?.adds_on?.groupBy {
                    it?.serial_number
                }?.mapValues {
                    product.adds_on = it.value
                    product.add_on_name = it.value.map { it?.adds_on_type_name }.joinToString()
                    product.prod_quantity = it.value[0]?.quantity ?: 0
                    product.fixed_price = product.price?.toFloatOrNull()?.plus(it.value.sumByDouble {
                        (it?.price ?: 0.0f).toDouble()
                    }.toFloat())?.times(product.quantity ?: 0).toString()
                    prodList += product.copy()
                }
            }
        }

        return prodList.takeIf { it.isNotEmpty() } ?: productList
    }

    private fun convertDate(dateToConvert: String, orderStatus: Double?, currentStatus: Double): String {

        return appUtil.convertDateOneToAnother(replaceInvalid(dateToConvert).replace("T", " ").replace("+00:00", ""),
                "yyyy-MM-dd HH:mm:ss", "EEE, dd\nhh:mm aa") ?: ""
    }

    private fun convertDateNew(dateToConvert: String): String {

        return appUtil.convertDateOneToAnother(replaceInvalid(dateToConvert).replace("T", " ").replace("+00:00", ""),
                "yyyy-MM-dd HH:mm:ss", "MMM dd EEE hh:mm aa") ?: ""
    }

    private fun convertAddDate(dateToConvert: String, duration: Int): String {

        return appUtil.convertDateToAddDate(replaceInvalid(dateToConvert).replace("T", " ").replace("+00:00", ""),
                "yyyy-MM-dd HH:mm:ss", "MMM dd EEE hh:mm aa", duration) ?: ""
    }

    private fun replaceInvalid(dateToConvert: String): String {
        return if (dateToConvert.contains("Invalid date")) {
            dateToConvert.replace("Invalid date", "")
        } else {
            dateToConvert
        }
    }

    fun setSupplierImage(image: String?, supplierI: Int?, supplierName: String?
                         , branchId: Int?, categoryId: Int?) {
        this.categoryId = categoryId ?: 0
        this.supplierName = supplierName
        this.supplierI = supplierI ?: 0
        this.branchId = branchId ?: 0

    }

    fun setZelleDoc(mSelectedPayment: CustomPayModel) {
        this.mSelectedPayment = mSelectedPayment
        notifyDataSetChanged()
    }

    class OrderListener(val clickListener: (model: Agent) -> Unit, val chatListner: (orderHist: OrderHistory) -> Unit
                        , val supplierListener: (orderHist: OrderHistory) -> Unit, val agentListener: (orderHist: OrderHistory) -> Unit,val chatRestaurantListener:(orderHistory: OrderHistory)->Unit) {
        fun callDriver(agentBean: Agent) = clickListener(agentBean)

        fun chatDriver(orderData: OrderHistory) = chatListner(orderData)
        fun rateSupplier(orderData: OrderHistory) = supplierListener(orderData)
        fun rateAgent(agentBean: OrderHistory) = agentListener(agentBean)
        fun chatRestaurant(orderHistory: OrderHistory)  = chatRestaurantListener(orderHistory)
    }


    inner class View_holder(root: View) : RecyclerView.ViewHolder(root) {

        var tvStatus1: TextView? = null
        var tvPayment: TextView? = null
        var tvItemsCount: TextView? = null
        var rvProduct: RecyclerView? = null
        var rvStatus: RecyclerView? = null
        var rvQuestion: RecyclerView? = null

        var tvOrder: TextView? = null
        var tvPlaced: TextView? = null
        var tvOrderNo: TextView? = null
        var txtTable: TextView? = null
        var tvTableNo: TextView? = null

        var tvTax: TextView? = null
        var tvSubTotal: TextView? = null
        var tvDelivery: TextView? = null
        var tvDelivered: TextView? = null
        var tvAmount: TextView? = null
        var tvAddress_t: TextView? = null
        var tvAddress: TextView? = null
        var tvPament_method_t: TextView? = null
        var tvPament_method: TextView? = null

        // var txtDeliver: TextView? = null
        var ivSupplierIcon: CircleImageView? = null
        var zelleDoc: ImageView? = null
        var tvPromoCode: TextView? = null
        var tvDiscount: TextView? = null
        var btnTrackOrder: MaterialButton? = null
        var btnRateSupplier: MaterialButton? = null
        var btnRateAgent: MaterialButton? = null
        var agentLayout: View? = null
        var tvAgentChat: TextView? = null
        var tvAgentDetail: TextView? = null
        var ivUserImage: CircleImageView? = null
        var tvName: TextView? = null
        var tvOccupation: TextView? = null
        var tvAddonPrice: TextView? = null
        var tvExperience: TextView? = null

        // var rbAgent: RatingBar? = null
        var tvTotalReviews: TextView? = null
        var tvReferralAmt: TextView? = null
        var gpAction: Group? = null
        var gpDiscount: Group? = null
        var gpAddress: Group? = null
        var gpReferral: Group? = null
        var gpDelivery: Group? = null
        var gpQuestion: Group? = null
        var grouptipCharges: Group? = null
        var groupRemaining: Group? = null
        var groupRefund: Group? = null
        var groupZelle: Group? = null

        var tvTipCharges: TextView? = null
        var tvRemaining: TextView? = null
        var tvRefund: TextView? = null

        var txtPlaced: TextView? = null
        var txtDelivered: TextView? = null
        var gpPrescription: Group? = null
        var additionRemark: TextView? = null
        var callDriver: ImageView? = null
        var chatDriver: ImageView? = null
        var tvRestaurantChat: TextView? = null
        var rvPhoto: RecyclerView? = null
        var groupSupplierCharge: Group? = null
        var tvSupplierCharge: TextView? = null
        var supportEmail: TextView? = null
        var tvHavePets: TextView? = null
        var tvCleanerIn: TextView? = null
        var tvParkingInstructions: TextView? = null
        var tvAreaToFoucus: TextView? = null
        var grpExtInst: Group? = null


        var tvShippingStatus: TextView? = null
        var tvShippingStatusTag: TextView? = null

        init {
            gpAction = root.gp_action
            gpDiscount = root.grp_discount
            gpAddress = root.grp_address
            gpReferral = root.group_referral
            gpDelivery = root.grpDelivery
            gpQuestion = root.grp_question
            tvTableNo= root.tvTableNo
            txtTable= root.txtTable
            txtPlaced = root.txtPlaced
            txtDelivered = root.txtDeliver
            gpPrescription = root.grp_preciption
            additionRemark = root.edAdditionalRemarks
            rvPhoto = root.rvPhotoList

            tvStatus1 = root.tvStatus1
            tvPayment = root.tvPayment
            tvAddonPrice = root.tvAddonCharges
            tvItemsCount = root.tvItemsCount
            rvProduct = root.rvProduct
            rvStatus = root.rvStatus
            rvQuestion = root.recyclerviewQuest

            tvOrder = root.tvOrder
            tvPlaced = root.tvPlaced
            tvOrderNo = root.tvOrderNo
            tvTax = root.tvTax
            tvSubTotal = root.tvSubTotal
            tvDelivery = root.tvDelivery
            tvDelivered = root.tvDelivered
            tvAmount = root.tvAmount
            tvAddress_t = root.tvAddress_t
            tvAddress = root.tvAddress
            tvPament_method_t = root.txtPayMtd
            tvPament_method = root.tvPament_method
            ivSupplierIcon = root.ivSupplierIcon
            //  txtDeliver = root.txtDeliver

            tvShippingStatus = root.tvShippingStatus
            tvShippingStatusTag = root.tvShippingStatusTag
            tvPromoCode = root.tvPromoCode
            tvDiscount = root.tvDiscount
            btnTrackOrder = root.btnTrackOrder
            btnRateSupplier = root.btnRateSupplier
            btnRateAgent = root.btnRateAgent
            agentLayout = root.lyt_agent
            tvAgentChat = root.tvAgentChat
            tvRestaurantChat = root.tvReatsurantChat
            tvAgentDetail = root.tvAgentDetail
            ivUserImage = root.iv_userImage
            tvName = root.tv_name
            tvOccupation = root.tv_occupation
            tvExperience = root.tv_experience
            //  rbAgent = root.rb_agent
            tvTotalReviews = root.tv_total_reviews
            tvReferralAmt = root.tvReferral
            callDriver = root.ic_call
            chatDriver = root.iv_chat_agent
            grouptipCharges = root.grptipCharges
            groupRemaining = root.grp_remaining
            groupRefund = root.grp_refund
            groupZelle = root.group_zelle
            zelleDoc = root.iv_doc
            tvTipCharges = root.tvTipChargesOrder
            tvRemaining = root.tvRemaining
            tvRefund = root.tvRefund
            groupSupplierCharge = root.group_service
            tvSupplierCharge = root.tvServiceFee
            supportEmail = root.tvSupportEmail
            tvHavePets = root.textHavePets
            tvCleanerIn = root.textCleanerIn
            tvParkingInstructions = root.tvParkingInstructions
            tvAreaToFoucus = root.tvAreasToFocusOn
            grpExtInst = root.grpExtInst

            ivSupplierIcon?.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("categoryId", categoryId)
                bundle.putString("title", supplierName)
                bundle.putInt("isDetails", DataNames.ISDETAILS)
                bundle.putInt(DataNames.DATA_NAMES_ORDER, 0)
                bundle.putInt("supplierId", supplierI)
                bundle.putInt("branchId", branchId)
                mContext.startActivity(Intent(mContext, MainActivity::class.java)
                        .putExtras(bundle)
                        .putExtra("ISDEATILS", true))
                (mContext as AppCompatActivity).overridePendingTransition(0, 0)
            }


            callDriver?.setOnClickListener {
                if (!orderHistoryBeans[adapterPosition].agent.isNullOrEmpty()) {
                    orderHistoryBeans[adapterPosition].agent?.get(0)?.let { it1 -> mCallback?.callDriver(it1) }
                }
            }

            chatDriver?.setOnClickListener {
                mCallback?.chatDriver(orderHistoryBeans[adapterPosition])
            }

            tvAgentChat?.setOnClickListener {
                mCallback?.chatDriver(orderHistoryBeans[adapterPosition])
            }

            tvRestaurantChat?.setOnClickListener {
                mCallback?.chatRestaurant(orderHistoryBeans[adapterPosition])
            }


            btnRateSupplier?.setOnClickListener {
                mCallback?.rateSupplier(orderHistoryBeans[adapterPosition])
            }

            btnRateAgent?.setOnClickListener {
                if (!orderHistoryBeans[adapterPosition].agent.isNullOrEmpty()) {
                    mCallback?.rateAgent(orderHistoryBeans[adapterPosition])
                }
            }

        }

        fun checkChatVisiblity() {
            settingData?.chat_enable.let {

                if (it == "1") {
                    val status = orderHistoryBeans[adapterPosition].status ?: 0.0

                    if (orderHistoryBeans[adapterPosition].type == AppDataType.HomeServ.type) {

                        if (status != OrderStatus.Pending.orderStatus && status != OrderStatus.Rejected.orderStatus && status != OrderStatus.Delivered.orderStatus
                                && status != OrderStatus.Customer_Canceled.orderStatus) {
                            chatDriver?.visibility = View.VISIBLE
                        } else {
                            chatDriver?.visibility = View.GONE
                        }
                    } else {
                        if (status >= OrderStatus.In_Kitchen.orderStatus && status != OrderStatus.Delivered.orderStatus
                                && status != OrderStatus.Rating_Given.orderStatus && status != OrderStatus.Customer_Canceled.orderStatus
                                && status != OrderStatus.Scheduled.orderStatus
                                && status != OrderStatus.Rejected.orderStatus) {
                            chatDriver?.visibility = View.VISIBLE
                        } else {
                            chatDriver?.visibility = View.GONE
                        }

                    }
                } else {
                    chatDriver?.visibility = View.GONE
                }

            }
        }


    }

}
