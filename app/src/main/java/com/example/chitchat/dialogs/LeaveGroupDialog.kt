package com.example.chitchat.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.chitchat.R
import com.example.chitchat.fragments.GroupDialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LeaveGroupDialog(private val dialogInterface: GroupDialogInterface) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity.let {
            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)

            builder.setTitle(R.string.text_leave_group_title)
                .setNegativeButton(R.string.text_cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.text_leave_group) { _, _ ->
                    dialogInterface.onLeaveGroup()
                }.create()
        }
    }
}