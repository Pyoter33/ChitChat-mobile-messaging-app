package com.example.chitchat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.ItemGroupUsersListBinding


class GroupUsersListAdapter :
    ListAdapter<User, GroupUsersListAdapter.UsersViewHolder>(UsersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }


    class UsersViewHolder(private val binding: ItemGroupUsersListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            Glide.with(itemView).load(user.photoUrl).into(binding.imageUser)
            binding.textUserName.text = user.name

        }

        companion object {
            fun create(parent: ViewGroup): UsersViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroupUsersListBinding.inflate(layoutInflater, parent, false)
                return UsersViewHolder(
                    binding
                )
            }
        }
    }
}
