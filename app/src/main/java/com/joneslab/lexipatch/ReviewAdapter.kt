package com.joneslab.lexipatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(
    private val items: List<VocabItem>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    val selectedItems = BooleanArray(items.size) { true }

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val englishText: TextView = view.findViewById(R.id.text_english)
        val chineseText: TextView = view.findViewById(R.id.text_chinese)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = items[position]
        holder.englishText.text = if (item.abbr.isNullOrEmpty()) item.english else "${item.english} (${item.abbr})"
        holder.chineseText.text = item.chinese
        
        holder.checkBox.isChecked = selectedItems[position]
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            selectedItems[position] = isChecked
        }
    }

    override fun getItemCount() = items.size
    
    fun getSelectedItems(): List<VocabItem> {
        val result = mutableListOf<VocabItem>()
        for (i in items.indices) {
            if (selectedItems[i]) {
                result.add(items[i])
            }
        }
        return result
    }
}
