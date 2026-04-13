package com.wstxda.toolkit.ui.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import com.google.android.material.divider.MaterialDivider
import com.wstxda.toolkit.R
import com.wstxda.toolkit.databinding.DialogWriteSecureSettingsBinding

class WriteSecureSettingsBottomSheet : BaseBottomSheet<DialogWriteSecureSettingsBinding>() {

    companion object {
        const val TAG = "write_secure_settings"
    }

    override val topDivider: MaterialDivider get() = binding.dividerTop
    override val bottomDivider: MaterialDivider get() = binding.dividerBottom
    override val scrollView: NestedScrollView get() = binding.scrollView
    override val titleTextView: TextView get() = binding.dialogTitle
    override val titleResId: Int = R.string.write_secure_settings_title

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogWriteSecureSettingsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageName = requireContext().packageName
        val command = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"

        binding.apply {
            dialogMessage.setText(R.string.write_secure_settings_message)
            adbCommand.text = command
            dialogSupportMessage.setText(R.string.write_secure_settings_support)

            negativeButton.apply {
                setText(android.R.string.cancel)
                setOnClickListener { dismiss() }
            }

            positiveButton.apply {
                setText(R.string.copy_clipboard)
                setOnClickListener {
                    copyToClipboard(command)
                    dismiss()
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", text))
        Toast.makeText(requireContext(), R.string.write_secure_settings_clipboard_message, Toast.LENGTH_SHORT).show()
    }
}