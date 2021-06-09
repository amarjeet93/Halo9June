package com.codebrew.clikat.module.home_screen.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.CommonUtils
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.setColorScale
import com.codebrew.clikat.app_utils.extension.setGreyScale
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemSpecialOfferBinding
import com.codebrew.clikat.databinding.ItemSpecialOfferListBinding
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.home_screen.adapter.SpecialListAdapter.ViewHolder
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.customviews.ClikatTextView
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_special_offer.view.*
import java.util.*

const val SERVICE_OFFER = 1

class SpecialListAdapter internal constructor(private val offerList: List<ProductDataBean>?,
                                              private val screenType: Int, private val singleVendor: Int
                                              , private val mSpecialType: Int, val clientInform: SettingModel.DataBean.SettingData?)
    : Adapter<ViewHolder>() {
    private var mContext: Context? = null
    private var mCallback: OnProductDetail? = null
    private val calendar = Calendar.getInstance()

    internal fun settingCllback(mCallback: OnProductDetail?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        mContext = viewGroup.context
        return if (viewType == SERVICE_OFFER) {
            val binding: ItemSpecialOfferBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_special_offer, viewGroup, false)
            binding.color = Configurations.colors
            binding.singleVndorType = singleVendor == VendorAppType.Single.appType
            binding.screenType = screenType
            ViewHolder(binding.root, viewType)
        } else {
            val binding: ItemSpecialOfferListBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context), R.layout.item_special_offer_list, viewGroup, false)
            binding.color = Configurations.colors
            binding.singleVndorType = singleVendor == VendorAppType.Single.appType
            binding.screenType = screenType
            ViewHolder(binding.root, viewType)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        calendar.clear()

        viewHolder.onBind(offerList?.get(viewHolder.adapterPosition))
    }

    override fun getItemViewType(position: Int): Int {
        return mSpecialType
    }

    override fun getItemCount(): Int {
        return offerList?.size ?: 0
    }

    interface OnProductDetail {
        fun onProductDetail(bean: ProductDataBean?)
        fun addToCart(position: Int, productBean: ProductDataBean?)
        fun removeToCart(position: Int, productBean: ProductDataBean?)
        fun addtoWishList(adapterPosition: Int, status: Int?, productId: Int?)
    }

    inner class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView), OnClickListener {
        internal var viewType: Int
        internal var ivProduct: RoundedImageView
        internal var tvName: TextView
        internal var tvSupplierName: TextView
        internal var tvDiscountPrice: TextView
        internal var tvActualPrice: TextView
        internal var tvFoodReview: TextView
        internal var ivPlus: ImageView
        internal var ivMinus: ImageView
        internal var ivWishlist: ImageView
        internal var tv_type_custmize: TextView
        internal var tv_percentage_price: TextView

        //internal var qty_text: TextView
        internal var tvQuant: ClikatTextView
        internal var rbRating: RatingBar
        internal var groupRating: Group
        internal var groupCart: Group
        internal var groupPrice: Group
        internal var itemLayout: ConstraintLayout
        internal var tvStock: TextView
        internal fun onBind(offerItem: ProductDataBean?) {



            var displayTime = ""

            /*   if (viewType == SERVICE_OFFER) {
                actionLayout.setVisibility(View.GONE);
            }*/

            ivProduct.loadImage(offerItem?.image_path.toString())
            //loadImage(offerItem?.image_path.toString(), ivProduct, false)
            tvName.text = offerItem?.name
            if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) {
                itemView.iv_product?.cornerRadius = StaticFunction.pxFromDp(24, mContext).toFloat()
                val set = ConstraintSet()
                set.clone(itemLayout)
                set.constrainWidth(ivProduct.id, StaticFunction.pxFromDp(184, mContext))
                set.setDimensionRatio(ivProduct.id, "H,3.8:4")
                set.applyTo(itemLayout)
            }


            tvSupplierName.visibility = if (screenType > AppDataType.Custom.type) {
                View.GONE
            } else {
                View.VISIBLE
            }

            tvSupplierName.text = mContext!!.getString(R.string.supplier_tag, offerItem?.supplier_name)

            if (offerItem?.prod_quantity == 0) {
                groupCart.visibility = View.INVISIBLE
            } else {
                groupCart.visibility = View.VISIBLE
            }

            tvQuant.text = offerItem?.prod_quantity.toString()


            if (offerItem?.is_product == 1) {
                tvActualPrice.visibility = View.GONE
                //price_type 1 for hourly 0 for fixed price
                if (offerItem.price_type == 1) {
                    calendar.add(Calendar.MINUTE, offerItem.duration ?: 0)
                    displayTime = (if (calendar[Calendar.HOUR] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                            if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""

                    tvDiscountPrice.text = mContext?.getString(R.string.discountprice_search_multiple, AppConstants.CURRENCY_SYMBOL, offerItem.netPrice, displayTime)
                } else {
                    tvDiscountPrice.text = mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, offerItem.fixed_price?.toFloatOrNull())
                }
            } else { //service

                calendar.add(Calendar.MINUTE, offerItem?.duration ?: 0)
                displayTime = (if (calendar[Calendar.HOUR] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR]) else "") + " " +
                        if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
                //if (productData.price_type == 1) {

                tvDiscountPrice.text = if (offerItem?.price_type == 0) {
                    mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, offerItem.netPrice)
                } else {
                    mContext?.getString(R.string.discountprice_search_multiple, AppConstants.CURRENCY_SYMBOL, offerItem?.netPrice, displayTime)
                }


                offerItem?.fixed_price = offerItem?.netPrice.toString()

                if (offerItem?.netPrice != offerItem?.netDiscount && offerItem?.netDiscount ?: 0.0f > 0) {
                    offerItem?.display_price = offerItem?.netDiscount.toString()
                }

            }


            if (offerItem?.fixed_price != offerItem?.display_price && offerItem?.netDiscount ?: 0.0f > 0) {
                tvActualPrice.visibility = View.VISIBLE
                tvActualPrice.paintFlags = tvDiscountPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvActualPrice.text = mContext?.getString(R.string.discountprice_tag, AppConstants.CURRENCY_SYMBOL, offerItem?.display_price?.toFloatOrNull())
            } else {
                tvActualPrice.visibility = View.GONE
            }


            if (offerItem?.is_product == 0 && offerItem.price_type == 1) {
                calendar[Calendar.HOUR_OF_DAY] = 0
                calendar[Calendar.MINUTE] = 0
                calendar[Calendar.DAY_OF_MONTH] = 1
                calendar.add(Calendar.MINUTE, (offerItem.serviceDuration?.times(offerItem.prod_quantity
                        ?: 0)) ?: 0)
                displayTime = (if (calendar[Calendar.DAY_OF_MONTH] - 1 > 0) mContext?.getString(R.string.day_tag, calendar[Calendar.DAY_OF_MONTH] - 1) else "") + " " + (if (calendar[Calendar.HOUR_OF_DAY] > 0) mContext?.getString(R.string.hour_tag, calendar[Calendar.HOUR_OF_DAY]) else "") + " " +
                        if (calendar[Calendar.MINUTE] > 0) mContext?.getString(R.string.minute_tag, calendar[Calendar.MINUTE]) else ""
                // if (productData.price_type == 1) {
                tvQuant.text = if (offerItem.prod_quantity == 0) "00:00" else displayTime
                /*  } else {
                      holder.tvQuant.text = mContext?.getString(R.string.hourly_tag, productData.prod_quantity)
                  }*/
            } else {
                tvQuant.text = offerItem?.prod_quantity.toString()
            }



            if (offerItem?.adds_on != null && offerItem.adds_on?.isNotEmpty() == true) {
                tv_type_custmize.visibility = View.VISIBLE
            } else {
                tv_type_custmize.visibility = View.INVISIBLE
            }

            if (screenType > AppDataType.Custom.type) {
                groupPrice.visibility = View.GONE
                tv_percentage_price.visibility = View.VISIBLE

                //real=offerItem?.display_price
                // disc= offerItem?.netprice

                val perncentageAmt = (offerItem?.display_price?.toFloat()?.minus(offerItem.netPrice
                        ?: 0.0f)
                        ?.div(offerItem.display_price?.toFloat() ?: 0.0f))?.times(100f)

                tv_percentage_price.text = itemView.context.getString(R.string.percentage_tag, perncentageAmt
                        ?: 0.0f)

            } else {
                groupPrice.visibility = View.VISIBLE
                tv_percentage_price.visibility = View.GONE
            }



            if (offerItem?.is_favourite == 1) {
                ivWishlist.setImageResource(R.drawable.ic_favourite)
            } else {
                ivWishlist.setImageResource(R.drawable.ic_unfavorite)
            }


            //tvActualPrice.setVisibility(View.GONE);
            // if (offerItem?.avg_rating != 0f) {
            rbRating.rating = offerItem?.avg_rating ?: 0f
            //  }


            if (screenType == AppDataType.Ecom.type && offerItem?.is_variant == 1) {
                groupCart.visibility = View.INVISIBLE
                //  qty_text.visibility = View.INVISIBLE
            }

            if (offerItem?.purchased_quantity ?: 0 >= offerItem?.quantity ?: 0 || offerItem?.quantity == 0) {
                ivProduct.setGreyScale()
                groupCart.visibility = View.INVISIBLE
                ivPlus.visibility = View.INVISIBLE
                tvStock.visibility = View.VISIBLE
                tv_type_custmize.visibility = View.INVISIBLE
                // qty_text.visibility = View.INVISIBLE
            } else {
                ivProduct.setColorScale()
                tvStock.visibility = View.GONE
                groupCart.visibility = View.VISIBLE
                //qty_text.visibility = View.VISIBLE
            }

            ivWishlist.visibility = View.GONE
            tvFoodReview.visibility = View.GONE

            when (screenType) {
                AppDataType.Food.type -> {
                    groupRating.visibility = View.GONE
                    // tvFoodReview.visibility = View.VISIBLE
                    // tvFoodReview.text = offerItem?.avg_rating.toString()
                }
                AppDataType.Ecom.type -> {
                    if (clientInform?.is_product_wishlist == "1")
                        ivWishlist.visibility = View.VISIBLE
                    tv_type_custmize.visibility = View.INVISIBLE
                    // qty_text.visibility = View.GONE
                    groupCart.visibility = View.INVISIBLE
                    ivPlus.visibility = View.INVISIBLE
                }
                AppDataType.HomeServ.type -> {
                    if (clientInform?.is_product_wishlist == "1")
                        ivWishlist.visibility = View.VISIBLE
                    groupRating.visibility = View.GONE
                    tvFoodReview.visibility = View.VISIBLE
                    tvFoodReview.text = mContext?.getString(R.string.reviews_text, offerItem?.avg_rating)
                }
                else -> {
                    groupRating.visibility = View.GONE
                    tvFoodReview.visibility = View.VISIBLE
                    tvFoodReview.text = mContext?.getString(R.string.reviews_text, offerItem?.avg_rating)
                }
            }

            if (screenType != AppDataType.Food.type) {
                itemView.setOnClickListener {
                    mCallback?.onProductDetail(offerItem)
                }
            }


        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_minus -> {
                    //offerList?.get(adapterPosition)?.discount = 1
                    mCallback?.removeToCart(adapterPosition, offerList?.get(adapterPosition))
                }
                R.id.tv_plus -> {
                    offerList?.get(adapterPosition)?.discount = 1
                    mCallback?.addToCart(adapterPosition, offerList?.get(adapterPosition))
                }
                R.id.iv_wishlist -> {
                    if (clientInform?.is_product_wishlist == "1" && (screenType == AppDataType.HomeServ.type || screenType == AppDataType.Ecom.type)) {
                        if (checkFavouriteImage(ivWishlist)) {
                            mCallback?.addtoWishList(adapterPosition, 1, offerList?.get(adapterPosition)?.product_id)
                        } else {
                            mCallback?.addtoWishList(adapterPosition, 0, offerList?.get(adapterPosition)?.product_id)
                        }
                    }
                }
            }
        }

        init {
            ivProduct = itemView.findViewById(R.id.iv_product)
            ivWishlist = itemView.findViewById(R.id.iv_wishlist)
            //  qty_text = itemView.findViewById(R.id.qty_text)
            tvName = itemView.findViewById(R.id.tv_name)
            tvSupplierName = itemView.findViewById(R.id.tv_supplier_name)
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price)
            tv_percentage_price = itemView.findViewById(R.id.tv_product_prentage)
            tvActualPrice = itemView.findViewById(R.id.tv_real_price)
            ivPlus = itemView.findViewById(R.id.tv_plus)
            ivMinus = itemView.findViewById(R.id.tv_minus)
            tvQuant = itemView.findViewById(R.id.tv_quant)
            tv_type_custmize = itemView.findViewById(R.id.tv_type_custmize)
            tvFoodReview = itemView.findViewById(R.id.tv_food_rating)
            groupRating = itemView.findViewById(R.id.rate_group)
            groupCart = itemView.findViewById(R.id.cart_group)
            groupPrice = itemView.findViewById(R.id.price_group)
            itemLayout = itemView.findViewById(R.id.itemLayout)
            rbRating = itemView.findViewById(R.id.rb_rating)
            tvStock = itemView.findViewById(R.id.stock_label)
            ivMinus.setOnClickListener(this)
            ivPlus.setOnClickListener(this)
            ivWishlist.setOnClickListener(this)

            this.viewType = viewType
        }
    }


    private fun checkFavouriteImage(tbFavourite: ImageView): Boolean {
        return CommonUtils.areDrawablesIdentical(tbFavourite.drawable, mContext?.let { ContextCompat.getDrawable(it, R.drawable.ic_unfavorite) }!!)

    }

}