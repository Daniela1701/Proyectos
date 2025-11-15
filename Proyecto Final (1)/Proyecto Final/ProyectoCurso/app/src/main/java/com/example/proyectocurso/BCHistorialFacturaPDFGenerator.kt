package com.example.proyectocurso

import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import kotlin.apply

class BCHistorialFacturaPDFGenerator {

    fun generarPDFDesdeVista(
        context: Context,
        view: View,
        nombreArchivo: String
    ): String? {
        return try {

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            // Crear documento PDF
            val documento = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val pagina = documento.startPage(pageInfo)
            pagina.canvas.drawBitmap(bitmap, 0f, 0f, null)
            documento.finishPage(pagina)

            // Guardar en Descargas
            var outputStream: OutputStream? = null
            var uri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val directorio =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!directorio.exists()) directorio.mkdirs()
                val archivo = File(directorio, nombreArchivo)
                outputStream = FileOutputStream(archivo)
                uri = Uri.fromFile(archivo)
            }

            if (outputStream != null) {
                documento.writeTo(outputStream)
                outputStream.flush()
                outputStream.close()
                documento.close()
            }

            // Abrir PDF automáticamente
            if (uri != null) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Abrir PDF con..."))
            }

            uri?.path
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF ❌", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
