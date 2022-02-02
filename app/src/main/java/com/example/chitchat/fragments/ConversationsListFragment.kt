package com.example.chitchat.fragments

import android.os.Bundle
import android.util.Log
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
import com.example.chitchat.databinding.FragmentConversationsListBinding
import com.example.chitchat.adapters.ConversationsListAdapter
import com.example.chitchat.viewModels.ConversationsPagerViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ConversationsListFragment(private val viewModel: ConversationsPagerViewModel) : Fragment(),
    ConversationClickListener {

    private lateinit var binding: FragmentConversationsListBinding
    private lateinit var adapter: ConversationsListAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_conversations_list,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = Firebase.auth.uid!!
        adapter = ConversationsListAdapter(this, userId)
        binding.conversationsList.adapter = adapter
        binding.conversationsList.layoutManager = LinearLayoutManager(context)
        binding.conversationsList.itemAnimator = null
        observeConversationsList()
    }

    private fun observeConversationsList() {
        viewModel.getConversations(userId)
        viewModel.conversations.observe(viewLifecycleOwner, { resource ->
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
                    Log.i("indicator", list.toString())
                    if (list.isEmpty())
                        binding.textNoConversationsFound.visibility = View.VISIBLE
                    else {
                        binding.textNoConversationsFound.visibility = View.GONE
                        viewModel.sortConversationsList(list)
                        adapter.submitList(list)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }


    override fun onClickConversation(id: String) {
        findNavController().navigate(
            ConversationsPagerFragmentDirections.actionConversationsPagerFragmentToConversationFragment(
                id
            )
        )
    }


}

interface ConversationClickListener {
    fun onClickConversation(id: String)

}