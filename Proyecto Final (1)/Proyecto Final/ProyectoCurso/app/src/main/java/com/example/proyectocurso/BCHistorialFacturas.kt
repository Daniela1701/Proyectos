package com.example.proyectocurso

import android.content.Intent
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
import com.example.proyectocurso.databinding.ActivityBchistorialFacturasBinding
import com.example.proyectocurso.miSQLiteHelper
import kotlin.jvm.java
import kotlin.let


class BCHistorialFacturas : AppCompatActivity() {
    private lateinit var binding: ActivityBchistorialFacturasBinding
    private lateinit var miSQLiteHelper: miSQLiteHelper
    private lateinit var adapter: BCHistorialFacturaAdapter

    private var unidadId = 1 // puedes cambiarlo según el spinner
    private var currentPage = 0
    private val pageSize = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBchistorialFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === HEADER ===
        binding.header.tvTitulo.text = "Historial de Facturas"
        BaseActivity().setupMenu(this, binding.header.root)

        // === FOOTER ===
        BaseActivity().setupFooter(this, binding.footer.root)

        // 🔵 Marcar el botón activo (Facturas)
        binding.footer.iconFacturas.setColorFilter(Color.parseColor("#1976D2"))
        binding.footer.txtFacturas.setTextColor(Color.parseColor("#1976D2"))

        miSQLiteHelper = miSQLiteHelper(this)
        adapter = BCHistorialFacturaAdapter { reciboId ->
            val intent = Intent(this, BCHistorialDetalleFactura::class.java)
            intent.putExtra("RECIBO_ID", reciboId)
            startActivity(intent)
        }

        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = adapter

        binding.btnPrev.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                cargarRecibos()
            }
        }

        binding.btnNext.setOnClickListener {
            currentPage++
            cargarRecibos()
        }

        cargarUnidadesEnSpinner()
    }

    private fun cargarRecibos() {
        val offset = currentPage * pageSize
        val cursor: Cursor? = miSQLiteHelper.BClistarRecibosPorUnidad(unidadId, pageSize, offset)
        adapter.setData(cursor)
    }

    private fun mostrarTotal() {
        val total = miSQLiteHelper.BCcontarRecibosPorUnidad(unidadId)
        binding.tvTotalFacturas.text = total.toString()
    }

    private fun cargarUnidadesEnSpinner() {
        val cursor = miSQLiteHelper.BClistarUnidades()
        val listaUnidades = mutableListOf<String>()
        val listaIds = mutableListOf<Int>()

        cursor?.let {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("unidad_id"))
                val nombreCompleto = it.getString(it.getColumnIndexOrThrow("nombre_completo"))
                listaIds.add(id)
                listaUnidades.add(nombreCompleto)
            }
            it.close()
        }

        val adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaUnidades
        )
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPredio.adapter = adapterSpinner

        binding.spPredio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                unidadId = listaIds[position]
                currentPage = 0
                cargarRecibos()
                mostrarTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}