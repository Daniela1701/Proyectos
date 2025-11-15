package com.example.proyectocurso

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class HU_GestionPrediosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var containerPredios: LinearLayout
    private lateinit var db: miSQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hu_activity_gestion_predios)

        // Llamar a la función de BaseActivity sin heredar
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Gestión de predios"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        containerPredios = findViewById(R.id.containerPredios)
        db = miSQLiteHelper(this)

        listarPredios()
    }

    private fun listarPredios() {
        val listaPredios = db.obtenerPrediosActivos()

        containerPredios.removeAllViews()

        if (listaPredios.isEmpty()) {
            val noData = TextView(this)
            noData.text = "No hay predios registrados."
            noData.textSize = 16f
            noData.setPadding(16, 16, 16, 16)
            containerPredios.addView(noData)
        } else {
            //  Mostrar cada predio existente
            for (predio in listaPredios) {
                val card = LayoutInflater.from(this)
                    .inflate(R.layout.hu_item_lista_predio, containerPredios, false)

                val tvNombre = card.findViewById<TextView>(R.id.tvNombrePredio)
                val tvDireccion = card.findViewById<TextView>(R.id.tvDireccionPredio)
                val tvServicio = card.findViewById<TextView>(R.id.tvServicioPredio)
            //botones
                val btnEliminar = card.findViewById<ImageButton>(R.id.btnEliminarPredio)
                val btnEditar = card.findViewById<ImageButton>(R.id.btnEditarPredio)
                val btnGestionar = card.findViewById<ImageButton>(R.id.btnGestionarPredio)

                tvNombre.text = "${predio["nombre"]} - ${predio["clave"]}"
                tvDireccion.text = "Dirección: ${predio["direccion"]}"
                tvServicio.text = "Distrito ID: ${predio["distrito_id"]}"

                // Aquí agregarás las rutas
                /*
                btnEliminar.setOnClickListener {
                    startActivity(Intent(this, apellido_EliminarPredioActivity::class.java))
                }


  */
                btnEditar.setOnClickListener {
                    val idPredio = predio["predio_id"] as? Int
                    if (idPredio != null) {
                        val intent = Intent(this, CM_EditarPredio::class.java)
                        intent.putExtra("PREDIO_ID", idPredio)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No se encontró el ID del predio", Toast.LENGTH_SHORT).show()
                    }
                }

                btnGestionar.setOnClickListener {
                    val idPredio = predio["predio_id"] as? Int
                    if (idPredio != null) {
                        val intent = Intent(this, la_GestionUnidadesActivity::class.java).apply {
                            putExtra("PREDIO_ID", idPredio)
                        }
                        startActivity(intent)
                    }
                }
                containerPredios.addView(card)
            }
        }

        // agregar un nuevo predio
        val cardAgregar = LayoutInflater.from(this)
            .inflate(R.layout.hu_item_agregar_predio, containerPredios, false)

        val btnAgregar = cardAgregar.findViewById<ImageButton>(R.id.btnAgregarPredio)
        btnAgregar.setOnClickListener {
            val intent = Intent(this, la_CrearPredioActivity::class.java)
            startActivityForResult(intent, 1001)
        }
        containerPredios.addView(cardAgregar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val nuevoPredioId = data.getIntExtra("NUEVO_PREDIO_ID", -1)

            if (nuevoPredioId != -1) {
                Toast.makeText(this, "Predio creado correctamente (ID: $nuevoPredioId)", Toast.LENGTH_SHORT).show()

                // 🔹 OPCIÓN 1: simplemente refrescar lista
                listarPredios()

                // 🔹 OPCIÓN 2 (opcional): abrir directamente la gestión de unidades
                /*
                val intent = Intent(this, la_GestionUnidadesActivity::class.java).apply {
                    putExtra("PREDIO_ID", nuevoPredioId)
                }
                startActivity(intent)
                */
            }
        }
    }



}
