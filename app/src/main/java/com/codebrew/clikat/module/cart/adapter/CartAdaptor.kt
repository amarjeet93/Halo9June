package com.codebrew.clikat.module.cart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.extension.setColorScale
import com.codebrew.clikat.app_utils.extension.setGreyScale
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.databinding.ItemCartsBinding
import com.codebrew.clikat.modal.CartInfo
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_carts.view.*
import java.util.*

/*
 * Created by Ankit Jindal on 19/4/16.
 */
class CartAdaptor(private val mContext: Context, private val list: MutableList<CartInfo>,
                  private val screenFlow: SettingModel.DataBean.ScreenFlowBean?, private val appUtils: AppUtils) : Adapter<ViewHolder>() {
    var edAdditionalRemarks: EditText? = null
    private var mCallback: CartCallback? = null
    //    float charges = 0;
    private val textConfig by lazy {   appUtils.loadAppConfig(0).strings}
    private val calendar = Calendar.getInstance()
    private var displayTime: String? = null
    fun settingCallback(mCallback: CartCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCartsBinding = ItemCartsBinding.inflate(LayoutInflater.from(mContext), parent, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = appUtils.loadAppConfig(0).strings
        binding.singleVndorType = screenFlow?.is_single_vendor == VendorAppType.Single.appType
        return HeaderViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {

            calendar.clear()
            val mProduct = list[position]

            displayTime = ""
            holder.tvName.text = mProduct.productName
            //  holder.tvMeasurementQuantity.setText(list.get(position).getMeasuringUnit());
            if (mProduct.serviceType == 0) {
                mProduct.serviceDurationSum = mProduct.serviceDuration * mProduct.quantity
            }
            holder.tvSupplierName.text = mContext.getString(R.string.supplier_tag, mProduct.supplierName)
            if (mProduct.serviceType == 0) {
                holder.cartAction.visibility = View.VISIBLE
            } else {
                holder.cartAction.visibility = if (mProduct.agentType == 1) View.INVISIBLE else View.VISIBLE
            }
            holder.cartAction.visibility = if (mProduct.isQuant == 1) View.VISIBLE else View.GONE
            if(mProduct.imagePath.toString().isNullOrEmpty()){
                holder.sdvImage.visibility = View.GONE
            }else{
                holder.sdvImage.visibility = View.VISIBLE
            }
            loadImage(mProduct.imagePath, holder.sdvImage, false)
            holder.tvAgentType.visibility = if (mProduct.agentType != 0) View.VISIBLE else View.GONE
            holder.tvAgentType.text = if (mProduct.agentType == 0) mContext.getString(R.string.agent_not_available, textConfig?.agent) else
                mContext.getString(R.string.agent_available, textConfig?.agent)


            if (mProduct.serviceType == 0) {
                //price_type 1 for hourly 0 for fixed price
                if (mProduct.hourlyPrice.isNotEmpty()) {
                    calendar.add(Calendar.MINUTE, mProduct.serviceDuration)
                    displayTime = (if (calendar[Calendar.HOUR] > 0) mContext.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                            if (calendar[Calendar.MINUTE] > 0) mContext.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
                    holder.tvPrice.text = mContext.getString(R.string.discountprice_search_multiple, CURRENCY_SYMBOL, mProduct.price, displayTime)
                } else {
                    holder.tvPrice.text = mContext.getString(R.string.discountprice_tag, CURRENCY_SYMBOL, mProduct.price)
                }
            } else { //service

                calendar.add(Calendar.MINUTE, mProduct.serviceDuration)
                displayTime = (if (calendar[Calendar.HOUR] > 0) mContext.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                        if (calendar[Calendar.MINUTE] > 0) mContext.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""

                holder.tvPrice.text = mContext.getString(R.string.discountprice_search_multiple, CURRENCY_SYMBOL, mProduct.price, displayTime)
            }

            if (mProduct.serviceType == 0 && mProduct.priceType ==1) {
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.DAY_OF_MONTH] = 1
                calendar.add(Calendar.MINUTE, mProduct.serviceDurationSum)
                displayTime = (if (calendar[Calendar.DAY_OF_MONTH] - 1 > 0) mContext.getString(R.string.day_tag, calendar[Calendar.DAY_OF_MONTH] - 1) else "") + " " + (if (calendar[Calendar.HOUR_OF_DAY] > 0) mContext.getString(R.string.hour_tag, calendar[Calendar.HOUR_OF_DAY]) else "") + " " +
                        if (calendar[Calendar.MINUTE] > 0) mContext.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
                // if (mProduct?.hourlyPrice.isNotEmpty()) {
                holder.tvCount.text = if (mProduct.quantity == 0) "00:00" else displayTime
                /* } else {
                     holder.tvCount.text = mContext?.getString(R.string.hourly_tag, mProduct.quantity)
                 }*/
            } else {
                holder.tvCount.text = mProduct.quantity.toString()
            }


            if (mProduct.add_ons != null && mProduct.add_ons?.isNotEmpty() == true) {
                holder.addonName.text = mContext.getString(R.string.addon_name_tag, mProduct.add_on_name)
                holder.addonName.visibility = View.VISIBLE
            } else {
                holder.addonName.visibility = View.GONE
            }

           // holder.foodRating.text = mProduct.avgRating.toString()

            if (screenFlow?.app_type == AppDataType.Food.type) {
                holder.reviewGroup.visibility = View.GONE
              //  holder.foodRating.visibility = View.VISIBLE
            } else {
                holder.reviewGroup.visibility = View.GONE
              //  holder.foodRating.visibility = View.GONE
            }

            //Out of Stock
            if (mProduct.isStock == true) {
                holder.sdvImage.setColorScale()
                holder.cartAction.visibility = View.VISIBLE
            } else {
                holder.sdvImage.setGreyScale()
                holder.cartAction.visibility = View.INVISIBLE
            }


            if (mProduct.varients?.isNotEmpty() == true) {
                val adapter = VarientItemAdapter()

                holder.rvVarientlist.adapter = adapter

                adapter.submitItemList(mProduct.varients)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
        internal var tvName: TextView
        internal var tvCount: TextView
        internal var sdvImage: RoundedImageView
        internal var ivPlus: ImageView
        internal var ivMinus: ImageView
        internal var tvPrice: TextView
        internal var tvSupplierName: TextView
        internal var ivDelete: ImageView
        internal var tvAgentType: TextView
        internal var cartAction: Group
        internal var reviewGroup: Group
        internal var addonName: TextView
        internal var foodRating: TextView
        internal var rvVarientlist: RecyclerView

        init {
            tvName = itemView.tvName
            tvCount = itemView.tvCount
            sdvImage = itemView.sdvImage
            ivPlus = itemView.ivPlus
            ivMinus = itemView.ivMinus
            tvPrice = itemView.tv_total_prod
            tvSupplierName = itemView.tv_supplier_name
            ivDelete = itemView.iv_delete
            tvAgentType = itemView.tv_agentType
            cartAction = itemView.cart_action
            reviewGroup = itemView.group_review
            addonName = itemView.tv_addon_name
            foodRating = itemView.tv_food_rating
            rvVarientlist = itemView.rv_varient_list

            ivPlus.setOnClickListener { v: View? -> mCallback?.addItem(adapterPosition) }
            ivMinus.setOnClickListener { v: View? -> mCallback?.removeItem(adapterPosition) }
            ivDelete.setOnClickListener { view: View? -> mCallback!!.onDeleteCart(adapterPosition) }
        }
    }

    interface CartCallback {
        fun onDeleteCart(position: Int)
        fun addItem(position: Int)
        fun removeItem(position: Int)
    }


}