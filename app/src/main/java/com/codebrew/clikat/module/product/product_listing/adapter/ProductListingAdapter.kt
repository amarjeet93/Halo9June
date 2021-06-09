package com.codebrew.clikat.module.product.product_listing.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.extension.setColorScale
import com.codebrew.clikat.app_utils.extension.setGreyScale
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemProductGridBinding
import com.codebrew.clikat.databinding.ItemProductListBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.BookingFlowBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter.ViewHolder
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.button.MaterialButton
import java.util.*
import kotlin.collections.ArrayList

/*
 * Created by Ankit Jindal on 19/4/16.
 */
class ProductListingAdapter(private var mContext: Context?, private val varientList: List<ProductDataBean>?,
                            private val appUtils: AppUtils) : Adapter<ViewHolder>(), Filterable {
    private var mCallback: ProductCallback? = null
    private var viewType = true
    private var varientFilteredList: List<ProductDataBean>

    private val cart_flow: Int
    private val bookingFlowBean: BookingFlowBean
    private val screenFlowBean: ScreenFlowBean
    private val calendar = Calendar.getInstance()
    fun settingCallback(mCallback: ProductCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        return if (this.viewType) {
            val binding: ItemProductGridBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_product_grid, parent, false)
            binding.color = Configurations.colors
            binding.drawables = Configurations.drawables
            binding.strings = appUtils.loadAppConfig(0).strings
            binding.singleVndorType = screenFlowBean.is_single_vendor == VendorAppType.Single.appType
            ViewHolder(binding.root)
        } else {
            val binding: ItemProductListBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_product_list, parent, false)
            binding.color = Configurations.colors
            binding.drawables = Configurations.drawables
            binding.strings = appUtils.loadAppConfig(0).strings
            binding.singleVndorType = screenFlowBean.is_single_vendor == VendorAppType.Single.appType
            ViewHolder(binding.root)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productData = varientFilteredList[position]
        calendar.clear()
        var displayTime = ""
        holder.tvName.text = varientFilteredList[position].name
        holder.tvRating.visibility = View.GONE

        holder.tvSupplierName.text = mContext?.getString(R.string.supplier_tag, productData.supplier_name)
        when (cart_flow) {


            0, 2 -> holder.groupCart.visibility = View.GONE
            1, 3 -> holder.groupCart.visibility = View.VISIBLE
        }
        if (varientFilteredList[position].is_product == 0) {
            holder.groupCart.visibility = View.VISIBLE
            holder.tvAddCart.visibility = View.GONE
        } else {
            holder.groupCart.visibility = if (varientFilteredList[position].is_agent == 1 && varientFilteredList[position].agent_list == 1) View.GONE else View.VISIBLE
            holder.tvAddCart.visibility = if (varientFilteredList[position].is_agent == 1 && varientFilteredList[position].agent_list == 1) View.VISIBLE else View.GONE
        }
        if (varientFilteredList[position].is_variant == 1) {
            holder.groupCart.visibility = View.INVISIBLE
            holder.tvAddCart.visibility = View.GONE
        } else {
            holder.groupCart.visibility = if (productData.is_quantity != 0) View.VISIBLE else View.INVISIBLE
            holder.tvAddCart.visibility = if (productData.is_quantity == 0) View.VISIBLE else View.GONE
        }
        loadImage(varientFilteredList[position].image_path.toString(), holder.sdvImage, false)


        //product
        if (productData.is_product == 1) {
            holder.tvActualPrice.visibility = View.GONE
            //price_type 1 for hourly 0 for fixed price
            if (productData.price_type == 1) {
                calendar.add(Calendar.MINUTE, productData.duration ?: 0)
                displayTime = (if (calendar[Calendar.HOUR] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                        if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
                holder.tvPrice.text = mContext?.getString(R.string.discountprice_search_multiple, AppConstants.CURRENCY_SYMBOL, productData.netPrice, displayTime)
            } else {
                holder.tvPrice.text = mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, productData.fixed_price?.toFloatOrNull())
            }
        } else { //service

            calendar.add(Calendar.MINUTE, productData.duration ?: 0)
            displayTime = (if (calendar[Calendar.HOUR] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                    if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""

            holder.tvPrice.text = if (productData.price_type == 0) {
                mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, productData.netPrice)
            } else {
                mContext?.getString(R.string.discountprice_search_multiple, AppConstants.CURRENCY_SYMBOL, productData.netPrice, displayTime)
            }

            productData.fixed_price = productData.netPrice.toString()

            if (productData.netPrice != productData.netDiscount && productData.netDiscount ?: 0.0f > 0) {
                productData.display_price = productData.netDiscount.toString()
            }

        }

        if (productData.fixed_price != productData.display_price && productData.netDiscount ?: 0.0f > 0) {
            holder.tvActualPrice.visibility = View.VISIBLE
            holder.tvActualPrice.paintFlags = holder.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvActualPrice.text = mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, productData.display_price?.toFloatOrNull())
        } else {
            holder.tvActualPrice.visibility = View.GONE
        }


        // if (productData.avg_rating != 0f) {
        holder.rbProdRating.rating = productData.avg_rating
        //  holder.tvRating.text = mContext?.getString(R.string.reviews_text, productData.avg_rating)

        //   }


        if (productData.is_product == 0 && productData.price_type == 1) {
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.MINUTE, (productData.serviceDuration?.times(productData.prod_quantity
                    ?: 0)) ?: 0)
            displayTime = (if (calendar[Calendar.DAY_OF_MONTH] - 1 > 0) mContext?.getString(R.string.day_tag, calendar[Calendar.DAY_OF_MONTH] - 1) else "") + " " + (if (calendar[Calendar.HOUR_OF_DAY] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR_OF_DAY]) else "") + " " +
                    if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
            // if (productData.price_type == 1) {
            holder.tvQuant.text = if (productData.prod_quantity == 0) "00:00" else displayTime
            /*  } else {
                  holder.tvQuant.text = mContext?.getString(R.string.hourly_tag, productData.prod_quantity)
              }*/
        } else {
            holder.tvQuant.text = productData.prod_quantity.toString()
        }



        if (screenFlowBean.app_type == AppDataType.HomeServ.type) {
            if (productData.is_quantity == 0 && productData.prod_quantity ?: 0 > 0) {
                holder.tvAddCart.text = mContext?.getString(R.string.remove_cart)
            } else {
                holder.tvAddCart.text = mContext?.getString(R.string.add_cart_tag)
            }
        }

        if (productData.is_favourite == 1) {
            holder.ivWishlist.setImageResource(R.drawable.ic_favourite)
        } else {
            holder.ivWishlist.setImageResource(R.drawable.ic_unfavorite)
        }


        holder.ivWishlist.visibility = View.GONE
        holder.tvFoodReview.visibility = View.GONE

        when (screenFlowBean.app_type) {
            AppDataType.Food.type -> {
                holder.groupRating.visibility = View.GONE
                holder.tvFoodReview.visibility = View.VISIBLE
                holder.tvFoodReview.text = productData.avg_rating.toString()
            }
            AppDataType.Ecom.type -> {
                holder.ivWishlist.visibility = View.VISIBLE
            }
            else -> {
                holder.groupRating.visibility = View.VISIBLE
            }
        }

        holder.qtyText.text = if (productData.price_type == 1) mContext?.getString(R.string.per_hour) else mContext?.getString(R.string.qty)

        //holder.itemView.setEnabled(true);
        holder.itemView.setOnClickListener {
            mCallback!!.productDetail(varientFilteredList[holder.adapterPosition])
        }


        if (productData.purchased_quantity ?: 0 >= productData.quantity ?: 0 || productData.quantity == 0) {
            holder.sdvImage.setGreyScale()
            holder.groupCart.visibility = View.GONE
            holder.tvStock.visibility = View.VISIBLE
        } else {
            holder.sdvImage.setColorScale()
            holder.tvStock.visibility = View.GONE
            holder.groupCart.visibility = View.VISIBLE
        }

        if (productData.adds_on?.isNotEmpty() == true) {
            holder.tvcustmize.visibility = View.VISIBLE
        } else {
            holder.tvcustmize.visibility = View.INVISIBLE
        }

        if (screenFlowBean.app_type == AppDataType.Ecom.type) {
            holder.groupCart.visibility = View.GONE
            holder.tvcustmize.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return varientFilteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                varientFilteredList = if (charString.isEmpty()) {
                    varientList ?: emptyList()
                } else {
                    val filteredList: MutableList<ProductDataBean> = ArrayList()
                    if (varientList != null) {
                        for (row in varientList) { // name match condition. this might differ depending on your requirement
                            // here we are looking for name  match
                            if (row.name?.toLowerCase(Locale.getDefault())?.contains(charString) == true) {
                                filteredList.add(row)
                            }
                        }
                    }
                    /*        if (filteredList.isEmpty())
                        varientFilteredList=varientList;
                    else*/filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = varientFilteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                varientFilteredList = filterResults.values as List<ProductDataBean>
                mCallback!!.publishResult(varientFilteredList.size)
                notifyDataSetChanged()
            }
        }
    }

    fun settingLayout(viewType: Boolean) {
        this.viewType = viewType
        //   notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnClickListener {

        internal var tvPrice: TextView
        internal var tvActualPrice: TextView
        internal var tvName: TextView
        internal var sdvImage: ImageView
        internal var tvSupplierName: TextView
        internal var qtyText: TextView
        internal var tvMinus: ImageView
        internal var tvQuant: TextView
        internal var tvPlus: ImageView
        internal var rbProdRating: RatingBar
        internal var tvRating: TextView
        internal var groupCart: Group
        internal var tvAddCart: MaterialButton
        internal var tvcustmize: TextView
        internal var tvFoodReview: TextView
        internal var ivWishlist: ImageView
        internal var groupRating: Group
        internal var tvStock: TextView

        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_minus -> mCallback?.removeToCart(varientList?.indexOf(varientFilteredList[adapterPosition])
                        ?: -1)
                R.id.tv_plus -> {
                    //if both agent_type & is_agent ==1 then it's service
                    mCallback?.addToCart(varientList?.indexOf(varientFilteredList[adapterPosition])
                            ?: -1, varientFilteredList.any {
                        it.is_agent == 1 && it.agent_list == 1
                    })
                }
                R.id.tvAddCart -> if (tvAddCart.text.toString() == mContext?.getString(R.string.add_cart_tag) || tvAddCart.text.toString() == mContext?.getString(R.string.add_to_cart)) {
                    //if both agent_type & is_agent ==1 then it's service
                    mCallback?.addToCart(varientList?.indexOf(varientFilteredList[adapterPosition])
                            ?: -1, varientFilteredList.any {
                        it.is_agent == 1 && it.agent_list == 1
                    })
                } else {
                    mCallback?.removeToCart(varientList?.indexOf(varientFilteredList[adapterPosition])
                            ?: -1)
                }

                R.id.iv_wishlist -> {
                    if (screenFlowBean.app_type == AppDataType.Ecom.type) {
                        if (checkFavouriteImage(ivWishlist)) {
                            mCallback?.addtoWishList(adapterPosition, 1, varientFilteredList.get(adapterPosition).product_id)
                        } else {
                            mCallback?.addtoWishList(adapterPosition, 0, varientFilteredList.get(adapterPosition).product_id)
                        }
                    }
                }
            }
        }

        init {
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            tvActualPrice = itemView.findViewById(R.id.tvActualPrice)
            tvName = itemView.findViewById(R.id.tvName)
            sdvImage = itemView.findViewById(R.id.sdvImage)
            tvSupplierName = itemView.findViewById(R.id.tvSupplierName)
            qtyText = itemView.findViewById(R.id.qty_text)
            tvMinus = itemView.findViewById(R.id.tv_minus)
            tvQuant = itemView.findViewById(R.id.tv_quant)
            tvPlus = itemView.findViewById(R.id.tv_plus)
            rbProdRating = itemView.findViewById(R.id.rb_prod_rating)
            tvRating = itemView.findViewById(R.id.tv_rating)
            groupCart = itemView.findViewById(R.id.group_cart)
            tvAddCart = itemView.findViewById(R.id.tvAddCart)
            tvcustmize = itemView.findViewById(R.id.tv_type_custmize)
            tvFoodReview = itemView.findViewById(R.id.tv_food_rating)
            ivWishlist = itemView.findViewById(R.id.iv_wishlist)
            groupRating = itemView.findViewById(R.id.rate_group)
            tvStock = itemView.findViewById(R.id.stock_label)

            tvMinus.setOnClickListener(this)
            tvPlus.setOnClickListener(this)
            ivWishlist.setOnClickListener(this)
            tvAddCart.setOnClickListener(this)

            tvPrice.typeface = AppGlobal.regular
            tvName.typeface = AppGlobal.regular
            tvSupplierName.typeface = AppGlobal.regular
            qtyText.typeface = AppGlobal.regular
            tvActualPrice.typeface = AppGlobal.semi_bold
            tvActualPrice.paintFlags = tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    interface ProductCallback {
        fun addToCart(position: Int, agentType: Boolean)
        fun removeToCart(position: Int)
        fun productDetail(bean: ProductDataBean?)
        fun publishResult(count: Int)
        fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?)
    }

    init {
        /*      this.list.clear();
        this.list.addAll(list);*/
        varientFilteredList = varientList ?: emptyList()
        bookingFlowBean = Prefs.with(mContext).getObject(DataNames.BOOKING_FLOW, BookingFlowBean::class.java)
        screenFlowBean = Prefs.with(mContext).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
        cart_flow = bookingFlowBean.cart_flow
    }

    private fun checkFavouriteImage(tbFavourite: ImageView): Boolean {
        return CommonUtils.areDrawablesIdentical(tbFavourite.drawable, mContext?.let { ContextCompat.getDrawable(it, R.drawable.ic_unfavorite) }!!)

    }
}