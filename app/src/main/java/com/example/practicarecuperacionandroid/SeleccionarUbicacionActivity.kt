package com.example.practicarecuperacionandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class SeleccionarUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION = 2001

    private lateinit var googleMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var posicionSeleccionada: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.btnMiUbicacion).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(latLng).title("Estás aquí"))
                    posicionSeleccionada = latLng
                } else {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }

        geocoder = Geocoder(this, Locale.getDefault())

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapSeleccion ) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnConfirmarUbicacion).setOnClickListener {
            posicionSeleccionada?.let { latLng ->
                val direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val direccion = direcciones?.firstOrNull()?.getAddressLine(0) ?: "Sin dirección"

                val resultIntent = Intent().apply {
                    putExtra("direccion", direccion)
                    putExtra("latitud", latLng.latitude)
                    putExtra("longitud", latLng.longitude)
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: Toast.makeText(this, "Seleccioná una ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val centroInicial = LatLng(40.4168, -3.7038)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroInicial, 15f))

        // Al hacer tap, movemos marcador
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            posicionSeleccionada = latLng
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }
}