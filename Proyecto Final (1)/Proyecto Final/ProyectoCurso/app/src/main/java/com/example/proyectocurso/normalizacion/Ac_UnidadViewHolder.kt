package com.example.proyectocurso.normalizacion

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.R

class Ac_UnidadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val unitHeader: LinearLayout = itemView.findViewById(R.id.unitHeader)
    val tvNombreUnidad: TextView = itemView.findViewById(R.id.tvNombreUnidad)
    val tvEstadoFecha: TextView = itemView.findViewById(R.id.tvEstadoFecha)
    val viewEstadoFecha: View = itemView.findViewById(R.id.viewEstadoFecha)
    val tvEstadoUnidad: TextView = itemView.findViewById(R.id.tvEstadoUnidad)
    val viewEstadoUnidad: View = itemView.findViewById(R.id.viewEstadoUnidad)
    val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)
    val unitContent: LinearLayout = itemView.findViewById(R.id.unitContent)
    val controlesFecha: LinearLayout = itemView.findViewById(R.id.controlesFecha)
    val tvFechaInicial: TextView = itemView.findViewById(R.id.tvFechaInicial)
    val spinnerFechaFinal: Spinner = itemView.findViewById(R.id.spinnerFechaFinal)
    val btnGuardarFecha: Button = itemView.findViewById(R.id.btnGuardarFecha)
    val btnGuardarYNormalizar: Button = itemView.findViewById(R.id.btnGuardarYNormalizar)
    val layoutBotonNormalizar: LinearLayout = itemView.findViewById(R.id.layoutBotonNormalizar)
    val btnNormalizar: Button = itemView.findViewById(R.id.btnNormalizar)
    val detalleContainer: LinearLayout = itemView.findViewById(R.id.detalleContainer)
    val tvConsumoMedido: TextView = itemView.findViewById(R.id.tvConsumoMedido)
    val tvDiasMedidos: TextView = itemView.findViewById(R.id.tvDiasMedidos)
    val tvConsumoDiario: TextView = itemView.findViewById(R.id.tvConsumoDiario)
    val tvNormalizado: TextView = itemView.findViewById(R.id.tvNormalizado)
}