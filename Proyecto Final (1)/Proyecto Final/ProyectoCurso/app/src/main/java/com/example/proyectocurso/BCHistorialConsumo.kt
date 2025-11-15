package com.example.proyectocurso

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectocurso.databinding.ActivityBchistorialConsumoBinding
import com.example.proyectocurso.miSQLiteHelper
import kotlin.io.use
import kotlin.text.format


class BCHistorialConsumo : AppCompatActivity() {
    private lateinit var binding: ActivityBchistorialConsumoBinding
    private lateinit var db: miSQLiteHelper
    private lateinit var adapter: BCHistorialConsumoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBchistorialConsumoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === HEADER ===
        binding.header.tvTitulo.text = "Historial de Consumo"
        BaseActivity().setupMenu(this, binding.header.root)
        // === FOOTER ===
        BaseActivity().setupFooter(this, binding.footer.root)

        binding.footer.iconConsumos.setColorFilter(Color.parseColor("#1976D2"))
        binding.footer.txtConsumos.setTextColor(Color.parseColor("#1976D2"))

        db = miSQLiteHelper(this)

        binding.recyclerHistorial.layoutManager = LinearLayoutManager(this)
        adapter = BCHistorialConsumoAdapter(kotlin.collections.ArrayList())
        binding.recyclerHistorial.adapter = adapter

        cargarUnidades()
        configurarSpinner()
    }
    private fun cargarUnidades() {
        val cursor: Cursor? = db.BClistarUnidades()
        val listaUnidades = kotlin.collections.ArrayList<String>()

        cursor?.use {
            while (it.moveToNext()) {
                val nombreCompleto = it.getString(it.getColumnIndexOrThrow("nombre_completo"))
                listaUnidades.add(nombreCompleto)
            }
        }
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaUnidades)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnidad.adapter = adaptador
    }
    private fun configurarSpinner() {
        binding.spinnerUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val nombreCompleto = binding.spinnerUnidad.selectedItem.toString()

                val nombreUnidad = if (nombreCompleto.contains(" - ")) {
                    nombreCompleto.split(" - ")[1]
                } else {
                    nombreCompleto
                }

                val listaConsumo = db.BCobtenerHistorialConsumo(nombreUnidad)
                adapter.actualizarLista(listaConsumo)

                val promedios = db.BCcalcularPromedios(listaConsumo)
                binding.textView2.text = String.format("%.1f", promedios["promedioMensual"])
                binding.textView6.text = "S/. " + String.format("%.2f", promedios["costoPromedio"])
                binding.textView4.text = String.format("%.1f", promedios["discrepancia"])
                binding.textView8.text = String.format("%.1f%%", promedios["tendencia"])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}