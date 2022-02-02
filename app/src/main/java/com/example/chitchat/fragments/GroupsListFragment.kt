package com.example.chitchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chitchat.R
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User
import com.example.chitchat.databinding.FragmentGroupsListBinding
import com.example.chitchat.viewModels.ConversationsPagerViewModel
import com.example.chitchat.adapters.GroupsListAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class GroupsListFragment(
    private val viewModel: ConversationsPagerViewModel,
    private val currentUser: User
) : Fragment(), GroupsClickListener {

    private lateinit var binding: FragmentGroupsListBinding
    private lateinit var adapter: GroupsListAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_groups_list, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = Firebase.auth.uid!!
        adapter = GroupsListAdapter(this, userId)
        binding.groupsList.adapter = adapter
        binding.groupsList.layoutManager = LinearLayoutManager(context)
        binding.groupsList.itemAnimator = null
        observeGroupsList()
    }

    private fun observeGroupsList() {
        viewModel.getGroups(userId)
        viewModel.groups.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    val list = resource.data
                    if (list.isEmpty())
                        binding.textNoGroupsFound.visibility = View.VISIBLE
                    else {
                        binding.textNoGroupsFound.visibility = View.GONE
                        viewModel.compareSubscriptionsWithGroups(
                            currentUser,
                            list,
                            Firebase.messaging
                        )
                        viewModel.sortGroupsList(list)
                        adapter.submitList(list)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    override fun onClickGroup(groupId: String) {
        findNavController().navigate(
            ConversationsPagerFragmentDirections.actionConversationsPagerFragmentToGroupFragment(
                groupId
            )
        )
    }

}

interface GroupsClickListener {
    fun onClickGroup(groupId: String)
}