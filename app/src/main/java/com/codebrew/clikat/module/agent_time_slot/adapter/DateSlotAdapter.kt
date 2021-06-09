package com.codebrew.clikat.module.agent_time_slot.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemDateSlotBinding
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_date_slot.view.*

class DateSlotAdapter(private val dateSlots: List<String>) : RecyclerView.Adapter<DateSlotAdapter.ViewHolder>() {


    private val checkedPosition = -1

    private val primaryColor=Configurations.colors.primaryColor

    private val subHeadColor=Configurations.colors.textSubhead

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemDateSlotBinding>(LayoutInflater.from(parent.context),
                R.layout.item_date_slot, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return dateSlots.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (checkedPosition == position) {
            holder.tv_name.setTextColor(Color.parseColor(primaryColor))
            holder.tv_name.background = StaticFunction.changeBorderTextColor(primaryColor?:"", GradientDrawable.RECTANGLE)
            //checked
        } else {

            holder.tv_name.setTextColor(Color.parseColor(subHeadColor))
            holder.tv_name.background = StaticFunction.changeBorderColor(subHeadColor, "", GradientDrawable.RECTANGLE)
            //unchecked
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tv_name = itemView.tv_date_slot
    }
}