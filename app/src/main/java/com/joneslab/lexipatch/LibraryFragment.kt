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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LibraryFragment : Fragment() {

    private lateinit var adapter: VocabAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        val fab: FloatingActionButton = view.findViewById(R.id.fab_add)

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = VocabAdapter(
            items = emptyList(),
            onItemClick = { item -> 
                // Optional: Edit functionality or detail view
            },
            onItemLongClick = { item, position ->
                showDeleteDialog(item, position)
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
}
