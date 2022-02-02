package com.example.chitchat.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.models.appModels.User

class UserGroupSearchFragment : UserSearchHandling() {

    private lateinit var args: UserGroupSearchFragmentArgs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args =
           UserGroupSearchFragmentArgs.fromBundle(requireArguments())
        observeAddingUserToGroup()
    }

    private fun observeAddingUserToGroup() {
        viewModel.groupUserAddedResult.observe(viewLifecycleOwner, { resource ->
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
                        Toast.makeText(context, "New user added to the group!", Toast.LENGTH_SHORT)
                            .show()
                    else
                        Toast.makeText(
                            context,
                            "This user is already in the group!",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
        })
    }

    override fun onAddUser(user: User) {
        viewModel.addNewUserToGroup(args.groupId, user.id!!)
    }
}