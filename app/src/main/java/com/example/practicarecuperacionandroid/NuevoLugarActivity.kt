package com.example.practicarecuperacionandroid

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NuevoLugarActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_PICK = 1001
    private val REQUEST_IMAGE_CAPTURE = 1002
    private var uriFotoSeleccionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        val lugarId = intent.getLongExtra("EXTRA_LUGAR_ID", -1)
        val modoEdicion = lugarId != -1L
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_lugar)

        // Elementos de la UI y configuración de estos
        val root = findViewById<View>(R.id.nuevo_lugar_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val spinnerTipo = findViewById<Spinner>(R.id.spinnerTipo)
        val tipos = listOf("Naturaleza", "Monumento", "Urbanismo", "Rural", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapter
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        etDireccion.inputType = InputType.TYPE_NULL
        etDireccion.isFocusable = false
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etWeb = findViewById<EditText>(R.id.etWeb)
        val etFechaHora = findViewById<EditText>(R.id.etFechaHora)
        etFechaHora.inputType = InputType.TYPE_NULL
        etFechaHora.isFocusable = false
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
        val btnGaleria = findViewById<Button>(R.id.btnGaleria)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val previewImage = findViewById<ImageView>(R.id.previewImage)
        val btnOk = findViewById<Button>(R.id.btnOk)
        val btnBorrar = findViewById<Button>(R.id.btnBorrar)
        btnBorrar.setBackgroundColor(Color.RED)
        if (modoEdicion) {
            btnBorrar.visibility = View.VISIBLE
        }
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        //Comprobamos si estamos en modo edición, en caso positivo rellenamos los campos
        if (modoEdicion) {
            val dbHelper = LugaresDbHelper(this)
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT * FROM ${LugaresContract.LugarEntry.TABLE_NAME} WHERE id = ?",
                arrayOf(lugarId.toString())
            )

            if (cursor.moveToFirst()) {
                etNombre.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
                val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                spinnerTipo.setSelection(tipos.indexOf(tipo))
                etDireccion.setText(cursor.getString(cursor.getColumnIndexOrThrow("direccion")))
                etTelefono.setText(
                    cursor.getInt(cursor.getColumnIndexOrThrow("telefono")).toString()
                )
                etWeb.setText(cursor.getString(cursor.getColumnIndexOrThrow("web")))
                etFechaHora.setText(cursor.getString(cursor.getColumnIndexOrThrow("fechaHora")))
                val calificacion =
                    cursor.getFloat(cursor.getColumnIndexOrThrow("calificacion")).toInt().toFloat()
                slider.value = calificacion
                val uriStr = cursor.getString(cursor.getColumnIndexOrThrow("foto"))
                uriFotoSeleccionada = Uri.parse(uriStr)

                try {
                    if (esDeGaleria(uriFotoSeleccionada!!)) {
                        // Solo si viene de la galería (SAF)
                        contentResolver.takePersistableUriPermission(
                            uriFotoSeleccionada!!,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }

                    // Verifica acceso real a la imagen
                    contentResolver.openInputStream(uriFotoSeleccionada!!)?.use {
                        previewImage.setImageURI(uriFotoSeleccionada)
                        previewImage.visibility = View.VISIBLE
                        Log.d("IMG_OK", "Imagen mostrada correctamente: $uriStr")
                    } ?: throw FileNotFoundException("InputStream nulo")

                } catch (e: Exception) {
                    uriFotoSeleccionada = null
                    previewImage.setImageURI(null)
                    previewImage.visibility = View.GONE
                    Log.e("IMG_ERR", "Error al mostrar imagen: ${e.message}")
                    Toast.makeText(
                        this,
                        "No se puede acceder a la imagen seleccionada previamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            cursor.close()
            db.close()
        }

        // ONCLICK LISTENERS
        // Vamos a un activity para recibir la direccion
        etDireccion.setOnClickListener {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
            startActivityForResult(intent, 1234) // Código cualquiera para distinguir
        }

        // Dialogs para elegir hora y fecha
        etFechaHora.setOnClickListener {
            val ahora = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    TimePickerDialog(this, { _, hour, minute ->
                        val calendario = Calendar.getInstance()
                        calendario.set(year, month, day, hour, minute)

                        val ahoraMillis = System.currentTimeMillis()
                        if (calendario.timeInMillis > ahoraMillis) {
                            Toast.makeText(this, "La fecha no puede ser futura", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            etFechaHora.setText(formato.format(calendario.time))
                        }
                    }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true).show()
                },
                ahora.get(Calendar.YEAR),
                ahora.get(Calendar.MONTH),
                ahora.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        // Para coger foto de la galeria
        btnGaleria.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
        // Para sacar una foto
        btnCamara.setOnClickListener {
            val photoFile = File.createTempFile("foto_", ".jpg", cacheDir)
            uriFotoSeleccionada = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                photoFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uriFotoSeleccionada)
            }

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
        // Comprueba que todos los campos estén ok y guarda en la bbdd
        btnOk.setOnClickListener {
            val etNombre = findViewById<EditText>(R.id.etNombre)
            val spinnerTipo = findViewById<Spinner>(R.id.spinnerTipo)
            val etDireccion = findViewById<EditText>(R.id.etDireccion)
            val etTelefono = findViewById<EditText>(R.id.etTelefono)
            val etWeb = findViewById<EditText>(R.id.etWeb)
            val etFechaHora = findViewById<EditText>(R.id.etFechaHora)
            val camposObligatorios = listOf(etNombre, etDireccion, etTelefono, etWeb, etFechaHora)

            var valido = true

            // Reiniciar colores
            camposObligatorios.forEach { it.setBackgroundColor(Color.TRANSPARENT) }

            // Validar campos vacíos
            for (campo in camposObligatorios) {
                if (campo.text.toString().trim().isEmpty()) {
                    campo.setBackgroundColor(Color.parseColor("#FFCDD2")) // rojo claro
                    valido = false
                }
            }

            // Validar teléfono (exactamente 9 números)
            val telefonoRegex = Regex("^\\d{9}$")
            if (!telefonoRegex.matches(etTelefono.text.toString())) {
                etTelefono.setBackgroundColor(Color.parseColor("#FFCDD2"))
                valido = false
            }

            // Validar web
            val webRegex = Regex("^(https?://)?(www\\.)?[^\\s]+\\.[a-z]{2,}$")
            if (!webRegex.matches(etWeb.text.toString())) {
                etWeb.setBackgroundColor(Color.parseColor("#FFCDD2"))
                valido = false
            }

            // Validar foto
            if (uriFotoSeleccionada == null) {
                Toast.makeText(this, "Seleccioná una foto", Toast.LENGTH_SHORT).show()
                valido = false
            }

            if (valido) {
                val dbHelper = LugaresDbHelper(this)
                val db = dbHelper.writableDatabase

                val values = ContentValues().apply {
                    put(LugaresContract.LugarEntry.COLUMN_NOMBRE, etNombre.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_TIPO, spinnerTipo.selectedItem.toString())
                    put(LugaresContract.LugarEntry.COLUMN_DIRECCION, etDireccion.text.toString())
                    put(
                        LugaresContract.LugarEntry.COLUMN_TELEFONO,
                        etTelefono.text.toString().toInt()
                    )
                    put(LugaresContract.LugarEntry.COLUMN_WEB, etWeb.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_FECHA_HORA, etFechaHora.text.toString())
                    put(LugaresContract.LugarEntry.COLUMN_CALIFICACION, slider.value.toInt())
                    put(LugaresContract.LugarEntry.COLUMN_FOTO, uriFotoSeleccionada.toString())
                }
                // Depende de los datos actualizamos o insertamos nueva fila
                if (modoEdicion) {
                    db.update(
                        LugaresContract.LugarEntry.TABLE_NAME,
                        values,
                        "id = ?",
                        arrayOf(lugarId.toString())
                    )
                } else {
                    db.insert(LugaresContract.LugarEntry.TABLE_NAME, null, values)
                }
                db.close()

                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Completá todos los campos correctamente", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        // simplemente cerramos sin guardar
        btnVolver.setOnClickListener {
            finish()
        }

        btnBorrar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Eliminar lugar?")
                .setMessage("¿Estás seguro de que querés borrar este lugar?")
                .setPositiveButton("Sí") { _, _ ->
                    val dbHelper = LugaresDbHelper(this)
                    val db = dbHelper.writableDatabase
                    db.delete(
                        LugaresContract.LugarEntry.TABLE_NAME,
                        "id = ?",
                        arrayOf(lugarId.toString())
                    )
                    db.close()

                    setResult(RESULT_OK) // para actualizar la lista
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    fun esDeGaleria(uri: Uri): Boolean {
        return uri.authority == "com.android.providers.media.documents"
    }

    // Funcion para cuando volvemos de la galería/echar una foto:
    // Si viene bien ponemos la foto para que se vea
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val previewImage = findViewById<ImageView>(R.id.previewImage)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    uriFotoSeleccionada = data?.data

//                    uriFotoSeleccionada?.let { uri ->
//                        // Pedimos persistencia del permiso
//                        contentResolver.takePersistableUriPermission(
//                            uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        )
//
//                        // Mostramos la imagen en el ImageView
//                        previewImage.setImageURI(uri)
//                        previewImage.visibility = View.VISIBLE
//                    }
                }

                REQUEST_IMAGE_CAPTURE -> {
//                    uriFotoSeleccionada?.let { uri ->
//                        previewImage.setImageURI(uri)
//                        previewImage.visibility = View.VISIBLE
//                    }

                    uriFotoSeleccionada?.let { tempUri ->

                        val nombreArchivo = "foto_${System.currentTimeMillis()}.jpg"

                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MiApp")
                        }

                        val resolver = contentResolver
                        val uriGaleria = resolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        if (uriGaleria != null) {
                            // Copiar datos desde el archivo temporal a MediaStore
                            resolver.openOutputStream(uriGaleria).use { outputStream ->
                                contentResolver.openInputStream(tempUri).use { inputStream ->
                                    inputStream?.copyTo(outputStream!!)
                                }
                            }



                            Log.d("Camera", "✅ Imagen guardada en galería: $uriGaleria")

                            // Guardar la URI definitiva
                            uriFotoSeleccionada = uriGaleria

                            previewImage.setImageURI(uriFotoSeleccionada)
                            previewImage.visibility = View.VISIBLE
                        } else {
                            Log.e("Camera", "❌ No se pudo guardar en galería.")
                        }
                    }
                }
            }

            if (requestCode == 1234 && resultCode == RESULT_OK && data != null) {
                val direccion = data.getStringExtra("direccion")
                val etDireccion = findViewById<EditText>(R.id.etDireccion)
                etDireccion.setText(direccion ?: "")
            }
        }
    }
}