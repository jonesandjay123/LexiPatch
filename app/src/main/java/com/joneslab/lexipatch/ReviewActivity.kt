package com.joneslab.lexipatch

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddSelected: Button
    private lateinit var adapter: ReviewAdapter
    private var proposedItems: List<VocabItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recycler_view_review)
        btnAddSelected = findViewById(R.id.btn_add_selected)

        val json = intent.getStringExtra("items_json")
        if (json != null) {
            val type = object : TypeToken<List<VocabItem>>() {}.type
            proposedItems = Gson().fromJson(json, type)
        }

        adapter = ReviewAdapter(proposedItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAddSelected.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                VocabularyRepository.addAll(this, selected)
                Toast.makeText(this, "${selected.size} items added to Library", Toast.LENGTH_SHORT).show()
                finish() // Close activity and return to previous screen
            } else {
                Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
