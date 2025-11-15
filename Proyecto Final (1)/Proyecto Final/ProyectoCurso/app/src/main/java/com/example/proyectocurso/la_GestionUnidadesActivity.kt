package com.example.proyectocurso

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.adapter.UnidadCardAdapter
import com.example.proyectocurso.model.UnidadCard

class la_GestionUnidadesActivity : AppCompatActivity() {

    private lateinit var db: miSQLiteHelper
    private var predios = listOf<Pair<Int, String>>()
    private var predioSeleccionadoId = -1
    private lateinit var btnNuevaUnidad: Button
    private lateinit var adapterNuevo: UnidadCardAdapter
    private val tarjetas = mutableListOf<UnidadCard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.la_activity_gestion_unidades)

        db = miSQLiteHelper(this)
        // Configurar encabezado
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Gestión de unidades"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        btnNuevaUnidad = findViewById(R.id.btnNuevaUnidad)

        val rv = findViewById<RecyclerView>(R.id.rvUnidades)
        rv.layoutManager = LinearLayoutManager(this)


        val predioIdRecibido = intent.getIntExtra("PREDIO_ID", -1)
        mostrarBotonesPredios(predioIdRecibido)

        findViewById<EditText>(R.id.etBuscarUnidad).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filtrarUnidades(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnNuevaUnidad.setOnClickListener {
            if (predioSeleccionadoId == -1) {
                Toast.makeText(this, "Selecciona un predio primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, la_CrearUnidadActivity::class.java).apply {
                putExtra("PREDIO_ID", predioSeleccionadoId)
                putExtra(
                    "PREDIO_NOMBRE",
                    predios.find { it.first == predioSeleccionadoId }?.second ?: ""
                )
            }
            startActivityForResult(intent, 1002)
        }
    }

    private fun mostrarBotonesPredios(predioSeleccionado: Int = -1) {
        val contenedor = findViewById<LinearLayout>(R.id.llBotonesPredios)
        contenedor.removeAllViews()

        predios = db.obtenerPredios()

        // 🔹 Crear tarjeta visual para cada predio
        for (predio in predios) {
            val card = layoutInflater.inflate(R.layout.la_item_predio_card, contenedor, false)
            val tvNombre = card.findViewById<TextView>(R.id.tvNombre)
            val ivIcono = card.findViewById<ImageView>(R.id.ivIcono)
            val bgIcono = card.findViewById<View>(R.id.bgIcono)

            tvNombre.text = predio.second
            ivIcono.setImageResource(R.drawable.predio1)
            bgIcono.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_predio)

            card.setOnClickListener {
                predioSeleccionadoId = predio.first
                cargarUnidadesDelPredio(predio.first)
                marcarActivoIcono(card, contenedor)
                actualizarEstadoBotonNueva()
            }

            contenedor.addView(card)
        }

        // 🔹 Tarjeta "+ Agregar predio"
        val cardAgregar = layoutInflater.inflate(R.layout.la_item_predio_card, contenedor, false)
        val tvNombreAgregar = cardAgregar.findViewById<TextView>(R.id.tvNombre)
        val ivIconoAgregar = cardAgregar.findViewById<ImageView>(R.id.ivIcono)
        val bgIconoAgregar = cardAgregar.findViewById<View>(R.id.bgIcono)

        tvNombreAgregar.text = "Agregar"
        ivIconoAgregar.setImageResource(R.drawable.cruz)
        bgIconoAgregar.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_predio)

        cardAgregar.setOnClickListener {
            startActivityForResult(
                Intent(this@la_GestionUnidadesActivity, la_CrearPredioActivity::class.java),
                1001
            )
        }

        contenedor.addView(cardAgregar)

        // 🔹 Seleccionar el predio recibido o el primero
        val index = predios.indexOfFirst { it.first == predioSeleccionado }
        if (index != -1) {
            val cardSeleccionada = contenedor.getChildAt(index)
            cardSeleccionada?.post { cardSeleccionada.performClick() }
        } else if (predios.isNotEmpty()) {
            val primerCard = contenedor.getChildAt(0)
            primerCard?.post { primerCard.performClick() }
        }
    }

    private fun marcarActivoIcono(cardActiva: View, contenedor: LinearLayout) {
        for (i in 0 until contenedor.childCount - 1) {
            val child = contenedor.getChildAt(i)
            val bgIcono = child.findViewById<View>(R.id.bgIcono)
            val ivIcono = child.findViewById<ImageView>(R.id.ivIcono)

            bgIcono.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_predio)
            ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.text_gray))
        }

        val bgIconoActivo = cardActiva.findViewById<View>(R.id.bgIcono)
        val ivIconoActivo = cardActiva.findViewById<ImageView>(R.id.ivIcono)

        bgIconoActivo.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_predio_activo)
        ivIconoActivo.setColorFilter(ContextCompat.getColor(this, android.R.color.white)) // pone blanco el activo
    }
    private fun cargarUnidadesDelPredio(predioId: Int) {
        val listaUnidades = db.obtenerUnidadesPorPredio(predioId)

        tarjetas.clear()
        tarjetas.addAll(listaUnidades.map { (unidadId, unidadNombre) ->
            val inquilino = db.obtenerInquilinoDeUnidad(unidadId)
            val puedeEditar = db.puedeEditarUnidad(unidadId)
            UnidadCard(
                unidadId = unidadId,
                unidadNombre = unidadNombre,
                ocupanteNombres = inquilino?.second,
                ocupanteApellidos = inquilino?.third,
                puedeEditar = puedeEditar
            )
        })

        adapterNuevo = UnidadCardAdapter(
            tarjetas,
            onEditar = { tarjeta, position ->
                val intent = Intent(this, la_EditarUnidadActivity::class.java)
                intent.putExtra("UNIDAD_ID", tarjeta.unidadId)
                intent.putExtra("POSITION", position)
                startActivityForResult(intent, 1003)
            },
            onEliminarUnidad = { tarjeta ->
                db.eliminarUnidadCompleta(tarjeta.unidadId)
                val pos = tarjetas.indexOf(tarjeta)
                adapterNuevo.eliminarItem(pos)
            },
            onEliminarOcupante = { tarjeta ->
                db.desocuparUnidad(tarjeta.unidadId)
                val pos = tarjetas.indexOf(tarjeta)
                val nueva = tarjeta.copy(ocupanteNombres = null, ocupanteApellidos = null)
                adapterNuevo.actualizarItem(pos, nueva)
            }
        )

        findViewById<RecyclerView>(R.id.rvUnidades).adapter = adapterNuevo
    }

    private fun filtrarUnidades(texto: String) {
        val lista = db.obtenerUnidadesPorPredio(predioSeleccionadoId)
        val filtrada = lista.filter { it.second.contains(texto, ignoreCase = true) }
        val tarjetasFiltradas = filtrada.map { (id, nombre) ->
            val inq = db.obtenerInquilinoDeUnidad(id)
            val editable = db.puedeEditarUnidad(id)
            UnidadCard(id, nombre, inq?.second, inq?.third, editable)
        }.toMutableList()

        val adapterFiltrado = UnidadCardAdapter(
            tarjetasFiltradas,
            onEditar = { tarjeta, position ->
                val intent = Intent(this, la_EditarUnidadActivity::class.java)
                intent.putExtra("UNIDAD_ID", tarjeta.unidadId)
                intent.putExtra("POSITION", position)
                startActivityForResult(intent, 1003)
            },
            onEliminarUnidad = { tarjeta ->
                db.eliminarUnidadCompleta(tarjeta.unidadId)
                cargarUnidadesDelPredio(predioSeleccionadoId)
            },
            onEliminarOcupante = { tarjeta ->
                db.desocuparUnidad(tarjeta.unidadId)
                cargarUnidadesDelPredio(predioSeleccionadoId)
            }
        )

        findViewById<RecyclerView>(R.id.rvUnidades).adapter = adapterFiltrado
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1003 && resultCode == RESULT_OK && data != null) {
            val position = data.getIntExtra("POSITION", -1)
            if (position == -1) return

            val nuevoNombre = data.getStringExtra("NOMBRE_UNIDAD") ?: return
            val nuevosNombres = data.getStringExtra("NOMBRES_INQUILINO")
            val nuevosApellidos = data.getStringExtra("APELLIDOS_INQUILINO")

            val tarjetaActual = tarjetas[position]
            val tarjetaActualizada = tarjetaActual.copy(
                unidadNombre = nuevoNombre,
                ocupanteNombres = nuevosNombres,
                ocupanteApellidos = nuevosApellidos
            )
            adapterNuevo.actualizarItem(position, tarjetaActualizada)
        }
    }

    private fun actualizarEstadoBotonNueva() {
        btnNuevaUnidad.isEnabled = predioSeleccionadoId != -1
        btnNuevaUnidad.alpha = if (predioSeleccionadoId != -1) 1f else 0.5f
    }

    private fun setupFooter() {
        // sin cambios
    }
}
