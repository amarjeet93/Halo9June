package com.codebrew.clikat.module.product_addon.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.data.model.api.Value
import com.codebrew.clikat.databinding.ItemAddonBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddOnItemAdapter(val clickListener: ItemListener) :
    ListAdapter<ProductItem, RecyclerView.ViewHolder>(ItemListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitItemList(list: List<Value>?) {
        adapterScope.launch {
            val items = list?.map { ProductItem.AddonItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddOnViewHolder -> {
                val nightItem = getItem(position) as ProductItem.AddonItem
                holder.bind(nightItem.mAddonData, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  AddOnViewHolder.from(parent)
        }
    }


    class AddOnViewHolder private constructor(val binding: ItemAddonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Value, clickListener: ItemListener) {
            binding.itemData = item
            binding.clickListener=clickListener
            binding.currency=AppConstants.CURRENCY_SYMBOL
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AddOnViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAddonBinding.inflate(layoutInflater, parent, false)
                return AddOnViewHolder(binding)
            }
        }
    }


class ItemListDiffCallback : DiffUtil.ItemCallback<ProductItem>() {
    override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem.addonData == newItem.addonData
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem == newItem
    }
}


sealed class ProductItem {
    data class AddonItem(val mAddonData: Value) : ProductItem() {
        override val addonData = mAddonData
    }

    abstract val addonData: Value
}

