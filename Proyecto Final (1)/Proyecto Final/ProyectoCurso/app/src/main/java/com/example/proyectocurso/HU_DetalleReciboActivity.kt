package com.example.proyectocurso

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HU_DetalleReciboActivity : AppCompatActivity() {

    private lateinit var tvProveedor: TextView
    private lateinit var tvPeriodo: TextView
    private lateinit var tvFechaRegistro: TextView
    private lateinit var tvCodigoFactura: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hu_activity_detalle_recibo)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Detalle de registro"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // Referencias
        tvProveedor = findViewById(R.id.tv_proveedor)
        tvPeriodo = findViewById(R.id.tv_periodo)
        tvFechaRegistro = findViewById(R.id.tv_fecha_registro)
        tvCodigoFactura = findViewById(R.id.tv_codigo_factura)
        tvTotal = findViewById(R.id.tv_total)
        btnVolver = findViewById(R.id.btn_volver)

        // Obtener datos enviados desde la lista
        val proveedor = intent.getStringExtra("proveedor") ?: "Luz"
        val periodo = intent.getStringExtra("periodo") ?: ""
        val fechaRegistro = intent.getStringExtra("fechaRegistro") ?: ""
        val codigoFactura = intent.getStringExtra("codigoFactura") ?: "0000"
        val total = intent.getDoubleExtra("total", 0.0)

        // Mostrar datos
        tvProveedor.text = "Proveedor: $proveedor"
        val fechas = periodo.split(" - ")
        val nombreMes = if (fechas.isNotEmpty()) obtenerNombreMes(fechas[0]) else ""
        tvPeriodo.text = nombreMes
        tvFechaRegistro.text = fechaRegistro
        tvCodigoFactura.text = codigoFactura
        tvTotal.text = "S/. $total"

        // Botón Volver
        btnVolver.setOnClickListener { finish() }
    }

    private fun obtenerNombreMes(fecha: String): String {
        // fecha esperada: "2024-04-01"
        val partes = fecha.split("-")
        if (partes.size < 2) return ""
        val mes = partes[1].toIntOrNull() ?: return ""
        val nombresMes = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return nombresMes.getOrNull(mes - 1) ?: ""
    }

}
