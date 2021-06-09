package com.codebrew.clikat.module.agent_listing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.loadUserImage
import com.codebrew.clikat.databinding.ItemAgentListBinding
import com.codebrew.clikat.modal.agent.DataBean
import com.codebrew.clikat.utils.configurations.Configurations
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class AgentListAdapter(private val agentList: MutableList<DataBean>) : RecyclerView.Adapter<AgentListAdapter.ViewHolder>() , Filterable {
    private var context: Context? = null
    private var mCallback: AgentCallback? = null

    private var updatedList:MutableList<DataBean>

    fun settingCallback(mCallback: AgentCallback?) {
        this.mCallback = mCallback
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        context = viewGroup.context
        val binding: ItemAgentListBinding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.context),
                R.layout.item_agent_list, viewGroup, false)
        binding.color = Configurations.colors
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val pos = viewHolder.adapterPosition
        val userBean = updatedList[pos].cbl_user

        viewHolder.ivUserImage.loadUserImage(userBean?.image?:"")

        if (userBean!!.image != null) {
            Glide.with(context!!).load(userBean.image).apply(RequestOptions().placeholder(R.drawable.placeholder_product)).into(viewHolder.ivUserImage)
        }
        if (userBean.name != null) {
            viewHolder.tvName.text = userBean.name
        }
        if (userBean.occupation != null) {
            viewHolder.tvOccupation.text = userBean.occupation
        }
        if (userBean.experience != 0) {
            viewHolder.tvExperience.text = context!!.getString(R.string.experience_tag, userBean.experience)
        }

        viewHolder.itemView.setBackgroundResource(R.drawable.shape_agent_border)

        viewHolder.btnViewSlot.setOnClickListener { view: View? -> mCallback?.getAgentDetail(updatedList[viewHolder.adapterPosition]) }
        viewHolder.btnBook.setOnClickListener { view: View? -> mCallback?.onBookAgent(updatedList[viewHolder.adapterPosition]) }
    }

    override fun getItemCount(): Int {
        return updatedList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivUserImage: CircleImageView
        var tvName: TextView
        var tvOccupation: TextView
        var tvExperience: TextView
        var rbAgent: TextView
        var tvTotalReviews: TextView
        var gpAction: Group
        var btnViewSlot: MaterialButton
        var btnBook: MaterialButton

        init {
            ivUserImage = itemView.findViewById(R.id.iv_userImage)
            tvName = itemView.findViewById(R.id.tv_name)
            tvOccupation = itemView.findViewById(R.id.tv_occupation)
            tvExperience = itemView.findViewById(R.id.tv_experience)
            rbAgent = itemView.findViewById(R.id.rb_agent)
            tvTotalReviews = itemView.findViewById(R.id.tv_total_reviews)
            gpAction = itemView.findViewById(R.id.gp_action)
            btnBook = itemView.findViewById(R.id.btn_book)
            btnViewSlot = itemView.findViewById(R.id.btn_view_slot)
            gpAction.visibility = View.VISIBLE
            itemView.setOnClickListener { v: View? -> mCallback!!.getAgentDetail(updatedList[adapterPosition]) }
        }
    }

    interface AgentCallback {
        fun getAgentDetail(agentDetail: DataBean?)
        fun onBookAgent(agentDetail: DataBean?)
    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    updatedList=agentList
                } else {
                    val filteredList: MutableList<DataBean> = ArrayList()
                    for (row in agentList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.cbl_user?.name?.toLowerCase(Locale.getDefault())?.contains(charString.toLowerCase(Locale.getDefault()))==true) {
                            filteredList.add(row)
                        }
                    }
                    updatedList=filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = updatedList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {

                updatedList=filterResults.values as MutableList<DataBean>

                // refresh the list with filtered data
                notifyDataSetChanged()
            }
        }
    }

    init {
        updatedList = agentList
    }

}