package com.example.proyectocurso.model

data class UnidadCard(
    val unidadId: Int,
    val unidadNombre: String,
    val ocupanteNombres: String?,
    val ocupanteApellidos: String?,
    val puedeEditar: Boolean
)