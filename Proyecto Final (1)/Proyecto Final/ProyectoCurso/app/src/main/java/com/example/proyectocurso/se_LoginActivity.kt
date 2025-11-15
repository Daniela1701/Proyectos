package com.example.proyectocurso

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectocurso.databinding.SeActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class se_LoginActivity : AppCompatActivity() {

    private lateinit var binding: SeActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SeActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        enableEdgeToEdge()
        // Ajustar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ejecutarModificacionesBD()

        // BOTÓN LOGIN
        binding.btnIngresar.setOnClickListener {
            val email = binding.etUsuario.text.toString().trim()
            val password = binding.etContrasena.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, se_DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // ENLACE: ir al registro
        binding.tvIrRegistro.setOnClickListener {
            val user = auth.currentUser
            val intent = Intent(this,se_RegistroUsuarioActivity::class.java )
            startActivity(intent)
        }
    }


    private fun ejecutarModificacionesBD() {
        try {
            val dbHelper = miSQLiteHelper(this)
            val db = dbHelper.writableDatabase

            //db.execSQL("update contratos set lectura_id = 31 where contrato_id = 7")



            db.close()

        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error en modificaciones BD: ${e.message}")
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, se_DashboardActivity::class.java)
            startActivity(intent)
        }
    }

}
