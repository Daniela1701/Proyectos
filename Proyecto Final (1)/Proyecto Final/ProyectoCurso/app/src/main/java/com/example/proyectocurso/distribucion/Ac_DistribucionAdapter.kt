package com.example.proyectocurso.distribucion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.R

class DistribucionAdapter(
    private val unidades: List<Triple<String, Int, Array<String>>>
) : RecyclerView.Adapter<DistribucionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DistribucionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ac_item_unidad_distribucion, parent, false)
        return DistribucionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DistribucionViewHolder, position: Int) {
        val (nombre, ocupantes, valores) = unidades[position]
        holder.tvNombreUnidad.text = nombre
        holder.tvOcupantes.text = "Ocupantes: $ocupantes"
        holder.tvConsumoNormalizado.text = valores[0]
        holder.tvConsumoTotalSoles.text = valores[1]
        holder.tvDiferenciaAsignada.text = valores[2]
        holder.tvTotalPagar.text = valores[3]

        holder.unitHeader.setOnClickListener {
            val isExpanded = holder.unitContent.visibility == View.VISIBLE
            holder.unitContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            holder.ivArrow.animate().rotation(if (isExpanded) 0f else 180f).start()
        }
    }

    override fun getItemCount(): Int = unidades.size
}

class DistribucionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val unitHeader = itemView.findViewById<View>(R.id.unitHeader)
    val unitContent = itemView.findViewById<View>(R.id.unitContent)
    val ivArrow = itemView.findViewById<ImageView>(R.id.ivArrow)
    val tvNombreUnidad = itemView.findViewById<TextView>(R.id.tvNombreUnidad)
    val tvOcupantes = itemView.findViewById<TextView>(R.id.tvOcupantes)
    val tvConsumoNormalizado = itemView.findViewById<TextView>(R.id.tvConsumoNormalizado)
    val tvConsumoTotalSoles = itemView.findViewById<TextView>(R.id.tvConsumoTotalSoles)
    val tvDiferenciaAsignada = itemView.findViewById<TextView>(R.id.tvDiferenciaAsignada)
    val tvTotalPagar = itemView.findViewById<TextView>(R.id.tvTotalPagar)
}