package com.example.proyectocurso

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class se_LecturaMedidoresListActivity : AppCompatActivity() {

    // ===== Variables globales =====
    private lateinit var dbHelper: miSQLiteHelper
    private lateinit var spinnerPredios: Spinner
    private lateinit var spinnerUnidades: Spinner
    private lateinit var layoutServicioLuz: LinearLayout
    private lateinit var iconoLuz: ImageView
    private lateinit var tvServicioLuz: TextView
    private lateinit var rvLecturas: RecyclerView
    private lateinit var layoutSinRegistros: LinearLayout
    private lateinit var adaptadorLecturas: se_LecturaItemRegistro
    private lateinit var btnNuevaLectura: TextView

    private var listaPredios = mutableListOf<Pair<Int, String>>()
    private var listaUnidades = mutableListOf<Pair<Int, String>>()
    private var predioIdSeleccionado = 0
    private var unidadIdSeleccionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.se_activity_lectura_medidor_list)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Lectura de Medidores - Luz"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // ==== Inicialización de vistas ====
        dbHelper = miSQLiteHelper(this)
        spinnerPredios = findViewById(R.id.spinnerPredios)
        spinnerUnidades = findViewById(R.id.spinnerUnidades)
        layoutServicioLuz = findViewById(R.id.layoutServicioLuz)
        iconoLuz = findViewById(R.id.iconoLuz)
        tvServicioLuz = findViewById(R.id.tvServicioLuz)
        rvLecturas = findViewById(R.id.rvLecturas)
        layoutSinRegistros = findViewById(R.id.layoutSinRegistros)
        btnNuevaLectura = findViewById(R.id.btnNuevaLectura)

        // ==== Cargar datos iniciales ====
        cargarPredios()
        deshabilitarNuevaLectura()

        // ==== Listeners ====
        spinnerPredios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    listaUnidades.clear()
                    unidadIdSeleccionado = 0
                    actualizarSpinnerUnidadesVacio()
                    deshabilitarNuevaLectura()
                    rvLecturas.adapter = null
                    rvLecturas.visibility = View.GONE
                    layoutSinRegistros.visibility = View.VISIBLE
                    return
                }

                val predioSeleccionado = listaPredios[position - 1]
                predioIdSeleccionado = predioSeleccionado.first

                // Cargar unidades del predio seleccionado
                cargarUnidades(predioIdSeleccionado)
                actualizarEstadoPredioSeleccionado()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerUnidades.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (listaUnidades.isEmpty() || position < 0) {
                    unidadIdSeleccionado = 0
                    deshabilitarNuevaLectura()
                    return
                }

                unidadIdSeleccionado = listaUnidades[position].first
                habilitarNuevaLectura()
                listarLecturas(unidadIdSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                unidadIdSeleccionado = 0
                deshabilitarNuevaLectura()
            }
        }

        // ==== Botón Nueva Lectura ====
        btnNuevaLectura.setOnClickListener {
            if (unidadIdSeleccionado == 0) {
                Toast.makeText(this, "Selecciona una unidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val intent = Intent(this, se_LecturaMedidorCrearActivity::class.java)
                intent.putExtra("unidadId", unidadIdSeleccionado)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "No se pudo abrir 'Registrar Lectura': ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (unidadIdSeleccionado != 0) {
            listarLecturas(unidadIdSeleccionado)
        }
    }

    // =========================
    // FUNCIONES AUXILIARES
    // =========================

    private fun cargarPredios() {
        listaPredios = dbHelper.obtenerPredios().toMutableList()
        val nombres = mutableListOf("--Seleccione--")
        nombres.addAll(listaPredios.map { it.second })

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPredios.adapter = adapter
    }

    private fun cargarUnidades(predioId: Int) {
        listaUnidades = dbHelper.obtenerUnidadesPorPredio(predioId).toMutableList()

        if (listaUnidades.isEmpty()) {
            unidadIdSeleccionado = 0
            actualizarSpinnerUnidadesVacio()
            deshabilitarNuevaLectura()
            Toast.makeText(this, "No hay unidades registradas para este predio", Toast.LENGTH_SHORT).show()
            rvLecturas.adapter = null
            rvLecturas.visibility = View.GONE
            layoutSinRegistros.visibility = View.VISIBLE
            return
        }

        val nombres = listaUnidades.map { it.second }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnidades.adapter = adapter

        unidadIdSeleccionado = 0
        deshabilitarNuevaLectura()
    }

    private fun listarLecturas(unidadId: Int) {
        val lecturasCrudas = dbHelper.obtenerLecturasPorUnidad(unidadId)

        val lecturas = lecturasCrudas.map {
            se_LecturaItem(
                id = it["id"] as Int,
                fecha = it["fecha"] as String,
                valor = it["valor"] as Double,
                notas = it["notas"] as? String ?: "Sin observaciones",
                tipoLectura = it["tipo_lectura"] as? String ?: "NORMAL",
                unidadNombre = "Depto $unidadId",
                predioNombre = "Predio Central",
                facturado = (it["facturado"] as Int) == 1,
                fotoRuta = it["fotoRuta"] as? String
            )
        }

        if (lecturas.isEmpty()) {
            rvLecturas.adapter = null
            rvLecturas.visibility = View.GONE
            layoutSinRegistros.visibility = View.VISIBLE
        } else {
            rvLecturas.visibility = View.VISIBLE
            layoutSinRegistros.visibility = View.GONE
            if (rvLecturas.layoutManager == null) {
                rvLecturas.layoutManager = LinearLayoutManager(this)
            }
            rvLecturas.adapter = se_LecturaItemRegistro(this, lecturas)
        }
    }

    private fun actualizarSpinnerUnidadesVacio() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("--Sin unidades--")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnidades.adapter = adapter

        unidadIdSeleccionado = 0
        deshabilitarNuevaLectura()
    }

    private fun actualizarEstadoPredioSeleccionado() {
        layoutServicioLuz.setBackgroundResource(R.drawable.lectura_card_activo)
        iconoLuz.setColorFilter(getColor(android.R.color.white))
        tvServicioLuz.setTextColor(getColor(android.R.color.white))
    }

    private fun deshabilitarNuevaLectura() {
        btnNuevaLectura.isEnabled = false
        btnNuevaLectura.alpha = 0.5f
    }

    private fun habilitarNuevaLectura() {
        btnNuevaLectura.isEnabled = true
        btnNuevaLectura.alpha = 1f
    }
}
