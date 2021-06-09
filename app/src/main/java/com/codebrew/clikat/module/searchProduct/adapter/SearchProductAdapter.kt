package com.codebrew.clikat.module.searchProduct.adapter

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.activities.MainActivity
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemCompareProductGridBinding
import com.codebrew.clikat.databinding.ItemCompareProductListBinding
import com.codebrew.clikat.modal.AppGlobal
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel.DataBean.ScreenFlowBean
import com.codebrew.clikat.module.compare_product.CompareProductsResultFragment
import com.codebrew.clikat.module.product_detail.ProductDetails
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailFrag
import com.codebrew.clikat.module.searchProduct.adapter.SearchProductAdapter.ViewHolder
import com.codebrew.clikat.module.supplier_detail.SupplierDetailFragment
import com.codebrew.clikat.preferences.DataNames
import com.codebrew.clikat.preferences.Prefs
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.customviews.ClikatTextView
import java.text.DecimalFormat

/*
 * Created by cbl80 on 8/7/16.
 */
class SearchProductAdapter(private val mContext: Context, private val list: List<ProductDataBean>, private val screenType: Boolean) : Adapter<ViewHolder>() {
    private var viewType = true

    fun settingLayout(viewType: Boolean) {
        this.viewType = viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (this.viewType) {
            val binding: ItemCompareProductGridBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_compare_product_grid, parent, false)
            binding.color = Configurations.colors
            binding.drawables = Configurations.drawables
            binding.strings = Configurations.strings
            ViewHolder(binding.root)
        } else {
            val binding: ItemCompareProductListBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_compare_product_list, parent, false)
            binding.color = Configurations.colors
            binding.drawables = Configurations.drawables
            binding.strings = Configurations.strings
            ViewHolder(binding.root)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productData = list[position]
        holder.tvName.text = productData.name
        holder.tvSupplierName.text = mContext.getString(R.string.supplier_tag, productData.supplier_name)
        //Discount ==Real price
        if (productData.price == productData.display_price) {
            holder.tvDiscountPrice.visibility = View.GONE
            if (productData.sku?.isNotEmpty()==true && productData.sku != "1") {
                holder.tvProdPrice.text = mContext.getString(R.string.discountprice_search_multiple,AppConstants.CURRENCY_SYMBOL,productData.price, productData.measuring_unit)
            } else {
                holder.tvProdPrice.text = mContext.getString(R.string.currency_tag,AppConstants.CURRENCY_SYMBOL, productData.price)
            }
        } else {
            holder.tvDiscountPrice.visibility = View.VISIBLE
            holder.tvDiscountPrice.paintFlags = holder.tvProdPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvDiscountPrice.text = mContext.getString(R.string.currency_tag,AppConstants.CURRENCY_SYMBOL, productData.display_price)
            if (productData.sku?.isNotEmpty()==true && productData.sku != "1") {
                holder.tvProdPrice.text = mContext.getString(R.string.discountprice_search_multiple,AppConstants.CURRENCY_SYMBOL, productData.price, productData.measuring_unit)
            } else {
                holder.tvProdPrice.text = mContext.getString(R.string.currency_tag,AppConstants.CURRENCY_SYMBOL, productData.price)
            }
        }
        loadImage(productData.image_path.toString(), holder.sdvImage, false)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var sdvImage: ImageView
        internal var tvName: ClikatTextView
        internal var tvDiscountPrice: ClikatTextView
        internal var tvProdPrice: ClikatTextView
        internal var tvSupplierName: ClikatTextView
        internal var rbRating: RatingBar
        internal var tvRating: ClikatTextView

        /*  override fun onClick(view: View) {
              when (view.id) {
                  R.id.tvSupplierName -> {
                      val bundle = Bundle()
                      bundle.putString("title", list[adapterPosition].supplier_name)
                      bundle.putInt("categoryId", list[adapterPosition].category_id!!)
                      bundle.putInt("supplierId", list[adapterPosition].supplier_id!!)
                      bundle.putInt("branchId", list[adapterPosition].supplier_branch_id!!)
                      val screenFlowBean = Prefs.with(mContext).getObject(DataNames.SCREEN_FLOW, ScreenFlowBean::class.java)
                      val fragment: Fragment
                      fragment = if (screenFlowBean.app_type == AppDataType.Food.type) {
                          RestaurantDetailFrag()
                      } else {
                          SupplierDetailFragment()
                      }
                      fragment.arguments = bundle
                      (mContext as MainActivity).pushFragments(DataNames.TAB1,
                              fragment,
                              true, true, "supplierDetail", true)
                  }
              }
          }*/

        init {
            sdvImage = itemView.findViewById(R.id.sdvImage)
            tvName = itemView.findViewById(R.id.tvName)
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price)
            tvProdPrice = itemView.findViewById(R.id.tv_prod_discount)
            tvSupplierName = itemView.findViewById(R.id.tvSupplierName)
            rbRating = itemView.findViewById(R.id.rb_rating)
            tvRating = itemView.findViewById(R.id.tv_rating)
            tvName.typeface = AppGlobal.regular
            tvDiscountPrice.typeface = AppGlobal.regular
            tvProdPrice.typeface = AppGlobal.regular
            tvSupplierName.typeface = AppGlobal.regular
          //  tvRating.typeface = AppGlobal.regular
            itemView.setOnClickListener { view: View? ->
                if (screenType) {
                    val bundle = Bundle()
                    val compareProdResult = CompareProductsResultFragment()
                    bundle.putParcelable("product", list[getAdapterPosition()])
                    compareProdResult.arguments = bundle
                    (mContext as MainActivity).pushFragments(DataNames.TAB1,
                            compareProdResult,
                            true, true, "", true)
                } else {
                    val bundle = Bundle()
                    bundle.putInt("productId", list[adapterPosition].product_id!!)
                    bundle.putString("title", list[adapterPosition].name)
                    bundle.putInt("offerType", 0)
                    bundle.putInt("supplier_branch_id", list[adapterPosition].supplier_branch_id!!)
                    bundle.putInt("categoryId", list[adapterPosition].category_id!!)
                    //bundle.putInt("categoryId",cat);
                    val productDetails = ProductDetails()
                    productDetails.arguments = bundle
                    (mContext as MainActivity).pushFragments(DataNames.TAB1,
                            productDetails, true, true, "", true)
                }
            }
        }
    }

}