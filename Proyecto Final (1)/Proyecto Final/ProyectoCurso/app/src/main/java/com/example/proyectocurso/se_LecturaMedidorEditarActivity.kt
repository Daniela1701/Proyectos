package com.example.proyectocurso

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class se_LecturaMedidorEditarActivity : AppCompatActivity() {

    private lateinit var dbHelper: miSQLiteHelper
    private lateinit var etValor: EditText
    private lateinit var etNotas: EditText
    private lateinit var etFecha: EditText
    private lateinit var etUnidad: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnSubirFotoEditar: LinearLayout
    private lateinit var imgPreviewEditar: ImageView

    private var lecturaId = 0
    private var facturado = false
    private var fotoRuta: String? = null

    private val REQ_CAMARA = 100
    private val REQ_GALERIA = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.se_activity_lectura_medidor_editar)

        dbHelper = miSQLiteHelper(this)

        // === Header ===
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Editar Lectura"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // === Referencias a vistas ===
        etValor = findViewById(R.id.etValor)
        etNotas = findViewById(R.id.etNotas)
        etFecha = findViewById(R.id.etFecha)
        etUnidad = findViewById(R.id.etUnidad)
        btnGuardar = findViewById(R.id.btnGuardarLectura)
        btnSubirFotoEditar = findViewById(R.id.btnSubirFotoEditar)
        imgPreviewEditar = findViewById(R.id.imgPreviewEditar)

        // === Datos recibidos del intent ===
        lecturaId = intent.getIntExtra("lecturaId", 0)
        val valor = intent.getDoubleExtra("valor", 0.0)
        val fecha = intent.getStringExtra("fecha") ?: ""
        val notas = intent.getStringExtra("notas") ?: ""
        val unidad = intent.getStringExtra("unidad") ?: ""
        val btnCancelar = findViewById<Button>(R.id.btnCancelarLectura)
        fotoRuta = intent.getStringExtra("fotoRuta")
        facturado = intent.getBooleanExtra("facturado", false)


        // === Mostrar datos ===
        etValor.setText(valor.toString())
        etNotas.setText(notas)
        etFecha.setText(fecha)
        etUnidad.setText(unidad)

        // === Mostrar foto existente si hay ===
        if (!fotoRuta.isNullOrEmpty()) {
            val archivo = File(fotoRuta!!)
            if (archivo.exists()) {
                val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
                imgPreviewEditar.setImageBitmap(bitmap)
                imgPreviewEditar.visibility = ImageView.VISIBLE
            }
        }

        // === Si está facturado, bloquear edición ===
        if (facturado) {
            etValor.isEnabled = false
            etNotas.isEnabled = false
            btnGuardar.isEnabled = false
            btnSubirFotoEditar.isEnabled = false
            Toast.makeText(this, "No se puede editar: esta lectura ya fue facturada.", Toast.LENGTH_LONG).show()
        }
        btnCancelar.setOnClickListener {
            finish()
            // === overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
        // === Cambiar foto ===
        btnSubirFotoEditar.setOnClickListener {
            val opciones = arrayOf("Tomar foto", "Elegir de galería")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Cambiar imagen")
            builder.setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirCamara()
                    1 -> abrirGaleria()
                }
            }
            builder.show()
        }

        // === Guardar cambios ===
        btnGuardar.setOnClickListener {
            val nuevoValor = etValor.text.toString().toDoubleOrNull()
            val nuevasNotas = etNotas.text.toString()

            if (nuevoValor == null) {
                Toast.makeText(this, "Ingrese un valor válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("valor", nuevoValor)
                put("notas", nuevasNotas)
                put("foto_ruta", fotoRuta)
            }
            db.update("lecturas_sub", values, "lectura_id = ?", arrayOf(lecturaId.toString()))
            db.close()

            Toast.makeText(this, "Lectura actualizada correctamente ✅", Toast.LENGTH_SHORT).show()

            val resultIntent = Intent()
            resultIntent.putExtra("lecturaEditada", true)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // === Métodos para cámara / galería ===
    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQ_CAMARA)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQ_GALERIA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CAMARA -> {
                    val foto = data?.extras?.get("data") as? Bitmap ?: return
                    guardarImagen(foto)
                }
                REQ_GALERIA -> {
                    val uri: Uri? = data?.data
                    uri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        guardarImagen(bitmap)
                    }
                }
            }
        }
    }

    private fun guardarImagen(bitmap: Bitmap) {
        val carpeta = File(getExternalFilesDir(null), "Lecturas")
        if (!carpeta.exists()) carpeta.mkdirs()

        val nombreArchivo = "lectura_edit_${System.currentTimeMillis()}.jpg"
        val archivo = File(carpeta, nombreArchivo)
        val fos = FileOutputStream(archivo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()

        fotoRuta = archivo.absolutePath
        imgPreviewEditar.setImageBitmap(bitmap)
        imgPreviewEditar.visibility = ImageView.VISIBLE
    }
}
