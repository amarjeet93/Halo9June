package com.codebrew.clikat.module.cart.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.VendorAppType
import com.codebrew.clikat.data.constants.AppConstants
import com.codebrew.clikat.databinding.ItemTipsBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.module.cart.model.TiPModel
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_tips.view.*

class TipAdapter(private val mContext: Context, val tipList: ArrayList<TiPModel>,
                 private val screenFlow: SettingModel.DataBean.ScreenFlowBean?,
                 private val appUtils: AppUtils,
                 private val addTipInPercentage: String?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mCallback: TipCallback? = null

    fun tipCallback(mCallback: TipCallback?) {
        this.mCallback = mCallback
    }

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemTipsBinding = ItemTipsBinding.inflate(LayoutInflater.from(mContext), parent, false)
        binding.color = Configurations.colors
        binding.drawables = Configurations.drawables
        binding.strings = appUtils.loadAppConfig(0).strings
        binding.singleVndorType = screenFlow?.is_single_vendor == VendorAppType.Single.appType
        return HeaderViewHolder(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            val holder2 = holder
            if (addTipInPercentage == "1") {

                holder2.tvTip.text = "${tipList[position].tip}${mContext.getString(R.string.tip_percentage_tag)}"

                if (selectedPosition == position)
                    holder.tvTip.setTextColor(Color.parseColor(Configurations.colors.yellowcolor))
                else
                    holder.tvTip.setTextColor(Color.parseColor(Configurations.colors.textListHead))

            } else
                holder2.tvTip.text = mContext.getString(R.string.currency_tag, AppConstants.CURRENCY_SYMBOL, tipList.get(position).tip.toFloat())


        }
    }

    override fun getItemCount(): Int {
        return tipList.size
    }

    fun clearTipSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTip = itemView.tvTip

        init {
            tvTip.setOnClickListener { v: View? ->

                mCallback?.onTipSelected(adapterPosition)

                if (addTipInPercentage == "1") {
                    selectedPosition = adapterPosition
                    notifyDataSetChanged()
                }


            }
        }
    }

    interface TipCallback {
        fun onTipSelected(position: Int)
    }
}