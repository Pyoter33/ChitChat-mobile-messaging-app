package com.example.chitchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.ItemUsersListBinding
import com.example.chitchat.fragments.UsersGroupClickListener

class UsersGroupListAdapter(private val usersGroupClickListener: UsersGroupClickListener) :
    ListAdapter<User, UsersGroupListAdapter.UsersViewHolder>(UsersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, usersGroupClickListener)
    }


    class UsersViewHolder(private val binding: ItemUsersListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, usersGroupClickListener: UsersGroupClickListener) {

            Glide.with(itemView).load(user.photoUrl).into(binding.imageUser)
            binding.textUserName.text = user.name

            binding.buttonAddConversation.setOnClickListener {
                usersGroupClickListener.onAddUser(user)
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
