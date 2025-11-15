package com.example.proyectocurso.distribucion

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.BaseActivity
import com.example.proyectocurso.distribucion.Ac_ReciboSpinnerAdapter
import com.example.proyectocurso.R
import com.example.proyectocurso.normalizacion.Ac_Calcnorm_Activity
import com.example.proyectocurso.miSQLiteHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Ac_CalcDist_Activity : AppCompatActivity() {

    private lateinit var miSQLiteHelper: miSQLiteHelper
    private val predioIdSeleccionado: Int by lazy {
        intent.getIntExtra("predio_id", 1)
    }

    private lateinit var tvReciboProveedor: TextView
    private lateinit var tvFechaInicio: TextView
    private lateinit var tvFechaFin: TextView
    private lateinit var tvConsumo: TextView
    private lateinit var tvMontoTotal: TextView
    private lateinit var tvDiscrepancia: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAsignarRecibo: Button
    private lateinit var btnEmitirRecibos: Button

    private var recibosList: List<Triple<Int, String, Boolean>> = emptyList()
    private var esPendienteSeleccionado = false
    private var reciboSeleccionadoId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.ac_calculo_distribucion)


        miSQLiteHelper = miSQLiteHelper(this)

        // Header
        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Distribución de consumo"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // Navegación
        findViewById<Button>(R.id.btnNormalizacion).setOnClickListener {
            val intent = Intent(this, Ac_Calcnorm_Activity::class.java)
            intent.putExtra("PREDIO_ID", predioIdSeleccionado)
            startActivity(intent)
            finish()
        }

        // Inicializar vistas
        tvReciboProveedor = findViewById(R.id.tvReciboProveedor)
        tvFechaInicio = findViewById(R.id.tvFechaInicioProveedor)
        tvFechaFin = findViewById(R.id.tvFechaFinProveedor)
        tvConsumo = findViewById(R.id.tvConsumoProveedor)
        tvMontoTotal = findViewById(R.id.tvMontoTotalProveedor)
        tvDiscrepancia = findViewById(R.id.tvDiscrepanciaActual)
        recyclerView = findViewById(R.id.recyclerViewUnidades)
        btnAsignarRecibo = findViewById(R.id.btnAsignarRecibo)
        btnEmitirRecibos = findViewById(R.id.btnEmitirRecibos)

        recyclerView.visibility = View.GONE
        btnAsignarRecibo.isEnabled = false
        btnEmitirRecibos.isEnabled = false

        // Cargar spinner
        cargarUltimoRecibo()

        // Listener del botón principal
        btnAsignarRecibo.setOnClickListener {
            if (!miSQLiteHelper.todasUnidadesNormalizadas(predioIdSeleccionado)) {
                AlertDialog.Builder(this)
                    .setTitle("Normalización incompleta")
                    .setMessage("No se puede distribuir el recibo hasta que todas las unidades del predio estén normalizadas.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            } else {
                generarCalculoDistribucion()
                btnAsignarRecibo.isEnabled = false
            }
        }

        btnEmitirRecibos.setOnClickListener {
            emitirRecibosInternos()
        }
    }

    private fun cargarUltimoRecibo() {
        miSQLiteHelper.obtenerUltimoReciboProveedor(predioIdSeleccionado)?.let { (reciboId, inicio, fin) ->
            reciboSeleccionadoId = reciboId

            // Formatear fechas para mostrar
            val formatterDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatterUI = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val texto = "${formatterUI.format(formatterDB.parse(inicio))} - ${formatterUI.format(formatterDB.parse(fin))}"
            tvReciboProveedor.text = texto

            // Verificar si ya existe una distribución para este recibo
            val yaCalculado = miSQLiteHelper.existeDistribucionParaRecibo(reciboId)
            if (yaCalculado) {
                cargarDistribucionExistente()
                btnAsignarRecibo.isEnabled = false
            } else {
                cargarDatosRecibo(reciboId)
                if (miSQLiteHelper.todasUnidadesNormalizadas(predioIdSeleccionado)) {
                    btnAsignarRecibo.isEnabled = true
                } else {
                    btnAsignarRecibo.isEnabled = false
                    AlertDialog.Builder(this)
                        .setTitle("Normalización incompleta")
                        .setMessage("No se puede distribuir el recibo hasta que todas las unidades del predio estén normalizadas.")
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
            }
        } ?: run {
            // No hay recibos
            limpiarTodo()
            Toast.makeText(this, "No hay recibos del proveedor disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosRecibo(reciboProvId: Int) {
        miSQLiteHelper.obtenerDatosRecibo(reciboProvId)?.let { (inicio, fin, consumo, monto) ->
            val formatterDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatterUI = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            tvFechaInicio.text = formatterUI.format(formatterDB.parse(inicio))
            tvFechaFin.text = formatterUI.format(formatterDB.parse(fin))
            tvConsumo.text = "${String.format("%.1f", consumo)} kWh"
            tvMontoTotal.text = "S/ ${String.format("%.2f", monto)}"

            val sumaNormalizado = miSQLiteHelper.obtenerSumaConsumoNormalizado(predioIdSeleccionado)
            val discrepancia = consumo - sumaNormalizado
            tvDiscrepancia.text = "${String.format("%.2f", discrepancia)} kWh"
        }
    }

    private fun generarCalculoDistribucion() {
        miSQLiteHelper.obtenerDatosRecibo(reciboSeleccionadoId)?.let { (_, _, consumoTotal, montoTotal) ->
            val unidades = miSQLiteHelper.obtenerUnidadesNormalizadasNoAsignadas(predioIdSeleccionado)
            val sumaNormalizado = unidades.sumOf { it.fourth }
            val costoPorKwh = montoTotal / consumoTotal

            // Insertar en calculos_distribucion
            val calcDistId = miSQLiteHelper.insertarCalculoDistribucion(
                reciboSeleccionadoId,
                "pendiente",
                sumaNormalizado,
                consumoTotal - sumaNormalizado
            )

            // Calcular y guardar distribución
            val listaCards = mutableListOf<Triple<String, Int, Array<String>>>()
            for ((unidadId, nombre, calculoMedidorId, consumoNorm) in unidades) {
                val consumoTotalUnidad = consumoNorm * costoPorKwh
                val proporcion = if (sumaNormalizado > 0) consumoNorm / sumaNormalizado else 0.0
                val montoAsignado = (consumoTotal - sumaNormalizado) * proporcion * costoPorKwh
                val totalPagar = consumoTotalUnidad + montoAsignado

                // Insertar con ambos IDs
                miSQLiteHelper.insertarDistribucionUnidad(
                    calcDistId,
                    calculoMedidorId,
                    consumoTotalUnidad,
                    montoAsignado
                )

                listaCards.add(Triple(
                    nombre,
                    miSQLiteHelper.getOcupantes(unidadId),
                    arrayOf(
                        "${String.format("%.1f", consumoNorm)} kWh",
                        "S/ ${String.format("%.2f", consumoTotalUnidad)}",
                        "S/ ${String.format("%.2f", montoAsignado)}",
                        "S/ ${String.format("%.2f", totalPagar)}"
                    )
                ))
            }

            // Mostrar RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = DistribucionAdapter(listaCards)
            recyclerView.visibility = View.VISIBLE
            btnEmitirRecibos.isEnabled = true

            Log.d("DEBUG", "Generados ${listaCards.size} cards")
        }
    }

    private fun cargarDistribucionExistente() {
        val datos = miSQLiteHelper.obtenerDistribucionPorRecibo(reciboSeleccionadoId)
        val listaCards = datos.map { (nombre, consumoNorm, consumoTotalUnidad, montoAsignado) ->
            val totalPagar = consumoTotalUnidad + montoAsignado
            Triple(
                nombre,
                miSQLiteHelper.getOcupantesPorNombre(nombre),
                arrayOf(
                    "${String.format("%.1f", consumoNorm)} kWh",
                    "S/ ${String.format("%.2f", consumoTotalUnidad)}",
                    "S/ ${String.format("%.2f", montoAsignado)}",
                    "S/ ${String.format("%.2f", totalPagar)}"
                )
            )
        }

        // Mostrar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DistribucionAdapter(listaCards)
        recyclerView.visibility = View.VISIBLE
        btnEmitirRecibos.isEnabled = true

        Log.d("DEBUG", "Cargados ${listaCards.size} cards existentes")
    }

    private fun limpiarTodo() {
        tvFechaInicio.text = ""
        tvFechaFin.text = ""
        tvConsumo.text = ""
        tvMontoTotal.text = ""
        tvDiscrepancia.text = ""
        recyclerView.visibility = View.GONE
        btnAsignarRecibo.isEnabled = false
        btnEmitirRecibos.isEnabled = false
    }

    private fun emitirRecibosInternos() {
        miSQLiteHelper.obtenerDatosRecibo(reciboSeleccionadoId)?.let { (_, _, consumoTotalProveedor, montoTotalProveedor) ->
            val costoKw = montoTotalProveedor / consumoTotalProveedor

            // Obtener la distribución ya calculada (misma que se muestra en el RecyclerView)
            val distribucion = miSQLiteHelper.obtenerDistribucionPorRecibo(reciboSeleccionadoId)

            val fechaEmision = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cal = Calendar.getInstance().apply { time = Date(); add(Calendar.DAY_OF_MONTH, 15) }
            val fechaVencimiento = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

            for ((nombreUnidad, consumoNormalizado, consumoTotalUnidad, montoAsignado) in distribucion) {
                // Obtener unidad_id y submedidor_id desde el nombre (mejor: desde calculos_medidores)
                val unidadId = miSQLiteHelper.getUnidadIdPorNombre(nombreUnidad)
                val submedidorId = miSQLiteHelper.getSubmedidorIdPorUnidad(unidadId)

                // Obtener el calculo_medidor_id asociado a esta unidad en esta distribución
                val calculoMedidorId = miSQLiteHelper.getCalculoMedidorIdPorUnidadYDistribucion(
                    unidadId,
                    miSQLiteHelper.getCalculoDistribucionIdPorRecibo(reciboSeleccionadoId)
                )

                // Obtener fechas y lecturas desde calculos_medidores
                miSQLiteHelper.getLecturasYFechasDesdeCalculo(calculoMedidorId)?.let { (fechaInicio, fechaFin, lecturaInicio, lecturaFin) ->
                    Log.d("DEBUG", "Fecha inicio: $fechaInicio, Fecha fin: $fechaFin")
                    val totalPagar = consumoTotalUnidad + montoAsignado

                    // Generar ID único de recibo interno (puedes usar UUID o formato personalizado)
                    val reciboIntId = "R-${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}-${unidadId}"

                    miSQLiteHelper.insertarReciboInterno(
                        reciboIntId = reciboIntId,
                        unidadId = unidadId,
                        fechaEmision = fechaEmision,
                        fechaVencimiento = fechaVencimiento,
                        montoTotal = totalPagar,
                        submedidorId = submedidorId,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        lecturaInicio = lecturaInicio,
                        lecturaFin = lecturaFin,
                        costoKw = costoKw,
                        consumo = consumoNormalizado,
                        calculoMedidorId = calculoMedidorId
                    )
                }
            }

            // Marcar distribución como 'calculado'
            miSQLiteHelper.marcarDistribucionComoCalculada(reciboSeleccionadoId)

            // Navegar a BCFactura
            val intent = Intent(this, com.example.proyectocurso.factura.BCFactura::class.java)
            intent.putExtra("predio_id", predioIdSeleccionado)
            intent.putExtra("year_month", SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
            startActivity(intent)
        }
    }
}