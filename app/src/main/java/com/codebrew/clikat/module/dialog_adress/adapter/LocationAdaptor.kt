package com.codebrew.clikat.module.dialog_adress.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.data.model.SupplierLocation
import com.codebrew.clikat.databinding.ItemRestuarentAddressBinding
import com.codebrew.clikat.databinding.ListSearchFooterBinding
import com.codebrew.clikat.databinding.ListSearchHeaderBinding
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_FOOTER = 2


class LocationAdaptor(internal var activity:Activity,private val clickListener: LocationListener) :
        ListAdapter<LocationDataItem, RecyclerView.ViewHolder>(LocationDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SupplierLocation>?, loginStatus: Boolean) {
        adapterScope.launch {


            val items: List<LocationDataItem> = if (loginStatus) {
                when (list) {
                    null -> listOf(LocationDataItem.Header) + listOf(LocationDataItem.Footer)
                    else -> listOf(LocationDataItem.Header) + listOf(LocationDataItem.Footer) + list.map { LocationDataItem.SleepNightItem(it) }
                }
            } else {
                when (list) {
                    null -> listOf(LocationDataItem.Header)
                    else -> listOf(LocationDataItem.Header) + list.map { LocationDataItem.SleepNightItem(it) }
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
                val nightItem = getItem(position) as LocationDataItem.SleepNightItem
                holder.bind(nightItem.address, clickListener,activity)
            }

            is TextViewFooter -> {
                holder.bind(clickListener)
            }

            is TextViewHeader -> {
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
            is LocationDataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is LocationDataItem.Footer -> ITEM_VIEW_TYPE_FOOTER
            is LocationDataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class TextViewHeader private constructor(val binding: ListSearchHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: LocationListener) {
            binding.locationListener = clickListener
            binding.executePendingBindings()
            binding.color = Configurations.colors
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHeader {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListSearchHeaderBinding.inflate(layoutInflater, parent, false)
                return TextViewHeader(binding)
            }
        }
    }


    class TextViewFooter private constructor(val binding: ListSearchFooterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: LocationListener) {
            binding.locationListener = clickListener
            binding.color = Configurations.colors
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


    class ViewHolder private constructor(val binding: ItemRestuarentAddressBinding) : RecyclerView.ViewHolder(binding.root) {
        private var timer: Timer? = null
        fun bind(item: SupplierLocation, clickListener: LocationListener, activity: Activity) {
            binding.supplierLocation = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

            binding.clMain.setOnClickListener {

                binding.tvAddress.setTextColor(activity.resources.getColor(R.color.pink_color))
              Toast.makeText(activity,binding.tvAddress.text.toString(),Toast.LENGTH_SHORT).show()

                        clickListener.onClick(item)


            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRestuarentAddressBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class LocationDiffCallback : DiffUtil.ItemCallback<LocationDataItem>() {
    override fun areItemsTheSame(oldItem: LocationDataItem, newItem: LocationDataItem): Boolean {
        return oldItem.address == newItem.address
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: LocationDataItem, newItem: LocationDataItem): Boolean {
        return oldItem == newItem
    }
}

class LocationListener(val clickListener: (addressData: SupplierLocation) -> Unit) {

    fun onClick(model: SupplierLocation) = clickListener(model)

}

sealed class LocationDataItem {
    data class SleepNightItem(val addressBean: SupplierLocation) : LocationDataItem() {
        override val address = addressBean
    }

    object Header : LocationDataItem() {
        override val address = SupplierLocation()
    }

    object Footer : LocationDataItem() {
        override val address = SupplierLocation()
    }

    abstract val address: SupplierLocation
}

