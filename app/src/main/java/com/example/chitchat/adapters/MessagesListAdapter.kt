package com.example.chitchat.adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.models.databaseModels.MessageType
import com.example.chitchat.models.appModels.ImageMessage
import com.example.chitchat.models.appModels.Message
import com.example.chitchat.models.appModels.TextMessage
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.*
import com.example.chitchat.fragments.MessageClickListener
import com.example.chitchat.viewModels.ConversationViewModel
import java.text.SimpleDateFormat
import java.util.*

class MessagesListAdapter(
    private val messageClickListener: MessageClickListener,
    private val viewModel: ConversationViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val userId: String
) :
    ListAdapter<Message, RecyclerView.ViewHolder>(MessagesComparator()) {

    companion object {
        const val VIEW_TYPE_USER_MESSAGE = 0
        const val VIEW_TYPE_OTHER_MESSAGE = 1
        const val VIEW_TYPE_USER_MESSAGE_IMAGE = 2
        const val VIEW_TYPE_OTHER_MESSAGE_IMAGE = 3
        const val VIEW_TYPE_DELETED_USER_MESSAGE = 4
        const val VIEW_TYPE_DELETED_OTHER_MESSAGE = 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER_MESSAGE -> UserMessageViewHolder.create(
                parent,
                viewModel,
                viewLifecycleOwner
            )
            VIEW_TYPE_OTHER_MESSAGE -> OtherMessageViewHolder.create(
                parent
            )
            VIEW_TYPE_USER_MESSAGE_IMAGE -> UserMessageImageViewHolder.create(
                parent,
                viewModel,
                viewLifecycleOwner
            )
            VIEW_TYPE_OTHER_MESSAGE_IMAGE -> OtherMessageImageViewHolder.create(
                parent
            )
            VIEW_TYPE_DELETED_USER_MESSAGE -> UserDeletedMessageViewHolder.create(
                parent
            )
            else -> OtherDeletedMessageViewHolder.create(
                parent
            )

        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val current = getItem(position)
        when (holder.itemViewType) {
            VIEW_TYPE_USER_MESSAGE -> (holder as UserMessageViewHolder).bind(
                current as TextMessage,
                messageClickListener
            )
            VIEW_TYPE_OTHER_MESSAGE -> (holder as OtherMessageViewHolder).bind(
                current as TextMessage
            )
            VIEW_TYPE_USER_MESSAGE_IMAGE -> (holder as UserMessageImageViewHolder).bind(
                current as ImageMessage,
                messageClickListener
            )
            VIEW_TYPE_OTHER_MESSAGE_IMAGE -> (holder as OtherMessageImageViewHolder).bind(
                current as ImageMessage,
                messageClickListener
            )
            VIEW_TYPE_DELETED_USER_MESSAGE -> (holder as UserDeletedMessageViewHolder).bind(
                current as TextMessage
            )
            VIEW_TYPE_DELETED_OTHER_MESSAGE -> (holder as OtherDeletedMessageViewHolder).bind(
                current as TextMessage
            )
        }

    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        Log.i("Message type", message.type)
        return when {
            (message.sender.id == userId) -> {
                when (message.type) {
                    MessageType.TEXT -> VIEW_TYPE_USER_MESSAGE
                    MessageType.IMAGE -> VIEW_TYPE_USER_MESSAGE_IMAGE
                    else -> VIEW_TYPE_DELETED_USER_MESSAGE
                }
            }
            else ->
                when (message.type) {
                    MessageType.TEXT -> VIEW_TYPE_OTHER_MESSAGE
                    MessageType.IMAGE -> VIEW_TYPE_OTHER_MESSAGE_IMAGE
                    else -> VIEW_TYPE_DELETED_OTHER_MESSAGE
                }
        }
    }

}

class UserMessageViewHolder(
    private val binding: ItemUserMessageBinding,
    private val viewModel: ConversationViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: TextMessage, messageClickListener: MessageClickListener) {
        binding.textMessageContent.text = message.content

        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }

        val readUsersListAdapter = ReadUsersListAdapter()
        binding.listUsersRead.adapter = readUsersListAdapter
        binding.listUsersRead.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, true)

        viewModel.lastMessage.observe(viewLifecycleOwner, { result ->
            Log.i("read", "adapter " + result.second.toString())
            if (result.third == message.time) {
                val usersReadList = mutableListOf<User>()
                for (user in result.second.keys)
                    if (result.second[user]!! && user.id != result.first)
                        usersReadList.add(user)
                readUsersListAdapter.submitList(usersReadList)
                binding.listUsersRead.visibility = View.VISIBLE
            } else
                binding.listUsersRead.visibility = View.GONE

        })

        binding.layoutMessageContent.setOnLongClickListener {
            Log.i("click", "long clicked")
            messageClickListener.onLongClickMessage(it, message.time)
            true
        }

    }

    companion object {
        fun create(
            parent: ViewGroup,
            viewModel: ConversationViewModel,
            viewLifecycleOwner: LifecycleOwner,
        ): UserMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemUserMessageBinding.inflate(layoutInflater, parent, false)
            return UserMessageViewHolder(
                binding,
                viewModel,
                viewLifecycleOwner
            )
        }
    }
}

