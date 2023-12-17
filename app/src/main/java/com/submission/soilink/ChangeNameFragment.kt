package com.submission.soilink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.submission.soilink.R
import com.submission.soilink.util.showToast
import com.submission.soilink.view.customview.CustomEditTextView

class ChangeNameFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Handle arguments if needed
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as AppCompatActivity)
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.fragment_change_name, null)

        val changeNameTextLayout = view.findViewById<TextInputLayout>(R.id.changeNameTextLayout)
        val changeNameText = view.findViewById<EditText>(R.id.changeNameText)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        // Customize the dialog as needed, set title, buttons, etc.
        builder.setView(view)

        // Set OnClickListener for "Yes" button
        btnYes.setOnClickListener {
            val enteredName = changeNameText.text.toString()

            // Reset error
            changeNameTextLayout.error = null

            if (enteredName.length < 3) {
                // Display error message for minimum length
                changeNameTextLayout.error = "Name must be at least 3 characters"
            } else {
                showToast(requireContext(), "Nama Berhasil Diganti: $enteredName")
                // Do something with enteredName
                // Dismiss the dialog or perform other actions
                dismiss()
            }
        }

        // Set OnClickListener for "No" button
        btnNo.setOnClickListener {
            // Handle negative button click
            // Dismiss the dialog or perform other actions
            dismiss()
        }

        return builder.create()
    }
}
