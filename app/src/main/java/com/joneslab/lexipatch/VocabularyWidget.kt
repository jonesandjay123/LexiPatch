package com.joneslab.lexipatch

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews

/**
 * Implementation of App Widget functionality.
 */
class VocabularyWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_PREV = "com.joneslab.lexipatch.ACTION_PREV"
        const val ACTION_NEXT = "com.joneslab.lexipatch.ACTION_NEXT"
        const val PREFS_NAME = "WidgetPrefs"
        const val KEY_INDEX = "CONTENT_INDEX"

        // Hardcoded vocabulary list: English -> Chinese
        val VOCABULARY = listOf(
            "Apple" to "苹果",
            "Banana" to "香蕉",
            "Car" to "汽车",
            "Dog" to "狗",
            "Elephant" to "大象",
            "Flower" to "花",
            "Guitar" to "吉他",
            "House" to "房子",
            "Ice cream" to "冰淇淋",
            "Jacket" to "夹克",
            "Key" to "钥匙",
            "Lion" to "狮子",
            "Moon" to "月亮",
            "Notebook" to "笔记本",
            "Orange" to "橙子",
            "Pencil" to "铅笔",
            "Queen" to "女王",
            "Rain" to "雨",
            "Sun" to "太阳",
            "Tree" to "树"
        )
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

        if (intent.action == ACTION_PREV || intent.action == ACTION_NEXT) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var index = prefs.getInt(KEY_INDEX, 0)

            if (intent.action == ACTION_PREV) {
                index--
                if (index < 0) index = VOCABULARY.size - 1
            } else {
                index++
                if (index >= VOCABULARY.size) index = 0
            }

            prefs.edit().putInt(KEY_INDEX, index).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, VocabularyWidget::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Retrieve current index
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val index = prefs.getInt(KEY_INDEX, 0)
        // Ensure index is valid just in case list changed size or prefs corrupted
        val safeIndex = if (index in VOCABULARY.indices) index else 0

        val (english, chinese) = VOCABULARY[safeIndex]

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_vocabulary)
        views.setTextViewText(R.id.appwidget_text_english, english)
        views.setTextViewText(R.id.appwidget_text_chinese, chinese)

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

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
