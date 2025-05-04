package com.example.practicarecuperacionandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1002
    private val listaDeMarcadores = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ubicación actual
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        view.findViewById<Button>(R.id.btnMiUbicacion).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    googleMap.addMarker(
                        MarkerOptions().position(latLng).title("Tu ubicación").visible(false)
                    )
                } else {
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val madrid = LatLng(40.4168, -3.7038)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 12f))

        val dbHelper = LugaresDbHelper(requireContext())
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT id, nombre, direccion, calificacion FROM ${LugaresContract.LugarEntry.TABLE_NAME}",
            null
        )

        val geocoder = Geocoder(requireContext())

        while (cursor.moveToNext()) {
            val lugarId = cursor.getLong(0)
            val nombre = cursor.getString(1)
            val direccion = cursor.getString(2)
            val calificacion = cursor.getInt(3)

            try {
                val resultados = geocoder.getFromLocationName(direccion, 1)
                if (!resultados.isNullOrEmpty()) {
                    val ubicacion = resultados[0]
                    val latLng = LatLng(ubicacion.latitude, ubicacion.longitude)

                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(nombre)
                            .snippet("Calificación: $calificacion")
                    )
                    marker?.tag = lugarId
                    marker?.let { listaDeMarcadores.add(it) }
                }
            } catch (e: Exception) {
                Log.e("MapaFragment", "No se pudo geolocalizar $direccion: ${e.message}")
            }
        }

        cursor.close()
        db.close()

        // Mostrar info al hacer click en marcador
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // Ir a edición al tocar infoWindow
        googleMap.setOnInfoWindowClickListener { marker ->
            val lugarId = marker.tag as? Long ?: return@setOnInfoWindowClickListener
            val intent = Intent(requireContext(), NuevoLugarActivity::class.java)
            intent.putExtra("EXTRA_LUGAR_ID", lugarId)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permiso concedido. Volvé a presionar el botón.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}