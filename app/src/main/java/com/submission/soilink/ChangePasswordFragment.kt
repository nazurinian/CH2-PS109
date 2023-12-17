package com.submission.soilink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.submission.soilink.util.showToast
import com.submission.soilink.view.customview.CustomEditTextView

class ChangePasswordFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Handle arguments if needed
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity as AppCompatActivity)
        val inflater = LayoutInflater.from(requireActivity())
        val view = inflater.inflate(R.layout.fragment_change_password, null)

        val changePasswordTextLayout = view.findViewById<TextInputLayout>(R.id.changePasswordTextLayout)
        val changePasswordText = view.findViewById<EditText>(R.id.changePasswordText)
        val changeConfirmPasswordTextLayout = view.findViewById<TextInputLayout>(R.id.changeConfirmPasswordTextLayout)
        val changeConfirmPasswordText = view.findViewById<EditText>(R.id.changeConfirmPasswordText)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        // Customize the dialog as needed, set title, buttons, etc.
        builder.setView(view)

        // Set OnClickListener for "Yes" button
        btnYes.setOnClickListener {
            val enteredPassword = changePasswordText.text.toString()
            val enteredConfirmPassword = changeConfirmPasswordText.text.toString()

            // Reset errors
            changePasswordTextLayout.error = null
            changeConfirmPasswordTextLayout.error = null

            if (enteredPassword.length < 8 || enteredConfirmPassword.length < 8) {
                // Display error message for minimum length
                if (enteredPassword.length < 8) {
                    changePasswordTextLayout.error = "Password must be at least 8 characters"
                }
                if (enteredConfirmPassword.length < 8) {
                    changeConfirmPasswordTextLayout.error = "Password must be at least 8 characters"
                }
            } else if (enteredPassword != enteredConfirmPassword) {
                // Display error message for mismatched passwords
                changeConfirmPasswordTextLayout.error = "Passwords do not match"
            } else {
                showToast(requireContext(), "Password berhasil diganti")
                // Perform other actions related to password change
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