package com.wstxda.toolkit.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.wstxda.toolkit.ui.component.AboutAppBottomSheet

class AboutAppActivity : BaseActivity() {

    companion object {
        private const val TAG_ABOUT_DIALOG = "about_app"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val bottomSheet = AboutAppBottomSheet()
            bottomSheet.show(supportFragmentManager, TAG_ABOUT_DIALOG)
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    if (f is AboutAppBottomSheet) {
                        if (!isFinishing && !isChangingConfigurations) {
                            finish()
                        }
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            }, false
        )
    }
}