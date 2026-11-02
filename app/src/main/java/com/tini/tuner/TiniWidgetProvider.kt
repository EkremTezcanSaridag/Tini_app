package com.tini.tuner

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class TiniWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TiniWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                ids.forEach { id -> updateWidget(context, manager, id) }
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_tini)
            views.setTextViewText(R.id.widget_note, TiniWidgetPrefs.readNote(context))
            views.setTextViewText(R.id.widget_status, TiniWidgetPrefs.readStatus(context))
            val cents = TiniWidgetPrefs.readCents(context)
            views.setTextViewText(R.id.widget_cents, if (cents == 0f) "0¢" else "%+.0f¢".format(cents))
            manager.updateAppWidget(id, views)
        }
    }
}
