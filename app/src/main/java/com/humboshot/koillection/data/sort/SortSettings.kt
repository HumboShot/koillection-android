package com.humboshot.koillection.data.sort

import android.content.Context
import androidx.core.content.edit
import com.humboshot.koillection.MainApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The sort order of collections and items. It stays after a restart and after a logout. */
object SortSettings {
    private const val PREFS_NAME = "goshiki_list_settings"
    private const val KEY_ORDER = "sort_order"

    private val prefs by lazy { MainApplication.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    private val _order by lazy {
        val saved = prefs.getString(KEY_ORDER, null)
        MutableStateFlow(SortOrder.entries.firstOrNull { it.name == saved } ?: SortOrder.TITLE_ASC)
    }

    val order: StateFlow<SortOrder> get() = _order.asStateFlow()

    fun setOrder(order: SortOrder) {
        _order.value = order
        prefs.edit { putString(KEY_ORDER, order.name) }
    }
}
