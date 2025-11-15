package com.example.proyectocurso

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CM_EditarPredio : AppCompatActivity() {
    private lateinit var miSQLiteHelper: miSQLiteHelper
    private lateinit var etClave: EditText
    private lateinit var etNombre: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etComentarios: EditText
    private lateinit var spinnerDepartamento: Spinner
    private lateinit var spinnerProvincia: Spinner
    private lateinit var spinnerDistrito: Spinner
    private lateinit var btnGuardar: Button

    private var predioId: Int = 2

    private val departamentos = mutableListOf<Pair<Int, String>>()
    private val provincias = mutableListOf<Pair<Int, String>>()
    private val distritos = mutableListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cm_activity_editar_predio)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Editar predio"
        BaseActivity().setupMenu(this, header)


        // Inicializar helper y vistas
        miSQLiteHelper = miSQLiteHelper(this)
        etClave = findViewById(R.id.clavePredio)
        etNombre = findViewById(R.id.nombrePredio)
        etDireccion = findViewById(R.id.direccionPredio)
        etComentarios = findViewById(R.id.comentarios)
        spinnerDepartamento = findViewById(R.id.spinnerDepartamento)
        spinnerProvincia = findViewById(R.id.spinnerProvincia)
        spinnerDistrito = findViewById(R.id.spinnerDistrito)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Obtener el ID del predio que se va a editar
        predioId = intent.getIntExtra("PREDIO_ID", -1)

        if (predioId != -1) {
            cargarDepartamentos()
            cargarPredio(predioId)
        } else {
            Toast.makeText(this, "Error: ID de predio no recibido", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnGuardar.setOnClickListener { actualizarPredio() }
    }

    // === Cargar datos del predio y seleccionar los combos ===
    private fun cargarPredio(id: Int) {
        val cursor = miSQLiteHelper.BCbuscarPredioPorId(id)
        if (cursor != null && cursor.moveToFirst()) {
            val clave = cursor.getString(cursor.getColumnIndexOrThrow("clave"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion"))
            val notas = cursor.getString(cursor.getColumnIndexOrThrow("notas"))
            val distritoId = cursor.getInt(cursor.getColumnIndexOrThrow("distrito_id"))
            val provinciaId = cursor.getInt(cursor.getColumnIndexOrThrow("provincia_id"))
            val departamentoId = cursor.getInt(cursor.getColumnIndexOrThrow("departamento_id"))

            etClave.setText(clave)
            etNombre.setText(nombre)
            etDireccion.setText(direccion)
            etComentarios.setText(notas)

            // Seleccionar los valores correctos en los spinners
            seleccionarDepartamento(departamentoId)
            cargarProvincias(departamentoId) {
                seleccionarProvincia(provinciaId)
                cargarDistritos(provinciaId) {
                    seleccionarDistrito(distritoId)
                }
            }
        }
        cursor?.close()
    }

    // === Cargar departamentos ===
    private fun cargarDepartamentos() {
        departamentos.clear()
        val cursor = miSQLiteHelper.BClistarDepartamentos()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("departamento_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                departamentos.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departamentos.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartamento.adapter = adapter

        spinnerDepartamento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val departamentoId = departamentos[position].first
                cargarProvincias(departamentoId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // === Cargar provincias ===
    private fun cargarProvincias(departamentoId: Int, callback: (() -> Unit)? = null) {
        provincias.clear()
        val cursor = miSQLiteHelper.BCbuscarProvinciasPorDepartamento(departamentoId)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("provincia_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                provincias.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provincias.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvincia.adapter = adapter

        spinnerProvincia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val provinciaId = provincias[position].first
                cargarDistritos(provinciaId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        callback?.invoke()
    }

    // === Cargar distritos ===
    private fun cargarDistritos(provinciaId: Int, callback: (() -> Unit)? = null) {
        distritos.clear()
        val cursor = miSQLiteHelper.BCbuscarDistritosPorProvincia(provinciaId)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("distrito_id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                distritos.add(Pair(id, nombre))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distritos.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrito.adapter = adapter

        callback?.invoke()
    }

    // === Actualizar predio ===
    private fun actualizarPredio() {
        val nombre = etNombre.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()
        val notas = etComentarios.text.toString().trim()
        val distritoId = if (spinnerDistrito.selectedItemPosition >= 0)
            distritos[spinnerDistrito.selectedItemPosition].first else -1

        if (nombre.isEmpty() || direccion.isEmpty() || distritoId == -1) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val exito = miSQLiteHelper.BCeditarPredio(predioId, nombre, direccion, distritoId, notas)
        if (exito) {
            Toast.makeText(this, "Predio actualizado correctamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al actualizar el predio", Toast.LENGTH_SHORT).show()
        }
    }

    // === Métodos para seleccionar en los Spinners ===
    private fun seleccionarDepartamento(id: Int) {
        val index = departamentos.indexOfFirst { it.first == id }
        if (index != -1) spinnerDepartamento.setSelection(index)
    }

    private fun seleccionarProvincia(id: Int) {
        val index = provincias.indexOfFirst { it.first == id }
        if (index != -1) spinnerProvincia.setSelection(index)
    }

    private fun seleccionarDistrito(id: Int) {
        val index = distritos.indexOfFirst { it.first == id }
        if (index != -1) spinnerDistrito.setSelection(index)
    }

}