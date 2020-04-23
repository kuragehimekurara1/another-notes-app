/*
 * Copyright 2020 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.notes.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.maltaisn.notes.R
import com.maltaisn.notes.ui.AppTheme
import com.maltaisn.notes.ui.note.adapter.NoteListLayoutMode
import kotlin.time.days


open class PrefsManager(protected val prefs: SharedPreferences) {

    val theme: AppTheme
        get() {
            val value = prefs.getString(THEME, AppTheme.SYSTEM.value)
            return AppTheme.values().find { it.value == value }!!
        }

    var listLayoutMode: NoteListLayoutMode
        get() {
            val value = prefs.getInt(LIST_LAYOUT_MODE, NoteListLayoutMode.LIST.value)
            return NoteListLayoutMode.values().find { it.value == value }!!
        }
        set(value) {
            prefs.edit { putInt(LIST_LAYOUT_MODE, value.value) }
        }

    var lastTrashReminderTime: Long
        get() = prefs.getLong(LAST_TRASH_REMIND_TIME, 0)
        set(value) = prefs.edit { putLong(LAST_TRASH_REMIND_TIME, value) }


    fun setDefaults(context: Context) {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, false)
    }


    companion object {
        // Settings keys
        const val THEME = "theme"
        const val EXPORT_DATA = "export_data"
        const val CLEAR_DATA = "clear_data"
        const val VIEW_LICENSES = "view_licenses"
        const val VERSION = "version"

        const val GROUP_SYNC = "group_sync"

        // Other keys
        private const val LIST_LAYOUT_MODE = "is_in_list_layout"
        private const val LAST_TRASH_REMIND_TIME = "last_deleted_remind_time"

        // Values
        val TRASH_AUTO_DELETE_DELAY = 7.days
        val TRASH_REMINDER_DELAY = 60.days
    }

}