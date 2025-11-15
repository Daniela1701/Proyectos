package com.example.proyectocurso

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class se_LecturaItemRegistro(
    private val context: Context,
    private val lecturas: List<se_LecturaItem>
) : RecyclerView.Adapter<se_LecturaItemRegistro.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemMes: TextView = view.findViewById(R.id.tvItemMes)
        val tvItemFecha: TextView = view.findViewById(R.id.tvItemFecha)
        val tvItemValor: TextView = view.findViewById(R.id.tvItemValor)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.se_activity_lectura_item_registro, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lecturas[position]

        val mes = obtenerMesAbreviado(item.fecha)
        holder.tvItemMes.text = mes
        holder.tvItemFecha.text = item.fecha
        holder.tvItemValor.text = String.format("%.2f", item.valor)

        //  Desactivar botón si la lectura ya fue facturada
        if (item.facturado) {
            holder.btnEditar.isEnabled = false
            holder.btnEditar.alpha = 0.5f
            holder.btnEditar.text = "Facturado"
            holder.btnEditar.setTextColor(Color.GRAY)
        } else {
            holder.btnEditar.isEnabled = true
            holder.btnEditar.alpha = 1f
            holder.btnEditar.text = "Editar"
            holder.btnEditar.setTextColor(Color.parseColor("#1976D2"))
        }

        // ir a Detalle
        holder.itemView.setOnClickListener {
            val intent = Intent(context, se_LecturaMedidorDetalleActivity::class.java).apply {
                putExtra("valor", item.valor)
                putExtra("fecha", item.fecha)
                putExtra("notas", item.notas)
                putExtra("unidad", item.unidadNombre)
                putExtra("predio", item.predioNombre)
                putExtra("tipoLectura", item.tipoLectura)
                putExtra("fotoRuta", item.fotoRuta)
            }
            context.startActivity(intent)
        }

        // abrir edición
        holder.btnEditar.setOnClickListener {
            if (item.facturado) {
                Toast.makeText(
                    context,
                    "No se puede editar: esta lectura ya fue facturada.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val intent = Intent(context, se_LecturaMedidorEditarActivity::class.java).apply {
                    putExtra("lecturaId", item.id)
                    putExtra("valor", item.valor)
                    putExtra("fecha", item.fecha)
                    putExtra("notas", item.notas)
                    putExtra("unidad", item.unidadNombre)
                    putExtra("fotoRuta", item.fotoRuta)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = lecturas.size

    private fun obtenerMesAbreviado(fecha: String): String {
        return try {
            val mes = fecha.split("-")[1].toInt()
            val meses = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
            meses[mes - 1]
        } catch (e: Exception) {
            ""
        }
    }
}
