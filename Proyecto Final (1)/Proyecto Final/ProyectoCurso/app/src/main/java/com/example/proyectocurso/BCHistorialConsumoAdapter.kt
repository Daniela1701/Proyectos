package com.example.proyectocurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.databinding.ActivityBchistorialConsumoBinding
import com.example.proyectocurso.databinding.ItemBchistorialConsumoBinding
import kotlin.toString


class BCHistorialConsumoAdapter(private var lista: ArrayList<HashMap<String, Any>>) :
    RecyclerView.Adapter<BCHistorialConsumoAdapter.ViewHolder>()  {

    inner class ViewHolder(val binding: ItemBchistorialConsumoBinding ) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBchistorialConsumoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        val consumo = item["consumo"].toString().toDoubleOrNull() ?: 0.0
        val costo = item["costo"].toString().toDoubleOrNull() ?: 0.0

        holder.binding.tvMes.text = item["mes"].toString()
        holder.binding.tvConsumo.text = "${"%.2f".format(consumo)} kWh"
        holder.binding.tvCosto.text = "S/. ${"%.2f".format(costo)}"
        holder.binding.tvKwhDia.text = item["kwhDia"].toString()

        holder.binding.btnExpandir.setOnClickListener {
            if (holder.binding.layoutDetalles.visibility == View.VISIBLE) {
                holder.binding.layoutDetalles.visibility = View.GONE
                holder.binding.btnExpandir.setImageResource(R.drawable.ic_expand_more)
            } else {
                holder.binding.layoutDetalles.visibility = View.VISIBLE
                holder.binding.btnExpandir.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: ArrayList<HashMap<String, Any>>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}