package com.example.chitchat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.activities.AuthActivity
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.databinding.FragmentUserProfileBinding
import com.example.chitchat.dialogs.DeleteUserDialog
import com.example.chitchat.dialogs.RenameUserDialog
import com.example.chitchat.viewModels.UserProfileViewModel
import com.example.chitchat.viewModels.UserProfileViewModelFactory
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserProfileFragment : Fragment(), ProfileDialogInterface {

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var renameUserDialog: RenameUserDialog
    private lateinit var deleteUserDialog: DeleteUserDialog
    private lateinit var userId: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory =
            UserProfileViewModelFactory(ChatDatabaseImpl(ChatDatabaseRepositoryImpl()))
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserProfileViewModel::class.java)
        renameUserDialog = RenameUserDialog(this)
        deleteUserDialog = DeleteUserDialog(this)
        userId = Firebase.auth.uid!!
        observeUserProfile()
        observeDeletingUser()
        setOnGoBackClickListener()
        setOnRenameUserClickListener()
        setOnDeleteUserClickListener()
        setOnLogoutClickListener()
    }

    private fun setOnGoBackClickListener() {
        binding.imageButtonGoBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun setOnRenameUserClickListener() {
        binding.buttonChangeName.setOnClickListener {
            renameUserDialog.show(childFragmentManager, "RenameUserDialog")
        }
    }

    private fun setOnDeleteUserClickListener() {
        binding.buttonDeleteAccount.setOnClickListener {
            deleteUserDialog.show(childFragmentManager, "DeleteUserDialog")
        }
    }

    private fun setOnLogoutClickListener() {
        binding.buttonLogout.setOnClickListener {
            Toast.makeText(requireContext(), "You've been logged out!", Toast.LENGTH_SHORT).show()
            signOut()
            changeActivity()
        }

    }

    private fun observeUserProfile() {
        viewModel.getUserProfile(userId)
        viewModel.userProfile.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.imageUser.setImageResource(R.drawable.image_loading)
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    Glide.with(this).load(resource.data.photoUrl).into(binding.imageUser)
                    binding.textUserName.text = resource.data.name
                }
            }
        })
    }

    private fun observeDeletingUser() {
        viewModel.deleteResult.observe(viewLifecycleOwner, { resource ->
            if (resource is Resource.Success) {
                Toast.makeText(
                    requireContext(),
                    "Account deleted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                signOut()
                changeActivity()
            }
        })

    }

    override fun onChangeName(name: String) {
        viewModel.renameUser(userId, name)
    }

    override fun onDeleteUser() {
        try {
            viewModel.deleteUser(Firebase.auth.currentUser!!)

        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Toast.makeText(
                requireContext(),
                "To do this operation you need to sign in once again!",
                Toast.LENGTH_SHORT
            ).show()
            signOut()
            changeActivity()
            return
        }
    }

    private fun changeActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun signOut() {
        Firebase.auth.signOut()
        LoginManager.getInstance().logOut()
        GoogleSignIn.getClient(
            requireActivity(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut()
    }

}


interface ProfileDialogInterface {
    fun onChangeName(name: String)
    fun onDeleteUser()
}