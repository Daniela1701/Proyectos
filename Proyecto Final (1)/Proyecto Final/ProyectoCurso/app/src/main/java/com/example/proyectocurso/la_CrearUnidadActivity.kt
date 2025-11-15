package com.example.proyectocurso

import android.content.ContentValues
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.text.isEmpty
import kotlin.text.toDoubleOrNull
import kotlin.text.toIntOrNull
import kotlin.text.trim

class la_CrearUnidadActivity : AppCompatActivity() {

    private lateinit var db: miSQLiteHelper

    // Spinners
    private lateinit var spClasificacion: Spinner
    private lateinit var spTipoDocumento: Spinner

    // IDs externos que recibimos
    private var predioId = -1
    private var predioNombre = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.la_activity_crear_unidad)

        db = miSQLiteHelper(this)

        // === Header ===
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Crear unidad"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // Recibimos datos del predio activo
        predioId = intent.getIntExtra("PREDIO_ID", -1)
        predioNombre = intent.getStringExtra("PREDIO_NOMBRE") ?: ""

        // Vistas
        spClasificacion = findViewById(R.id.spClasificacion)
        spTipoDocumento = findViewById(R.id.spTipoDocumento)

        // Título dinámico (opcional)
        title = "Crear Unidad - $predioNombre"

        // Cargar spinners estáticos
        cargarClasificaciones()
        cargarTiposDocumento()

        // Botón Guardar
        findViewById<Button>(R.id.btnGuardarUnidad).setOnClickListener { guardarUnidad() }
    }

    /* ========================================================= */

    private fun cargarClasificaciones() {
        val lista = listOf("HAB", "DEP", "MDE", "DUP", "LCO", "COC")
        spClasificacion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun cargarTiposDocumento() {
        val lista = listOf("DNI", "RUC", "CE", "PAS")
        spTipoDocumento.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    /* ========================================================= */

    private fun guardarUnidad() {
        val clasificacion = spClasificacion.selectedItem.toString()
        val nombreUnidad = findViewById<EditText>(R.id.etNombreUnidad).text.toString().trim()
        val cantidadOcupantes = findViewById<EditText>(R.id.etCantidadOcupantes).text.toString().toIntOrNull() ?: 0
        val tipoDoc = spTipoDocumento.selectedItem.toString()
        val documento = findViewById<EditText>(R.id.etDocumento).text.toString().trim()
        val nombres = findViewById<EditText>(R.id.etNombresOcupante).text.toString().trim()
        val apellidos = findViewById<EditText>(R.id.etApellidosOcupante).text.toString().trim()
        val telefono = findViewById<EditText>(R.id.etTelefono).text.toString().trim()
        val correo = findViewById<EditText>(R.id.etCorreo).text.toString().trim()
        val medidor = findViewById<EditText>(R.id.etMedidor).text.toString().trim()
        val lecturaInicial = findViewById<EditText>(R.id.etLecturaInicial).text.toString().toDoubleOrNull() ?: 0.0

        if (nombreUnidad.isEmpty() || documento.isEmpty() || nombres.isEmpty() || apellidos.isEmpty() || medidor.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val db = db.writableDatabase

        try {
            db.beginTransaction()

            // 1. Insertar inquilino
            val valuesInq = ContentValues().apply {
                put("nombres", nombres)
                put("apellidos", apellidos)
                put("documento_tipo", tipoDoc)
                put("documento_numero", documento)
                put("telefono", telefono)
                put("correo", correo)
            }
            val inquilinoId = db.insert("inquilinos", null, valuesInq).toInt()

            // 2. Insertar unidad
            val valuesUni = ContentValues().apply {
                put("predio_id", predioId)
                put("clasificacion", clasificacion)
                put("nombre", nombreUnidad)
                put("ocupantes", cantidadOcupantes)
                put("activo", 1)
            }
            val unidadId = db.insert("unidades", null, valuesUni).toInt()

            // 3. Insertar submedidor
            val valuesSub = ContentValues().apply {
                put("unidad_id", unidadId)
                put("identificador", medidor)
                put("activo", 1)
            }
            val submedidorId = db.insert("submedidores", null, valuesSub).toInt()

            // 4. Insertar lectura inicial y CAPTURAR EL ID
            val valuesLec = ContentValues().apply {
                put("submedidor_id", submedidorId)
                put("valor", lecturaInicial)
                put("es_inicial", 1)
                put("fecha", getCurrentDate())
                put("tipo_lectura", "INICIAL")
                put("estimacion", "EXACTO")
            }
            val lecturaInicialId = db.insert("lecturas_sub", null, valuesLec).toInt()

            // 5. Insertar contrato USANDO EL ID DE LA LECTURA INICIAL
            val valuesCont = ContentValues().apply {
                put("unidad_id", unidadId)
                put("inquilino_id", inquilinoId)
                put("fecha_inicio", getCurrentDate())
                put("lectura_id", lecturaInicialId) // ¡AQUÍ ESTÁ LA CORRECCIÓN!
                put("estado", "ACTIVO")
            }
            db.insert("contratos", null, valuesCont)

            db.setTransactionSuccessful()
            Toast.makeText(this, "Unidad creada con éxito", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)

        } catch (e: Exception) {
            Toast.makeText(this, "Error al crear la unidad: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            db.endTransaction()
            db.close()
        }

        finish()
    }

    fun getCurrentDate(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalDate.now().toString()
        } else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(Date())
        }
    }
}