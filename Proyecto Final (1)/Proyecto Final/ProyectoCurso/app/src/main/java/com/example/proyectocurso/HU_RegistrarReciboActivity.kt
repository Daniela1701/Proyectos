package com.example.proyectocurso

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class HU_RegistrarReciboActivity : AppCompatActivity() {
    private lateinit var etFechaRegistro: TextInputEditText
    private lateinit var etFechaInicio: TextInputEditText
    private lateinit var etFechaFin: TextInputEditText
    private lateinit var etConsumo: TextInputEditText
    private lateinit var etTotal: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var dbHelper: miSQLiteHelper
    private var predioId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hu_activity_registrar_recibo)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Nuevo registro"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        predioId = intent.getIntExtra("predioId", 1)

        etFechaRegistro = findViewById(R.id.et_fecha_registro)
        etFechaInicio = findViewById(R.id.et_fecha_inicio)
        etFechaFin = findViewById(R.id.et_fecha_fin)
        etConsumo = findViewById(R.id.et_consumo)
        etTotal = findViewById(R.id.et_total)
        btnGuardar = findViewById(R.id.btn_guardar)

        dbHelper = miSQLiteHelper(this)

        // Fecha de registro por defecto
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etFechaRegistro.setText(hoy)

        // Abrir DatePicker al tocar el campo
        abrirDatePicker(etFechaRegistro)
        abrirDatePicker(etFechaInicio)
        abrirDatePicker(etFechaFin)

        // Botón Guardar
        btnGuardar.setOnClickListener {
            val fechaRegistro = etFechaRegistro.text.toString()
            val periodoInicio = etFechaInicio.text.toString()
            val periodoFin = etFechaFin.text.toString()
            val consumo = etConsumo.text.toString().toDoubleOrNull() ?: 0.0
            val total = etTotal.text.toString().toDoubleOrNull() ?: 0.0

            if (periodoInicio.isBlank() || periodoFin.isBlank() || consumo <= 0 || total <= 0) {
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idRecibo = dbHelper.insertarRecibo(
                predioId, fechaRegistro, periodoInicio, periodoFin, consumo, total
            )

            if (idRecibo == -2L) {
                Toast.makeText(this, "Ya existe un recibo registrado en este mes para este predio", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //no insertar 2 recibos en el mismo mes
            if (idRecibo != -1L) {
                Toast.makeText(this, "Recibo guardado correctamente", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)// se refresca el activity anterior
                finish() // vuelve a la lista de recibos
            } else {
                Toast.makeText(this, "Error al guardar el recibo", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun abrirDatePicker(campo: TextInputEditText) {
        campo.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, y, m, d ->
                val mes = (m + 1).toString().padStart(2, '0')
                val dia = d.toString().padStart(2, '0')
                campo.setText("$y-$mes-$dia")
            }, year, month, day)
            dpd.show()
        }
    }
}
