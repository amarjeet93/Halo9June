package com.codebrew.clikat.module.agent_time_slot.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemTimeSlotBinding
import com.codebrew.clikat.utils.StaticFunction
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_time_slot.view.*

class TimeSlotAdapter(private val timeSlots: List<String>): RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {

    private val checkedPosition = -1

    private val primaryColor=Configurations.colors.primaryColor

    private val subHeadColor=Configurations.colors.textSubhead

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ItemTimeSlotBinding>(LayoutInflater.from(parent.context),
                R.layout.item_time_slot, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)

    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        if (checkedPosition == position) {
            holder.tv_name.setTextColor(Color.parseColor(primaryColor))
            holder.tv_name.setBackgroundColor(Color.parseColor(primaryColor))
          //  holder.tv_name.background = StaticFunction.changeBorderTextColor(primaryColor?:"", GradientDrawable.RECTANGLE)
            //checked
        } else {

            holder.tv_name.setTextColor(Color.parseColor(subHeadColor))
            holder.tv_name.setBackgroundColor(Color.parseColor(subHeadColor))
          //  holder.tv_name.background = StaticFunction.changeBorderColor(subHeadColor, "", GradientDrawable.RECTANGLE)
            //unchecked
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tv_name=itemView.tv_time_slot
    }
}