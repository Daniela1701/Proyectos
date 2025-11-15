package com.example.proyectocurso

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectocurso.databinding.SeActivityRegistroUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class se_RegistroUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: SeActivityRegistroUsuarioBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SeActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Ajustar márgenes para pantalla completa (opcional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 🔹 Botón de registro
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            // 🔸 Validación básica
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser


                        // Guardar datos en Firestore
                        val firestore = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "uid" to user?.uid,
                            "nombre" to nombre,
                            "correo" to correo,
                            "fechaRegistro" to System.currentTimeMillis()
                        )

                        firestore.collection("usuarios").document(user!!.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Usuario guardado en Firestore correctamente")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al guardar en Firestore", e)
                            }

                        val dbHelper = miSQLiteHelper(this)
                        val db = dbHelper.writableDatabase
                        val values = ContentValues().apply {
                            put("nombre", nombre)
                            put("usuario", correo)
                            put("pasword", contrasena)

                        }
                        val resultado = db.insert("autenticacion", null, values)

                        if (resultado != -1L) {
                            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al guardar en SQLite", Toast.LENGTH_SHORT).show()
                        }

                        // Redirigir al login
                        startActivity(Intent(this, se_LoginActivity::class.java))
                        finish()

                    } else {
                        Log.w("Registro", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // volver al Login
        binding.tvVolverLogin.setOnClickListener {
            startActivity(Intent(this, se_LoginActivity::class.java))
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        // Si ya está logueado y verificado, va al dashboard
        if (currentUser != null && currentUser.isEmailVerified) {
            startActivity(Intent(this, se_DashboardActivity::class.java))
            finish()
        }
    }
}
