package com.example.proyectocurso

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class se_LecturaMedidorCrearActivity : AppCompatActivity() {

    private lateinit var dbHelper: miSQLiteHelper
    private var unidadId: Int = 0

    // Variables para manejo de imagen
    private var fotoRuta: String? = null
    private lateinit var imgPreview: ImageView
    private lateinit var btnSubirFoto: LinearLayout
    private val REQ_CAMARA = 100
    private val REQ_GALERIA = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.se_activity_lectura_medidor_crear)

        // Inicializar base de datos
        dbHelper = miSQLiteHelper(this)

        // Recuperar unidadId enviado desde el intent
        unidadId = intent.getIntExtra("unidadId", 0)

        // Referencias a vistas
        val etValor = findViewById<EditText>(R.id.etValor)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etUnidad = findViewById<EditText>(R.id.etUnidad)
        val etComentarios = findViewById<EditText>(R.id.etComentarios)
        val rbNormal = findViewById<RadioButton>(R.id.rbNormal)
        val rbInicial = findViewById<RadioButton>(R.id.rbInicial)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarLectura)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarLectura)

        // Referencias a foto
        btnSubirFoto = findViewById(R.id.btnSubirFoto)
        imgPreview = findViewById(R.id.imgPreview)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Registrar de Lectura"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)


        // Mostrar nombre de la unidad asociada
        val nombreUnidad = obtenerNombreUnidad(unidadId)
        etUnidad.setText(nombreUnidad)

        //  Asignar fecha + hora actual
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        etFecha.setText(sdf.format(Date()))
        etFecha.isEnabled = false

        btnCancelar.setOnClickListener {
            finish()
        }
        // Botón para adjuntar foto
        btnSubirFoto.setOnClickListener {
            val opciones = arrayOf("Tomar foto", "Elegir de galería")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Adjuntar imagen")
            builder.setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirCamara()
                    1 -> abrirGaleria()
                }
            }
            builder.show()
        }

        // Evento Guardar
        btnGuardar.setOnClickListener {
            val valorTexto = etValor.text.toString()
            val fechaTexto = etFecha.text.toString()
            val notasTexto = etComentarios.text.toString()
            val tipoLectura = if (rbInicial.isChecked) "INICIAL" else "NORMAL"

            if (valorTexto.isEmpty()) {
                Toast.makeText(this, "Ingrese el valor de lectura", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valor = valorTexto.toDoubleOrNull()
            if (valor == null) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val submedidorId = obtenerSubmedidorPorUnidad(unidadId)
            if (submedidorId == 0) {
                Toast.makeText(this, "No se encontró submedidor para esta unidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbHelper.registrarLecturaConFoto(submedidorId, valor, fechaTexto, notasTexto, tipoLectura, fotoRuta)

            Toast.makeText(this, "Lectura registrada correctamente", Toast.LENGTH_LONG).show()
            finish()
        }
    }

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

        val nombreArchivo = "lectura_${System.currentTimeMillis()}.jpg"
        val archivo = File(carpeta, nombreArchivo)
        val fos = FileOutputStream(archivo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()

        fotoRuta = archivo.absolutePath
        imgPreview.setImageBitmap(bitmap)
        imgPreview.visibility = View.VISIBLE
    }

    private fun obtenerSubmedidorPorUnidad(unidadId: Int): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT submedidor_id FROM submedidores WHERE unidad_id = ? LIMIT 1",
            arrayOf(unidadId.toString())
        )
        var submedidorId = 0
        if (cursor.moveToFirst()) {
            submedidorId = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return submedidorId
    }

    private fun obtenerNombreUnidad(unidadId: Int): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nombre FROM unidades WHERE unidad_id = ? LIMIT 1",
            arrayOf(unidadId.toString())
        )
        var nombre = ""
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return nombre
    }
}
