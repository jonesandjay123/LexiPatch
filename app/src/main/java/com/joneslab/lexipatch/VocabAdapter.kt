package com.joneslab.lexipatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VocabAdapter(
    private var items: List<VocabItem>,
    private val onItemClick: (VocabItem) -> Unit,
    private val onItemLongClick: (VocabItem, Int) -> Unit,
    private val onSpeakClick: (VocabItem) -> Unit
) : RecyclerView.Adapter<VocabAdapter.VocabViewHolder>() {

    class VocabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val englishText: TextView = view.findViewById(R.id.text_english)
        val chineseText: TextView = view.findViewById(R.id.text_chinese)
        val speakButton: View = view.findViewById(R.id.btn_speak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vocab, parent, false)
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
        holder.speakButton.setOnClickListener {
            onSpeakClick(item)
        }
    }

    override fun getItemCount() = items.size
    
    fun updateList(newItems: List<VocabItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
