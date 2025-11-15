// Ac_UnidadAdapter.kt
package com.example.proyectocurso.normalizacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectocurso.normalizacion.Ac_UnidadViewHolder
import com.example.proyectocurso.R
import com.example.proyectocurso.miSQLiteHelper
import java.text.SimpleDateFormat
import java.util.*

data class Unidad(
    val id: Int,
    val nombre: String,
    val contratoId: Int
)

class UnidadAdapter(
    private val unidades: List<Pair<Int, String>>,
    private val helper: miSQLiteHelper,
    private val spinnerNormalizarA: Spinner
) : RecyclerView.Adapter<Ac_UnidadViewHolder>() {

    private val formatterDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val formatterUI = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val estadoDetalle = mutableMapOf<Int, Triple<Double, Int, Double>>()
    private val expandido = mutableSetOf<Int>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Ac_UnidadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ac_item_unidad_expandible, parent, false)
        return Ac_UnidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: Ac_UnidadViewHolder, position: Int) {
        val (unidadId, nombre) = unidades[position]
        val contrato = helper.obtenerContratoVigente(unidadId)

        if (contrato == null) {
            holder.tvNombreUnidad.text = "$nombre (Sin Contrato Vigente)"
            holder.tvEstadoFecha.text = "N/A"
            holder.tvEstadoUnidad.text = "N/A"
            holder.viewEstadoFecha.setBackgroundResource(R.drawable.ac_estadocirculorojo)
            holder.viewEstadoUnidad.setBackgroundResource(R.drawable.ac_estadocirculorojo)
            holder.unitContent.visibility = View.GONE
            return
        }

        val (contratoId, lecturaInicialId) = contrato
        holder.tvNombreUnidad.text = nombre

        var tieneFecha = false
        var requiereNormalizacion = true
        var lecturaInicioId: Int? = null
        var lecturaFinId: Int? = null
        var consumoRaw: Double? = null
        var diasMedidos: Int? = null
        var consumoNormalizado: Double? = null
        var fechaInicioCalculo: String? = null
        var fechaFinCalculo: String? = null

        val ultimoCalculo = helper.obtenerUltimoCalculoNoAsignado(contratoId)
        if (ultimoCalculo != null) {
            val (calculoId, tieneFechasDB, estaNormalizadoDB) = ultimoCalculo
            tieneFecha = tieneFechasDB
            requiereNormalizacion = !estaNormalizadoDB

            if (tieneFechasDB) {
                val db = helper.readableDatabase
                val cursor = db.rawQuery(
                    "SELECT lectura_inicio_id, lectura_fin_id, consumo_raw, dias_medidos, consumo_normalizado FROM calculos_medidores WHERE calculo_medidor_id = ?",
                    arrayOf(calculoId.toString())
                )
                if (cursor.moveToFirst()) {
                    lecturaInicioId = cursor.getInt(0)
                    lecturaFinId = cursor.getInt(1)
                    consumoRaw = if (!cursor.isNull(2)) cursor.getDouble(2) else null
                    diasMedidos = if (!cursor.isNull(3)) cursor.getInt(3) else null
                    consumoNormalizado = if (!cursor.isNull(4)) cursor.getDouble(4) else null

                    val detalleInicio = helper.obtenerDetalleLectura(lecturaInicioId)
                    val detalleFin = helper.obtenerDetalleLectura(lecturaFinId)
                    fechaInicioCalculo = detalleInicio?.first
                    fechaFinCalculo = detalleFin?.first

                    // ✅ Guardar en estado si ya está normalizado
                    if (!requiereNormalizacion && consumoRaw != null && diasMedidos != null && consumoNormalizado != null) {
                        estadoDetalle[unidadId] = Triple(consumoRaw!!, diasMedidos!!, consumoNormalizado!!)
                    }
                }
                cursor.close()
                db.close()
            }
        } else {
            var lecturaInicial = helper.obtenerLecturaInicialContrato(lecturaInicialId)
            var fechaInicial = lecturaInicial?.third
            val ultimaLecturaFinRecibo = helper.obtenerUltimaLecturaFinRecibo(contratoId)

            if (ultimaLecturaFinRecibo != null) {
                fechaInicial = ultimaLecturaFinRecibo.third
                lecturaInicioId = ultimaLecturaFinRecibo.first
            } else {
                lecturaInicioId = lecturaInicial?.first
            }

            if (fechaInicial != null) {
                holder.tvFechaInicial.text = try {
                    formatterUI.format(formatterDB.parse(fechaInicial))
                } catch (e: Exception) {
                    fechaInicial
                }
            } else {
                holder.tvFechaInicial.text = ""
            }
        }

        // Actualizar UI de estados
        holder.tvEstadoFecha.text = if (tieneFecha) "Con Fecha" else "Sin Fecha"
        holder.viewEstadoFecha.setBackgroundResource(
            if (tieneFecha) R.drawable.ac_estadocirculoverde else R.drawable.ac_estadocirculorojo
        )

        holder.tvEstadoUnidad.text = if (requiereNormalizacion) "Requiere normalización" else "No requiere normalización"
        holder.viewEstadoUnidad.setBackgroundResource(
            if (requiereNormalizacion) R.drawable.ac_estadocirculorojo else R.drawable.ac_estadocirculoverde
        )

        // Restaurar estado de expansión y detalle
        val estaExpandido = unidadId in expandido
        holder.unitContent.visibility = if (estaExpandido) View.VISIBLE else View.GONE
        holder.ivArrow.rotation = if (estaExpandido) 180f else 0f

        if (estaExpandido) {
            configurarVisibilidadContenido(holder, tieneFecha, requiereNormalizacion)
            estadoDetalle[unidadId]?.let { (cr, d, cn) ->
                mostrarDetalle(holder, cr, d, cn)
            }
            if (!tieneFecha) {
                cargarFechasCombo(holder, unidadId, contratoId)
            } else if (fechaInicioCalculo != null) {
                holder.tvFechaInicial.text = try {
                    formatterUI.format(formatterDB.parse(fechaInicioCalculo))
                } catch (e: Exception) {
                    fechaInicioCalculo
                }
            }
        }

        holder.unitHeader.setOnClickListener {
            val isExpanded = holder.unitContent.visibility == View.VISIBLE
            if (isExpanded) {
                expandido.remove(unidadId)
            } else {
                expandido.add(unidadId)
            }
            holder.unitContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            holder.ivArrow.animate().rotation(if (isExpanded) 0f else 180f).start()

            if (!isExpanded) {
                configurarVisibilidadContenido(holder, tieneFecha, requiereNormalizacion)
                if (!tieneFecha) {
                    cargarFechasCombo(holder, unidadId, contratoId)
                } else if (fechaInicioCalculo != null) {
                    holder.tvFechaInicial.text = try {
                        formatterUI.format(formatterDB.parse(fechaInicioCalculo))
                    } catch (e: Exception) {
                        fechaInicioCalculo
                    }
                }

                // ✅ Mostrar detalle si ya está normalizado (caso 3)
                if (tieneFecha && !requiereNormalizacion) {
                    estadoDetalle[unidadId]?.let { (cr, d, cn) ->
                        mostrarDetalle(holder, cr, d, cn)
                    }
                }
            }
        }

        holder.btnGuardarFecha.setOnClickListener { guardarFecha(holder, contratoId, lecturaInicioId, false, unidadId) }
        holder.btnGuardarYNormalizar.setOnClickListener { guardarFecha(holder, contratoId, lecturaInicioId, true, unidadId) }
        holder.btnNormalizar.setOnClickListener { normalizarExistente(holder, contratoId, lecturaInicioId, lecturaFinId, unidadId) }
    }

    private fun guardarFecha(holder: Ac_UnidadViewHolder, contratoId: Int, lecturaInicioId: Int?, normalizar: Boolean, unidadId: Int) {
        val fechaFinSeleccionada = holder.spinnerFechaFinal.selectedItem as? String ?: run {
            Toast.makeText(holder.itemView.context, "Seleccione una fecha final.", Toast.LENGTH_SHORT).show()
            return
        }

        val lecturaFinId = getLecturaIdFromSpinner(holder.spinnerFechaFinal, fechaFinSeleccionada)
        if (lecturaFinId == -1) {
            Toast.makeText(holder.itemView.context, "Fecha final no válida.", Toast.LENGTH_SHORT).show()
            return
        }

        val detalleInicio = helper.obtenerDetalleLectura(lecturaInicioId ?: 0)
        val detalleFin = helper.obtenerDetalleLectura(lecturaFinId)
        if (detalleInicio == null || detalleFin == null) {
            Toast.makeText(holder.itemView.context, "Error al obtener datos de lectura.", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaInicioLect = formatterDB.parse(detalleInicio.first)
        val fechaFinLect = formatterDB.parse(detalleFin.first)

        val calInicio = Calendar.getInstance().apply { time = fechaInicioLect; clearTime() }
        val calFin = Calendar.getInstance().apply { time = fechaFinLect; clearTime() }

        var dias = 0
        while (!calInicio.after(calFin)) {
            dias++
            calInicio.add(Calendar.DAY_OF_MONTH, 1)
        }

        val valorInicio = detalleInicio.second.toDoubleOrNull() ?: 0.0
        val valorFin = detalleFin.second.toDoubleOrNull() ?: 0.0
        val consumoRaw = valorFin - valorInicio

        val diasNormalizar = (spinnerNormalizarA.selectedItem as? String ?: "30 días")
            .replace(" días", "").toIntOrNull() ?: 30

        val consumoNormalizado = if (dias > 0) (consumoRaw / dias) * diasNormalizar else 0.0

        val success = helper.insertarActualizarCalculoMedidor(
            contratoId,
            lecturaInicioId ?: 0,
            lecturaFinId,
            if (normalizar) consumoRaw else null,
            if (normalizar) dias else null,
            if (normalizar) consumoNormalizado else null
        ) != -1L

        if (success) {
            val msg = if (normalizar) "Guardado y normalizado." else "Fecha guardada."
            Toast.makeText(holder.itemView.context, "$msg Unidad $unidadId", Toast.LENGTH_SHORT).show()
            if (normalizar) {
                estadoDetalle[unidadId] = Triple(consumoRaw, dias, consumoNormalizado)
            }
            actualizarEstadoPostGuardado(holder, normalizar, consumoRaw, dias, consumoNormalizado)
        } else {
            Toast.makeText(holder.itemView.context, "Error al guardar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizarExistente(holder: Ac_UnidadViewHolder, contratoId: Int, lecturaInicioId: Int?, lecturaFinId: Int?, unidadId: Int) {
        if (lecturaInicioId == null || lecturaFinId == null) {
            Toast.makeText(holder.itemView.context, "Datos incompletos para normalizar.", Toast.LENGTH_SHORT).show()
            return
        }

        val detalleInicio = helper.obtenerDetalleLectura(lecturaInicioId)
        val detalleFin = helper.obtenerDetalleLectura(lecturaFinId)
        if (detalleInicio == null || detalleFin == null) {
            Toast.makeText(holder.itemView.context, "Error al obtener datos de lectura.", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaInicioLect = formatterDB.parse(detalleInicio.first)
        val fechaFinLect = formatterDB.parse(detalleFin.first)

        val calInicio = Calendar.getInstance().apply { time = fechaInicioLect; clearTime() }
        val calFin = Calendar.getInstance().apply { time = fechaFinLect; clearTime() }

        var dias = 0
        while (!calInicio.after(calFin)) {
            dias++
            calInicio.add(Calendar.DAY_OF_MONTH, 1)
        }

        val valorInicio = detalleInicio.second.toDoubleOrNull() ?: 0.0
        val valorFin = detalleFin.second.toDoubleOrNull() ?: 0.0
        val consumoRaw = valorFin - valorInicio

        val diasNormalizar = (spinnerNormalizarA.selectedItem as? String ?: "30 días")
            .replace(" días", "").toIntOrNull() ?: 30

        val consumoNormalizado = if (dias > 0) (consumoRaw / dias) * diasNormalizar else 0.0

        val success = helper.insertarActualizarCalculoMedidor(
            contratoId,
            lecturaInicioId,
            lecturaFinId,
            consumoRaw,
            dias,
            consumoNormalizado
        ) != -1L

        if (success) {
            Toast.makeText(holder.itemView.context, "Normalizado. Unidad $unidadId", Toast.LENGTH_SHORT).show()
            estadoDetalle[unidadId] = Triple(consumoRaw, dias, consumoNormalizado)
            actualizarEstadoPostNormalizacion(holder, consumoRaw, dias, consumoNormalizado)
        } else {
            Toast.makeText(holder.itemView.context, "Error al normalizar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarEstadoPostGuardado(holder: Ac_UnidadViewHolder, normalizado: Boolean, consumoRaw: Double, dias: Int, consumoNormalizado: Double) {
        holder.tvEstadoFecha.text = "Con Fecha"
        holder.viewEstadoFecha.setBackgroundResource(R.drawable.ac_estadocirculoverde)
        holder.controlesFecha.visibility = View.GONE

        if (normalizado) {
            holder.tvEstadoUnidad.text = "No requiere normalización"
            holder.viewEstadoUnidad.setBackgroundResource(R.drawable.ac_estadocirculoverde)
            mostrarDetalle(holder, consumoRaw, dias, consumoNormalizado)
        }
    }

    private fun actualizarEstadoPostNormalizacion(holder: Ac_UnidadViewHolder, consumoRaw: Double, dias: Int, consumoNormalizado: Double) {
        holder.tvEstadoUnidad.text = "No requiere normalización"
        holder.viewEstadoUnidad.setBackgroundResource(R.drawable.ac_estadocirculoverde)
        holder.layoutBotonNormalizar.visibility = View.GONE
        mostrarDetalle(holder, consumoRaw, dias, consumoNormalizado)
    }

    private fun mostrarDetalle(holder: Ac_UnidadViewHolder, consumoRaw: Double, dias: Int, consumoNormalizado: Double) {
        holder.detalleContainer.visibility = View.VISIBLE
        holder.tvConsumoMedido.text = "${consumoRaw} kWh"
        holder.tvDiasMedidos.text = "$dias días"
        holder.tvConsumoDiario.text = String.format("%.2f", consumoRaw / dias) + " kWh/día"
        holder.tvNormalizado.text = String.format("%.2f", consumoNormalizado) + " kWh"
    }

    private fun cargarFechasCombo(holder: Ac_UnidadViewHolder, unidadId: Int, contratoId: Int) {
        var lecturaIdBase = helper.obtenerLecturaInicialContrato(
            helper.obtenerContratoVigente(unidadId)?.second ?: 0
        )?.first

        val ultimaLecturaFinRecibo = helper.obtenerUltimaLecturaFinRecibo(contratoId)
        if (ultimaLecturaFinRecibo != null) {
            lecturaIdBase = ultimaLecturaFinRecibo.first
        }

        if (lecturaIdBase == null) {
            holder.spinnerFechaFinal.adapter = ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_spinner_item,
                listOf("No hay fechas disponibles")
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            return
        }

        val lecturas = helper.obtenerLecturasPosterioresDisponibles(lecturaIdBase, unidadId)
        val fechasFormateadas = lecturas.map { it ->
            try {
                formatterUI.format(formatterDB.parse(it.third))
            } catch (e: Exception) {
                it.third
            }
        }

        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            fechasFormateadas
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        holder.spinnerFechaFinal.adapter = adapter

        val map = lecturas.associate { lectura ->
            val fechaFormateada = try {
                formatterUI.format(formatterDB.parse(lectura.third))
            } catch (e: Exception) {
                lectura.third
            }
            fechaFormateada to lectura.first
        }
        holder.spinnerFechaFinal.tag = map
    }

    private fun getLecturaIdFromSpinner(spinner: Spinner, fecha: String): Int {
        return (spinner.tag as? Map<String, Int>)?.get(fecha) ?: -1
    }

    private fun configurarVisibilidadContenido(holder: Ac_UnidadViewHolder, tieneFecha: Boolean, requiereNormalizacion: Boolean) {
        holder.controlesFecha.visibility = View.GONE
        holder.layoutBotonNormalizar.visibility = View.GONE
        holder.detalleContainer.visibility = View.GONE

        when {
            !tieneFecha && requiereNormalizacion -> holder.controlesFecha.visibility = View.VISIBLE
            tieneFecha && requiereNormalizacion -> holder.layoutBotonNormalizar.visibility = View.VISIBLE
            tieneFecha && !requiereNormalizacion -> holder.detalleContainer.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = unidades.size

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}