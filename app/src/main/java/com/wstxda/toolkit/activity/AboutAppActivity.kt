package com.wstxda.toolkit.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.wstxda.toolkit.services.UpdaterService
import com.wstxda.toolkit.ui.component.AboutAppBottomSheet
import com.wstxda.toolkit.ui.component.FreeAndroidWarnDialog
import com.wstxda.toolkit.utils.Constants

class AboutAppActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            AboutAppBottomSheet().show(supportFragmentManager, Constants.ABOUT_DIALOG)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    if (f is AboutAppBottomSheet) {
                        if (!isFinishing && !isChangingConfigurations) finish()
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false
        )

        FreeAndroidWarnDialog.show(supportFragmentManager, this)
        UpdaterService.checkForUpdatesAuto(lifecycleScope, this, supportFragmentManager)
    }
}