class UserDeletedMessageViewHolder(
    private val binding: ItemDeletedUserMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: TextMessage) {
        binding.textMessageContent.text = message.content

        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }

    }

    companion object {

        fun create(
            parent: ViewGroup
        ): UserDeletedMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemDeletedUserMessageBinding.inflate(layoutInflater, parent, false)
            return UserDeletedMessageViewHolder(
                binding
            )
        }
    }
}


class UserMessageImageViewHolder(
    private val binding: ItemUserMessageImageBinding,
    private val viewModel: ConversationViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(message: ImageMessage, messageClickListener: MessageClickListener) {
        Log.i("image", message.image)

        Glide.with(itemView).load(message.image.toUri()).into(binding.imageMessageImage)
        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }

        val readUsersListAdapter = ReadUsersListAdapter()
        binding.listUsersRead.adapter = readUsersListAdapter
        binding.listUsersRead.layoutManager =
            LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, true)

        viewModel.lastMessage.observe(viewLifecycleOwner, { result ->
            Log.i("read", "adapter " + result.second.toString())
            if (result.third == message.time) {
                val usersReadList = mutableListOf<User>()
                for (user in result.second.keys)
                    if (result.second[user]!! && user.id != result.first)
                        usersReadList.add(user)
                readUsersListAdapter.submitList(usersReadList)
                binding.listUsersRead.visibility = View.VISIBLE
            } else
                binding.listUsersRead.visibility = View.GONE

        })

        binding.layoutMessageContent.setOnLongClickListener {
            messageClickListener.onLongClickMessage(it, message.time)
            true
        }

        binding.imageMessageImage.setOnClickListener {
            messageClickListener.onClickImage(message.image)
        }

    }

    companion object {

        fun create(
            parent: ViewGroup,
            viewModel: ConversationViewModel,
            viewLifecycleOwner: LifecycleOwner,
        ): UserMessageImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemUserMessageImageBinding.inflate(layoutInflater, parent, false)
            return UserMessageImageViewHolder(
                binding,
                viewModel,
                viewLifecycleOwner
            )
        }
    }
}

class OtherMessageImageViewHolder(
    private val binding: ItemOtherMessageImageBinding,
) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(message: ImageMessage, messageClickListener: MessageClickListener) {

        Glide.with(itemView).load(message.sender.photoUrl).into(binding.imageUser)
        Glide.with(itemView).load(message.image.toUri()).into(binding.imageMessageImage)
        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }

        binding.imageMessageImage.setOnClickListener {
            messageClickListener.onClickImage(message.image)
        }

    }

    companion object {

        fun create(
            parent: ViewGroup,
        ): OtherMessageImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemOtherMessageImageBinding.inflate(layoutInflater, parent, false)
            return OtherMessageImageViewHolder(
                binding
            )
        }
    }
}


class OtherMessageViewHolder(
    private val binding: ItemOtherMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(message: TextMessage) {
        Glide.with(itemView).load(message.sender.photoUrl).into(binding.imageUser)
        binding.textMessageContent.text = message.content
        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }
    }

    companion object {
        fun create(
            parent: ViewGroup
        ): OtherMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemOtherMessageBinding.inflate(layoutInflater, parent, false)
            return OtherMessageViewHolder(
                binding
            )
        }
    }
}

class OtherDeletedMessageViewHolder(
    private val binding: ItemDeletedOtherMessageBinding
) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(message: TextMessage) {
        Glide.with(itemView).load(message.sender.photoUrl).into(binding.imageUser)
        binding.textMessageContent.text = message.content
        val currentDate = Date()
        val timeDifference = (currentDate.time - message.time.time) /
                (1000 * 60 * 60 * 24)

        when {
            DateUtils.isToday(message.time.time) -> binding.textMessageTime.text =
                SimpleDateFormat("HH:mm").format(message.time.time)
            timeDifference <= 7 -> binding.textMessageTime.text =
                SimpleDateFormat("E 'at' HH:mm").format(message.time.time)
            else -> binding.textMessageTime.text =
                SimpleDateFormat("dd.MM.yyyy").format(message.time.time)
        }
    }

    companion object {
        fun create(
            parent: ViewGroup
        ): OtherDeletedMessageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemDeletedOtherMessageBinding.inflate(layoutInflater, parent, false)
            return OtherDeletedMessageViewHolder(
                binding
            )
        }
    }
}


class MessagesComparator : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.time == newItem.time
    }
}