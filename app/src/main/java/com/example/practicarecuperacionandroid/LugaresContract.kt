package com.example.practicarecuperacionandroid

import android.provider.BaseColumns

object LugaresContract {
    object LugarEntry : BaseColumns {
        const val ID = "id"
        const val TABLE_NAME = "lugares"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_DIRECCION = "direccion"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_WEB = "web"
        const val COLUMN_FECHA_HORA = "fechaHora"
        const val COLUMN_CALIFICACION = "calificacion"
        const val COLUMN_FOTO = "foto"
    }
}