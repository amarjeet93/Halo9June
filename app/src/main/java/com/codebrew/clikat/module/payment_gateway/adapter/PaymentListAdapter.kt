package com.codebrew.clikat.module.payment_gateway.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.data.model.others.CustomPayModel
import com.codebrew.clikat.databinding.ItemPaymentListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentListAdapter  : ListAdapter<PaymentItem, RecyclerView.ViewHolder>(ItemListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    private lateinit var mCallback: PayListener

    fun settingCallback(mCallback: PayListener) {
        this.mCallback = mCallback
    }

    fun submitItemList(list: List<CustomPayModel>?) {
        adapterScope.launch {
            val items = list?.map { PaymentItem.ProductDataItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                val modelItem = getItem(position) as PaymentItem.ProductDataItem
                holder.bind(modelItem.payItem, mCallback)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  ImageViewHolder.from(parent)
    }
}


class ImageViewHolder private constructor(val binding: ItemPaymentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CustomPayModel, listener: PayListener) {
        binding.payModel=item
        binding.listener=listener
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemPaymentListBinding.inflate(layoutInflater, parent, false)
            return ImageViewHolder(binding)
        }
    }
}


class ItemListDiffCallback : DiffUtil.ItemCallback<PaymentItem>() {
    override fun areItemsTheSame(oldItem: PaymentItem, newItem: PaymentItem): Boolean {
        return oldItem.payItem == newItem.payItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: PaymentItem, newItem: PaymentItem): Boolean {
        return oldItem.payItem == newItem.payItem
    }
}

class PayListener(val payListener: (imageModel: CustomPayModel) -> Unit) {
    fun imageClick(image: CustomPayModel) = payListener(image)
}




sealed class PaymentItem {
    data class ProductDataItem(val mPaymentData: CustomPayModel) : PaymentItem() {
        override val payItem = mPaymentData
    }

    abstract val payItem: CustomPayModel
}


