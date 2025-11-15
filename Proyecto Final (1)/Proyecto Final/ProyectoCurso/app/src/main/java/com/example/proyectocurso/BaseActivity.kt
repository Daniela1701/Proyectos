package com.example.proyectocurso

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    fun setupMenu(context: Context, headerView: View) {
        val btnMenu = headerView.findViewById<ImageView>(R.id.btnMenu)

        btnMenu.setOnClickListener {
            val popup = PopupMenu(context, btnMenu)
            popup.menuInflater.inflate(R.menu.menu_dashboard, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_home -> {
                        val intent = Intent(context, se_DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (context is Activity) context.finish()
                        true
                    }

                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, se_LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (context is Activity) context.finish()
                        true
                    }

                    else -> false
                }
            }

            try {
                val popupField = PopupMenu::class.java.getDeclaredField("mPopup")
                popupField.isAccessible = true
                val menuPopupHelper = popupField.get(popup)
                menuPopupHelper.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menuPopupHelper, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            popup.show()
        }
    }
    // === FOOTER ===
    fun setupFooter(context: Context, footerView: View) {
        val btnInicio = footerView.findViewById<LinearLayout>(R.id.btnInicio)
        val btnFacturas = footerView.findViewById<LinearLayout>(R.id.btnFacturas)
        val btnConsumos = footerView.findViewById<LinearLayout>(R.id.btnConsumos)

        btnInicio.setOnClickListener {
            val intent = Intent(context, se_DashboardActivity::class.java)
            context.startActivity(intent)
            if (context is Activity) context.finish()
        }

        btnFacturas.setOnClickListener {
            val intent = Intent(context, BCHistorialFacturas::class.java)
            context.startActivity(intent)
            if (context is Activity) context.finish()
        }

        btnConsumos.setOnClickListener {
            val intent = Intent(context, BCHistorialConsumo::class.java)
            context.startActivity(intent)
            if (context is Activity) context.finish()
        }
    }
}
