package com.example.proyectocurso.distribucion

import android.R
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class Ac_ReciboSpinnerAdapter(
    context: Context,
    private val recibos: List<Triple<Int, String, Boolean>>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = recibos.size + 1

    override fun getItem(position: Int): Any? = if (position == 0) null else recibos[position - 1]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, R.layout.simple_spinner_item)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, R.layout.simple_spinner_dropdown_item)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup, layoutId: Int): View {
        val view = convertView ?: inflater.inflate(layoutId, parent, false)
        val textView = view.findViewById<TextView>(R.id.text1)

        if (position == 0) {
            textView.text = "Seleccionar Recibo"
            textView.setTypeface(null, Typeface.NORMAL)
        } else {
            val (_, texto, esPendiente) = recibos[position - 1]
            textView.text = texto
            textView.setTypeface(null, if (esPendiente) Typeface.BOLD else Typeface.NORMAL)
        }
        return view
    }
}