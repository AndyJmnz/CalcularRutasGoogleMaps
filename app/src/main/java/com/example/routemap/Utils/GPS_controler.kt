package com.example.routemap.Utils
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

class GPSController(private val context: Context) : LocationListener {

    private var locationManager: LocationManager? = null

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun getLocation(): Location? {
        // Verificar si se tiene permiso para acceder a la ubicación
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("GPSController", "Permisos de ubicación no concedidos.")
            return null
        }

        try {
            // Verificar si el GPS está habilitado
            val isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            if (isGPSEnabled) {
                // Solicitar actualizaciones de ubicación
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L, // Tiempo mínimo de espera entre actualizaciones (en milisegundos)
                    1f,   // Distancia mínima entre actualizaciones (en metros)
                    this
                )

                // Obtener la última ubicación conocida
                val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                return location
            } else {
                Log.e("GPSController", "GPS no está habilitado.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("GPSController", "Error al obtener la ubicación.")
        }

        return null
    }

    override fun onLocationChanged(location: Location) {
        // Aquí puedes manejar el cambio de ubicación si lo necesitas
        Log.d("GPSController", "Ubicación actualizada: ${location.latitude}, ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Deprecated en Android 11, pero puedes manejarlo si es necesario
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(context, "Proveedor $provider habilitado", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(context, "Proveedor $provider deshabilitado", Toast.LENGTH_SHORT).show()
    }
}