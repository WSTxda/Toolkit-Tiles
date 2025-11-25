package com.wstxda.toolkit.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object CounterValue {
    private const val PREFS_NAME = "CounterPrefs"
    private const val KEY_COUNTER = "counterValue"
    private const val KEY_LAST_ACTION = "lastAction"
    const val ACTION_ADD = "add"
    const val ACTION_REMOVE = "remove"
    const val ACTION_RESET = "reset"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getValue(context: Context): Int {
        return getPrefs(context).getInt(KEY_COUNTER, 0)
    }

    fun setValue(context: Context, value: Int) {
        getPrefs(context).edit { putInt(KEY_COUNTER, value) }
    }

    fun setLastAction(context: Context, action: String) {
        getPrefs(context).edit { putString(KEY_LAST_ACTION, action) }
    }

    fun getLastAction(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_ACTION, ACTION_RESET)!!
    }

    fun add(context: Context): Int {
        setLastAction(context, ACTION_ADD)
        val newValue = getValue(context) + 1
        setValue(context, newValue)
        return newValue
    }

    fun remove(context: Context): Int {
        setLastAction(context, ACTION_REMOVE)
        val newValue = getValue(context) - 1
        setValue(context, newValue)
        return newValue
    }

    fun reset(context: Context) {
        setLastAction(context, ACTION_RESET)
        setValue(context, 0)
    }
}