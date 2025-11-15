package com.example.proyectocurso

data class se_LecturaItem(
    val id: Int,
    val fecha: String,
    val valor: Double,
    val notas: String?,
    val tipoLectura: String,
    val unidadNombre: String,
    val predioNombre: String,
    val facturado: Boolean = false,
    val fotoRuta: String? = null
)