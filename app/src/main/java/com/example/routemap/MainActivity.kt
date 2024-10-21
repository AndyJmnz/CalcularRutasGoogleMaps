package com.example.routemap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.routemap.Utils.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private lateinit var nMap: GoogleMap
    private lateinit var request: RequestQueue
    private var polyline: Polyline? = null
    private var isOriginSet = false
    private var originLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        request = Volley.newRequestQueue(applicationContext)

        val btnNuevaRuta: Button = findViewById(R.id.btnNuevaRuta)
        btnNuevaRuta.setOnClickListener {
            nuevaRuta()
        }
    }
    private fun nuevaRuta() {
        nMap.clear()

        Utils.markersDefault(nMap, applicationContext)

        polyline = null
        isOriginSet = false

        Toast.makeText(this, "Seleccione una nueva ruta.", Toast.LENGTH_SHORT).show()
    }
    override fun onMapReady(map: GoogleMap) {
        nMap = map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        nMap.isMyLocationEnabled = true

        val defaultLocation = LatLng(20.778876607830476, -103.4316065872849)
        nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        Utils.markersDefault(nMap, applicationContext)
        nMap.setOnMapClickListener(this)
        nMap.setOnMarkerClickListener(this)
        nMap.setOnMapLongClickListener(this)
    }
    override fun onMapLongClick(latLng: LatLng) {
        if (!isOriginSet) {
            originLatLng = latLng
            Utils.coordenadas.origenLat = latLng.latitude
            Utils.coordenadas.origenLong = latLng.longitude
            Toast.makeText(this, "Origen establecido", Toast.LENGTH_SHORT).show()
            isOriginSet = true
        } else {
            val destinationLatLng = latLng
            Toast.makeText(this, "Destino establecido", Toast.LENGTH_SHORT).show()
            if (originLatLng != null) {
                fetchDirections(originLatLng!!.latitude, originLatLng!!.longitude, destinationLatLng)
            }
            isOriginSet = false
        }
    }
    private fun fetchDirections(originLat: Double, originLng: Double, destination: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$originLat,$originLng&destination=${destination.latitude},${destination.longitude}&key=AIzaSyC9RMr64zwG_deCUGkVGMx35gNwV3vn4Wk"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val routePoints = parseRoute(response)
                drawRoute(routePoints)
                nMap.addMarker(
                    MarkerOptions().position(LatLng(originLat, originLng)).title("Origen")
                )
                nMap.addMarker(MarkerOptions().position(destination).title("Destino"))
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error al obtener la ruta: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        request.add(jsonObjectRequest)
    }
    private fun parseRoute(response: JSONObject): List<LatLng> {
        val routePoints = mutableListOf<LatLng>()
        val routes: JSONArray = response.getJSONArray("routes")

        if (routes.length() > 0) {
            val legs: JSONArray = routes.getJSONObject(0).getJSONArray("legs")
            if (legs.length() > 0) {
                val steps: JSONArray = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val polyline: String = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    routePoints.addAll(decodePoly(polyline))
                }
            }
        }
        return routePoints
    }
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latitude = lat / 1E5
            val longitude = lng / 1E5
            poly.add(LatLng(latitude, longitude))
        }
        return poly
    }
    override fun onMapClick(p0: LatLng) {
        Toast.makeText(this, "Clic en el mapa: ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        if (!isOriginSet) {
            Utils.coordenadas.origenLat = marker.position.latitude
            Utils.coordenadas.origenLong = marker.position.longitude
            originLatLng = marker.position
            isOriginSet = true
            Toast.makeText(this, "Origen establecido", Toast.LENGTH_SHORT).show()
        } else {
            val destinationLatLng = marker.position
            Toast.makeText(this, "Destino establecido", Toast.LENGTH_SHORT).show()
            fetchDirections(originLatLng!!.latitude, originLatLng!!.longitude, destinationLatLng)
            isOriginSet = false
        }

        return true
    }
    private fun drawRoute(routePoints: List<LatLng>) {
        polyline?.remove()

        val polylineOptions = PolylineOptions()
            .width(10f)
            .color(getColor(R.color.colorRoute))

        for (point in routePoints) {
            polylineOptions.add(point)
        }
        polyline = nMap.addPolyline(polylineOptions)
    }
}