package com.wstxda.toolkit.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.wstxda.toolkit.component.SecureSettingsBottomSheetDialog

class SecureSettingsPermissionActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (savedInstanceState == null) {
            SecureSettingsBottomSheetDialog().show(supportFragmentManager, "secure_settings_sheet")
        }
    }
}