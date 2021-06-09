package com.github.vipulasri.timelineview.sample.example

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.data.model.others.OrderStatusModel
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.utils.StaticFunction
import com.github.vipulasri.timelineview.TimelineView
import com.github.vipulasri.timelineview.sample.utils.VectorDrawableUtils
import kotlinx.android.synthetic.main.item_order_status.view.*


class OrderStatusAdapter(private val mStatusList: List<OrderStatusModel>, private val appType: Int?,
                         private val selfPickup: Int?,private val orderTerminology: String) : RecyclerView.Adapter<OrderStatusAdapter.TimeLineViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater

    private var mContext: Context? = null

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        mContext = parent.context

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TimeLineViewHolder(mLayoutInflater.inflate(R.layout.item_order_status, parent, false), viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val timeLineModel = mStatusList[position]

        when {
            timeLineModel.status.orderStatus <= timeLineModel.orderStatus -> {
                setMarker(holder, R.drawable.marker, R.color.colorPrimary, true)
            }
            else -> {
                setMarker(holder, R.drawable.marker, R.color.black_10, false)
            }
        }



        if (timeLineModel.statusTime.isEmpty()) {
            holder.date.visibility = View.GONE
        } else {
            holder.date.visibility = View.VISIBLE
            holder.date.text = timeLineModel.statusTime
        }

        holder.message.text = StaticFunction.statusProduct(timeLineModel.status.orderStatus, appType
                ?: 0, selfPickup ?: 0, mContext, orderTerminology?:"")
    }

    private fun setMarker(holder: TimeLineViewHolder, drawableResId: Int, colorFilter: Int, status: Boolean) {
        if (Build.VERSION.SDK_INT >= 24) {
            holder.timeline.marker = VectorDrawableUtils.getDrawable(holder.itemView.context, drawableResId, ContextCompat.getColor(holder.itemView.context, colorFilter))
        } else {
            if (status) {
                holder.timeline.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.radio_on)
            } else {
                holder.timeline.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.radio_off)
            }
        }
    }

    override fun getItemCount() = mStatusList.size

    inner class TimeLineViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {

        val date = itemView.tv_order_time
        val message = itemView.tv_status
        val timeline = itemView.timeline

        init {
            timeline.initLine(viewType)
        }
    }

}
