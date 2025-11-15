package com.example.proyectocurso.factura

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class BCFacturaPDFGenerator {

    fun generarPDFDesdeVista(
        context: Context,
        view: View,
        nombreArchivo: String
    ): String? {
        return try {
            // === 1. Crear Bitmap desde la vista ===
            val bitmap = getBitmapFromView(view)

            // === 2. Crear documento PDF ===
            val documento = PdfDocument()
            val paginaInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val pagina = documento.startPage(paginaInfo)
            val canvas: Canvas = pagina.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            documento.finishPage(pagina)

            // === 3. Guardar usando MediaStore (según versión de Android) ===
            val outputStream: OutputStream?
            val uri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                outputStream = uri?.let { resolver.openOutputStream(it) }

                documento.writeTo(outputStream)
                documento.close()

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                uri?.let { resolver.update(it, contentValues, null, null) }

                uri?.toString()
            } else {
                // Android 9 o menor
                val carpeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val archivo = File(carpeta, nombreArchivo)
                outputStream = FileOutputStream(archivo)

                documento.writeTo(outputStream)
                documento.close()

                archivo.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    // === Convertir vista a Bitmap ===
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}