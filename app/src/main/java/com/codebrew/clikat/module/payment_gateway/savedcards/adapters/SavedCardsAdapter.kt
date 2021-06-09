package com.codebrew.clikat.module.payment_gateway.savedcards.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.AppUtils
import com.codebrew.clikat.data.model.api.SavedCardList
import com.codebrew.clikat.databinding.ItemSavedCardBinding
import com.codebrew.clikat.utils.configurations.Configurations
import kotlinx.android.synthetic.main.front_card.view.*
import kotlinx.android.synthetic.main.item_saved_card.view.*
import timber.log.Timber

class SavedCardsAdapter(private val saveCardList: MutableList<SavedCardList>,
                        private val appUtils: AppUtils) : RecyclerView.Adapter<SavedCardsAdapter.Viewholder>() {

    lateinit var cardClick: OnCardClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {

        val binding: ItemSavedCardBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_saved_card, parent, false)
        binding.colors = Configurations.colors
        binding.strings = appUtils.loadAppConfig(0).strings
        return Viewholder(binding.root)
    }

    override fun getItemCount(): Int {
        return saveCardList.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(saveCardList[position])

    }

    fun setCardListener(card: OnCardClickListener) {
        this.cardClick = card
    }


    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(list: SavedCardList?) {
            itemView.front_card_expiry.text = "${list?.exp_month.toString()}/${list?.exp_year.toString()}"
            itemView.front_card_number.text = itemView.context.getString(R.string.card_tag, list?.last4)
            itemView.front_card_holder_name.text = list?.name

            list?.isDefault?.let {
                itemView.rbDefault.isChecked = it
            }
        }


        init {
            itemView.ivDeleteCard.setOnClickListener {
                cardClick.onDeleteCard(saveCardList[adapterPosition], adapterPosition)
            }
            itemView.setOnClickListener {
                cardClick.onCardClick(saveCardList[adapterPosition], adapterPosition)
            }


            itemView.rbDefault.setOnCheckedChangeListener { _, isChecked ->

                if (isChecked) {
                    if (!saveCardList[adapterPosition].isDefault) {
                        cardClick.onSetDefaultCardClick(saveCardList[adapterPosition], adapterPosition)
                    }

                }

            }
        }
    }


    interface OnCardClickListener {
        fun onCardClick(savedCard: SavedCardList, position: Int)
        fun onDeleteCard(savedCard: SavedCardList, position: Int)
        fun onSetDefaultCardClick(savedCard: SavedCardList, position: Int)
    }

}


