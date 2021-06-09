package com.codebrew.clikat.module.wishlist_prod.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.databinding.ItemHomeSupplierBinding
import com.codebrew.clikat.modal.other.SupplierDataBean
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_home_supplier.view.*


class SuppliersWishListAdapter(val clickListener: OnItemClicked) : RecyclerView.Adapter<SuppliersWishListAdapter.ViewHolder>() {

    private var suppliersList = ArrayList<SupplierDataBean>()
    private var binding: ItemHomeSupplierBinding? = null

    override fun getItemCount(): Int {
        return suppliersList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(suppliersList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = ItemHomeSupplierBinding.inflate(layoutInflater, parent, false)
        binding?.color = Configurations.colors
        binding?.drawables = Configurations.drawables
        binding?.strings = Configurations.strings
        return ViewHolder(binding?.root!!)
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.iv_wishlist?.setOnClickListener {
                clickListener.removeProduct(suppliersList[adapterPosition], adapterPosition)
            }
            itemView.setOnClickListener {
                clickListener.onItemClicked(suppliersList[adapterPosition])
            }
        }

        fun bind(item: SupplierDataBean) {
            binding?.supplierData = item
            // binding.suppplierListener = clickListener
            itemView.iv_wishlist?.visibility = View.VISIBLE
            binding?.executePendingBindings()
        }
    }

    fun addList(list: MutableList<SupplierDataBean>) {
        suppliersList.clear()
        suppliersList.addAll(list)
        notifyDataSetChanged()
    }

    fun removedProduct(position: Int?) {
        if (position != null) {
            suppliersList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getList(): MutableList<SupplierDataBean>? {
        return suppliersList
    }
}


interface OnItemClicked {
    fun onItemClicked(dataItem: SupplierDataBean)
    fun removeProduct(dataItem: SupplierDataBean, position: Int)
}

