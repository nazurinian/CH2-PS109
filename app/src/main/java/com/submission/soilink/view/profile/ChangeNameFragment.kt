package com.submission.soilink.view.profile

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.submission.soilink.R
import com.submission.soilink.util.showToast

class ChangeNameFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as AppCompatActivity)
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.fragment_change_name, null)

        val changeNameTextLayout = view.findViewById<TextInputLayout>(R.id.changeNameTextLayout)
        val changeNameText = view.findViewById<EditText>(R.id.changeNameText)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        builder.setView(view)

        btnYes.setOnClickListener {
            val enteredName = changeNameText.text.toString()
            changeNameTextLayout.error = null

            if (enteredName.length < 3) {
                changeNameTextLayout.error = getString(R.string.error_name_too_short)
            } else {
                showToast(requireContext(), getString(R.string.success_change_name, enteredName))
                dismiss()
            }
        }

        btnNo.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }
}
