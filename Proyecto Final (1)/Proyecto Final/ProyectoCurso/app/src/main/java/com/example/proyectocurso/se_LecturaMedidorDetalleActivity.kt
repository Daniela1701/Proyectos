package com.example.proyectocurso

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class se_LecturaMedidorDetalleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.se_activity_lectura_medidor_detalle)


        val valor = intent.getDoubleExtra("valor", 0.0)
        val fecha = intent.getStringExtra("fecha") ?: "-"
        val notas = intent.getStringExtra("notas") ?: "Sin observaciones"
        val unidad = intent.getStringExtra("unidad") ?: "-"
        val predio = intent.getStringExtra("predio") ?: "-"
        val tipoLectura = intent.getStringExtra("tipoLectura") ?: "NORMAL"
        val fotoRuta = intent.getStringExtra("fotoRuta")

        // 🔹 Configurar encabezado
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Detalle de Lectura"
        BaseActivity().setupMenu(this, header)
        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        findViewById<TextView>(R.id.tvTituloDetalle).text = "Lectura del $fecha"
        findViewById<TextView>(R.id.tvValorDetalle).text = "Valor: $valor kWh"
        findViewById<TextView>(R.id.tvNotasDetalle).text = "Notas: $notas"
        findViewById<TextView>(R.id.tvUnidadDetalle).text = "Unidad: $unidad"
        findViewById<TextView>(R.id.tvPredioDetalle).text = "Predio: $predio"
        findViewById<TextView>(R.id.tvTipoLecturaDetalle).text = "Tipo de lectura: $tipoLectura"


        val imgLectura = findViewById<ImageView>(R.id.imgLecturaFoto)

        if (!fotoRuta.isNullOrEmpty()) {
            val archivo = File(fotoRuta)
            if (archivo.exists()) {
                val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
                imgLectura.setImageBitmap(bitmap)
                imgLectura.visibility = View.VISIBLE
            } else {
                imgLectura.visibility = View.GONE
                Log.e("LecturaDetalle", "⚠️ Archivo no encontrado en: $fotoRuta")
            }
        } else {
            imgLectura.visibility = View.GONE
            Log.d("LecturaDetalle", "Sin imagen asociada para esta lectura.")
        }

        findViewById<Button>(R.id.btnVolverDetalle).setOnClickListener {
            finish()
        }
        // 🎨 Mantener ícono siempre blanco
        val icono = findViewById<ImageView>(R.id.icTipoLectura)
        icono.imageTintList = null
        icono.setColorFilter(Color.WHITE)
    }
}
