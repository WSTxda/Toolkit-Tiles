package com.wstxda.toolkit.activity

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.wstxda.toolkit.R
import com.wstxda.toolkit.permissions.PermissionManager

class LocationPermissionActivity : BaseActivity() {

    private val permissionManager by lazy { PermissionManager(applicationContext) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        val messageRes = if (granted) {
            R.string.location_permission_granted_message
        } else {
            R.string.location_permission_denied_message
        }
        Toast.makeText(this, getString(messageRes), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissionManager.hasLocationPermission()) {
            finish()
            return
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }
}