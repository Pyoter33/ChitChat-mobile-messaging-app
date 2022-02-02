package com.example.chitchat.adapters

import android.graphics.Typeface
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chitchat.R
import com.example.chitchat.models.appModels.Group
import com.example.chitchat.models.databaseModels.MessageType
import com.example.chitchat.models.databaseModels.TextMessageDB
import com.example.chitchat.databinding.ItemConversationsListBinding
import com.example.chitchat.fragments.GroupsClickListener
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class GroupsListAdapter(
    private val groupsClickListener: GroupsClickListener,
    private val userId: String
) :
    ListAdapter<Group, GroupsListAdapter.GroupViewHolder>(
        GroupsComparator()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, groupsClickListener, userId)
    }


    class GroupViewHolder(private val binding: ItemConversationsListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: Group, groupsClickListener: GroupsClickListener, userId: String) {
            val senderId = group.lastMessage.senderId

            binding.textConversationName.text = group.name
            binding.imageConversation.setImageBitmap(group.groupImage)

            if (senderId == userId)
                binding.textLastMessageSender.setText(R.string.text_you)
            else {
                for (elem in group.members) {
                    when (elem.id) {
                        senderId -> {
                            binding.textLastMessageSender.text = "${elem.name}:"
                            break
                        }
                        DELETED_USER_ID -> binding.textLastMessageSender.setText(R.string.text_user_deleted)

                        else -> {
                            if (senderId == NEW_GROUP_ID) {
                                binding.textLastMessageSender.text = NEW_GROUP_ID
                                break
                            } else
                                binding.textLastMessageSender.setText(R.string.text_user_left)
                        }
                    }
                }
            }

            val readIndicator = try {
                group.lastMessage.read[userId]!!

            } catch (e: NullPointerException) {
                true
            }

            if (!readIndicator) {
                binding.textLastMessage.typeface = Typeface.DEFAULT_BOLD
                binding.textLastMessageSender.typeface = Typeface.DEFAULT_BOLD
                binding.textConversationName.typeface = Typeface.DEFAULT_BOLD
                binding.imageNewMessage.visibility = View.VISIBLE
            }

            if (group.lastMessage.type == MessageType.TEXT)
                binding.textLastMessage.text = (group.lastMessage as TextMessageDB).content
            else
                binding.textLastMessage.text = "New image!"

            val currentDate = Date()
            val timeDifference = (currentDate.time - group.lastMessage.time.time) /
                    (1000 * 60 * 60 * 24)


            when {
                DateUtils.isToday(group.lastMessage.time.time) -> binding.textLastMessageTime.text =
                    SimpleDateFormat("HH:mm").format(group.lastMessage.time)
                timeDifference <= 7 -> binding.textLastMessageTime.text =
                    SimpleDateFormat("E").format(group.lastMessage.time)
                else -> binding.textLastMessageTime.text =
                    SimpleDateFormat("dd.MM.yyyy").format(group.lastMessage.time)
            }

            binding.layoutMessage.setOnClickListener {
                groupsClickListener.onClickGroup(group.groupId)
            }
        }

        companion object {
            const val DELETED_USER_ID = "Deleted user"
            const val NEW_GROUP_ID = "New group!"

            fun create(parent: ViewGroup): GroupViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemConversationsListBinding.inflate(layoutInflater, parent, false)
                return GroupViewHolder(
                    binding
                )
            }
        }
    }


}

class GroupsComparator : DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.groupId == newItem.groupId
    }
}