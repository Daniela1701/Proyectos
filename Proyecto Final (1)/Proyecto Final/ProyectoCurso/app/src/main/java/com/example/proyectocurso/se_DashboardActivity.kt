package com.example.proyectocurso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectocurso.databinding.SeActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class se_DashboardActivity : AppCompatActivity() {

    private lateinit var binding: SeActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SeActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        BaseActivity().setupMenu(this, binding.header.root)
        BaseActivity().setupFooter(this, binding.footer.root)

        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email
        val tips = listOf(
            "Revisa tus consumos cada semana.",
            "No olvides registrar tus lecturas antes del 30.",
            "Consulta tus indicadores en la sección correspondiente.",
            "Revisa tus recibos para mantener tu historial al día.",
            "Gestiona tus predios directamente desde este panel."
        )
        if (email != null) {
            val dbHelper = miSQLiteHelper(this)
            val nombre = dbHelper.obtenerNombrePorCorreo(email)
            if (nombre != null) {
                binding.tvBienvenida.text = "Bienvenido, $nombre"
            }
        }

        binding.header.tvTitulo.text = "Dashboard Principal"

        binding.footer.iconInicio.setColorFilter(Color.parseColor("#1976D2"))
        binding.footer.txtInicio.setTextColor(Color.parseColor("#1976D2"))

        binding.btnIrGestionPredios.setOnClickListener {
            val intent = Intent(this, HU_GestionPrediosActivity::class.java)
            startActivity(intent)
        }
        binding.btnIrGestionUnidades.setOnClickListener {
            val intent = Intent(this, la_GestionUnidadesActivity::class.java)
            startActivity(intent)
        }
        binding.btnReciboProveedor.setOnClickListener {
            val intent = Intent(this, HU_ListarRecibosActivity::class.java)
            startActivity(intent)
        }

        binding.btnLecturaMedidores.setOnClickListener {
            val intent = Intent(this, se_LecturaMedidoresListActivity::class.java)
            startActivity(intent)
        }
        binding.btnConsumo.setOnClickListener {
            val intent = Intent(this, BCHistorialConsumo::class.java)
            startActivity(intent)
        }

        binding.btnRecibos.setOnClickListener {
            val intent = Intent(this, BCHistorialFacturas::class.java)
            startActivity(intent)
        }

        binding.tvTipFinal.text = tips.random()
        val handler = android.os.Handler()
        val fadeDuration = 500L // milisegundos para la animación

        val runnable = object : Runnable {
            override fun run() {
                binding.tvTipFinal.animate()
                    .alpha(0f)
                    .setDuration(fadeDuration)
                    .withEndAction {
                        binding.tvTipFinal.text = tips.random()
                        binding.tvTipFinal.animate()
                            .alpha(1f)
                            .setDuration(fadeDuration)
                            .start()
                    }.start()

                handler.postDelayed(this, 5000)
            }
        }
        handler.postDelayed(runnable, 5000)
    }
}
