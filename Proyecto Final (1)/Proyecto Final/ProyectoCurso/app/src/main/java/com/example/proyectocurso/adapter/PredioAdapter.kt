package com.example.proyectocurso.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.R
import com.example.proyectocurso.model.Predio

class PredioAdapter(
    private val lista: List<Predio>,
    private val onClick: (Predio) -> Unit
) : RecyclerView.Adapter<PredioAdapter.PredioViewHolder>() {

    inner class PredioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombrePredio)
        val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccionPredio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.la_item_predio_card, parent, false)
        return PredioViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredioViewHolder, position: Int) {
        val predio = lista[position]
        holder.tvNombre.text = predio.nombre
        holder.tvDireccion.text = predio.direccion ?: "Sin dirección"
        holder.itemView.setOnClickListener { onClick(predio) }
    }

    override fun getItemCount() = lista.size
}