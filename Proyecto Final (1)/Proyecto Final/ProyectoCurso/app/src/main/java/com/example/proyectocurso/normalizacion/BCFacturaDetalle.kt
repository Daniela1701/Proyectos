package com.example.proyectocurso.normalizacion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectocurso.factura.BCFactura
import com.example.proyectocurso.BCHistorialFacturaPDFGenerator
import com.example.proyectocurso.databinding.ActivityBcfacturaDetalleBinding
import com.example.proyectocurso.miSQLiteHelper

class BCFacturaDetalle : AppCompatActivity() {

    private lateinit var binding: ActivityBcfacturaDetalleBinding
    private lateinit var miSQLiteHelper: miSQLiteHelper

    companion object {
        private const val REQUEST_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBcfacturaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        miSQLiteHelper = miSQLiteHelper(this)

        val reciboId = intent.getStringExtra("RECIBO_ID")

        if (reciboId != null) {
            cargarDatosFactura(reciboId)

            binding.btnGenerarPdf.setOnClickListener {
                verificarPermisosYGenerarPDF(reciboId)
            }


            binding.btnRetornar.setOnClickListener {
                val intent = Intent(this, BCFactura::class.java)
                startActivity(intent)
                finish() // cierra esta activity
            }

        } else {
            Toast.makeText(this, "No se recibió el ID del recibo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosFactura(reciboId: String) {
        val cursor: Cursor? = miSQLiteHelper.BCobtenerReciboPorId(reciboId)
        cursor?.use {
            if (it.moveToFirst()) {
                binding.txtDireccion.text = it.getString(0)
                binding.txtTelefono.text = "Fono: ${it.getString(1)}"
                binding.txtNombre.text = "NOMBRE: ${it.getString(2)}"
                binding.txtRecibo.text = "RECIBO N° ${it.getString(3)}"
                binding.txtUnidad.text = "UNIDAD ${it.getString(4)}"
                binding.Mes.text = "MES CALC.: ${it.getString(5)}"
                binding.txtFechaEmision.text = "FECHA DE EMISIÓN: ${it.getString(8)}"
                binding.txtDepartamento.text = "DEPARTAMENTO: ${it.getString(9)}"
                binding.txtMedidor.text = "MEDIDOR: ${it.getString(10)}"
                binding.txtLecturaAnterior.text = "LECTURA ANTERIOR: ${it.getDouble(11)}"
                binding.txtLecturaPosterior.text = "LECTURA ACTUAL: ${it.getDouble(12)}"
                binding.txtFechaInicio.text = it.getString(13)
                binding.txtFechaFin.text = it.getString(14)
                binding.txtCostokWh.text = "PRECIO UNIT kWh: S/ ${it.getDouble(15)}"
                binding.txtDiferenciaLecturas.text = "CONSUMO: ${it.getDouble(16)} kWh"
                binding.txtMontoTotal.text = "S/${it.getDouble(17)}"
                binding.MontoTotal.text = String.format("%.2f", it.getDouble(17))
                binding.MontoTexto.text = convertirMontoATexto(it.getDouble(17))
            }
        }
    }

    private fun verificarPermisosYGenerarPDF(reciboId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION
                )
            } else {
                generarPDFVisual(reciboId)
            }
        } else {
            generarPDFVisual(reciboId)
        }
    }

    private fun generarPDFVisual(reciboId: String) {
        val pdfGenerator = BCHistorialFacturaPDFGenerator()
        val rutaPdf = pdfGenerator.generarPDFDesdeVista(
            context = this,
            view = binding.root,
            nombreArchivo = "Recibo_$reciboId.pdf"
        )

        if (rutaPdf != null) {
            Toast.makeText(this, "✅ PDF guardado en Descargas", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "❌ Error al generar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertirMontoATexto(monto: Double): String {
        val partes = String.format("%.2f", monto).split(".")
        val soles = partes[0]
        val centimos = partes[1]
        return "$soles CON $centimos/100 SOLES"
    }
}