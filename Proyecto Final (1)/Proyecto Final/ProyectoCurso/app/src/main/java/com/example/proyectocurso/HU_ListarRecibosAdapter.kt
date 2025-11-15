package com.example.proyectocurso

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class HU_ListarRecibosAdapter(private val listaRecibos: List<Map<String, Any>>) :
    RecyclerView.Adapter<HU_ListarRecibosAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMes: TextView = itemView.findViewById(R.id.tvMes)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val iconoMas: ImageView = itemView.findViewById(R.id.iconoMas) // <-- agregar

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hu_item_lista_recibos, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaRecibos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recibo = listaRecibos[position]
        val periodoFin = recibo["periodo_fin"] as String
        holder.tvMes.text = obtenerNombreMes(periodoFin)
        holder.tvTotal.text = "S/. ${(recibo["total"] as Double)}"
        holder.tvFechaHora.text = recibo["fecha_registro"] as String

        holder.iconoMas.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, HU_DetalleReciboActivity::class.java)

            // Enviar datos necesarios
            intent.putExtra("proveedor", recibo["proveedor"] as String)
            intent.putExtra(
                "periodo",
                "${recibo["periodo_inicio"]} - ${recibo["periodo_fin"]}"
            )
            intent.putExtra("fechaRegistro", recibo["fecha_registro"] as String)
            val idRecibo = (recibo["id_recibo"] as? Int) ?: 0
            val codigoFacturaFormateado = "FAC-" + idRecibo.toString().padStart(4, '0')
            intent.putExtra("codigoFactura", codigoFacturaFormateado)
            intent.putExtra("total", recibo["total"] as Double)

            context.startActivity(intent)
        }
    }
    private fun obtenerNombreMes(periodoFin: String): String {
        // Ejemplo de periodoInicio: "2024-04-01"
        val partes = periodoFin.split("-")
        if (partes.size < 2) return ""

        val mes = partes[1].toIntOrNull() ?: return ""
        val nombresMes = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return nombresMes.getOrNull(mes - 1) ?: ""
    }


}
