package com.example.proyectocurso.normalizacion

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
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
import com.example.proyectocurso.distribucion.Ac_CalcDist_Activity
import com.example.proyectocurso.R
import com.example.proyectocurso.miSQLiteHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Ac_Calcnorm_Activity : AppCompatActivity() {

    private lateinit var miSQLiteHelper: miSQLiteHelper
    private var predioIdSeleccionado = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.ac_calculo_normalizacion)

        // Recibir el predioId del Intent
        predioIdSeleccionado = intent.getIntExtra("PREDIO_ID", -1)

        // Validar que se recibió un predioId válido
        if (predioIdSeleccionado == -1) {
            Toast.makeText(this, "Error: No se recibió un predio válido", Toast.LENGTH_LONG).show()
            finish() // Cerrar la actividad si no hay predio válido
            return
        }

        miSQLiteHelper = miSQLiteHelper(this)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Cálculo de consumo"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        // Fechas del proveedor
        val tvFechaInicio = findViewById<TextView>(R.id.tvFechaInicioProveedor)
        val tvFechaFin = findViewById<TextView>(R.id.tvFechaFinProveedor)
        val tvDiasPeriodo = findViewById<TextView>(R.id.tvDiasPeriodoProveedor)

        miSQLiteHelper.ac_obtenerUltimoPeriodoProveedor(predioIdSeleccionado)?.let { (inicio, fin) ->
            val formatterDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatterUI = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val fInicio = formatterDB.parse(inicio)
                val fFin = formatterDB.parse(fin)
                tvFechaInicio.text = formatterUI.format(fInicio)
                tvFechaFin.text = formatterUI.format(fFin)

                val cal1 = Calendar.getInstance().apply { time = fInicio; clearTime() }
                val cal2 = Calendar.getInstance().apply { time = fFin; clearTime() }
                var dias = 0
                while (!cal1.after(cal2)) {
                    dias++
                    cal1.add(Calendar.DAY_OF_MONTH, 1)
                }
                tvDiasPeriodo.text = "$dias días"
            } catch (e: Exception) {
                Toast.makeText(this, "Error en fechas", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            tvFechaInicio.text = "Sin datos"
            tvFechaFin.text = "Sin datos"
            tvDiasPeriodo.text = "0 días"
        }

        // Spinner Normalizar a
        val spinnerNormalizarA = findViewById<Spinner>(R.id.spinnerNormalizarA)
        val adapterNormalizar = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("30 días", "28 días", "31 días")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerNormalizarA.adapter = adapterNormalizar

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUnidades)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val unidades = miSQLiteHelper.obtenerUnidadesPorPredio(predioIdSeleccionado)
        recyclerView.adapter = UnidadAdapter(unidades, miSQLiteHelper, spinnerNormalizarA)

        // Botón Distribución → sin validación
        findViewById<Button>(R.id.btnDistribucion).setOnClickListener {
            val intent = Intent(this, Ac_CalcDist_Activity::class.java)
            intent.putExtra("predio_id", predioIdSeleccionado)
            startActivity(intent)
        }
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}