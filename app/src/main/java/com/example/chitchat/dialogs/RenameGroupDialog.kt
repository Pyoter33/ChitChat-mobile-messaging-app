package com.example.chitchat.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.chitchat.R
import com.example.chitchat.fragments.GroupDialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RenameGroupDialog(private val dialogInterface: GroupDialogInterface) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity.let {
            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)

            val inflater = requireActivity().layoutInflater

            val dialogLayout = inflater.inflate(R.layout.fragment_rename_user_dialog, null)

            builder.setTitle(R.string.text_rename_group_title)
            builder.setView(dialogLayout)
                .setPositiveButton(R.string.text_change_name) { _, _ ->
                    val name = dialogLayout.findViewById<EditText>(R.id.editTextRename)?.text

                    if (!TextUtils.isEmpty(name)) {
                        dialogInterface.onChangeName(name.toString())
                    }
                }
                .setNegativeButton(R.string.text_cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()

        }
    }

}