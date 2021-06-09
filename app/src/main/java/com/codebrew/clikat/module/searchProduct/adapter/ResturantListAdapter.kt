package com.codebrew.clikat.module.searchProduct.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.databinding.ItemHomeSupplierBinding
import com.codebrew.clikat.modal.other.SupplierDataBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ResturantListAdapter(val clickListener: ResturantListener) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(MessageListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitMessageList(list: List<SupplierDataBean>?) {
        adapterScope.launch {
            val items = list?.map { DataItem.MessageItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.MessageItem
                holder.bind(nightItem.supplierData, clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  ViewHolder.from(parent)
        }
    }


    class ViewHolder private constructor(val binding: ItemHomeSupplierBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupplierDataBean, clickListener: ResturantListener) {
            binding.supplierData = item
           // binding.suppplierListener = clickListener

            binding.root.setOnClickListener {
                clickListener.onClick(item)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemHomeSupplierBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }


class MessageListDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.chatData == newItem.chatData
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}


class ResturantListener(val clickListener: (model: SupplierDataBean) -> Unit) {
    fun onClick(addressBean: SupplierDataBean) = clickListener(addressBean)
}

sealed class DataItem {
    data class MessageItem(val supplierData: SupplierDataBean) : DataItem() {
        override val chatData = supplierData
    }

    abstract val chatData: SupplierDataBean
}

