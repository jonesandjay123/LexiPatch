package com.joneslab.lexipatch

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class VocabularyWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_PREV = "com.joneslab.lexipatch.ACTION_PREV"
        const val ACTION_NEXT = "com.joneslab.lexipatch.ACTION_NEXT"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_PREV) {
            VocabularyRepository.prev(context)
            updateAllWidgets(context)
        } else if (intent.action == ACTION_NEXT) {
            VocabularyRepository.next(context)
            updateAllWidgets(context)
        } else if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAllWidgets(context)
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget = ComponentName(context.packageName, VocabularyWidget::class.java.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val item = VocabularyRepository.getCurrentItem(context)
        
        val views = RemoteViews(context.packageName, R.layout.widget_vocabulary)

        if (item != null) {
            views.setTextViewText(R.id.appwidget_text_english, item.english)
            views.setTextViewText(R.id.appwidget_text_chinese, item.chinese)
        } else {
            views.setTextViewText(R.id.appwidget_text_english, "No words yet")
            views.setTextViewText(R.id.appwidget_text_chinese, "Open LexiPatch to add words")
        }

        // Setup Previous Button Intent
        val prevIntent = Intent(context, VocabularyWidget::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_prev, prevPendingIntent)

        // Setup Next Button Intent
        val nextIntent = Intent(context, VocabularyWidget::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)
        
        // Setup Click to Open App
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context, 2, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.appwidget_text_english, appPendingIntent)

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
