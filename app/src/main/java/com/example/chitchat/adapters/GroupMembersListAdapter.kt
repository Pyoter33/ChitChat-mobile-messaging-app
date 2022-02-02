package com.example.chitchat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.ItemMembersListBinding
import com.example.chitchat.fragments.UsersGroupClickListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GroupMembersListAdapter(private val usersGroupClickListener: UsersGroupClickListener) :
    ListAdapter<User, GroupMembersListAdapter.UsersViewHolder>(UsersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, usersGroupClickListener)
    }


    class UsersViewHolder(private val binding: ItemMembersListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, usersGroupClickListener: UsersGroupClickListener) {

            Glide.with(itemView).load(user.photoUrl).into(binding.imageUser)
            if (user.id == Firebase.auth.uid)
                binding.imageButtonDelete.visibility = View.INVISIBLE

            binding.imageButtonDelete.setOnClickListener {
                usersGroupClickListener.onDeleteUser(user)
            }

        }

        companion object {
            fun create(parent: ViewGroup): UsersViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMembersListBinding.inflate(layoutInflater, parent, false)
                return UsersViewHolder(
                    binding
                )
            }
        }
    }
}