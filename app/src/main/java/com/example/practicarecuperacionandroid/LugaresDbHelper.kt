package com.example.practicarecuperacionandroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LugaresDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE ${LugaresContract.LugarEntry.TABLE_NAME} (
                ${LugaresContract.LugarEntry.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${LugaresContract.LugarEntry.COLUMN_NOMBRE} TEXT,
                ${LugaresContract.LugarEntry.COLUMN_TIPO} TEXT,
                ${LugaresContract.LugarEntry.COLUMN_DIRECCION} TEXT,
                ${LugaresContract.LugarEntry.COLUMN_TELEFONO} INTEGER,
                ${LugaresContract.LugarEntry.COLUMN_WEB} TEXT,
                ${LugaresContract.LugarEntry.COLUMN_FECHA_HORA} TEXT,
                ${LugaresContract.LugarEntry.COLUMN_CALIFICACION} REAL,
                ${LugaresContract.LugarEntry.COLUMN_FOTO} TEXT
            )
        """.trimIndent()
        db.execSQL(createTableSQL)

        // Insertar 3 lugares de ejemplo
        val insertSQL = """
            INSERT INTO ${LugaresContract.LugarEntry.TABLE_NAME} 
            (${LugaresContract.LugarEntry.COLUMN_NOMBRE}, ${LugaresContract.LugarEntry.COLUMN_TIPO}, 
             ${LugaresContract.LugarEntry.COLUMN_DIRECCION}, ${LugaresContract.LugarEntry.COLUMN_TELEFONO}, 
             ${LugaresContract.LugarEntry.COLUMN_WEB}, ${LugaresContract.LugarEntry.COLUMN_FECHA_HORA}, 
             ${LugaresContract.LugarEntry.COLUMN_CALIFICACION}, ${LugaresContract.LugarEntry.COLUMN_FOTO})
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val lugaresEjemplo = listOf(
            arrayOf("Plaza Mayor de Madrid", "Parque", "Plaza Mayor, 28012 Madrid, España.", 123456789, "http://parquecentral.com", "2024-05-03 10:30:00", 4.5, "foto1.jpg"),
            arrayOf("Museo del Prado", "Museo", "Museo del Prado, Calle de Ruiz de Alarcón, 23, 28014 Madrid, España.", 987654321, "http://museohistorico.com", "2024-05-02 15:00:00", 4.0, "foto2.jpg"),
            arrayOf("Calle de alcalá", "Restaurante", "Calle de Alcalá, 123, 28009 Madrid, España.", 112233445, "http://sabores.com", "2024-05-01 20:00:00", 4.7, "foto3.jpg")
        )

        for (lugar in lugaresEjemplo) {
            db.execSQL(insertSQL, lugar)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${LugaresContract.LugarEntry.TABLE_NAME}")
        onCreate(db)
    }

    fun getAllNombres(): List<String> {
        val lista = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nombre FROM ${LugaresContract.LugarEntry.TABLE_NAME}",
            null
        )
        while (cursor.moveToNext()) {
            lista.add(cursor.getString(0))
        }
        cursor.close()
        return lista
    }

    companion object {
        const val DATABASE_NAME = "lugares.db"
        const val DATABASE_VERSION = 1
    }
}