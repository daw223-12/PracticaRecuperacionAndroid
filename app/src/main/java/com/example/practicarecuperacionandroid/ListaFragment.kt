package com.example.practicarecuperacionandroid

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_lista, container, false)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)

        val dbHelper = LugaresDbHelper(requireContext())
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT id, nombre FROM ${LugaresContract.LugarEntry.TABLE_NAME}",
            null
        )

        val listaLugares = mutableListOf<LugarItem>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(0)
            val nombre = cursor.getString(1)
            listaLugares.add(LugarItem(id, nombre))
        }
        cursor.close()
        db.close()

        val adapter = LugarAdapter(listaLugares) { lugar ->
            val intent = Intent(requireContext(), NuevoLugarActivity::class.java)
            intent.putExtra("EXTRA_LUGAR_ID", lugar.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return rootView
    }
}