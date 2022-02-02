package com.example.chitchat.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.chitchat.models.appModels.Resource

class UserSearchFragment : UserSearchHandling() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAddingConversation()

    }

    private fun observeAddingConversation() {
        viewModel.conversationAddedResult.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    if (resource.data)
                        Toast.makeText(context, "New conversation added!", Toast.LENGTH_SHORT)
                            .show()
                    else
                        Toast.makeText(
                            context,
                            "You already have a conversation with this user!",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }


        })

    }

}
