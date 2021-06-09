package com.codebrew.clikat.module.subcategory.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.databinding.ItemSubcategoryParentBinding
import com.codebrew.clikat.modal.other.Brand
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SubCatList
import com.codebrew.clikat.modal.other.SubCategoryData
import com.codebrew.clikat.module.home_screen.adapter.BrandsListAdapter
import com.codebrew.clikat.module.product.product_listing.adapter.ProductListingAdapter
import com.codebrew.clikat.module.subcategory.SubCategory
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_subcategory_parent.view.*

class SubCatAdapter(private val mChecklist: List<SubCatList>,
                    private val appUtils: AppUtils) : RecyclerView.Adapter<SubCatAdapter.ViewHolder>() {

    private var mContext:Context?=null

    private var fragment:SubCategory?=null

    private lateinit var mCallback:SubCatCallback

    fun setFragCallback(fragment: SubCategory,mCallback:SubCatCallback) {
        this.fragment = fragment
        this.mCallback=mCallback
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mContext=parent.context

        val binding = DataBindingUtil.inflate<ItemSubcategoryParentBinding>(LayoutInflater.from(parent.context),
                R.layout.item_subcategory_parent, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return mChecklist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var type=""
        var viewType=""

        when(mChecklist[holder.adapterPosition].name)
        {
            "subcategory"->{
                type=mContext!!.getString(R.string.shop_by_type)
                viewType="grid"

                val adapter=SubCategorySelectAdapter(mChecklist[holder.adapterPosition].datalist as MutableList<SubCategoryData>?)
                adapter.settingCallback(fragment)
                holder.rvList.adapter=adapter
            }
            "brand"->{
                type=mContext!!.getString(R.string.popular_brand)
                viewType="horizontal"

                val adapter= BrandsListAdapter(mChecklist[holder.adapterPosition].datalist as List<Brand>)
                fragment?.let { adapter.settingCallback(it) }
                holder.rvList.adapter=adapter

            }
            "offer"->{
                type=mContext!!.getString(R.string.discount_product)
                viewType="grid"

                val adapter = ProductListingAdapter(mContext, mChecklist[holder.adapterPosition].datalist as MutableList<ProductDataBean>?, appUtils)
                adapter.settingCallback(fragment)
                holder.rvList.adapter=adapter

                mCallback.onProdItemUpdate(adapter)
            }

        }

        holder.tvName.text=type
        holder.rvList.layoutManager=lytManager(viewType)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvName=itemView.tv_category
        var rvList=itemView.rv_subcategory

    }

    private fun lytManager(type: String): RecyclerView.LayoutManager {

        return if (type == "horizontal") {
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        } else if (type == "grid") {
            GridLayoutManager(mContext, 2)
        } else
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
    }

    interface SubCatCallback{
        fun onProdItemUpdate(adpater: ProductListingAdapter)
    }
}