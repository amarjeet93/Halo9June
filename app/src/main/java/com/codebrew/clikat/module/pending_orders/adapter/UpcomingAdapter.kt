package com.codebrew.clikat.module.pending_orders.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.adapters.ImagesAdapter
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.OrderUtils
import com.codebrew.clikat.data.*
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.data.model.api.orderDetail.OrderHistory
import com.codebrew.clikat.databinding.ItemOrderBinding
import com.codebrew.clikat.fragments.PaymentMethod
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.pending_orders.adapter.UpcomingAdapter.View_holder
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.GeneralFunctions
import com.codebrew.clikat.utils.StaticFunction.changeBorderColor
import com.codebrew.clikat.utils.StaticFunction.colorStatusProduct
import com.codebrew.clikat.utils.StaticFunction.statusProduct
import com.codebrew.clikat.utils.StaticFunction.sweetDialogueFailure
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.configurations.TextConfig
import java.text.ParseException

/*
 * Created by cbl80 on 20/4/16.
 */
class UpcomingAdapter(private val mContext: Context?, list1: List<OrderHistory>?, appType: Int,
                      private val settingData: SettingModel.DataBean.SettingData?,
                      private val appUtils: AppUtils? = null,
                      private val orderUtils: OrderUtils? = null) : Adapter<View_holder>() {
    private val list: ArrayList<OrderHistory>
    var selectedOrderId = 0
    var positionSelected = 0
    private val tabUnselected = Color.parseColor(Configurations.colors.tabUnSelected)
    private val tabSelected = Color.parseColor(Configurations.colors.tabSelected)
    private val appBackground = Color.parseColor(Configurations.colors.appBackground)
    private var textConfig: TextConfig? = null
    private val appType: Int
    private var mCallback: OrderCallback? = null
    fun settingCallback(mCallback: OrderCallback?) {
        this.mCallback = mCallback
    }

    fun remove() {
        list.removeAt(positionSelected)
        notifyItemRemoved(positionSelected)
        notifyItemRangeChanged(positionSelected, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): View_holder {
        val binding: ItemOrderBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_order, parent, false)
        binding.color = Configurations.colors
        return View_holder(binding.root)
    }

    override fun onBindViewHolder(holder: View_holder, position: Int) {
        val orderHistory = list[position]

        textConfig = appUtils?.loadAppConfig(orderHistory.type ?: 0)?.strings

        if (orderHistory.status == 11.0) {
            orderHistory.status = OrderStatus.In_Kitchen.orderStatus
        }


        if (orderHistory.status == 10.0) {
            orderHistory.status = OrderStatus.Shipped.orderStatus
        }

        holder.tvOrderNo.text = mContext?.getString(R.string.order_no, textConfig?.order) + "\n" + orderHistory.order_id
        holder.rvImages.adapter = ImagesAdapter(mContext, list[position].product, ImagesAdapter.ImageClickListener {
            mCallback?.onOrderDetail(list[position])
        })

        val s = (mContext?.getString(R.string.currency_tag, CURRENCY_SYMBOL, orderHistory.net_amount)
                + " / " + orderHistory.product?.count() + " " + mContext?.getString(R.string.items))

/*
        val s = (mContext?.getString(R.string.currency_tag, CURRENCY_SYMBOL, orderHistory.net_amount - orderHistory.discountAmount)
*/
        holder.tvPrice.text = s
        var dd = orderHistory.created_on?.replace("T", " ")
        dd = dd?.replace("Z", "")
        var deliveryDate = SpannableString.valueOf("")
        try {
            deliveryDate = setColor(mContext?.getString(R.string.placed_on) + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        holder.tvPlaced.text = deliveryDate
        val order = setColor(mContext?.getString(R.string.order_no, textConfig?.order) + "\n" + orderHistory.order_id)
        holder.tvOrderNo.text = order
        dd = orderHistory.service_date?.replace("T", " ")
        dd = dd?.replace("Z", "")
        deliveryDate = SpannableString.valueOf("")
        try {

            // orderHistory.sel

            val deliverMsg = if (appType == AppDataType.HomeServ.type) {
                mContext?.getString(R.string.expected_delivered_on, mContext.getString(R.string.service))
            } else if (orderHistory.self_pickup == FoodAppType.Pickup.foodType) {
                mContext?.getString(R.string.expected_delivered_on, mContext.getString(R.string.pickkup))
            } else {
                mContext?.getString(R.string.expected_delivered_on, mContext.getString(R.string.delivery_txt))
            }

            deliveryDate = setColor(deliverMsg + "\n"
                    + GeneralFunctions.getFormattedDateSide(dd))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        holder.tvDeliveryDate.text = deliveryDate

        val paymentFlow = orderUtils?.checkOrderListFlow(orderHistory)

        holder.tvStatus.text = if (paymentFlow == true) {
            "Payment Pending"
        } else {
            statusProduct(list[position].status, appType, orderHistory.self_pickup
                    ?: 0, mContext, orderHistory.terminology ?: "")
        }

        colorStatusProduct(holder.tvStatus, list[position].status, mContext, false)


        holder.tvOrder.text = mContext?.resources?.getString(R.string.cancel_order, textConfig?.order)
        //  holder.tvOrder.background = changeBorderColor(Configurations.colors.tabSelected?:"", "", GradientDrawable.RECTANGLE)
        //holder.tvOrder.setTextColor(appBackground)

        when (list[position].status) {
            OrderStatus.Pending.orderStatus -> {
                holder.tvOrder.visibility = View.VISIBLE
            }

            OrderStatus.Delivered.orderStatus, OrderStatus.Rejected.orderStatus,
            OrderStatus.Customer_Canceled.orderStatus, OrderStatus.Rating_Given.orderStatus,
            OrderStatus.Confirmed.orderStatus, OrderStatus.On_The_Way.orderStatus,
            OrderStatus.Near_You.orderStatus,
            OrderStatus.Ready_to_be_picked.orderStatus, OrderStatus.Reached.orderStatus,
            OrderStatus.In_Kitchen.orderStatus -> {
                holder.tvOrder.visibility = View.GONE
            }
        }

        settingData?.disable_order_cancel.let {
            if (it == "1") {
                holder.tvOrder.visibility = View.GONE
            }
        }

        //        list.get(position).gets
    }

    private fun setColor(string: String): SpannableString {
        val newString = SpannableString(string)
        val index = string.indexOf("\n") + 1
        newString.setSpan(ForegroundColorSpan(tabUnselected), index, string.length,
                0)
        return newString
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class View_holder(itemView: View) : ViewHolder(itemView) {
        internal var rvImages: RecyclerView
        internal var tvStatus: TextView
        internal var tvPrice: TextView
        internal var tvPlaced: TextView
        internal var tvOrderNo: TextView
        internal var tvDeliveryDate: TextView
        internal var tvOrder: TextView
        internal var cvContainer: CardView
        internal var mainLayout: LinearLayout

        init {
            rvImages = itemView.findViewById(R.id.rvImages)
            tvStatus = itemView.findViewById(R.id.tvStatus)
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            tvPlaced = itemView.findViewById(R.id.tvPlaced)
            tvOrderNo = itemView.findViewById(R.id.tvOrderNo)
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate)
            tvOrder = itemView.findViewById(R.id.tvOrder)
            cvContainer = itemView.findViewById(R.id.cvContainer)
            mainLayout = itemView.findViewById(R.id.mainLayout)



            rvImages.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            tvStatus.typeface = AppGlobal.semi_bold
            tvPrice.typeface = AppGlobal.semi_bold
            tvPlaced.typeface = AppGlobal.regular
            tvOrderNo.typeface = AppGlobal.regular
            tvDeliveryDate.typeface = AppGlobal.regular
            tvOrder.typeface = AppGlobal.semi_bold
            tvOrder.background = changeBorderColor(Configurations.colors.tabSelected
                    ?: "", "", GradientDrawable.RECTANGLE)
            tvOrder.setTextColor(appBackground)
            tvOrder.setOnClickListener { v: View? ->
                // CASE Confirm order
                if (list[adapterPosition].status == OrderStatus.Scheduled.orderStatus) {
                    Prefs.with(mContext).save(DataNames.ORDER_DETAIL, list[adapterPosition])
                    //  Select Payment Gateway
// Show Order Summary Screen
/*     if (list.get(getAdapterPosition()).getProduct().get(0).getOrder() == 13) {
                        Prefs.with(mContext).save(DataNames.FLOW_STROE, DataNames.PACKAGES);
                    } else {
                        Prefs.with(mContext).save(DataNames.FLOW_STROE, DataNames.GROCERY);
                    }*/Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_BRANCH_ID, "" + list[adapterPosition].supplier_branch_id)
                    Prefs.with(mContext).save(DataNames.SUPPLIER_LOGO_ID, "" + list[adapterPosition].supplier_id)
                    Prefs.with(mContext).getString(DataNames.SUPPLIER_LOGO, "" + list[adapterPosition].logo)
                    Prefs.with(mContext).save(DataNames.CATEGORY_ID, "" + (list[adapterPosition].product?.get(0)?.category_id
                            ?: 0))
                    val paymentMethod = PaymentMethod()
                    val bundle = Bundle()
                    bundle.putBoolean("confirmOrder", true)
                    paymentMethod.arguments = bundle
                    (mContext as MainActivity).pushFragments(DataNames.TAB1, paymentMethod
                            ,
                            true, true, "", true)
                } else {
                    selectedOrderId = list[adapterPosition].order_id ?: 0
                    positionSelected = adapterPosition
                    sweetDialogueFailure(mContext,
                            mContext?.getString(R.string.cancel_order, textConfig?.order) ?: "",
                            mContext?.getString(R.string.doYouCancel, textConfig?.order)
                                    ?: "", true, 101, true)
                }
            }
            //  mainLayout.setOnClickListener { mCallback?.onOrderDetail(list[adapterPosition]) }
            rvImages.setOnClickListener { mCallback?.onOrderDetail(list[adapterPosition]) }
            cvContainer.setOnClickListener { mCallback?.onOrderDetail(list[adapterPosition]) }
        }
    }

    interface OrderCallback {
        fun onOrderDetail(historyBean: OrderHistory?)
    }

    init {
        list = list1 as ArrayList<OrderHistory>
        this.appType = appType
    }
}