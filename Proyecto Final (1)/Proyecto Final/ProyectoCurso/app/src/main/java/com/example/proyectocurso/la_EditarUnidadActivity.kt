package com.example.proyectocurso

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.apply
import kotlin.text.isNullOrBlank
import kotlin.text.toDoubleOrNull
import kotlin.text.toIntOrNull
import kotlin.text.trim

class la_EditarUnidadActivity : AppCompatActivity() {

    private lateinit var db: miSQLiteHelper

    private var unidadId = -1
    private var submedidorId = -1
    private var inquilinoId = -1
    private var lecturaId = -1
    private var puedeEditar = false

    private lateinit var spClasificacion: Spinner
    private lateinit var etNombre: EditText
    private lateinit var etOcupantes: EditText
    private lateinit var spTipoDoc: Spinner
    private lateinit var etDocumento: EditText
    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etMedidor: EditText
    private lateinit var etLectura: EditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.la_activity_editar_unidad)
        // === Header ===
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Editar unidad"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        db = miSQLiteHelper(this)

        unidadId = intent.getIntExtra("UNIDAD_ID", -1)
        if (unidadId == -1) {
            Toast.makeText(this, "Error: no se recibió unidad", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        enlazarVistas()
        cargarDatosActualesCorregidos()
        configurarSpinners()
        configurarGuardar()
    }

    private fun enlazarVistas() {
        spClasificacion = findViewById(R.id.spClasificacionEdit)
        etNombre = findViewById(R.id.etNombreUnidadEdit)
        etOcupantes = findViewById(R.id.etCantidadOcupantesEdit)
        spTipoDoc = findViewById(R.id.spTipoDocumentoEdit)
        etDocumento = findViewById(R.id.etDocumentoEdit)
        etNombres = findViewById(R.id.etNombresOcupanteEdit)
        etApellidos = findViewById(R.id.etApellidosOcupanteEdit)
        etTelefono = findViewById(R.id.etTelefonoEdi)
        etCorreo = findViewById(R.id.etCorreoEdi)
        etMedidor = findViewById(R.id.etMedidorEdi)
        etLectura = findViewById(R.id.etLecturaInicialEdi)
        btnGuardar = findViewById(R.id.btnGuardarCambio)
    }

    private fun cargarDatosActualesCorregidos() {
        val cursorU = db.readableDatabase.rawQuery(
            "SELECT clasificacion, nombre, ocupantes FROM unidades WHERE unidad_id = ?",
            arrayOf(unidadId.toString())
        )
        if (cursorU.moveToFirst()) {
            val clas = cursorU.getString(0)
            val nom = cursorU.getString(1)
            val ocp = cursorU.getInt(2)
            etNombre.setText(nom)
            etOcupantes.setText(ocp.toString())
            spClasificacion.setSelection(
                when (clas) {
                    "HAB" -> 0; "DEP" -> 1; "MDE" -> 2; "DUP" -> 3; "LCO" -> 4; "COC" -> 5; else -> 0
                }
            )
        }
        cursorU.close()

        val inquilino = db.obtenerInquilinoDeUnidad(unidadId)
        if (inquilino != null) {
            inquilinoId = inquilino.first
            etNombres.setText(inquilino.second)
            etApellidos.setText(inquilino.third)
            spTipoDoc.setSelection(
                when (db.getTipoDocumentoInquilino(inquilinoId)) {
                    "DNI" -> 0; "RUC" -> 1; "CE" -> 2; "PAS" -> 3; else -> 0
                }
            )
            etDocumento.setText(db.getDocumentoInquilino(inquilinoId))
            etTelefono.setText(db.getTelefonoInquilino(inquilinoId))
            etCorreo.setText(db.getCorreoInquilino(inquilinoId))
        }

        val cursorS = db.readableDatabase.rawQuery(
            "SELECT submedidor_id, identificador FROM submedidores WHERE unidad_id = ? AND activo = 1 LIMIT 1",
            arrayOf(unidadId.toString())
        )
        if (cursorS.moveToFirst()) {
            submedidorId = cursorS.getInt(0)
            etMedidor.setText(cursorS.getString(1))

            val cursorL = db.readableDatabase.rawQuery(
                "SELECT lectura_id, valor FROM lecturas_sub WHERE submedidor_id = ? AND es_inicial = 1 LIMIT 1",
                arrayOf(submedidorId.toString())
            )
            if (cursorL.moveToFirst()) {
                lecturaId = cursorL.getInt(0)
                etLectura.setText(cursorL.getDouble(1).toString())
            }
            cursorL.close()
        }
        cursorS.close()

        puedeEditar = db.puedeEditarUnidad(unidadId)
        aplicarEstadoEdicion()
    }

    private fun configurarSpinners() {
        val clasifs = listOf("HAB", "DEP", "MDE", "DUP", "LCO", "COC")
        spClasificacion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clasifs)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val tipos = listOf("DNI", "RUC", "CE", "PAS")
        spTipoDoc.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun aplicarEstadoEdicion() {
        etLectura.isEnabled = puedeEditar
        etLectura.alpha = if (puedeEditar) 1f else 0.5f
        btnGuardar.isEnabled = true
    }

    private fun configurarGuardar() {
        btnGuardar.setOnClickListener {
            if (etNombre.text.isNullOrBlank() || etDocumento.text.isNullOrBlank() ||
                etNombres.text.isNullOrBlank() || etApellidos.text.isNullOrBlank() ||
                etMedidor.text.isNullOrBlank()
            ) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valuesUni = ContentValues().apply {
                put("clasificacion", spClasificacion.selectedItem.toString())
                put("nombre", etNombre.text.toString().trim())
                put("ocupantes", etOcupantes.text.toString().toIntOrNull() ?: 0)
            }
            db.writableDatabase.update(
                "unidades", valuesUni, "unidad_id = ?", arrayOf(unidadId.toString())
            )

            val inqIdCursor = db.readableDatabase.rawQuery(
                "SELECT inquilino_id FROM contratos WHERE unidad_id = ? AND estado = 'ACTIVO' LIMIT 1",
                arrayOf(unidadId.toString())
            )
            var inquilinoIdReal = -1
            if (inqIdCursor.moveToFirst()) {
                inquilinoIdReal = inqIdCursor.getInt(0)
            }
            inqIdCursor.close()

            if (inquilinoIdReal != -1) {
                val valuesInq = ContentValues().apply {
                    put("nombres", etNombres.text.toString().trim())
                    put("apellidos", etApellidos.text.toString().trim())
                    put("documento_tipo", spTipoDoc.selectedItem.toString())
                    put("documento_numero", etDocumento.text.toString().trim())
                    put("telefono", etTelefono.text.toString().trim())
                    put("correo", etCorreo.text.toString().trim())
                }
                db.writableDatabase.update(
                    "inquilinos",
                    valuesInq,
                    "inquilino_id = ?",
                    arrayOf(inquilinoIdReal.toString())
                )
            }

            val valuesSub = ContentValues().apply {
                put("identificador", etMedidor.text.toString().trim())
            }
            db.writableDatabase.update(
                "submedidores", valuesSub, "submedidor_id = ?", arrayOf(submedidorId.toString())
            )

            if (puedeEditar) {
                val valuesLec= ContentValues().apply {
                    put("valor", etLectura.text.toString().toDoubleOrNull() ?: 0.0)
                }
                db.writableDatabase.update(
                    "lecturas_sub", valuesLec, "lectura_id = ?", arrayOf(lecturaId.toString())
                )
            }
            val resultData = Intent().apply {
                putExtra("POSITION", intent.getIntExtra("POSITION", -1))
                putExtra("NOMBRE_UNIDAD", etNombre.text.toString().trim())
                putExtra("NOMBRES_INQUILINO", etNombres.text.toString().trim())
                putExtra("APELLIDOS_INQUILINO", etApellidos.text.toString().trim())
            }
            setResult(RESULT_OK, resultData)
            finish()
        }
    }
}