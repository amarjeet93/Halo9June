package com.codebrew.clikat.module.addon_quant.adpater

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemSavedAddonBinding
import com.codebrew.clikat.modal.CartInfo
import com.codebrew.clikat.modal.CartInfoServer
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ItemQuantAdapter : ListAdapter<ProductItem, RecyclerView.ViewHolder>(ItemListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    private lateinit var mCallback: ItemListener

    fun settingCallback(mCallback: ItemListener) {
        this.mCallback = mCallback
    }

    fun submitItemList(list: List<CartInfo>?) {
        adapterScope.launch {
            val items = list?.map { ProductItem.AddonItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SavedAddOnViewHolder -> {
                val nightItem = getItem(position) as ProductItem.AddonItem
                holder.bind(nightItem.mAddonData, mCallback)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  SavedAddOnViewHolder.from(parent)
        }
    }


    class SavedAddOnViewHolder private constructor(val binding: ItemSavedAddonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartInfo, clickListener: ItemListener) {
            binding.listener=clickListener
            binding.itemData=item
            binding.currency=AppConstants.CURRENCY_SYMBOL
            binding.color=Configurations.colors

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SavedAddOnViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemSavedAddonBinding.inflate(layoutInflater, parent, false)
                return SavedAddOnViewHolder(binding)
            }
        }
    }


class ItemListDiffCallback : DiffUtil.ItemCallback<ProductItem>() {
    override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem.addonData == newItem.addonData
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem): Boolean {
        return oldItem.addonData.quantity == newItem.addonData.quantity
    }
}

class ItemListener(val clickListener: (model: CartInfo) -> Unit,
                   val minuslistener: (model: CartInfo) -> Unit) {
    fun addItem(addonBean: CartInfo) = clickListener(addonBean)
    fun minusItem(addonBean: CartInfo) = minuslistener(addonBean)
}


sealed class ProductItem {
    data class AddonItem(val mAddonData: CartInfo) : ProductItem() {
        override val addonData = mAddonData
    }

    abstract val addonData: CartInfo
}

