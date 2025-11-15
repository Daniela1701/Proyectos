package com.example.proyectocurso

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.normalizacion.Ac_Calcnorm_Activity
import kotlin.text.get

class HU_ListarRecibosActivity : AppCompatActivity() {

    private lateinit var dbHelper: miSQLiteHelper
    private lateinit var spinnerPredio: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnNuevo: Button
    private lateinit var btnIrCalculos: Button

    // Lista de predios: List<Pair<id, nombre>>
    private lateinit var predios: List<Pair<Int, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hu_activity_listar_recibos)

        val header = findViewById<LinearLayout>(R.id.header)
        val tvTitulo = header.findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = "Recibo proveedores"
        BaseActivity().setupMenu(this, header)

        val footer = findViewById<LinearLayout>(R.id.footer)
        BaseActivity().setupFooter(this, footer)

        dbHelper = miSQLiteHelper(this)

        spinnerPredio = findViewById(R.id.spinner_predio)
        recyclerView = findViewById(R.id.rv_recibos)
        btnNuevo = findViewById(R.id.btn_nuevo_recibo)
        btnIrCalculos = findViewById(R.id.btnIrCalculos)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar predios desde la base de datos
        predios = dbHelper.obtenerPredios() // retorna List<Pair<Int, String>>
        val nombresPredios = predios.map { it.second }

        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresPredios)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPredio.adapter = adapterSpinner

        // Al seleccionar un predio, cargar recibos
        spinnerPredio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val predioId = predios[position].first
                val recibos = dbHelper.obtenerRecibosPorPredio(predioId)

                // Usar el adapter externo
                recyclerView.adapter = HU_ListarRecibosAdapter(recibos)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón NUEVO -> ir a registrar recibo
        btnNuevo.setOnClickListener {
            val predioId = predios[spinnerPredio.selectedItemPosition].first
            val intent = Intent(this, HU_RegistrarReciboActivity::class.java)
            intent.putExtra("predioId", predioId)  // Enviar predio seleccionado
            //aqui se refresca la pagina
            startActivityForResult(intent, 100)
        }

        // 🧩 Nuevo botón -> Ir a mis cálculos
        btnIrCalculos.setOnClickListener {
            if (predios.isEmpty()) {
                Toast.makeText(this, "No hay predios registrados", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val posicion = spinnerPredio.selectedItemPosition
            if (posicion != -1) {
                val predioIdSeleccionado = predios[posicion].first
                val intent = Intent(this, Ac_Calcnorm_Activity::class.java)
                intent.putExtra("PREDIO_ID", predioIdSeleccionado)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Selecciona un predio primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Adapter para RecyclerView sin modelos
    class RecibosAdapter(private val recibos: List<Map<String, Any>>) :
        RecyclerView.Adapter<RecibosAdapter.ReciboViewHolder>() {

        class ReciboViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvPeriodo: TextView = itemView.findViewById(R.id.tv_periodo)
            val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha_registro)
            val tvCodigo: TextView = itemView.findViewById(R.id.tv_codigo_factura)
            val tvTotal: TextView = itemView.findViewById(R.id.tv_total)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciboViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hu_item_lista_recibos, parent, false)
            return ReciboViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReciboViewHolder, position: Int) {
            val recibo = recibos[position]
            holder.tvPeriodo.text = recibo["periodo"].toString()
            holder.tvFecha.text = recibo["fecha_registro"].toString()
            holder.tvCodigo.text = recibo["codigo_factura"].toString()
            holder.tvTotal.text = "S/. ${recibo["total"]}"
        }

        override fun getItemCount(): Int = recibos.size
    }

    //cargar pagina
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // 🔄 Recargar los recibos del predio seleccionado
            val predioId = predios[spinnerPredio.selectedItemPosition].first
            val recibosActualizados = dbHelper.obtenerRecibosPorPredio(predioId)
            recyclerView.adapter = HU_ListarRecibosAdapter(recibosActualizados)
        }
    }

}
