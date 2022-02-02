package com.example.chitchat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.ItemUsersListBinding
import com.example.chitchat.fragments.UsersClickListener

class UsersListAdapter(
    private val usersClickListener: UsersClickListener,
    private val userId: String
) :
    ListAdapter<User, UsersListAdapter.UsersViewHolder>(UsersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, usersClickListener, userId)
    }


    class UsersViewHolder(private val binding: ItemUsersListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, usersClickListener: UsersClickListener, userId: String) {

            Glide.with(itemView).load(user.photoUrl).into(binding.imageUser)
            binding.textUserName.text = user.name
            if (user.id == userId)
                binding.buttonAddConversation.visibility = View.INVISIBLE

            binding.buttonAddConversation.setOnClickListener {
                usersClickListener.onAddUser(user)
            }

        }

        companion object {
            fun create(parent: ViewGroup): UsersViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemUsersListBinding.inflate(layoutInflater, parent, false)
                return UsersViewHolder(
                    binding
                )
            }
        }
    }


}

class UsersComparator : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }
}