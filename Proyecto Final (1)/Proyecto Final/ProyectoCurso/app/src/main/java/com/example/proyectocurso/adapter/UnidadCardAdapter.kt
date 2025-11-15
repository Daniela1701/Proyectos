package com.example.proyectocurso.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.R
import com.example.proyectocurso.model.UnidadCard

class UnidadCardAdapter(
    val lista: MutableList<UnidadCard>,
    private val onEditar: (UnidadCard, Int) -> Unit,
    private val onEliminarUnidad: (UnidadCard) -> Unit,
    private val onEliminarOcupante: (UnidadCard) -> Unit
) : RecyclerView.Adapter<UnidadCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreUnidad)
        val tvOcupante: TextView = itemView.findViewById(R.id.tvOcupante)

        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        val btnEliminarUnidad: ImageButton = itemView.findViewById(R.id.btnEliminarUnidad)
        val btnEliminarOcupante: ImageButton = itemView.findViewById(R.id.btnEliminarOcupante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.la_item_unidad_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = lista[position]

        holder.tvNombre.text = item.unidadNombre
        holder.tvOcupante.text = if (item.ocupanteNombres.isNullOrBlank()) {
            "Ocupante: Sin inquilino"
        } else {
            "Ocupante: ${item.ocupanteNombres} ${item.ocupanteApellidos ?: ""}".trim()
        }

        // Botón Editar
        holder.btnEditar.setOnClickListener { onEditar(item, position) }
        holder.btnEditar.isEnabled = item.puedeEditar
        holder.btnEditar.alpha = if (item.puedeEditar) 1f else 0.5f

        // Botón Eliminar Unidad
        holder.btnEliminarUnidad.setOnClickListener { onEliminarUnidad(item) }

        // Botón Eliminar Ocupante (solo si hay)
        holder.btnEliminarOcupante.visibility = if (item.ocupanteNombres.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.btnEliminarOcupante.setOnClickListener { onEliminarOcupante(item) }
    }

    override fun getItemCount() = lista.size

    fun actualizarItem(pos: Int, nuevo: UnidadCard) {
        lista[pos] = nuevo
        notifyItemChanged(pos)
    }

    fun eliminarItem(pos: Int) {
        lista.removeAt(pos)
        notifyItemRemoved(pos)
    }
}