package com.codebrew.clikat.module.dialog_adress.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ListSearchFooterBinding
import com.codebrew.clikat.databinding.ListSearchHeaderBinding
import com.codebrew.clikat.databinding.ListSearchItemBinding
import com.codebrew.clikat.data.model.api.AddressBean
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.logging.Handler


private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_FOOTER = 2

class AddressAdapter(private val clickListener: AddressListener, internal var requireActivity: FragmentActivity):
        ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<AddressBean>?,loginStatus:Boolean) {
        adapterScope.launch {


            val items:List<DataItem> = if(loginStatus) {
                when (list) {
                    null -> listOf(DataItem.Header)+ listOf(DataItem.Footer)
                    else -> listOf(DataItem.Header)+ listOf(DataItem.Footer) + list.map { DataItem.SleepNightItem(it) }
                }
            } else {
                when (list) {
                    null -> listOf(DataItem.Header)
                    else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
                }
            }


            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(nightItem.address,clickListener,requireActivity)
            }

            is TextViewFooter ->{
                holder.bind(clickListener)
            }

            is TextViewHeader ->{
                holder.bind(clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHeader.from(parent)
            ITEM_VIEW_TYPE_FOOTER -> TextViewFooter.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.Footer -> ITEM_VIEW_TYPE_FOOTER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class TextViewHeader private constructor(val binding: ListSearchHeaderBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: AddressListener) {
            binding.clickListener = clickListener
            binding.executePendingBindings()
            binding.color=Configurations.colors
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHeader {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListSearchHeaderBinding.inflate(layoutInflater, parent, false)
                return TextViewHeader(binding)
            }
        }
    }


    class TextViewFooter private constructor(val binding: ListSearchFooterBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: AddressListener) {
            binding.clickListener = clickListener
            binding.color=Configurations.colors
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextViewFooter {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListSearchFooterBinding.inflate(layoutInflater, parent, false)
                return TextViewFooter(binding)
            }
        }
    }



    class ViewHolder private constructor(val binding: ListSearchItemBinding) : RecyclerView.ViewHolder(binding.root){
var cl_main:ConstraintLayout
        private var timer: Timer? = null
init {
    cl_main=binding.clMain
}
        fun bind(item: AddressBean, clickListener: AddressListener, requireActivity: FragmentActivity) {
            binding.addressBean = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
            cl_main.setOnClickListener {

                binding.tvAdrsLineFirst.setTextColor(requireActivity.resources.getColor(R.color.pink_color))
                binding.tvCustomerAdrs.setTextColor(requireActivity.resources.getColor(R.color.pink_color))
                timer = Timer()
                timer!!.schedule(object : TimerTask() {
                    override fun run() {

                        timer!!.cancel()
                      clickListener.onClick(item)

                    }
                }, 1000)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListSearchItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.address == newItem.address
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class AddressListener(val clickListener: (addressData: AddressBean) -> Unit,
                      val editDellistener: (view: View, addressBean: AddressBean) -> Unit, val typelistener: (mType: String) -> Unit) {

    fun onClick(model: AddressBean) = clickListener(model)

    fun onEditDelete(view:View,addressBean: AddressBean)=editDellistener(view,addressBean)

    fun onTypeClick(mType: String)=typelistener(mType)

}



sealed class DataItem {
    data class SleepNightItem(val addressBean: AddressBean): DataItem() {
        override val address = addressBean
    }

    object Header: DataItem() {
        override val address = AddressBean()
    }

    object Footer: DataItem(){
        override val address = AddressBean()
    }

    abstract val address: AddressBean
}

