package com.submission.soilink.view.profile

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.submission.soilink.R
import com.submission.soilink.util.showToast

class ChangePasswordFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
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

        builder.setView(view)

        btnYes.setOnClickListener {
            val enteredPassword = changePasswordText.text.toString()
            val enteredConfirmPassword = changeConfirmPasswordText.text.toString()

            changePasswordTextLayout.error = null
            changeConfirmPasswordTextLayout.error = null

            if (enteredPassword.length < 8 || enteredConfirmPassword.length < 8) {
                if (enteredPassword.length < 8) {
                    changePasswordTextLayout.error = getString(R.string.error_password_too_short)
                }
                if (enteredConfirmPassword.length < 8) {
                    changeConfirmPasswordTextLayout.error = getString(R.string.error_confirmation_password_too_short)
                }
            } else if (enteredPassword != enteredConfirmPassword) {
                changeConfirmPasswordTextLayout.error = getString(R.string.password_mismatch)
            } else {
                showToast(requireContext(), getString(R.string.success_change_password))
                dismiss()
            }
        }

        btnNo.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }
}