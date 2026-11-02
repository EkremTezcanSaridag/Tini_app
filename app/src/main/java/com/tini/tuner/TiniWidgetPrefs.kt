package com.tini.tuner

import android.content.Context

object TiniWidgetPrefs {
    private const val PREFS = "tini_widget_prefs"
    private const val KEY_NOTE = "last_note"
    private const val KEY_STATUS = "last_status"
    private const val KEY_CENTS = "last_cents"

    fun update(context: Context, note: String, status: String, cents: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_NOTE, note)
            .putString(KEY_STATUS, status)
            .putFloat(KEY_CENTS, cents)
            .apply()
        TiniWidgetProvider.requestUpdate(context)
    }

    fun readNote(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_NOTE, "—") ?: "—"

    fun readStatus(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_STATUS, "Tını") ?: "Tını"

    fun readCents(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(KEY_CENTS, 0f)
}
