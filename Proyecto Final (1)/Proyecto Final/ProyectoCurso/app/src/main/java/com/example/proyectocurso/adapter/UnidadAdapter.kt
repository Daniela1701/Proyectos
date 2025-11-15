package com.example.proyectocurso.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.model.Unidad
import com.example.proyectocurso.R

class UnidadAdapter(
    private val lista: List<Unidad>,
    private val onClick: (Unidad) -> Unit
) : RecyclerView.Adapter<UnidadAdapter.UnidadViewHolder>() {

    inner class UnidadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUnidad)
        val tvDetalle: TextView = itemView.findViewById(R.id.tvDetalleUnidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnidadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.la_item_unidad, parent, false)
        return UnidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnidadViewHolder, position: Int) {
        val unidad = lista[position]
        holder.tvNombre.text = unidad.nombre
        holder.tvDetalle.text = "${unidad.clasificacion} - ${unidad.ocupantes ?: 0} ocupantes"
        holder.itemView.setOnClickListener { onClick(unidad) }
    }

    override fun getItemCount() = lista.size
}