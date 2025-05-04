package com.example.practicarecuperacionandroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LugarAdapter(
    private val lugares: List<LugarItem>,
    private val onItemClick: (LugarItem) -> Unit
) : RecyclerView.Adapter<LugarAdapter.LugarViewHolder>() {

    inner class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lugar, parent, false)
        return LugarViewHolder(view)
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        val lugar = lugares[position]
        holder.tvNombre.text = lugar.nombre

        holder.itemView.setOnClickListener {
            onItemClick(lugar)
        }
    }

    override fun getItemCount(): Int = lugares.size
}