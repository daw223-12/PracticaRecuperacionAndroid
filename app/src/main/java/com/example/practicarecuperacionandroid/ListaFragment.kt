package com.example.practicarecuperacionandroid

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
        val nombres = dbHelper.getAllNombres()

        val adapter = LugarAdapter(nombres)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        return rootView
    }
}