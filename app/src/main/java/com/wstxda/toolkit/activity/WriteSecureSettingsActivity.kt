package com.wstxda.toolkit.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.wstxda.toolkit.ui.component.WriteSecureSettingsBottomSheet
import com.wstxda.toolkit.utils.Constants

class WriteSecureSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            WriteSecureSettingsBottomSheet().show(
                supportFragmentManager, Constants.WRITE_SECURE_SETTINGS_DIALOG)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    if (f is WriteSecureSettingsBottomSheet) {
                        if (!isFinishing && !isChangingConfigurations) finish()
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false
        )
    }
}