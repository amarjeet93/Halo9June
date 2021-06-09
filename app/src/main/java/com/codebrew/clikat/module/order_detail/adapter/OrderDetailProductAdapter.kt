package com.codebrew.clikat.module.order_detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.OrderStatus
import com.codebrew.clikat.data.ReturnStatus
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants.Companion.CURRENCY_SYMBOL
import com.codebrew.clikat.databinding.ItemOrderDetailProductBinding
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.cart.adapter.VarientItemAdapter
import com.codebrew.clikat.module.order_detail.adapter.OrderDetailProductAdapter.ViewHolder
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.StaticFunction.openCustomChrome
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.button.MaterialButton

/*
 * Created by cbl80 on 27/4/16.
 */
class OrderDetailProductAdapter(private val mContext: Context,
                                private val list: List<ProductDataBean?>,
                                private val supplier_name: String,
                                private val screenFlowBean: ScreenFlowBean?,
                                private val settingsData: SettingModel.DataBean.SettingData?,
                                private val mCallback: OnReturnClicked,
                                private val status: Double?,private val appUtil: AppUtils) : Adapter<ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //        View view = LayoutInflater.from(mContext).inflate(R.layout.item_order_detail_product, parent, false);
        val binding: ItemOrderDetailProductBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.item_order_detail_product, parent, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = appUtil.loadAppConfig(0).strings
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mProductDataBean = list[position]
        holder.tvProductName.text = mProductDataBean?.name
        holder.tvRecipe.setOnClickListener {
            if (!mProductDataBean?.recipe_pdf.isNullOrEmpty()) {
                openCustomChrome(mContext, mProductDataBean?.recipe_pdf ?: "")
            }
        }
        holder.tvReturnProduct.setOnClickListener {
            if (mProductDataBean?.return_data.isNullOrEmpty()) {
                mCallback.onReturnProductClicked(mProductDataBean)
            }
        }

        if (screenFlowBean?.is_single_vendor == VendorAppType.Multiple.appType) {
            holder.tvSupplierName.visibility = View.VISIBLE
            holder.tvSupplierName.text = mContext.getString(R.string.supplier_tag, supplier_name)
        } else {
            holder.tvSupplierName.visibility = View.GONE
        }
        if ( mProductDataBean?.prod_variants != null) {
            holder.rvVarientlist.visibility = View.VISIBLE
            val adapter = VarientItemAdapter()
            holder.rvVarientlist.adapter = adapter
            adapter.submitItemList( mProductDataBean.prod_variants?.toMutableList())
        } else {
            holder.rvVarientlist.visibility = View.GONE
        }
        loadImage(mProductDataBean?.image_path.toString(), holder.sdvProduct, false)
        if (list.size == 1) {
            holder.divider.visibility = View.GONE
        } else {
            holder.divider.visibility = View.VISIBLE
        }
        holder.addonName.visibility = if (mProductDataBean?.adds_on!!.isNotEmpty()) View.VISIBLE else View.GONE
        holder.addonName.text = mContext.getString(R.string.addon_name_tag, mProductDataBean.add_on_name)
        val quant = if (mProductDataBean.prod_quantity == 0) mProductDataBean.quantity?:0 else mProductDataBean.prod_quantity?:0
        holder.tvProductcode.text = mContext.getString(R.string.qunatity) + " " + quant
        holder.tvPrice.text = mContext.getString(R.string.currency_tag, CURRENCY_SYMBOL, mProductDataBean.fixed_price?.toFloatOrNull())
        holder.tvRecipe.visibility =
                if (settingsData?.product_pdf_upload != null && settingsData.product_pdf_upload == "1")
                    View.VISIBLE else View.GONE


        if (settingsData?.is_return_request != null && settingsData.is_return_request == "1"
                && status==OrderStatus.Delivered.orderStatus)
            holder.tvReturnProduct.visibility = View.VISIBLE
        else
            holder.tvReturnProduct.visibility = View.GONE


        if((status==OrderStatus.Delivered.orderStatus || status==OrderStatus.Rating_Given.orderStatus)
                && mProductDataBean.is_rated==0 && settingsData?.is_product_rating=="1")
        {
            holder.rateProduct.visibility=View.VISIBLE
        }

        holder.rateProduct.setOnClickListener {
            mCallback.onRateProd(mProductDataBean)
        }


        if (!mProductDataBean.return_data.isNullOrEmpty()) {
            val status = mProductDataBean.return_data?.firstOrNull()?.status
            holder.tvReturnProduct.text = when (status) {
                ReturnStatus.Return_requested.returnStatus -> {
                    mContext.getString(R.string.return_requested)
                }
                ReturnStatus.Agent_on_the_way.returnStatus -> {
                    mContext.getString(R.string.agent_is_on_the_way)
                }
                ReturnStatus.Product_picked.returnStatus -> {
                    mContext.getString(R.string.product_picked_up)
                }
                ReturnStatus.returned.returnStatus -> {
                    mContext.getString(R.string.returned)
                }
                else -> ""
            }

        } else mContext.getString(R.string.return_product)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvProductName: TextView
        internal var tvSupplierName: TextView
        internal var tvProductcode: TextView
        internal var tvPrice: TextView
        internal var addonName: TextView
        internal var sdvProduct: ImageView
        internal var divider: View
        internal var tvRecipe: TextView
        internal var rvVarientlist: RecyclerView
        internal var tvReturnProduct: MaterialButton
        internal var rateProduct: MaterialButton

        init {
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvSupplierName = itemView.findViewById(R.id.tvSupplierName)
            tvProductcode = itemView.findViewById(R.id.tvProductcode)
            tvPrice = itemView.findViewById(R.id.tv_total_prod)
            addonName = itemView.findViewById(R.id.tvAddonName)
            sdvProduct = itemView.findViewById(R.id.sdvProduct)
            rvVarientlist = itemView.findViewById(R.id.rv_varient_list)
            divider = itemView.findViewById(R.id.divider)
            tvRecipe = itemView.findViewById(R.id.tvRecepie)
            tvReturnProduct = itemView.findViewById(R.id.tvReturnProduct)
            rateProduct = itemView.findViewById(R.id.btnRateProd)
        }
    }


    interface OnReturnClicked {
        fun onReturnProductClicked(item: ProductDataBean?)
        fun onRateProd(item: ProductDataBean?)
    }
}