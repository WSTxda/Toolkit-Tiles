package com.wstxda.toolkit.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wstxda.toolkit.R
import com.wstxda.toolkit.data.AboutItem

class AboutAppViewModel(application: Application) : AndroidViewModel(application) {

    private val _appVersion = MutableLiveData<String>()
    val applicationVersion: LiveData<String> = _appVersion

    private val _links = MutableLiveData<List<AboutItem>>()
    val links: LiveData<List<AboutItem>> = _links

    init {
        loadAppVersion()
        loadLinks()
    }

    private fun loadAppVersion() {
        try {
            val context = getApplication<Application>()
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION") context.packageManager.getPackageInfo(
                    context.packageName, 0
                )
            }
            _appVersion.value = packageInfo.versionName ?: "Unknown"
        } catch (_: Exception) {
            _appVersion.value = "Unknown"
        }
    }

    private fun loadLinks() {
        val linkList = listOf(
            AboutItem(
                icon = R.drawable.ic_developer,
                title = R.string.about_wstxda,
                summary = R.string.about_developer,
                url = "https://github.com/WSTxda"
            ),
            AboutItem(
                icon = R.drawable.ic_github,
                title = R.string.about_github,
                summary = R.string.about_repository,
                url = "https://github.com/WSTxda/Toolkit-Tiles"
            ),
            AboutItem(
                icon = R.drawable.ic_contributors,
                title = R.string.about_contributors,
                summary = R.string.about_contributors_summary,
                url = "https://github.com/WSTxda/Toolkit-Tiles/graphs/contributors"
            ),
            AboutItem(
                icon = R.drawable.ic_license,
                title = R.string.about_license,
                summary = R.string.about_license_summary,
                url = "https://github.com/WSTxda/Toolkit-Tiles/blob/main/LICENSE"
            ),
        )
        _links.value = linkList
    }

    fun openUrl(link: AboutItem) {
        try {
            val urlString = link.url ?: return
            val uri = if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                "https://$urlString".toUri()
            } else {
                urlString.toUri()
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInfo() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}