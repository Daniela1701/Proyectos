package com.example.proyectocurso

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HU_RegistroReciboAdapter(private val lista: ArrayList<HashMap<String, Any>>) :
    RecyclerView.Adapter<HU_RegistroReciboAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMes: TextView = view.findViewById(R.id.tvMes)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val iconoMas: ImageView = view.findViewById(R.id.iconoMas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hu_item_lista_recibos, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        val fechaRegistro = item["fechaRegistro"].toString()

        holder.tvMes.text = obtenerNombreMes(fechaRegistro)
        holder.tvFechaHora.text = fechaRegistro.replace("-", "/")
        holder.tvTotal.text = "S/. ${item["total"]}"

        holder.iconoMas.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HU_DetalleReciboActivity::class.java)
            intent.putExtra("fechaRegistro", fechaRegistro)
            intent.putExtra("periodoInicio", item["periodoInicio"].toString())
            intent.putExtra("periodoFin", item["periodoFin"].toString())
            intent.putExtra("consumo", item["consumo"].toString())
            intent.putExtra("total", item["total"].toString())
            context.startActivity(intent)
        }
    }


    override fun getItemCount() = lista.size

    private fun obtenerNombreMes(fecha: String): String {
        // Ejemplo esperado: "2025-10-26" o similar
        val partes = fecha.split("-", "/")
        if (partes.size < 2) return ""

        val anio = partes[0]
        val mes = partes[1].toIntOrNull() ?: return ""

        val nombresMes = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val nombreMes = nombresMes.getOrNull(mes - 1) ?: ""
        return "$nombreMes $anio"
    }
}
