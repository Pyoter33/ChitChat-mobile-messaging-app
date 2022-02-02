package com.example.chitchat.adapters

import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.models.appModels.Conversation
import com.example.chitchat.models.databaseModels.MessageType
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.databinding.ItemConversationsListBinding
import com.example.chitchat.fragments.ConversationClickListener
import java.text.SimpleDateFormat
import java.util.*

class ConversationsListAdapter(
    private val conversationClickListener: ConversationClickListener,
    private val userId: String
) :
    ListAdapter<Conversation, ConversationsListAdapter.ConversationViewHolder>(
        ConversationsComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        return ConversationViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, conversationClickListener, userId)
    }


    class ConversationViewHolder(private val binding: ItemConversationsListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            conversation: Conversation,
            conversationClickListener: ConversationClickListener,
            userId: String
        ) {
            if (conversation.members.isNotEmpty()) {
                if (conversation.members.first().id == userId) {
                    Glide.with(itemView).load(conversation.members.last().photoUrl)
                        .into(binding.imageConversation)
                    binding.textConversationName.text = conversation.members.last().name
                    if (conversation.members.last().active)
                        binding.imageUserActiveIndicator.visibility = View.VISIBLE
                    else
                        binding.imageUserActiveIndicator.visibility = View.INVISIBLE

                } else {
                    Glide.with(itemView).load(conversation.members.first().photoUrl)
                        .into(binding.imageConversation)
                    binding.textConversationName.text = conversation.members.first().name
                    if (conversation.members.first().active)
                        binding.imageUserActiveIndicator.visibility = View.VISIBLE
                    else
                        binding.imageUserActiveIndicator.visibility = View.INVISIBLE
                }

                val senderId = conversation.lastMessage.senderId

                if (senderId == userId)
                    binding.textLastMessageSender.setText(R.string.text_you)
                else {
                    when {
                        (conversation.members.first().id == null || conversation.members.last().id == null) -> {
                            binding.textLastMessageSender.setText(R.string.text_user_deleted)
                        }
                        (senderId == conversation.members.first().id) -> {
                            binding.textLastMessageSender.visibility = View.INVISIBLE
                        }
                        (senderId == conversation.members.last().id) ->
                            binding.textLastMessageSender.visibility = View.INVISIBLE

                        else -> binding.textLastMessageSender.text = senderId
                    }

                    if (!conversation.lastMessage.read[userId]!!) {
                        binding.textLastMessage.typeface = Typeface.DEFAULT_BOLD
                        binding.textLastMessageSender.typeface = Typeface.DEFAULT_BOLD
                        binding.textConversationName.typeface = Typeface.DEFAULT_BOLD
                        binding.imageNewMessage.visibility = View.VISIBLE
                    }
                }
                if (conversation.lastMessage.type == MessageType.TEXT)
                    binding.textLastMessage.text =
                        (conversation.lastMessage as TextMessageDB).content
                else
                    binding.textLastMessage.text = "New image!"

                val currentDate = Date()
                val timeDifference = (currentDate.time - conversation.lastMessage.time.time) /
                        (1000 * 60 * 60 * 24)


                when {
                    DateUtils.isToday(conversation.lastMessage.time.time) -> binding.textLastMessageTime.text =
                        SimpleDateFormat("HH:mm").format(conversation.lastMessage.time)
                    timeDifference <= 7 -> binding.textLastMessageTime.text =
                        SimpleDateFormat("E").format(conversation.lastMessage.time)
                    else -> binding.textLastMessageTime.text =
                        SimpleDateFormat("dd.MM.yyyy").format(conversation.lastMessage.time)
                }


            }

            binding.layoutMessage.setOnClickListener {
                conversationClickListener.onClickConversation(conversation.id)
            }
        }

        companion object {

            fun create(parent: ViewGroup): ConversationViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemConversationsListBinding.inflate(layoutInflater, parent, false)
                return ConversationViewHolder(
                    binding
                )
            }
        }
    }


}

class ConversationsComparator : DiffUtil.ItemCallback<Conversation>() {
    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return false
    }
}