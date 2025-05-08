package com.example.practicarecuperacionandroid

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.practicarecuperacionandroid.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Crear o abrir la base de datos
        val dbHelper = LugaresDbHelper(this)
        dbHelper.writableDatabase // Esto dispara onCreate si no existe

        binding.btnContinuar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}