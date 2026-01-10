package com.joneslab.lexipatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VocabAdapter(
    private var items: List<VocabItem>,
    private val onItemClick: (VocabItem) -> Unit,
    private val onItemLongClick: (VocabItem, Int) -> Unit
) : RecyclerView.Adapter<VocabAdapter.VocabViewHolder>() {

    class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val englishText: TextView = view.findViewById(android.R.id.text1)
        val chineseText: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VocabViewHolder(view)
    }

    override fun onBindViewHolder(holder: VocabViewHolder, position: Int) {
        val item = items[position]
        val displayEnglish = if (item.abbr.isNullOrEmpty()) item.english else "${item.english} (${item.abbr})"
        holder.englishText.text = displayEnglish
        holder.chineseText.text = item.chinese

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item, position)
            true
        }
    }

    override fun getItemCount() = items.size
    
    fun updateList(newItems: List<VocabItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
