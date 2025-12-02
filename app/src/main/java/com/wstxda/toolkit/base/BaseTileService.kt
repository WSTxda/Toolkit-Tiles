package com.wstxda.toolkit.base

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.CallSuper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

abstract class BaseTileService : TileService() {

    protected val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var collectionJob: Job? = null

    @CallSuper
    override fun onStartListening() {
        super.onStartListening()
        collectionJob?.cancel()
        collectionJob = flowsToCollect().merge().onEach { updateTile() }.launchIn(serviceScope)
        updateTile()
    }

    @CallSuper
    override fun onStopListening() {
        super.onStopListening()
        collectionJob?.cancel()
        collectionJob = null
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    abstract fun updateTile()

    protected open fun flowsToCollect(): List<Flow<*>> = emptyList()

    protected fun setTileState(
        state: Int,
        label: CharSequence,
        subtitle: CharSequence? = null,
        icon: android.graphics.drawable.Icon? = null,
        description: CharSequence? = null,
    ) {
        qsTile?.apply {
            this.state = state
            this.label = label
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.subtitle = subtitle
            }
            this.icon = icon
            this.contentDescription = description
            updateTile()
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    protected fun startActivityAndCollapseCompat(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION") startActivityAndCollapse(intent)
        }
    }

    protected fun startActivityAndCollapse(cls: Class<*>) {
        startActivityAndCollapseCompat(Intent(this, cls))
    }
}