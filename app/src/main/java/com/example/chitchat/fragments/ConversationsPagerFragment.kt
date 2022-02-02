package com.example.chitchat.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.chitchat.R
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.models.appModels.Resource
import com.example.chitchat.databinding.FragmentConversationsPagerBinding
import com.example.chitchat.viewModels.ConversationsPagerViewModel
import com.example.chitchat.viewModels.ConversationsPagerViewModelFactory
import com.example.chitchat.adapters.MainPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ConversationsPagerFragment : Fragment() {

    private lateinit var binding: FragmentConversationsPagerBinding
    private lateinit var viewModel: ConversationsPagerViewModel
    private lateinit var userId: String
    private var doubleBackToExitPressedOnce = false

    companion object {
        const val TIME_INTERVAL = 2000L
        const val PAGE_TIME_INTERVAL = 200L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_conversations_pager, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModelFactory =
            ConversationsPagerViewModelFactory(ChatDatabaseImpl(ChatDatabaseRepositoryImpl()))
        viewModel =
            ViewModelProvider(this, viewModelFactory)
                .get(ConversationsPagerViewModel::class.java)

        userId = Firebase.auth.uid!!

        Handler(Looper.getMainLooper()).postDelayed({
            binding.viewPager.setCurrentItem(viewModel.currentPage, false)
        }, PAGE_TIME_INTERVAL)

        observeUserProfile()
        setOnAddClickListener()
        setOnAddGroupClickListener()
        setOnUserProfileClickListener()
        addOnPageChangeListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    requireActivity().finish()
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(
                    requireContext(),
                    "Please click BACK again to exit",
                    Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.getMainLooper()).postDelayed(
                    { doubleBackToExitPressedOnce = false },
                    TIME_INTERVAL
                )
            }

        })
    }


    private fun setOnAddClickListener() {
        binding.imageButtonAddConversation.setOnClickListener {
            findNavController().navigate(ConversationsPagerFragmentDirections.actionConversationsPagerFragmentToUserSearchFragment())
        }
    }

    private fun setOnAddGroupClickListener() {
        binding.imageButtonAddGroup.setOnClickListener {
            findNavController().navigate(ConversationsPagerFragmentDirections.actionConversationsPagerFragmentToGroupCreatorFragment())
        }
    }


    private fun setOnUserProfileClickListener() {
        binding.imageButtonProfile.setOnClickListener {
            findNavController().navigate(ConversationsPagerFragmentDirections.actionConversationsPagerFragmentToUserProfileFragment())
        }
    }

    private fun observeUserProfile() {
        viewModel.getUserProfile(userId)
        viewModel.userProfile.observe(viewLifecycleOwner, { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.imageButtonProfile.setImageResource(R.drawable.image_loading)
                }

                is Resource.Failure -> {
                    Toast.makeText(
                        context,
                        "Couldn't retrieve data!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is Resource.Success -> {
                    val pagerAdapter = MainPagerAdapter(
                        listOf(
                            ConversationsListFragment(viewModel),
                            GroupsListFragment(viewModel, resource.data)
                        ), childFragmentManager, lifecycle
                    )
                    binding.viewPager.adapter = pagerAdapter
                    attachTabLayout()

                    Glide.with(this).load(resource.data.photoUrl)
                        .into(binding.imageButtonProfile)
                }
            }
        }
        )
    }

    private fun addOnPageChangeListener() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.currentPage = position
            }
        })

    }

    private fun attachTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getText(R.string.text_conversations)
                1 -> tab.text = getText(R.string.text_groups)
            }
        }.attach()
    }
}