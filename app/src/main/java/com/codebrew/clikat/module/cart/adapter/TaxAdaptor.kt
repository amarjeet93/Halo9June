package com.codebrew.clikat.module.cart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemTaxBinding
import com.codebrew.clikat.databinding.ItemTipsBinding
import com.codebrew.clikat.module.cart.model.TaxModel
import com.codebrew.clikat.utils.configurations.ColorConfig
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.item_tax.view.*
import java.util.*
import kotlin.collections.ArrayList

class TaxAdaptor(private val mContext: Context,
                 val categories: ArrayList<TaxModel>) : RecyclerView.Adapter<TaxAdaptor.TaxViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxViewHolder {
        val binding: ItemTaxBinding = ItemTaxBinding.inflate(LayoutInflater.from(mContext), parent, false)
        binding.color = Configurations.colors
        return TaxViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return categories.size
    }


    override fun onBindViewHolder(holder: TaxViewHolder, position: Int) {


        val amount = categories[position].price
        holder.tvPrice.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, amount)

        holder.tvCategoryName.text = mContext.getString(R.string.var_tag, categories[position].handleAdminCharges)

    }


    inner class TaxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var tvPrice: TextView = itemView.tvRestService
        internal var tvCategoryName: TextView = itemView.txtRestService

    }


}