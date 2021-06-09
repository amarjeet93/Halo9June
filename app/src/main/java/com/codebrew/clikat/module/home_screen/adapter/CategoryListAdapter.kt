package com.codebrew.clikat.module.home_screen.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.data.AppDataType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemCategoryCustomBinding
import com.codebrew.clikat.databinding.ItemCategoryHorizontalBinding
import com.codebrew.clikat.databinding.ItemCategoryVerticalNewBinding
import com.codebrew.clikat.modal.other.English
import com.codebrew.clikat.module.home_screen.adapter.CategoryListAdapter.CategoryViewHolder
import com.codebrew.clikat.utils.StaticFunction.loadImage
import com.codebrew.clikat.utils.configurations.Configurations
import com.makeramen.roundedimageview.RoundedImageView

class CategoryListAdapter internal constructor(private val beanList: List<English>, private val screenType: Int) : RecyclerView.Adapter<CategoryViewHolder>() {
    private var mCallback: CategoryDetail? = null

    private var mContext:Context?=null
    fun settingCallback(mCallback: CategoryDetail?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        mContext=parent.context

        return if (viewType == MULTIPLE_CATEGORY) {
            val binding: ItemCategoryVerticalNewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_vertical_new, parent, false)
            binding.color = Configurations.colors
            CategoryViewHolder(binding.root, viewType)
        } else if(viewType==CUSTOM_CATEGORY) {
            val binding: ItemCategoryCustomBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_custom, parent, false)
            binding.color = Configurations.colors
            CategoryViewHolder(binding.root, viewType)
        } else {
            val binding: ItemCategoryHorizontalBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_category_horizontal, parent, false)
            binding.color = Configurations.colors
            CategoryViewHolder(binding.root, viewType)
        }
    }

    override fun onBindViewHolder(viewHolder: CategoryViewHolder, i: Int) {
        viewHolder.onBind()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            screenType>AppDataType.Custom.type -> {
                CUSTOM_CATEGORY
            }
            screenType == AppDataType.Ecom.type -> {
                MULTIPLE_CATEGORY
            }
            screenType == AppDataType.HomeServ.type -> {
                SERVICE_CATEGORY
            }
            else -> {
                TWO_CATEGORY
            }
        }
    }

    override fun getItemCount(): Int {
        return beanList.size
    }

    interface CategoryDetail {
        fun onCategoryDetail(bean: English?)
    }

    inner class CategoryViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var ivCategory: RoundedImageView
        var tvCategory: TextView
        //var tvDesc: TextView
        var viewType: Int
        fun onBind() {
            //tvDesc.visibility = if (screenType == AppDataType.Beauty.type) View.VISIBLE else View.GONE
            tvCategory.text = beanList[adapterPosition].name
            loadImage(if (screenType == AppDataType.HomeServ.type || screenType == AppDataType.Beauty.type) beanList[adapterPosition].icon else beanList[adapterPosition].icon, ivCategory, true)
          //  tvDesc.text = beanList[adapterPosition].description
            itemView.isEnabled = true
            itemView.setOnClickListener { v: View? ->
                // holder.itemView.setEnabled(false);
                mCallback?.onCategoryDetail(beanList[adapterPosition])
            }
        }

        init {
            ivCategory = itemView.findViewById(R.id.iv_userImage)
            tvCategory = itemView.findViewById(R.id.category_text)
         //   tvDesc = itemView.findViewById(R.id.tv_desc)
            this.viewType = viewType
        }
    }

    companion object {
        private const val MULTIPLE_CATEGORY = 1
        private const val TWO_CATEGORY = 2
        private const val SERVICE_CATEGORY = 3
        private const val CUSTOM_CATEGORY = 4
    }

}