package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.memory.MemoryState

class MemoryIconProvider(private val context: Context) {

    fun getIcon(state: MemoryState): Icon {
        val resId = when (state) {
            MemoryState.RAM -> R.drawable.ic_ram
            MemoryState.STORAGE -> R.drawable.ic_storage
        }
        return Icon.createWithResource(context, resId)
    }
}