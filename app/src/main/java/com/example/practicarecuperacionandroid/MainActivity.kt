package com.example.practicarecuperacionandroid

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val NUEVO_LUGAR_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mainLayout = findViewById<View>(R.id.main)
        val selectorGroup = findViewById<RadioGroup>(R.id.selectorGroup)
        val rbLista = findViewById<RadioButton>(R.id.rbLista)
        val rbMapa = findViewById<RadioButton>(R.id.rbMapa)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        val vista = getSharedPreferences("prefs", MODE_PRIVATE)
            .getString("vista_actual", "lista")

        if (vista == "mapa") {
            rbMapa.isChecked = true;
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MapaFragment())
                .commit()
            // marcá el toggle visual si tenés uno
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ListaFragment())
                .commit()
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fragmento por defecto
        /*val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, ListaFragment())
        transaction.commit()*/

        rbLista.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val tx = supportFragmentManager.beginTransaction()
                tx.replace(R.id.fragmentContainer, ListaFragment())
                tx.commit()

                sharedPref.edit().putString("vista_actual", "lista").apply()
            }
        }

        rbMapa.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val tx = supportFragmentManager.beginTransaction()
                tx.replace(R.id.fragmentContainer, MapaFragment())
                tx.commit()
                val sharedPref = getSharedPreferences("prefs", MODE_PRIVATE)
                sharedPref.edit().putString("vista_actual", "mapa").apply()
            }
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, NuevoLugarActivity::class.java)
            startActivityForResult(intent, NUEVO_LUGAR_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NUEVO_LUGAR_REQUEST && resultCode == RESULT_OK) {
            // Recargar ListaFragment reemplazándolo
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, ListaFragment())
            transaction.commit()
        }
    }

}
