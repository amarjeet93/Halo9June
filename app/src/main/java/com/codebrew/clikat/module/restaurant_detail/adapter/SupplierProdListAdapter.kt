package com.codebrew.clikat.module.restaurant_detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemTimeslotViewBinding
import com.codebrew.clikat.modal.other.ProductBean
import com.codebrew.clikat.modal.other.ProductDataBean
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailFrag
import com.codebrew.clikat.module.restaurant_detail.RestaurantDetailNewFragment
import com.codebrew.clikat.module.restaurant_detail.RestaurantSearchDialogFragment
import com.codebrew.clikat.module.restaurant_detail.adapter.SupplierProdListAdapter.ViewHolder
import com.codebrew.clikat.utils.configurations.Configurations
import java.util.*
import kotlin.collections.ArrayList

class SupplierProdListAdapter(private val productBeans: List<ProductBean>,
                              private var fragment: Fragment,
                              private val settingBean: SettingModel.DataBean.SettingData?) : Adapter<ViewHolder>(), Filterable {
    private var varientList: List<ProductBean>
    private var mContext: Context? = null
    private var isOpen: Boolean = true

    fun checkResturantOpen(isOpen: Boolean) {
        this.isOpen = isOpen
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemTimeslotViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_timeslot_view, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productBean = varientList[holder.adapterPosition]
        holder.onBind(productBean)
    }

    override fun getItemCount(): Int {
        return varientList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    varientList = productBeans
                } else {
                    val filteredList: MutableList<ProductBean> = ArrayList()
                    var beanList: MutableList<ProductDataBean>?
                    //var productBean: ProductBean
                    for (productBean in productBeans) {
                        //  productBean = ProductBean(sub_cat_name, value)
                        // name match condition. this might differ depending on your requirement
// here we are looking for name or phone number match
                        beanList = ArrayList()
                        // beanList.clear();
                        for (valueBean in productBean.value ?: mutableListOf()) {
                            if (valueBean.name?.toLowerCase(Locale.getDefault())?.contains(charSequence) == true) {
                                beanList.add(valueBean)
                            }
                        }
                        if (beanList.size > 0) { //productBean.getValue().clear();
//productBean.getValue().addAll(beanList);
                            productBean.value = beanList
                            //row.setValue(beanList);
                            filteredList.add(productBean)
                        }
                    }
                    varientList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = varientList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                varientList = (filterResults.values as? List<ProductBean>) ?: listOf()
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        internal var tvSubTitle: TextView = itemView.findViewById(R.id.tv_sub_title)
        internal var rvProdList: RecyclerView = itemView.findViewById(R.id.rv_timeperiod_slot)

        internal fun onBind(productBean: ProductBean) {


            if (productBean.detailed_sub_category != null) {
                tvSubTitle.visibility = View.GONE
                tvSubTitle.text = productBean.detailed_sub_category
            } else {
                tvSubTitle.visibility = View.GONE
            }

            if (productBean.is_SubCat_visible == true) {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = productBean.sub_cat_name
            } else {
                tvTitle.visibility = View.GONE
            }

            rvProdList.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            val adapter = ProdListAdapter(productBean.value!!, adapterPosition, isOpen,settingBean)
            rvProdList.adapter = adapter

            if (fragment is RestaurantDetailNewFragment)
                adapter.settingCallback(fragment as RestaurantDetailNewFragment)
            else if (fragment is RestaurantSearchDialogFragment)
                adapter.settingCallback(fragment as RestaurantSearchDialogFragment)
            else adapter.settingCallback(fragment as RestaurantDetailFrag)

        }

    }

    init {
        varientList = productBeans
    }
}