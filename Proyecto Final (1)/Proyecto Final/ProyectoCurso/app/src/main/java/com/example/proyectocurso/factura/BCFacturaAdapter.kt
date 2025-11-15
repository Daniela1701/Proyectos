package com.example.proyectocurso.factura

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.BCHistorialFacturaPDFGenerator
import com.example.proyectocurso.R
import com.example.proyectocurso.factura.BCFacturaDetalle

class BCFacturaAdapter(
    private val lista: ArrayList<HashMap<String, Any?>>,
    private val context: Context
) : RecyclerView.Adapter<BCFacturaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNumeroUnidad: TextView = itemView.findViewById(R.id.tvNumeroUnidad)
        val tvUnidad: TextView = itemView.findViewById(R.id.tvUnidad)
        val tvRecibo: TextView = itemView.findViewById(R.id.tvRecibo)
        val tvCodigoRecibo: TextView = itemView.findViewById(R.id.tvCodigoRecibo)
        val btnVer: ImageView = itemView.findViewById(R.id.btnVer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_bcfactura, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        val unidadNombre = item["unidad_nombre"]?.toString() ?: ""
        val reciboId = item["recibo_int_id"]?.toString() ?: ""
        val montoTotal = item["monto_total"]?.toString() ?: ""
        val unidadId = item["unidad_id"]?.toString() ?: ""

        holder.tvNumeroUnidad.text = unidadId
        holder.tvUnidad.text = "Unidad $unidadNombre"
        holder.tvRecibo.text = "Recibo"
        holder.tvCodigoRecibo.text = "ID: $reciboId"

        // Botón ver detalle
        holder.btnVer.setOnClickListener {
            val intent = Intent(context, BCFacturaDetalle::class.java)
            intent.putExtra("RECIBO_ID", reciboId)
            context.startActivity(intent)
        }
    }
}