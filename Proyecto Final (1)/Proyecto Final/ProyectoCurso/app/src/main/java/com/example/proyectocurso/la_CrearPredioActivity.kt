package com.example.proyectocurso

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.apply
import kotlin.collections.map
import kotlin.text.isEmpty
import kotlin.text.trim

class la_CrearPredioActivity : AppCompatActivity() {

    private lateinit var db: miSQLiteHelper

    // Spinners
    private lateinit var spDep: Spinner
    private lateinit var spProv: Spinner
    private lateinit var spDist: Spinner

    // Datos seleccionados
    private var deptoSeleccionado = 0
    private var provSeleccionada = 0
    private var distSeleccionado = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.la_activity_crear_predio)

        db = miSQLiteHelper(this)

        // === Header ===
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Crear Predio"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        db = miSQLiteHelper(this)

        // Vistas
        spDep = findViewById(R.id.spDepartamento)
        spProv = findViewById(R.id.spProvincia)
        spDist = findViewById(R.id.spDistrito)

        // Carga inicial
        cargarDepartamentos()

        // Guardar
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardarPredio() }
    }

    /* ========================================================= */

    private fun cargarDepartamentos() {
        val lista = db.obtenerDepartamentos()
        spDep.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista.map { it.second })
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                deptoSeleccionado = lista[pos].first
                cargarProvincias(deptoSeleccionado)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun cargarProvincias(deptoId: Int) {
        val lista = db.obtenerProvincias(deptoId)
        spProv.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista.map { it.second })
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spProv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                provSeleccionada = lista[pos].first
                cargarDistritos(provSeleccionada)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun cargarDistritos(provId: Int) {
        val lista = db.obtenerDistritos(provId)
        spDist.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista.map { it.second })
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spDist.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                distSeleccionado = lista[pos].first
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    /* ========================================================= */

    private fun guardarPredio() {
        val clave   = findViewById<EditText>(R.id.etClave).text.toString().trim()
        val nombre  = findViewById<EditText>(R.id.etNombre).text.toString().trim()
        val direc   = findViewById<EditText>(R.id.etDireccion).text.toString().trim()
        val notas   = findViewById<EditText>(R.id.etComentarios).text.toString().trim()

        if (clave.isEmpty() || nombre.isEmpty() || direc.isEmpty() || distSeleccionado == 0) {
            Toast.makeText(this, "Completa todos los campos y selecciona ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        // Insertar y obtener ID del nuevo predio
        val values = ContentValues().apply {
            put("clave", clave)
            put("nombre", nombre)
            put("distrito_id", distSeleccionado)
            put("direccion", direc)
            put("notas", notas)
        }
        val newId = db.writableDatabase.insert("predios", null, values).toInt()

        // Devolver ID a la pantalla anterior
        val resultData = Intent().apply { putExtra("NUEVO_PREDIO_ID", newId) }
        setResult(RESULT_OK, resultData)
        finish()
    }
}