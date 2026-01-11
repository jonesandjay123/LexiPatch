package com.joneslab.lexipatch

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class LibraryFragment : Fragment() {

    private lateinit var adapter: VocabAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var ttsHelper: TTSHelper

    private lateinit var exportLauncher: ActivityResultLauncher<String>
    private lateinit var importLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttsHelper = TTSHelper(requireContext())
        setupLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)
        val btnMenu: ImageButton = view.findViewById(R.id.btn_menu)

        btnMenu.setOnClickListener { showMenu(it) }

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = VocabAdapter(
            items = emptyList(),
            onItemClick = { item -> 
                // Optional: Edit functionality or detail view
            },
            onItemLongClick = { item, position ->
                showDeleteDialog(item, position)
            },
            onSpeakClick = { item ->
                speakItem(item)
            }
        )
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            showAddDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }
    
    override fun onDestroy() {
        if (::ttsHelper.isInitialized) {
            ttsHelper.shutdown()
        }
        super.onDestroy()
    }

    private fun speakItem(item: VocabItem) {
        val textToSpeak = if (item.abbr.isNullOrEmpty()) {
            item.english
        } else {
            // Speak english then spell out abbreviation (spaced out)
            "${item.english}. ${item.abbr.toCharArray().joinToString(" ")}"
        }
        ttsHelper.speak(textToSpeak)
    }

    private fun refreshList() {
        context?.let {
            val items = VocabularyRepository.getAll(it)
            adapter.updateList(items)
        }
    }

    private fun showAddDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etEnglish = EditText(context).apply { hint = "English Word" }
        val etChinese = EditText(context).apply { hint = "Chinese Meaning" }
        val etAbbr = EditText(context).apply { hint = "Abbreviation (Optional)" }

        layout.addView(etEnglish)
        layout.addView(etChinese)
        layout.addView(etAbbr)

        AlertDialog.Builder(context)
            .setTitle("Add New Word")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val english = etEnglish.text.toString().trim()
                val chinese = etChinese.text.toString().trim()
                val abbr = etAbbr.text.toString().trim()

                if (english.isNotEmpty() && chinese.isNotEmpty()) {
                    val newItem = VocabItem(
                        english = english,
                        chinese = chinese,
                        abbr = if (abbr.isEmpty()) null else abbr
                    )
                    VocabularyRepository.addOne(context, newItem)
                    refreshList()
                    Toast.makeText(context, "Added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "English and Chinese are required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(item: VocabItem, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Word")
            .setMessage("Are you sure you want to delete '${item.english}'?")
            .setPositiveButton("Delete") { _, _ ->
                context?.let {
                    VocabularyRepository.deleteAt(it, position)
                    refreshList()
                    Toast.makeText(it, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun setupLaunchers() {
        exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                try {
                    val context = requireContext()
                    val items = VocabularyRepository.getAll(context)
                    val json = Gson().toJson(items)
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                showImportConfirmation(it)
            }
        }
    }

    private fun showMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add("Export Vocabulary")
        popup.menu.add("Import Vocabulary")
        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Export Vocabulary" -> {
                    exportLauncher.launch("vocabulary_backup.json")
                    true
                }
                "Import Vocabulary" -> {
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showImportConfirmation(uri: android.net.Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Import Vocabulary")
            .setMessage("Importing will replace your current vocabulary list. This action cannot be undone.")
            .setPositiveButton("Import") { _, _ ->
                performImport(uri)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performImport(uri: android.net.Uri) {
        try {
            val context = requireContext()
            val contentResolver = context.contentResolver
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            
            val json = stringBuilder.toString()
            val type = object : TypeToken<List<VocabItem>>() {}.type
            val items: List<VocabItem> = Gson().fromJson(json, type)
            
            if (items.isNotEmpty()) {
                VocabularyRepository.replaceAll(context, items)
                refreshList()
                Toast.makeText(context, "Imported ${items.size} words", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No items found in file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Import failed: Invalid format", Toast.LENGTH_SHORT).show()
        }
    }
}
