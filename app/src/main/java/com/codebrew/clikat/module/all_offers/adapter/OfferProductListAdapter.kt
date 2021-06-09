package com.codebrew.clikat.module.all_offers.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.setColorScale
import com.codebrew.clikat.app_utils.extension.setGreyScale
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemProductListBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.all_offers.adapter.OfferProductListAdapter.ViewHolder
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations

/*
 * Created by cbl80 on 16/6/16.
 */
class OfferProductListAdapter(private val list: List<ProductDataBean>, private val screenFlow: SettingModel.DataBean.ScreenFlowBean?) : Adapter<ViewHolder>() {
    private var mCallback: ProductCallback? = null

    private var mContext: Context? = null

    fun settingCallback(mCallback: ProductCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mContext = parent.context

        val binding: ItemProductListBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.item_product_list, parent, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = Configurations.strings
        binding.singleVndorType=screenFlow?.is_single_vendor== VendorAppType.Single.appType
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mProduct = list[position]

        holder.sdvImage.loadImage(mProduct.image_path.toString())
        holder.tvName.text = mProduct.name
        holder.tvSupplierName.text = mContext?.getString(R.string.supplier_tag, mProduct.supplier_name)
        holder.tvActualPrice.text = mContext?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mProduct.display_price?.toFloatOrNull())
        holder.tvPrice.text = mContext?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, mProduct.netPrice)
        holder.tvQuant.text = mProduct.prod_quantity.toString()

        if (mProduct.avg_rating > 0f) {
            holder.rbProdRating.rating = mProduct.avg_rating
        }


        if (mProduct.adds_on?.isEmpty() == true) {
            holder.tvCustomize.visibility = View.INVISIBLE
        } else {
            holder.tvCustomize.visibility = View.VISIBLE
        }

        if (mProduct.purchased_quantity ?: 0 >= mProduct.quantity ?: 0 || mProduct.quantity == 0) {
            holder.sdvImage.setGreyScale()
            holder.tvStock.visibility = View.VISIBLE
            holder.actionLayout.visibility = View.GONE
            holder.tvCustomize.visibility = View.GONE
        } else {
            holder.sdvImage.setColorScale()
            holder.tvStock.visibility = View.GONE
            holder.actionLayout.visibility = View.VISIBLE
        }

        holder.ivWishlist.visibility = View.GONE
        holder.tvFoodReview.visibility = View.GONE

        when (screenFlow?.app_type) {
            AppDataType.Food.type -> {
                holder.groupRating.visibility = View.GONE
                holder.tvFoodReview.visibility = View.VISIBLE
                holder.tvFoodReview.text = mProduct.avg_rating.toString()
            }
            AppDataType.Ecom.type -> {
                holder.ivWishlist.visibility = View.VISIBLE
                holder.tv_type_custmize.visibility=View.GONE
            }
            else -> {
                holder.groupRating.visibility = View.VISIBLE
            }
        }


        holder.itemView.setOnClickListener { v: View? ->
                if (screenFlow?.app_type != AppDataType.Food.type) {
                    mCallback?.productDetail(list[holder.adapterPosition])
                }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnClickListener {

        internal var tvPrice: TextView
        internal var tvName: TextView
        internal var sdvImage: ImageView
        internal var ivPlus: ImageView
        internal var ivMinus: ImageView
        internal var tvSupplierName: TextView
        internal var tvActualPrice: TextView
        internal var actionLayout: Group
        internal var tvRating: TextView
        internal var tvQuant: TextView
        internal var qtyText: TextView
        internal var rbProdRating: RatingBar
        internal var tvCustomize: TextView
        internal var tvFoodReview: TextView
        internal var tvStock: TextView
        internal var ivWishlist: ImageView
        internal var groupRating: Group
        internal var tv_type_custmize: TextView

        override fun onClick(view: View) {
            when (view.id) {
                R.id.tv_minus -> mCallback?.removeToCart(adapterPosition)
                R.id.tv_plus -> mCallback?.addToCart(adapterPosition)
            }
        }

        init {
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            tvName = itemView.findViewById(R.id.tvName)
            sdvImage = itemView.findViewById(R.id.sdvImage)
            ivPlus = itemView.findViewById(R.id.tv_plus)
            ivMinus = itemView.findViewById(R.id.tv_minus)
            tvSupplierName = itemView.findViewById(R.id.tvSupplierName)
            tvActualPrice = itemView.findViewById(R.id.tvActualPrice)
            actionLayout = itemView.findViewById(R.id.group_cart)
            tvQuant = itemView.findViewById(R.id.tv_quant)
            qtyText = itemView.findViewById(R.id.qty_text)
            rbProdRating = itemView.findViewById(R.id.rb_prod_rating)
            tvCustomize = itemView.findViewById(R.id.tv_type_custmize)
            tvFoodReview = itemView.findViewById(R.id.tv_food_rating)
            tvRating = itemView.findViewById(R.id.tv_rating)
            tvStock = itemView.findViewById(R.id.stock_label)
            ivWishlist = itemView.findViewById(R.id.iv_wishlist)
            groupRating = itemView.findViewById(R.id.rate_group)
            tv_type_custmize = itemView.findViewById(R.id.tv_type_custmize)

            tvPrice.typeface = AppGlobal.regular
            tvName.typeface = AppGlobal.regular
            tvQuant.typeface = AppGlobal.regular
            tvSupplierName.typeface = AppGlobal.regular
            tvSupplierName.visibility = View.VISIBLE
            tvActualPrice.typeface = AppGlobal.regular
            tvActualPrice.paintFlags = tvActualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            ivMinus.setOnClickListener(this)
            ivPlus.setOnClickListener(this)

        }
    }

    interface ProductCallback {
        fun addToCart(position: Int)
        fun removeToCart(position: Int)
        fun productDetail(bean: ProductDataBean?)
    }

}