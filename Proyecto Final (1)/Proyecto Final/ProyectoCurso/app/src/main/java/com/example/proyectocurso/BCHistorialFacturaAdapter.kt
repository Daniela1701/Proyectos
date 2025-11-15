package com.example.proyectocurso

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.databinding.ItemBchistorialFacturaBinding


class BCHistorialFacturaAdapter(private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<BCHistorialFacturaAdapter.ViewHolder>() {

    private var cursor: Cursor? = null

    fun setData(newCursor: Cursor?) {
        cursor = newCursor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBchistorialFacturaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor?.moveToPosition(position)
        holder.bind(cursor!!, onClick)
    }

    class ViewHolder(private val binding: ItemBchistorialFacturaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cursor: Cursor, onClick: (String) -> Unit) {
            val id = cursor.getString(0)
            val fechaEmision = cursor.getString(2)
            val fechaInicio = cursor.getString(3)
            val fechaFin = cursor.getString(4)


            val mesFormateado = obtenerMesYAnio(fechaFin)

            binding.tvMes.text = mesFormateado
            binding.tvFechas.text = "$fechaInicio - $fechaFin"
            binding.tvNumeroRecibo.text = id

            binding.btnVerFactura.setOnClickListener {
                onClick(id)
            }
        }

        private fun obtenerMesYAnio(fecha: String): String {
            return try {
                val formatoEntrada = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val date = formatoEntrada.parse(fecha)
                val formatoMes = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("es", "ES"))
                formatoMes.format(date).replaceFirstChar { it.uppercase() } // Ej: "Septiembre 2024"
            } catch (e: Exception) {
                fecha
            }
        }
    }
}