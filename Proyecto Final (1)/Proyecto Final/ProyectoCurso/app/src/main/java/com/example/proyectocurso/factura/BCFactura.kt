package com.example.proyectocurso.factura

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectocurso.BaseActivity
import com.example.proyectocurso.factura.BCFacturaAdapter
import com.example.proyectocurso.R
import com.example.proyectocurso.databinding.ActivityBcfacturaBinding
import com.example.proyectocurso.miSQLiteHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BCFactura : AppCompatActivity() {

    private lateinit var binding: ActivityBcfacturaBinding
    private lateinit var dbHelper: miSQLiteHelper
    private lateinit var adapter: BCFacturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBcfacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === Configurar encabezado ===
        // === HEADER ===
        binding.header.tvTitulo.text = "BC Facturas"
        BaseActivity().setupMenu(this, binding.header.root)

        // === FOOTER ===
        BaseActivity().setupFooter(this, binding.footer.root)

        dbHelper = miSQLiteHelper(this)

        val predioId = intent.getIntExtra("predio_id", 1)
        val yearMonth = intent.getStringExtra("year_month") ?: SimpleDateFormat(
            "yyyy-MM",
            Locale.getDefault()
        ).format(Date())

        val lista = dbHelper.BClistarRecibosInternosPorPredioYMes(predioId, yearMonth)

        adapter = BCFacturaAdapter(lista, this)
        binding.recyclerFacturas.layoutManager = LinearLayoutManager(this)
        binding.recyclerFacturas.adapter = adapter
    }

}