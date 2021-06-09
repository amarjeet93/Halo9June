package com.codebrew.clikat.module.restaurant_detail.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.app_utils.extension.setColorScale
import com.codebrew.clikat.app_utils.extension.setGreyScale
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemSupplierProductBinding
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.restaurant_detail.adapter.ProdListAdapter.ViewHolder
import com.codebrew.clikat.utils.configurations.Configurations
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.item_supplier_product.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class ProdListAdapter(private val beanList: List<ProductDataBean>,
                      private val parentPosition: Int, private val isOpen: Boolean,
                      private val settingBean: SettingModel.DataBean.SettingData?) : Adapter<ViewHolder>() {
    private var mContext: Context? = null
    private var mCallback: ProdCallback? = null

    fun settingCallback(mCallback: ProdCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemSupplierProductBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_supplier_product, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = beanList[holder.adapterPosition]
        holder.onBind(bean, isOpen)
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ivProd: RoundedImageView = itemView.findViewById(R.id.iv_prod)
        internal var tvName: TextView = itemView.findViewById(R.id.tv_name)
        internal var tvPrice: TextView
        internal var tvActualPrice: TextView
        internal var ivIncrement: ImageView
        internal var tvQuant: TextView
        internal var ivDecrement: ImageView
        internal var actionGroup: Group
        internal var tv_type_custmize: TextView
        internal var tvFoodReview: TextView
        internal var tvFoodDesc: TextView
        internal fun onBind(bean: ProductDataBean, open: Boolean) {
            itemView.setOnClickListener {
                mCallback?.onProdDetail(bean)
            }

            val svSE = DecimalFormat("#,###.00")
            val symbols = DecimalFormatSymbols(Locale("sv", "SE"))
            symbols.decimalSeparator = ','
            symbols.groupingSeparator = ' '
            svSE.decimalFormatSymbols = symbols
            if (bean.image_path.toString().isNotEmpty()){
                ivProd.visibility = View.VISIBLE
            }else{
                ivProd.visibility = View.GONE
            }
                ivProd.loadImage(bean.image_path.toString())
            // loadImage(bean.image_path.toString(), ivProd, false)
            tvName.text = bean.name

            //     val value = svSE.format(bean.fixed_price.toString())


            tvPrice.text = mContext?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, bean.fixed_price?.toFloatOrNull())
            if (bean.fixed_price != bean.display_price) {
                tvActualPrice.visibility = View.VISIBLE
                tvActualPrice.paintFlags = tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvActualPrice.text = mContext?.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, bean.display_price?.toFloatOrNull())

            } else {
                tvActualPrice.visibility = View.GONE
            }





            actionGroup.visibility = if (bean.prod_quantity ?: 0 >= 1) View.VISIBLE else View.GONE
            if(bean.prod_quantity!!>0)
            {
                ivDecrement.visibility=View.VISIBLE
                tvQuant.visibility=View.VISIBLE
            }else{
                ivDecrement.visibility=View.GONE
                tvQuant.visibility=View.GONE
            }
            tvQuant.text = bean.prod_quantity.toString()
            if (bean.adds_on != null && bean.adds_on?.isNotEmpty() == true) {
                tv_type_custmize.visibility = View.VISIBLE
            } else {
                tv_type_custmize.visibility = View.GONE
            }

            //Out of Stock
            if (bean.purchased_quantity ?: 0 >= bean.quantity ?: 0 || bean.quantity == 0) {
                ivProd.setGreyScale()
                actionGroup.visibility = View.GONE
                ivIncrement.visibility = View.INVISIBLE
            } else {
                ivProd.setColorScale()
                ivIncrement.visibility = View.VISIBLE
                actionGroup.visibility = if (bean.prod_quantity ?: 0 >= 1) View.VISIBLE else View.GONE
            }

            if (!open) {
                ivIncrement.setColorFilter(Color.parseColor("#FF64584E"), android.graphics.PorterDuff.Mode.SRC_IN)
                ivDecrement.setColorFilter(Color.parseColor("#FF64584E"), android.graphics.PorterDuff.Mode.SRC_IN)
            }

            tvFoodReview.text = bean.avg_rating.toString()

            tvFoodDesc.text = HtmlCompat.fromHtml(bean.product_desc.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

            itemView.tvViewDetail?.visibility = if (AppConstants.APP_SUB_TYPE > AppDataType.Custom.type) View.VISIBLE else View.GONE


            settingBean?.hide_ratings?.let {
                if (it == "1")
                    tvFoodReview.visibility = View.GONE
            }

            tvFoodReview.visibility = View.GONE


        }

        init {
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            tvActualPrice = itemView.findViewById(R.id.tvActualPrice)
            ivIncrement = itemView.findViewById(R.id.iv_increment)
            tvQuant = itemView.findViewById(R.id.tv_quant)
            ivDecrement = itemView.findViewById(R.id.iv_decrement)
            actionGroup = itemView.findViewById(R.id.actionGroup)
            tv_type_custmize = itemView.findViewById(R.id.tv_type_custmize)
            tvFoodReview = itemView.findViewById(R.id.tv_food_rating)
            tvFoodDesc = itemView.findViewById(R.id.tv_desc)

            ivIncrement.setOnClickListener { mCallback?.onProdAdded(beanList[adapterPosition], parentPosition, adapterPosition, !isOpen) }
            ivDecrement.setOnClickListener { mCallback?.onProdDelete(beanList[adapterPosition], parentPosition, adapterPosition, !isOpen) }

            tvFoodDesc.setOnClickListener {
                mCallback?.onDescExpand(tvFoodDesc, beanList[adapterPosition], parentPosition)
            }
        }
    }

    interface ProdCallback {
        fun onProdAdded(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean)
        fun onProdDelete(productBean: ProductDataBean?, parentPosition: Int, childPosition: Int, isOpen: Boolean)
        fun onProdDetail(productBean: ProductDataBean?)
        fun onDescExpand(tvDesc: TextView?, productBean: ProductDataBean?, childPosition: Int)
    }

}