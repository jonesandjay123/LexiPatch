package com.joneslab.lexipatch

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object VocabularyRepository {
    private const val FILENAME = "vocab.json"
    private const val PREFS_NAME = "WidgetPrefs"
    private const val KEY_INDEX = "CONTENT_INDEX"

    private val gson = Gson()
    private var cachedItems: MutableList<VocabItem>? = null

    // Hardcoded seed data
    private val SEED_DATA = listOf(
        VocabItem("Apple", "蘋果"),
        VocabItem("Banana", "香蕉"),
        VocabItem("Car", "汽車"),
        VocabItem("Dog", "狗"),
        VocabItem("Elephant", "大象"),
        VocabItem("Flower", "花"),
        VocabItem("Guitar", "吉他"),
        VocabItem("House", "房子"),
        VocabItem("Ice cream", "冰淇淋"),
        VocabItem("Jacket", "夾克")
    )

    fun getAll(context: Context): List<VocabItem> {
        if (cachedItems == null) {
            loadFromFile(context)
        }
        return cachedItems ?: emptyList()
    }

    fun addOne(context: Context, item: VocabItem) {
        val list = getAll(context).toMutableList()
        list.add(item)
        saveToFile(context, list)
    }

    fun addAll(context: Context, items: List<VocabItem>) {
        val list = getAll(context).toMutableList()
        list.addAll(items)
        saveToFile(context, list)
    }

    fun replaceAll(context: Context, items: List<VocabItem>) {
        saveToFile(context, items)
    }

    fun deleteAt(context: Context, index: Int) {
        val list = getAll(context).toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            saveToFile(context, list)
            
            // Adjust current index if needed
            val currentIndex = getCurrentIndex(context)
            if (currentIndex >= list.size) {
                 setCurrentIndex(context, maxOf(0, list.size - 1))
            }
        }
    }

    fun getCurrentIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_INDEX, 0)
    }

    fun setCurrentIndex(context: Context, index: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_INDEX, index).apply()
    }
    
    fun getCurrentItem(context: Context): VocabItem? {
        val list = getAll(context)
        if (list.isEmpty()) return null
        var index = getCurrentIndex(context)
        if (index !in list.indices) {
            index = 0
            setCurrentIndex(context, 0)
        }
        return list[index]
    }

    fun next(context: Context): VocabItem? {
        val list = getAll(context)
        if (list.isEmpty()) return null
        
        var index = getCurrentIndex(context)
        index++
        if (index >= list.size) index = 0
        setCurrentIndex(context, index)
        return list[index]
    }

    fun prev(context: Context): VocabItem? {
        val list = getAll(context)
        if (list.isEmpty()) return null
        
        var index = getCurrentIndex(context)
        index--
        if (index < 0) index = list.size - 1
        setCurrentIndex(context, index)
        return list[index]
    }

    private fun loadFromFile(context: Context) {
        val file = File(context.filesDir, FILENAME)
        if (!file.exists()) {
            cachedItems = SEED_DATA.toMutableList()
            saveToFile(context, cachedItems!!)
        } else {
            try {
                val json = file.readText()
                val type = object : TypeToken<List<VocabItem>>() {}.type
                cachedItems = gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
                cachedItems = mutableListOf()
            }
        }
    }

    private fun saveToFile(context: Context, items: List<VocabItem>) {
        cachedItems = items.toMutableList()
        val json = gson.toJson(items)
        val file = File(context.filesDir, FILENAME)
        file.writeText(json)
        
        // Notify widget update whenever data changes
        val intent = Intent(context, VocabularyWidget::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        }
        context.sendBroadcast(intent)
    }
}
