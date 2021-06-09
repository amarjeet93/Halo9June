package com.codebrew.clikat.module.more_setting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.databinding.ItemMoreTagBinding
import com.codebrew.clikat.modal.other.SettingModel
import com.codebrew.clikat.modal.other.more_list.MoreListModel
import com.codebrew.clikat.utils.configurations.Configurations
import com.codebrew.clikat.utils.configurations.TextConfig
import kotlinx.android.synthetic.main.item_more_tag.view.*

class MoreTagAdapter(private val context: Context, private val textConfig: TextConfig?) : RecyclerView.Adapter<MoreTagAdapter.ViewHolder>() {

    private var itemsCount = mutableListOf<MoreListModel>()
    private var screenFlowBean: SettingModel.DataBean.ScreenFlowBean? = null
    private var settingFlowBean: SettingModel.DataBean.SettingData? = null
    private lateinit var clickListener: TagListener

    fun settingCallback(clickListener: TagListener) {

        this.clickListener = clickListener
    }


    fun loadData(loginStatus: Boolean, screenFlowBean: SettingModel.DataBean.ScreenFlowBean?,
                 settingFlowBean: SettingModel.DataBean.SettingData?) {
        this.settingFlowBean = settingFlowBean
        this.screenFlowBean = screenFlowBean
        itemsCount = populateData(loginStatus)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemMoreTagBinding>(LayoutInflater.from(parent.context),
                R.layout.item_more_tag, parent, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return itemsCount.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.updateData(itemsCount[holder.adapterPosition])

        holder.itemView.setOnClickListener {

            clickListener.onClick(itemsCount[holder.adapterPosition].name)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemImage = itemView.iv_more
        val itemName = itemView.tv_more_name

        fun updateData(model: MoreListModel) {
            itemName.text = model.name
            itemImage.setImageResource(model.image)
        }

    }


    fun populateData(loginStatus: Boolean): MutableList<MoreListModel> {

        val itemList = mutableListOf<MoreListModel>()

        val items = context.resources.getStringArray(R.array.more_array)

        val itemImages = arrayOf(
                R.drawable.menu_orders, R.drawable.ic_more_share_app, R.drawable.ic_more_terms, R.drawable.ic_more_help,  R.drawable.ic_more_policy)


        for (i in items.indices) {
            itemList.add(MoreListModel(items[i], itemImages[i]))
        }

        settingFlowBean?.secondary_language?.let {
            if (it != "0") {
                // itemList.add(MoreListModel(context.getString(R.string.change_language), R.drawable.ic_more_policy))
            }
        }

        if (loginStatus)
            itemList.add(MoreListModel(context.getString(R.string.payment), R.drawable.ic_payment_card))

        if (loginStatus) {
            if (settingFlowBean?.is_product_wishlist == "1" || settingFlowBean?.is_supplier_wishlist == "1") {
                itemList.add(MoreListModel(textConfig?.wishlist
                        ?: "", R.drawable.ic_more_favourite))
            }

            if (!settingFlowBean?.referral_feature.isNullOrEmpty() && settingFlowBean?.referral_feature == "1") {
                itemList.add(MoreListModel(context.getString(R.string.referral), R.drawable.ic_more_help))
            }

//            if (!settingFlowBean?.show_prescription_requests.isNullOrEmpty()
//                    && settingFlowBean?.show_prescription_requests == "1") {
//                itemList.add(MoreListModel(context.getString(R.string.requests, textConfig?.order), R.drawable.ic_more_terms))
//            }

            itemList.add(MoreListModel(context.getString(R.string.logout), R.drawable.ic_more_logout))
        }

        if (settingFlowBean?.extra_instructions == "1") {

            itemList.add(MoreListModel(context.getString(R.string.faq), R.drawable.ic_more_faq))
        }

        if (settingFlowBean?.extra_functionality == "1") {
            itemList.add(MoreListModel(context.getString(R.string.become_care_giver), R.drawable.ic_more_caregiver1))
        }




        return itemList

    }

    class TagListener(val clickListener: (type: String) -> Unit) {
        fun onClick(type: String) = clickListener(type)
    }

}