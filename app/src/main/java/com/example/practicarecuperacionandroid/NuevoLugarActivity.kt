package com.example.practicarecuperacionandroid

import android.app.DatePickerDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import java.util.Calendar

class NuevoLugarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_lugar)

        val root = findViewById<View>(R.id.nuevo_lugar_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etTipo = findViewById<EditText>(R.id.etTipo)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etWeb = findViewById<EditText>(R.id.etWeb)
        val etFechaHora = findViewById<EditText>(R.id.etFechaHora)
        val sliderContainer = findViewById<FrameLayout>(R.id.sliderContainer)
        val slider = Slider(this)
        slider.valueFrom = 0f
        slider.valueTo = 100f
        slider.stepSize = 1f
        slider.value = 50f // valor inicial
        slider.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        sliderContainer.addView(slider)

        val etFoto = findViewById<EditText>(R.id.etFoto)

        val btnOk = findViewById<Button>(R.id.btnOk)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        etFechaHora.setOnClickListener {
            val calendario = Calendar.getInstance()
            val año = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Formato simple YYYY-MM-DD
                val fecha = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                etFechaHora.setText(fecha)
            }, año, mes, dia)

            datePicker.show()
        }

        btnOk.setOnClickListener {
            val campos = listOf(etNombre, etTipo, etDireccion, etTelefono, etWeb, etFechaHora, etFoto)
            var formularioValido = true

            // Reset errores
            for (campo in campos) {
                campo.background = getDrawable(android.R.drawable.edit_text)
            }

            for (campo in campos) {
                if (campo.text.toString().trim().isEmpty()) {
                    campo.background = getDrawable(android.R.drawable.editbox_background_normal)
                    campo.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                    formularioValido = false
                }
            }

            if (formularioValido) {
                val calificacion = slider.value.toInt()
                val dbHelper = LugaresDbHelper(this)
                val db = dbHelper.writableDatabase

                val values = ContentValues().apply {
                    put(LugaresContract.LugarEntry.COLUMN_NOMBRE, etNombre.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_TIPO, etTipo.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_DIRECCION, etDireccion.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_TELEFONO, etTelefono.text.toString().toInt())
                    put(LugaresContract.LugarEntry.COLUMN_WEB, etWeb.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_FECHA_HORA, etFechaHora.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_CALIFICACION, calificacion)
                    put(LugaresContract.LugarEntry.COLUMN_FOTO, etFoto.text.toString())
                }

                db.insert(LugaresContract.LugarEntry.TABLE_NAME, null, values)
                db.close()

                setResult(RESULT_OK)
                finish()
            }
        }

        btnVolver.setOnClickListener {
            finish() // simplemente cerramos sin guardar
        }
    }
}