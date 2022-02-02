package com.example.chitchat.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.chitchat.R
import com.example.chitchat.fragments.ProfileDialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteUserDialog(private val dialogInterface: ProfileDialogInterface) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity.let {
            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)

            builder.setMessage(R.string.text_delete_user_description)
                .setTitle(R.string.text_delete_user_title)
                .setPositiveButton(R.string.text_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .setNegativeButton(R.string.text_delete_account) { _, _ ->
                    dialogInterface.onDeleteUser()
                }.create()

        }
    }
}