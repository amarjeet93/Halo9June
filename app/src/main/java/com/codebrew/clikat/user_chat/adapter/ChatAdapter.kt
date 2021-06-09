package com.codebrew.clikat.user_chat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.data.model.api.ChatMessageListing
import com.codebrew.clikat.databinding.ItemChatHeaderBinding
import com.codebrew.clikat.databinding.ItemChatTextLeftBinding
import com.codebrew.clikat.databinding.ItemChatTextRightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TEXT_LEFT_VIEW_TYPE = 1
private const val TEXT_RIGHT_VIEW_TYPE = 2
private const val Date_VIEW_TYPE = 3

private const val MESSAGE_TYPE="text"
private const val TIME_TYPE="time"

class ChatAdapter(val clickListener: ChatListener) :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addItmSubmitList(list: List<ChatMessageListing>?) {
        adapterScope.launch {
            val items = list?.map {

                when (it.chat_type) {
                    MESSAGE_TYPE -> {
                        if (it.ownMessage==true) {
                            DataItem.TextRightItem(it)
                        } else {
                            DataItem.TextLeftItem(it)
                        }
                    }
                    TIME_TYPE ->{
                        DataItem.DateItem(it)
                    }

                    else -> DataItem.ChatItem(it)
                }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextLeftViewHolder -> {
                val nightItem = getItem(position) as DataItem.TextLeftItem
                holder.bind(nightItem.chatData, clickListener)
            }

            is TextRightViewHolder -> {
                val nightItem = getItem(position) as DataItem.TextRightItem
                holder.bind(nightItem.chatData, clickListener)
            }

            is TextHeaderViewHolder ->{
                val headItem=getItem(position) as DataItem.DateItem
                holder.bind(headItem.chatData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TEXT_LEFT_VIEW_TYPE -> TextLeftViewHolder.from(parent)
            TEXT_RIGHT_VIEW_TYPE -> TextRightViewHolder.from(parent)
            Date_VIEW_TYPE -> TextHeaderViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TextLeftItem -> TEXT_LEFT_VIEW_TYPE
            is DataItem.TextRightItem -> TEXT_RIGHT_VIEW_TYPE
            is DataItem.DateItem -> Date_VIEW_TYPE
            else ->  TEXT_LEFT_VIEW_TYPE
        }
    }


    class TextLeftViewHolder private constructor(val binding: ItemChatTextLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageListing, listener: ChatListener) {
            binding.clickListener = listener
            binding.chatData = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextLeftViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChatTextLeftBinding.inflate(layoutInflater, parent, false)
                return TextLeftViewHolder(binding)
            }
        }
    }


    class TextRightViewHolder private constructor(val binding: ItemChatTextRightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageListing, listener: ChatListener) {
            binding.clickListener = listener
            binding.chatData = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextRightViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChatTextRightBinding.inflate(layoutInflater, parent, false)
                return TextRightViewHolder(binding)
            }
        }
    }

    class TextHeaderViewHolder private constructor(val binding: ItemChatHeaderBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessageListing) {
            binding.chatData = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TextHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChatHeaderBinding.inflate(layoutInflater, parent, false)
                return TextHeaderViewHolder(binding)
            }
        }
    }

}

class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.chatData.c_id == newItem.chatData.c_id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}


class ChatListener(val clickListener: (model: ChatMessageListing) -> Unit) {
    fun onClick(chatBean: ChatMessageListing) = clickListener(chatBean)
}


sealed class DataItem {
    data class TextLeftItem(val messageData: ChatMessageListing) : DataItem() {
        override val chatData = messageData
    }

    data class TextRightItem(val messageData: ChatMessageListing) : DataItem() {
        override val chatData = messageData
    }

    data class DateItem(val messageData: ChatMessageListing) : DataItem() {
        override val chatData = messageData
    }


    data class ChatItem(val messageData: ChatMessageListing) : DataItem() {
        override val chatData = messageData
    }

    abstract val chatData: ChatMessageListing
}

