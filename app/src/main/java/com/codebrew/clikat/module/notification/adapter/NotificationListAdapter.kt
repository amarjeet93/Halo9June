package com.codebrew.clikat.module.notification.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.loadImage
import com.codebrew.clikat.databinding.ItemNotificationBinding
import com.codebrew.clikat.modal.GetTimeAgo
import com.codebrew.clikat.modal.Notification
import com.codebrew.clikat.utils.StaticFunction.convertFromUtcFormat
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.item_notification.view.*

class NotificationListAdapter(
        private val callback: OnNotificationClicked?)
    : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

    private var list = ArrayList<Notification>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ItemNotificationBinding>(LayoutInflater.from(parent.context),
                R.layout.item_notification, parent, false)
        binding.color = Configurations.colors
        binding.strings = Configurations.strings
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.onBind(item)
        with(holder.mView) {
            tag = item
        }
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        init {
            mView.setOnClickListener {
                callback?.onItemClicked(list[adapterPosition])
            }
        }

        fun onBind(item: Notification) {
            mView.ivIcon.loadImage(item.logo?:"")
            mView.tvDescription?.text = item.notification_message
            /*mView.tvTime?.text = GetTimeAgo.getTimeAgo(convertFromUtcFormat(item.created_on), mView.context)*/
            mView.tvTime?.text = convertFromUtcFormat("dd MMM, yyyy hh:mm a",item.created_on)
        }
    }

    fun addList(isFirstPage: Boolean?, list: ArrayList<Notification>) {
        if (isFirstPage==true)
            this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    interface OnNotificationClicked {
        fun onItemClicked(item: Notification?)
    }
}
